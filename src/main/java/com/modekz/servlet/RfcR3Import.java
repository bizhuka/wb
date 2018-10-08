package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.*;
import com.modekz.json.DbUpdateInfo;
import com.modekz.json.Message;
import com.modekz.rfc.WBRead;
import com.modekz.rfc.WBSetStatus;
import org.apache.commons.lang.ArrayUtils;
import org.hibersap.session.Session;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@WebServlet(urlPatterns = {"/r3/*"})
public class RfcR3Import extends ServletBase {

    static DbUpdateInfo persist(Session session, EntityManager em, List newList, R3Clause r3Clause) {
        try {
            StringBuilder sbSelect = new StringBuilder(r3Clause.dbSelect);
            if (r3Clause.where != null)
                sbSelect.append(r3Clause.where);
            List dbList = em.createQuery(sbSelect.toString(), r3Clause.mainClass).getResultList();

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
            }
            r3Clause.modifyBatch(info);

            // And save
            em.getTransaction().commit();

            // If ok send back
            r3Clause.sendBack(session, newList);

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

        // Where r3Clause
        StringBuilder sbWhere = null;
        String where = request.getParameter("where");

        if (where != null)
            sbWhere = new StringBuilder(where);
        else
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();

                if (paramName.charAt(0) != '_') {
                    if (sbWhere == null)
                        sbWhere = new StringBuilder();
                    else
                        sbWhere.append(" AND ");

                    sbWhere.append(paramName).append(" = ").append(request.getParameter(paramName));
                }
            }

        // No where ?
        if (sbWhere == null)
            sbWhere = new StringBuilder();

