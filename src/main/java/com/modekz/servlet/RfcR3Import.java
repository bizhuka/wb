package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.*;
import com.modekz.json.DbUpdateInfo;
import com.modekz.json.UserInfo;
import com.modekz.rfc.WBRead;
import com.modekz.rfc.WBSetStatus;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibersap.session.Session;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
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
            TypedQuery query = em.createQuery(sbSelect.toString(), r3Clause.mainClass);
            r3Clause.prepareQuery(query);

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

        // Whole where with all operators: >=, <= ...
        StringBuilder sbWhere = new StringBuilder();

        // Passed by param
        String where = request.getParameter("_where");
        if (where != null)
            sbWhere.append(where);

        // All other conditions with = operators only
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();

            if (paramName.charAt(0) != '_') {
                if (sbWhere.length() > 0)
                    sbWhere.append(" AND ");

                sbWhere.append(paramName).append(" = ").append(request.getParameter(paramName));
            }
        }

        // Save to DB
        R3Clause r3Clause = null;
        if (request.getParameter("_persist") != null)
            try {
                // Get current user
                UserInfo userInfo = UserInfo.getCurrentUserInfo(this);

                // Find method
                Method meth = RfcR3Import.class.getMethod("getR3Clause" + method,
                        UserInfo.class, EntityManager.class);

                r3Clause = (R3Clause) meth.invoke(this, userInfo, em);
                if (r3Clause.where != null) {
                    if (sbWhere.length() > 0)
                        sbWhere.append(" AND ");
                    sbWhere.append(r3Clause.r3Field).append(r3Clause.where);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException(e.getMessage());
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
    public R3Clause getR3ClauseWERK(UserInfo userInfo, EntityManager em) {
        return new R3Clause(
                "SELECT w FROM Werk w",
                Werk.class,
                new String[]{"Werks"}) {

            @Override
            String getKey(Object object) {
                Werk werk = (Werk) object;

                return werk.Werks;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseDRIVER(UserInfo userInfo, EntityManager em) throws SQLException {
        return new R3Clause(
                "SELECT d FROM Driver d WHERE d.Bukrs",
                Driver.class,
                new String[]{"Bukrs", "Pernr"}) {

            PreparedStatement prepStatInsert;
            PreparedStatement prepStatUpdate;

            {
                this.r3Field = "DR~BE";
                this.where = getBukrsR3Clause(em, userInfo);
            }

            {
                Connection connection = ODataServiceFactory.getConnection(em);
                prepStatInsert = connection.prepareStatement("INSERT INTO \"wb.db::pack.driver\" (\"datbeg\", \"fio\", \"podr\", \"post\", \"stcd3\", \"bukrs\", \"pernr\") VALUES(?,?,?,?,?,?,?);");
                prepStatUpdate = connection.prepareStatement("UPDATE \"wb.db::pack.driver\" SET \"datbeg\" = ?, \"fio\" = ?, \"podr\" = ?, \"post\" = ?, \"stcd3\" = ? WHERE \"bukrs\" = ? and \"pernr\" = ?");
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
    public R3Clause getR3ClauseEQUIPMENT(UserInfo userInfo, EntityManager em) {
        return new R3Clause(
                "SELECT e FROM Equipment e WHERE e.Swerk",
                Equipment.class,
                new String[]{"Equnr"}) {

            {
                this.r3Field = "ILOA~SWERK";
                this.where = getWerksR3Clause(userInfo);
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
        return new R3Clause(
                "SELECT s FROM Schedule s WHERE s.Datum >= :fromDate AND s.Werks",
                Schedule.class,
                new String[]{"Werks", "Datum", "Equnr"}) {

            {
                this.r3Field = "AFIH~IWERK";
                this.where = getWerksR3Clause(userInfo);
            }

            @Override
            void prepareQuery(TypedQuery query) {
                // Watch @29!
                query.setParameter("fromDate", DateUtils.addDays(new Date(), -29), TemporalType.DATE);
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
        return new R3Clause(
                "SELECT r FROM ReqHeader r WHERE r.Gstrp >= :fromDate AND r.Iwerk",
                ReqHeader.class,
                new String[]{"Objnr", "Waybill_Id"}) {

            {
                this.r3Field = "AFIH~IWERK";
                this.where = getWerksR3Clause(userInfo);
            }

            @Override
            void prepareQuery(TypedQuery query) {
                // Watch @29!
                query.setParameter("fromDate", DateUtils.addDays(new Date(), -29), TemporalType.DATE);
            }

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
    public R3Clause getR3ClauseGAS_TYPE(UserInfo userInfo, EntityManager em) {
        return new R3Clause(
                "SELECT g FROM GasType g",
                GasType.class,
                new String[]{"Matnr"}) {

            {
                this.deleteOld = true;
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
        return new R3Clause(
                "SELECT l FROM Lgort l",
                Lgort.class,
                new String[]{"Werks", "Lgort"}) {

            @Override
            String getKey(Object object) {
                Lgort lgort = (Lgort) object;

                return lgort.Werks + lgort.Lgort;
            }
        };
    }

    @SuppressWarnings("unused")
    public R3Clause getR3ClauseEQUNR_GRP(UserInfo userInfo, EntityManager em) {
        return new R3Clause(
                "SELECT p FROM EqunrGrp p",
                EqunrGrp.class,
                new String[]{"Ktsch"}) {

            {
                this.deleteOld = true;
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
        return new R3Clause(
                "SELECT t FROM StatusText t",
                StatusText.class,
                new String[]{"Stype", "Id"}) {

            {
                this.deleteOld = false; // Do not delete!
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
        String dbSelect;
        Class mainClass;
        List<Field> copyFields;

        String r3Field;
        String where;
        boolean deleteOld = false;

        R3Clause(String dbSelect, Class mainClass, String[] exclude) {
            this.dbSelect = dbSelect;
            this.mainClass = mainClass;

            // updating fields
            Field[] fields = mainClass.getFields();
            this.copyFields = new ArrayList<>(fields.length - exclude.length);

            for (Field fld : fields)
                if (ArrayUtils.indexOf(exclude, fld.getName()) < 0)
                    copyFields.add(fld);
        }

        void prepareQuery(TypedQuery query) {

        }

        abstract String getKey(Object object);

        final void copyTo(Object r3Item, Object dbItem) throws IllegalAccessException {
            // Copy value
            for (Field fld : copyFields) {
                Object src = fld.get(r3Item);
                if (src != null)
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
