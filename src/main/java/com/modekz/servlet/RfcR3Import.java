package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.*;
import com.modekz.json.DbUpdateInfo;
import com.modekz.json.UserInfo;
import com.modekz.rfc.WBRead;
import com.modekz.rfc.WBSetStatus;
import org.hibersap.annotations.Parameter;
import org.hibersap.session.Session;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(urlPatterns = {"/r3/*"})
public class RfcR3Import extends ServletBase {

    static DbUpdateInfo persist(HttpServletRequest request, Session session, EntityManager em, List newList, R3Clause r3Clause) {
        try {
            String strQuery = r3Clause.getQuery(request, newList);
            TypedQuery query = em.createQuery(strQuery, r3Clause.mainClass);
            List dbList = query.getResultList();

            // Send back info
            DbUpdateInfo info = new DbUpdateInfo();

            // Save DB info
            Map<String, Object> dbMap = new HashMap<>();
            for (Object dbItem : dbList) {
                String sKey = r3Clause.getKey(dbItem);
                dbMap.put(sKey, dbItem);
            }

            for (Object r3Item : newList) {
                String sKey = r3Clause.getKey(r3Item);
                Object dbItem = dbMap.get(sKey);

                // Insert or update
                r3Clause.modify(em, dbItem, r3Item, info);

                // Exclude from DB list
                dbMap.remove(sKey);
            }
            if (r3Clause.deleteOld)
                for (Map.Entry<String, Object> entry : dbMap.entrySet()) {
                    em.remove(entry.getValue());
                    info.deleted++;
                }

            r3Clause.modifyBatch(info);

            // And save
            em.getTransaction().commit();

            // If ok send back
            r3Clause.afterWrite(session, newList);

            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void init() {
        super.initialize("yyyy-MM-dd");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // for DB
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();
        em.getTransaction().begin();

        //  for request /r3/*
        Enumeration<String> parameterNames = request.getParameterNames();
        String method = request.getPathInfo().substring(1);

        // Save to DB
        R3Clause r3Clause;
        String where;
        try {
            // Get current user
            UserInfo userInfo = UserInfo.getCurrentUserInfo(this);

            // Find method
            Method meth = RfcR3Import.class.getMethod("getR3Clause" + method,
                    UserInfo.class, EntityManager.class);

            r3Clause = (R3Clause) meth.invoke(this, userInfo, em);
            where = r3Clause.getQuery(request, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        }

        // Pass method and filter
        WBRead wbRead = new WBRead(method, where);
        try (Session session = ODataServiceFactory.getRfcSession().openSession()) {
            session.execute(wbRead);

            // Save in DB
            if (request.getParameter("_persist") != null) {
                // From R3
                Field field = WBRead.class.getDeclaredField(camelCase(method.toLowerCase()) + "List");
                List newList = (List) field.get(wbRead);

                // Write to DB
                DbUpdateInfo info = persist(request, session, em, newList, r3Clause);

                // Show info
                writeJson(response, info);
            } else
                writeJson(response, wbRead);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new ServletException(e);
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseWERK(UserInfo userInfo, EntityManager em) {
        return new R3Clause(Werk.class) {

            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                if (r3List == null) // R3 where
                    return "";

                return "SELECT w FROM Werk w";
            }

            @Override
            String getKey(Object object) {
                Werk werk = (Werk) object;

                return werk.Werks;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseDRIVER(UserInfo userInfo, EntityManager em) throws SQLException {
        return new R3Clause(Driver.class) {

            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                String whereBukrs = getBukrsR3Clause(em, userInfo);

                if (r3List == null) // R3 where
                    return "DR~BE " + whereBukrs;

                return "SELECT d FROM Driver d WHERE d.Bukrs" + whereBukrs;
            }

            @Override
            String getKey(Object object) {
                Driver driver = (Driver) object;

                return driver.Bukrs + driver.Pernr;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseEQUIPMENT(UserInfo userInfo, EntityManager em) {
        return new R3Clause(Equipment.class) {
            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                String whereWerks = getWerksR3Clause(userInfo);

                if (r3List == null) // R3 where
                    return "ILOA~SWERK " + whereWerks;

                return "SELECT e FROM Equipment e WHERE e.Swerk " + whereWerks;
            }

            @Override
            String getKey(Object object) {
                Equipment equipment = (Equipment) object;

                return equipment.Equnr;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseSCHEDULE(UserInfo userInfo, EntityManager em) {
        return new R3Clause(Schedule.class) {
            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                String sapDateFrom = request.getParameter("FROM_DATE");
                String sapDateTo = request.getParameter("TO_DATE");
                String whereWerks = getWerksR3Clause(userInfo);

                if (r3List == null) // R3 where
                    return "AFIH~IWERK " + whereWerks +
                            " AND AFKO~GSTRP <= '" + sapDateTo.replace("-", "") +
                            "' AND AFKO~GLTRP >= '" + sapDateFrom.replace("-", "") + "'";

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                for (Object obj : r3List) {
                    Schedule schedule = (Schedule) obj;

                    String sDate = dateFormat.format(schedule.Datum);
                    if (sapDateFrom.compareTo(sDate) > 0)
                        sapDateFrom = sDate;

                    if (sapDateTo.compareTo(sDate) < 0)
                        sapDateTo = sDate;
                }

                return "SELECT s FROM Schedule s WHERE s.Werks " + whereWerks +
                        " AND s.Datum <= '" + sapDateTo +
                        "' AND s.Datum >= '" + sapDateFrom + "'";
            }

            @Override
            String getKey(Object object) {
                Schedule schedule = (Schedule) object;

                return schedule.Werks + "-" + schedule.Datum.getTime() + "-" + schedule.Equnr;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseREQ_HEADER(UserInfo userInfo, EntityManager em) {
        return new R3Clause(ReqHeader.class) {
            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                String sapDateFrom = request.getParameter("FROM_DATE");
                String sapDateTo = request.getParameter("TO_DATE");
                String whereWerks = getWerksR3Clause(userInfo);

                if (r3List == null) // R3 where
                    return "AFIH~IWERK " + whereWerks +
                            " AND AFKO~GSTRP <= '" + sapDateTo.replace("-", "") +
                            "' AND AFKO~GLTRP >= '" + sapDateFrom.replace("-", "") + "'";

                return "SELECT r FROM ReqHeader r WHERE r.Iwerk " + whereWerks +
                        " AND r.Gstrp <= '" + sapDateTo +
                        "' AND r.Gltrp >= '" + sapDateFrom + "'";
            }

            @Override
            String getKey(Object object) {
                ReqHeader reqHeader = (ReqHeader) object;

                return reqHeader.Objnr;
            }

            @Override
            void afterWrite(Session session, List newList) {
                List<WBSetStatus.Objnr> objnrs = new ArrayList<>(newList.size());

                Set<String> uniqueObjnrs = new HashSet<>(newList.size());

                // Do not set twice
                for (Object item : newList) {
                    ReqHeader reqHeader = (ReqHeader) item;
                    uniqueObjnrs.add("OR" + reqHeader.Aufnr);
                }
                for (String objnr : uniqueObjnrs)
                    objnrs.add(new WBSetStatus.Objnr(objnr));

                session.execute(new WBSetStatus("", "", objnrs));
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseGAS_TYPE(UserInfo userInfo, EntityManager em) {
        return new R3Clause(GasType.class) {

            {
                this.deleteOld = true;
            }

            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                if (r3List == null) // R3 where
                    return "";

                return "SELECT g FROM GasType g";
            }

            @Override
            String getKey(Object object) {
                GasType gasType = (GasType) object;

                return gasType.Matnr;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseLGORT(UserInfo userInfo, EntityManager em) {
        return new R3Clause(Lgort.class) {

            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                if (r3List == null) // R3 where
                    return "";

                return "SELECT l FROM Lgort l";
            }

            @Override
            String getKey(Object object) {
                Lgort lgort = (Lgort) object;

                return lgort.Werks + lgort.Lgort;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseEQUNR_GRP(UserInfo userInfo, EntityManager em) {
        return new R3Clause(EqunrGrp.class) {
            {
                this.deleteOld = true;
            }

            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                if (r3List == null) // R3 where
                    return "";

                return "SELECT p FROM EqunrGrp p";
            }

            @Override
            String getKey(Object object) {
                EqunrGrp equnrGrp = (EqunrGrp) object;

                return equnrGrp.Ktsch;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseSTATUS_TEXT(UserInfo userInfo, EntityManager em) {
        return new R3Clause(StatusText.class) {
            {
                this.deleteOld = false; // Do not delete!
            }

            @Override
            String getQuery(HttpServletRequest request, List r3List) {
                if (r3List == null) // R3 where
                    return "";

                return "SELECT t FROM StatusText t";
            }

            @Override
            String getKey(Object object) {
                StatusText statusText = (StatusText) object;

                return statusText.Stype + "-" + statusText.Id;
            }
        };
    }

    private String getWerksR3Clause(UserInfo userInfo) {
        StringBuilder result = new StringBuilder(" IN (");
        for (int i = 0; i < userInfo.werks.size(); i++) {
            if (i != 0)
                result.append(",");
            result.append("'").append(userInfo.werks.get(i)).append("'");
        }
        result.append(")");

        return result.toString();
    }

    private String getBukrsR3Clause(EntityManager em, UserInfo userInfo) {
        @SuppressWarnings("JpaQlInspection")
        TypedQuery<Werk> werksQuery = em.createQuery(
                "SELECT w FROM Werk w WHERE w.Werks" + getWerksR3Clause(userInfo), Werk.class);

        List<Werk> list = werksQuery.getResultList();
        StringBuilder result = new StringBuilder(" IN (");
        for (int i = 0; i < list.size(); i++) {
            if (i != 0)
                result.append(",");
            result.append("'").append(list.get(i).Bukrs).append("'");
        }
        result.append(")");

        return result.toString();
    }

    static abstract class R3Clause {
        Class mainClass;
        List<Field> copyFields;

        boolean deleteOld = false;

        R3Clause(Class mainClass) {
            this.mainClass = mainClass;

            // updating fields
            Field[] fields = mainClass.getFields();
            this.copyFields = new ArrayList<>(fields.length);

            for (Field fld : fields) {
                boolean isParameter = fld.getAnnotation(Parameter.class) != null;
                boolean isId = fld.getAnnotation(Id.class) != null;

                if (isParameter && !isId)
                    copyFields.add(fld);
            }
        }

        String getQuery(HttpServletRequest request, List r3List) {
            return "";
        }

        abstract String getKey(Object object);

        final void copyTo(Object r3Item, Object dbItem) throws IllegalAccessException {
            // Copy value
            for (Field fld : copyFields) {
                Object src = fld.get(r3Item);
//                Object dest = fld.get(dbItem);
                if (src != null) // && !src.equals(dest))
                    fld.set(dbItem, src);
            }
        }

        void afterWrite(Session session, List newList) throws FileNotFoundException, UnsupportedEncodingException {

        }

        void modify(EntityManager em, Object dbItem, Object r3Item, DbUpdateInfo info) throws IllegalAccessException, SQLException {
            if (dbItem == null) {
                em.persist(r3Item);
                info.inserted++;
                return;
            }

            // Update fields in DB
            this.copyTo(r3Item, dbItem);
            info.updated++;
        }

        void modifyBatch(DbUpdateInfo info) throws SQLException {

        }
    }
}
