package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.rfc.MeasureDoc;
import org.hibersap.session.Session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/measureDoc"})
public class RfcMeasureDoc extends ServletBase {
    public void init() {
        super.initialize(null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // From js
        MeasureDoc measureDoc = gson.fromJson(request.getParameter("doc"), MeasureDoc.class); // requestAsString(request)

        try (Session session = ODataServiceFactory.getRfcSession().openSession()) {
            session.execute(measureDoc);

            writeJson(response, measureDoc);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
