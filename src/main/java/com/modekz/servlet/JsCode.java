package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.StatusText;
import org.apache.commons.lang.StringEscapeUtils;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/jsCode/*"})
public class JsCode extends ServletBase {
    private static final String STATUS_LIB = "/controller/LibStatus.js";
    private static final String PETROL_LIB = "/controller/LibPetrol.js";
    private Map<String, String> cacheLib = new HashMap<>(2);

    public void init() {
        super.initialize(null);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sMethod = request.getPathInfo().substring(1).replace(".js", "");
        if (cacheLib.containsKey(sMethod)) {
            sendJsLib(response, cacheLib.get(sMethod));
            return;
        }

        // Call slow method
        String sCode;
        try {
            Method method = this.getClass().getMethod(sMethod, HttpServletRequest.class, HttpServletResponse.class);
            sCode = (String) method.invoke(this, request, response);
            cacheLib.put(sMethod, sCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }

        sendJsLib(response, sCode);
    }

    private void sendJsLib(HttpServletResponse response, String sCode) throws IOException {
        response.setContentType("application/javascript; charset=utf-8");
        response.getWriter().print(sCode);
    }

    @SuppressWarnings("unused")
    public String status(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        String sCode;
        try {
            // Get from DB
            String allTexts = gson.toJson(StatusText.getStatusList(em));

            // JS code
            sCode = getFileAsString(STATUS_LIB);

            // Put values
            sCode = sCode.replace("var allTexts = null;", "var allTexts = " + allTexts + ";");
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }

        return sCode;
    }

    @SuppressWarnings("unused")
    public String petrol(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        String sCode;
        try {
            // Get view fragment
            String textFrag = "\"" + StringEscapeUtils.escapeJava(getFileAsString("/view/frag/PetrolFrag.fragment.xml")) + "\"";

            // JS code
            sCode = getFileAsString(PETROL_LIB);

            // Put values
            sCode = sCode.replace("var textFrag = null;", "var textFrag = " + textFrag + ";");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        return sCode;
    }

    @SuppressWarnings("unused")
    public String statusCF(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        String template = "sap.ui.define([], function () { \"use strict\"; \n" +
                "     return {  \n" +
                "        getCfTexts: function () {  \n" +
                "           return _RESULT_  \n" +
                "         } \n" +
                "     }; \n" +
                " });";

        String sCode;
        try {
            // Get from DB
            String allTexts = gson.toJson(StatusText.getStatusList(em));

            // Put values
            sCode = template.replace("_RESULT_", allTexts);
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }

        return sCode;
    }
}
