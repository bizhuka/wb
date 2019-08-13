package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.GasSpent;
import com.modekz.rfc.WBPrintDoc;
import org.hibersap.session.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@WebServlet(urlPatterns = {"/print/*"})
public class RfcPrintDoc extends ServletBase {

    public void init() {
        super.initialize(null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        callByPathInfo(request, response);
    }

    @SuppressWarnings("unused")
    public void template(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String objid = request.getParameter("objid");
        WBPrintDoc printDoc = new WBPrintDoc(objid);

        try (Session session = ODataServiceFactory.getRfcSession().openSession()) {
            session.execute(printDoc);

            sendFile(response, printDoc.data, request.getParameter("contentType"), null);
        }
    }

    @SuppressWarnings("unused")
    public void doc(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // for DB
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();
        em.getTransaction().begin();

        // Only 1 parameter
        long waybillId = Long.parseLong(request.getParameter("id"));
        long setFile = Long.parseLong(request.getParameter("d"));

        Connection connection = ODataServiceFactory.getConnection(em);
        List<WBPrintDoc.PrintDoc> docs = new ArrayList<>();
        List<WBPrintDoc.PrintReq> reqs = new ArrayList<>();
        List<GasSpent> gasSpents = new ArrayList<>();

        String orig_class = null;
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "select CURRENT_DATE as datum, w.\"butxt\", d.\"fio\", e.\"eqktx\", e.\"license_num\", e.\"speed_max\", e.\"pltxt\", e.\"orig_class\", e.\"tooname\", e.\"typbz\", e.\"anln1\", waybill.*\n" +
                            "from \"wb.dbt::pack.waybill\" as  waybill\n" +
                            "left outer join \"wb.dbt::pack.werk\" as w on waybill.\"werks\" = w.\"werks\"\n" +
                            "left outer join \"wb.dbt::pack.driver\" as d on waybill.\"bukrs\" = d.\"bukrs\" and waybill.\"driver\" = d.\"pernr\"\n" +
                            "left outer join \"wb.dbt::pack.equipment\" as e on waybill.\"equnr\" = e.\"equnr\"\n" +
                            "where waybill.\"id\" = ?");
            statement.setLong(1, waybillId);
            ResultSet rs = statement.executeQuery();

            // Add single item
            WBPrintDoc.PrintDoc root = null;
            while (rs.next()) {
                // Original document
                JSONObject json = new JSONObject(getFileAsString("/json/printOption.json"));
                JSONArray jsonArr = json.getJSONArray("list");

                // Destination
                root = new WBPrintDoc.PrintDoc();
                Field[] fieldArr = WBPrintDoc.PrintDoc.class.getDeclaredFields();
                Map<String, Field> fieldMap = new HashMap<>();
                for (Field field : fieldArr)
                    fieldMap.put(field.getName(), field);

                // From js 16 base -> 2 base
                String n = Integer.toBinaryString(Integer.parseInt(request.getParameter("n"), 16));
                for (int i = 1; i <= n.length(); i++) {
                    // to empty string
                    if (n.charAt(i - 1) == '0')
                        continue;

                    Field kField = fieldMap.get("k" + i);
                    Field rField = fieldMap.get("r" + i);
                    json = jsonArr.getJSONObject(i - 1);

                    // get from file
                    kField.set(root, json.getString("kzText"));
                    rField.set(root, json.getString("ruText"));

                    // kz text - from url
                    String k = request.getParameter("k" + i);
                    if (k != null)
                        kField.set(root, k);

                    // ru text - from url
                    String r = request.getParameter("r" + i);
                    if (r != null)
                        rField.set(root, r);
                }

                orig_class = rs.getString("orig_class");

                root.id = rs.getString("id");
                root.datum = rs.getDate("datum");
                root.bukrsName = rs.getString("butxt");
                root.pltxt = rs.getString("pltxt");
                root.driverFio = rs.getString("fio");
                root.eqktx = rs.getString("eqktx");
                root.licenseNum = rs.getString("license_num");
                root.speedMax = BigDecimal.valueOf(rs.getDouble("speed_max"));
                root.fromDate = rs.getDate("fromdate");
                root.toDate = rs.getDate("todate");
                root.tooName = rs.getString("tooname");
                root.typbz = rs.getString("typbz");
                root.anln1 = rs.getString("anln1");

                // Delete leading zeros
                try {
                    root.driver = Integer.parseInt(rs.getString("driver"));
                } catch (Exception e) {
                    root.driver = 0;
                }

                docs.add(root);
            }

            // Requests
            if (root != null) {
                statement = connection.prepareStatement("select * from \"wb.dbt::pack.reqheader\" where \"waybill_id\" = ?");
                statement.setLong(1, waybillId);
                rs = statement.executeQuery();
                int num = 0;
                while (rs.next()) {
                    WBPrintDoc.PrintReq req = new WBPrintDoc.PrintReq();

                    req.num = String.valueOf(++num);
                    req.waybill_id = rs.getString("waybill_id");
                    req.gstrp = rs.getDate("gstrp");
                    req.gltrp = rs.getDate("gltrp");

                    // Copy from wb for too
                    if (!root.tooName.equals("-")) {
                        req.gstrp = root.fromDate;
                        req.gltrp = root.toDate;
                    }

                    req.dateDiff = String.valueOf(
                            TimeUnit.DAYS.convert(req.gltrp.getTime() - req.gstrp.getTime(), TimeUnit.MILLISECONDS) + 1);
                    BigDecimal hours = rs.getBigDecimal("duration");
                    if (BigDecimal.ZERO.compareTo(hours) != 0)
                        req.duration = "(" + hours + ")";
                    req.pltxt = rs.getString("pltxt");
                    req.stand = rs.getString("stand");
                    req.beber = rs.getString("beber");
                    req.ilatx = rs.getString("ilatx");
                    req.ltxa1 = rs.getString("ltxa1");

                    reqs.add(req);
                }

                // Just fill with something
                List<GasSpent> gasSpentList = em.createQuery(
                        "SELECT t FROM GasSpent t WHERE t.Waybill_Id = " + waybillId, GasSpent.class).getResultList();

                Map<String, GasSpent> petrolMap = new HashMap<>(gasSpentList.size());
                for (GasSpent gasSpent : gasSpentList)
                    if (!petrolMap.containsKey(gasSpent.GasMatnr))
                        petrolMap.put(gasSpent.GasMatnr, gasSpent);
                    else {
                        GasSpent prevGasSpent = petrolMap.get(gasSpent.GasMatnr);
                        prevGasSpent.GasBefore.add(gasSpent.GasBefore);
                        prevGasSpent.GasGive.add(gasSpent.GasGive);
                        prevGasSpent.GasGiven.add(gasSpent.GasGiven);
                    }
                // Pass overalls
                gasSpents = new ArrayList<>(petrolMap.values());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        } finally {
            em.close();
        }

        // Pass data for template
        WBPrintDoc printDoc = new WBPrintDoc(waybillId, orig_class, docs, reqs, gasSpents);

        try (Session session = ODataServiceFactory.getRfcSession().openSession()) {
            session.execute(printDoc);

            // Specify the filename
            sendFile(response, printDoc.data, printDoc.contentType,
                    setFile == 1 ? printDoc.filename : null);
        }
    }
}