        //String login = request.getParameter("_user");
        R3Clause r3Clause = null;
        if (request.getParameter("_persist") != null) { // login != null
            // Find by key
            Login login = em.find(Login.class, "BIRZHAN"); // TODO
            if (login == null) {
                writeJson(response, Message.createError("Пользователь не найден!"));
                return;
            }

            try {
                Method meth = RfcR3Import.class.getMethod("getR3Clause" + method,
                        Login.class, EntityManager.class);

                r3Clause = (R3Clause) meth.invoke(this, login, em);
                if (r3Clause.where != null) {
                    if (sbWhere.length() > 0)
                        sbWhere.append(" AND ");
                    sbWhere.append(r3Clause.r3Field).append(r3Clause.where);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException(e.getMessage());
            }
        }

        // Pass method and filter
        WBRead wbRead = new WBRead(method, sbWhere.toString());
        try (Session session = ODataServiceFactory.getRfcSession().openSession()) {
            session.execute(wbRead);

            // Save in DB
            if (request.getParameter("_persist") != null) {
                // From R3
                Field field = WBRead.class.getDeclaredField(camelCase(method.toLowerCase()) + "List");
                List newList = (List) field.get(wbRead);

                // Write to DB
                DbUpdateInfo info = persist(session, em, newList, r3Clause);

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
    public R3Clause getR3ClauseWERK(Login login, EntityManager em) {
        return new R3Clause(
                "SELECT w FROM Werk w",
                Werk.class,
                new String[]{"Werks"},
                null,
                null) {

            @Override
            String getKey(Object object) {
                Werk werk = (Werk) object;

                return werk.Werks;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseDRIVER(Login login, EntityManager em) throws SQLException {
        return new R3Clause(
                "SELECT d FROM Driver d WHERE d.Bukrs",
                Driver.class,
                new String[]{"Bukrs", "Pernr", "ValidDate"},
                "DR~BE",
                getBukrsR3Clause(em, login.getWerksList())) {

            PreparedStatement prepStatInsert;
            PreparedStatement prepStatUpdate;

            {
                Connection connection = ODataServiceFactory.getConnection(em);
                prepStatInsert = connection.prepareStatement("INSERT INTO DRIVER (DATBEG, FIO, PODR, POST, STCD3, BUKRS, PERNR) VALUES(?,?,?,?,?,?,?);");
                prepStatUpdate = connection.prepareStatement("UPDATE DRIVER SET DATBEG = ?, FIO = ?, PODR = ?, POST = ?, STCD3 = ? WHERE BUKRS = ? AND PERNR = ?");
            }

            @Override
            String getKey(Object object) {
                Driver driver = (Driver) object;

                return driver.Bukrs + driver.Pernr;
            }

            @Override
            void modify(EntityManager em, Object dbItem, Object r3Item, DbUpdateInfo info) throws SQLException {
                PreparedStatement prepStat = dbItem == null ? prepStatInsert : prepStatUpdate;

                Driver driver = (Driver) r3Item;
                prepStat.setDate(1, new java.sql.Date(driver.Datbeg.getTime()));
                prepStat.setString(2, driver.Fio);
                prepStat.setString(3, driver.Podr);
                prepStat.setString(4, driver.Post);
                prepStat.setString(5, driver.Stcd3);

                prepStat.setString(6, driver.Bukrs);
                prepStat.setString(7, driver.Pernr);

                prepStat.addBatch();
            }

            @Override
            void modifyBatch(DbUpdateInfo info) throws SQLException {
                info.inserted += DbUpdateInfo.countModified(prepStatInsert.executeBatch());
                info.updated += DbUpdateInfo.countModified(prepStatUpdate.executeBatch());
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseEQUIPMENT(Login login, EntityManager em) {
        return new R3Clause(
                "SELECT e FROM Equipment e WHERE e.Swerk",
                Equipment.class,
                new String[]{"Equnr"},
                "ILOA~SWERK",
                getWerksR3Clause(login.getWerksList())) {

            @Override
            String getKey(Object object) {
                Equipment equipment = (Equipment) object;

                return equipment.Equnr;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseSCHEDULE(Login login, EntityManager em) {
        return new R3Clause(
                "SELECT s FROM Schedule s WHERE s.Werks",
                Schedule.class,
                new String[]{"Werks", "Datum", "Equnr", "Waybill_Id"},
                "AFIH~IWERK",
                getWerksR3Clause(login.getWerksList())) {

            @Override
            String getKey(Object object) {
                Schedule schedule = (Schedule) object;

                return schedule.Werks + "-" + schedule.Datum.getTime() + "-" + schedule.Equnr;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseREQ_HEADER(Login login, EntityManager em) {
        return new R3Clause(
                "SELECT r FROM ReqHeader r WHERE r.Iwerk",
                ReqHeader.class,
                new String[]{"Objnr", "Waybill_Id"},
                "AFIH~IWERK",
                getWerksR3Clause(login.getWerksList())) {

            @Override
            String getKey(Object object) {
                ReqHeader reqHeader = (ReqHeader) object;

                return reqHeader.Objnr;
            }

            @Override
            void sendBack(Session session, List newList) {
                List<WBSetStatus.Objnr> objnrs = new ArrayList<>(newList.size());

                Set<String> uniqueObjnrs = new HashSet<>(newList.size());

                // Do not set twice
                for (Object item : newList) {
                    ReqHeader reqHeader = (ReqHeader) item;
                    uniqueObjnrs.add("OR" + reqHeader.Aufnr);
                }
                for (String objnr : uniqueObjnrs)
                    objnrs.add(new WBSetStatus.Objnr(objnr));

                session.execute(new WBSetStatus("E0019", "", objnrs));
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseGAS_TYPE(Login login, EntityManager em) {
        return new R3Clause(
                "SELECT g FROM GasType g",
                GasType.class,
                new String[]{"Matnr"},
                null,
                null) {

            @Override
            String getKey(Object object) {
                GasType gasType = (GasType) object;

                return gasType.Matnr;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseLGORT(Login login, EntityManager em) {
        return new R3Clause(
                "SELECT l FROM Lgort l",
                Lgort.class,
                new String[]{"Werks", "Lgort"},
                null,
                null) {

            @Override
            String getKey(Object object) {
                Lgort lgort = (Lgort) object;

                return lgort.Werks + lgort.Lgort;
            }
        };
    }

    private String getWerksR3Clause(String werksList) {
        String[] arrWerks = werksList.split(";");
        StringBuilder result = new StringBuilder(" IN (");
        for (int i = 0; i < arrWerks.length; i++) {
            if (i != 0)
                result.append(",");
            result.append("'").append(arrWerks[i]).append("'");
        }
        result.append(")");

        return result.toString();
    }

    private String getBukrsR3Clause(EntityManager em, String werksList) {
        TypedQuery<Werk> werksQuery = em.createQuery(
                "SELECT w FROM Werk w WHERE w.Werks" + getWerksR3Clause(werksList), Werk.class);

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
        String dbSelect;
        Class mainClass;
        List<Field> copyFields;

        String r3Field;
        String where;

        R3Clause(String dbSelect, Class mainClass, String[] exclude, String r3Field, String where) {
            this.dbSelect = dbSelect;
            this.mainClass = mainClass;
            this.r3Field = r3Field;
            this.where = where;

            // updating fields
            Field[] fields = mainClass.getDeclaredFields();
            this.copyFields = new ArrayList<>(fields.length - exclude.length);

            for (Field fld : fields)
                if (ArrayUtils.indexOf(exclude, fld.getName()) < 0)
                    copyFields.add(fld);
        }

        abstract String getKey(Object object);

        final void copyTo(Object r3Item, Object dbItem) throws IllegalAccessException {
            // Copy value
            for (Field fld : copyFields) {
                Object src = fld.get(r3Item);
                fld.set(dbItem, src);
            }
        }

        void sendBack(Session session, List newList) {

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
