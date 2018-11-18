package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.flag.Status;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(urlPatterns = {"/select/*"})
public class SelectInfo extends ServletBase {
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
    public void prevGas(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            Connection connection = ODataServiceFactory.getConnection(em);

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT w.\"id\", w.\"gasspent\", w.\"gastopspent\", g.*\n" +
                            "FROM \"wb.db::pack.waybill\" as w RIGHT JOIN \"wb.db::pack.gasspent\" as g ON g.\"waybill_id\" = w.\"id\"\n" +
                            "WHERE w.\"equnr\" = ? AND w.\"closedate\" = (SELECT max(i.\"closedate\")\n" +
                            "FROM \"wb.db::pack.waybill\" as i\n" +
                            "WHERE i.\"status\" = ? AND i.\"equnr\" = w.\"equnr\")\n" +
                            "ORDER BY g.\"pos\";");
            statement.setString(1, request.getParameter("equnr"));
            statement.setInt(2, Status.CLOSED);

            ResultSet rs = statement.executeQuery();
            PrevGas prevGas = new PrevGas();
            while (rs.next()) {
                int pos = rs.getInt("pos");
                if (pos == 0) {
                    prevGas.GasBefore = rs.getDouble("gasspent");
                }
                double given = rs.getDouble("gasgiven");
                double before = rs.getDouble("gasbefore");
                prevGas.GasMatnr = rs.getString("gasmatnr");

                prevGas.GasBefore -= (before + given);
            }

            prevGas.GasBefore = Math.abs(prevGas.GasBefore);
            writeJson(response, prevGas);

            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }
    }

    static class PrevGas {
        double GasBefore;
        String GasMatnr;
    }
}
