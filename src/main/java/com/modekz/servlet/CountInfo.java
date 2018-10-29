package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.json.UserInfo;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(urlPatterns = {"/count/*"})
public class CountInfo extends ServletBase {

    public void init() {
        super.initialize(null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        this.doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        callCaseMethod(request, response);
    }

    @SuppressWarnings("unused")
    public void wb(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doCount(request, response, "v_count_wb");
    }

    @SuppressWarnings("unused")
    public void req(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doCount(request, response, "v_count_req");
    }

    private void doCount(HttpServletRequest request, HttpServletResponse response, String viewName) throws ServletException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            Connection connection = ODataServiceFactory.getConnection(em);

            // Allowed werks
            UserInfo userInfo = UserInfo.getCurrentUserInfo(this);

            String sql = "SELECT status, sum(cnt) AS cnt FROM " + viewName + " WHERE werks IN " + userInfo.werksCondition() +
                    " GROUP BY status ORDER BY status;";
            PreparedStatement preparedSelect = connection.prepareStatement(sql);

            ResultSet result = preparedSelect.executeQuery();
            StringBuilder sb = new StringBuilder("");
            while (result.next()) {
                sb.append(",{\"status\":").append(
                        result.getInt("status")).append(",\"cnt\":").append(
                        result.getInt("cnt")).append("}");
            }

            response.setContentType("application/json; charset=utf-8");
            response.getWriter().print("[" + sb.toString().substring(1) + "]");

            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }
    }

}
