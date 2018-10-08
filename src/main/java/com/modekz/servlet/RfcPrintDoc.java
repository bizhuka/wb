package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.rfc.WBPrintDoc;
import org.hibersap.session.Session;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/printDoc/*"})
public class RfcPrintDoc extends ServletBase {

    public void init() {

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        callCaseMethod(request, response);
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
    public void templateWithData(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // for DB
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();
        em.getTransaction().begin();

        // Only 1 parameter
        long waybillId = Long.parseLong(request.getParameter("waybillId"));

        Connection connection = ODataServiceFactory.getConnection(em);
        List<WBPrintDoc.PrintDoc> docs = new ArrayList<>();
        List<WBPrintDoc.PrintReq> reqs = new ArrayList<>();

        String N_class = null;
        try {
            // Заявки
            PreparedStatement statement = connection.prepareStatement("select * from reqheader where waybill_id = ?");
            statement.setLong(1, waybillId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                WBPrintDoc.PrintReq req = new WBPrintDoc.PrintReq();

                req.waybill_id = rs.getString("waybill_id");
                req.gstrp = rs.getDate("gstrp");
                req.gltrp = rs.getDate("gltrp");
                req.pltxt = rs.getString("pltxt");
                req.stand = rs.getString("stand");
                req.beber = rs.getString("beber");

                reqs.add(req);
            }

            statement = connection.prepareStatement(
                    "select CURRENT_DATE as datum, w.butxt, d.fio, e.eqktx, e.license_num, e.speed_max, e.pltxt, e.n_class, waybill.*\n" +
                            "from waybill\n" +
                            "left outer join werk as w on waybill.werks = w.werks\n" +
                            "left outer join driver as d on waybill.bukrs = d.bukrs and waybill.driver = d.pernr\n" +
                            "left outer join equipment as e on waybill.equnr = e.equnr\n" +
                            "where waybill.id = ?");
            statement.setLong(1, waybillId);
            rs = statement.executeQuery();

            // Add single item
            while (rs.next()) {
                WBPrintDoc.PrintDoc root = new WBPrintDoc.PrintDoc();
                N_class = rs.getString("n_class");

                root.id = rs.getString("id");
                root.datum = rs.getDate("datum");
                root.bukrsName = rs.getString("butxt");
                root.pltxt = rs.getString("pltxt");
                root.driver = rs.getString("driver");
                root.driverFio = rs.getString("fio");
                root.eqktx = rs.getString("eqktx");
                root.licenseNum = rs.getString("license_num");
                root.speedMax = BigDecimal.valueOf(rs.getDouble("speed_max"));

                docs.add(root);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException(e);
        } finally {
            em.close();
        }

        // Pass data for template
        WBPrintDoc printDoc = new WBPrintDoc(N_class, docs, reqs);

        try (Session session = ODataServiceFactory.getRfcSession().openSession()) {
            session.execute(printDoc);

            sendFile(response, printDoc.data, printDoc.contentType, "Путевой_лист_№_" + waybillId + ".docx");
        }
    }
}
