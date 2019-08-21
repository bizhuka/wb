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
import java.util.ArrayList;
import java.util.List;

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
                    "SELECT w.\"id\", w.\"spent1\", w.\"spent2\", w.\"spent4\", p.*\n" +
                            "FROM \"wb.db::pack.waybill\" as w RIGHT JOIN \"wb.db::pack.gasspent\" as p ON p.\"waybill_id\" = w.\"id\"\n" +
                            "WHERE w.\"equnr\" = ? AND p.\"pttype\" IN (1, 2, 4) AND w.\"closedate\" = (SELECT max(i.\"closedate\")\n" +
                            "FROM \"wb.db::pack.waybill\" as i\n" +
                            "WHERE i.\"status\" = ? AND i.\"equnr\" = w.\"equnr\")\n" +
                            "ORDER BY p.\"pttype\", p.\"pos\";");
            statement.setString(1, request.getParameter("equnr"));
            statement.setInt(2, Status.CLOSED);

            ResultSet rs = statement.executeQuery();

            PrevPetrol prevPetrol = null;
            List<PrevPetrol> prevPetrolList = new ArrayList<>(3);
            while (rs.next()) {
                int pos = rs.getInt("pos");
                int ptType = rs.getInt("pttype");
                if (pos == 0) {
                    prevPetrol = new PrevPetrol(ptType);
                    prevPetrolList.add(prevPetrol);
                    prevPetrol.GasBefore = rs.getDouble("spent" + ptType);
                }
                if (prevPetrol == null)
                    continue;

                double given = rs.getDouble("gasgiven");
                double before = rs.getDouble("gasbefore");
                prevPetrol.GasMatnr = rs.getString("gasmatnr");

                prevPetrol.GasBefore -= (before + given);
            }

            // Change sign
            for (PrevPetrol petrol : prevPetrolList)
                petrol.GasBefore = Math.abs(petrol.GasBefore);

            writeJson(response, prevPetrolList);

            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }
    }

    static class PrevPetrol {
        double PtType;
        double GasBefore;
        String GasMatnr;

        PrevPetrol(int ptType) {
            this.PtType = ptType;
        }
    }
}
