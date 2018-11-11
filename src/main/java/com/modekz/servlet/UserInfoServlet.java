package com.modekz.servlet;

import com.modekz.json.UserInfo;
import com.sap.xs2.security.container.SecurityContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/userInfo", "/userInfo2"})
public class UserInfoServlet extends ServletBase {

    public void init() {
        super.initialize(null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        callByServletPath(request, response);
    }

    public void userInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            UserInfo userInfo = UserInfo.getCurrentUserInfo(this);

            response.setContentType("application/json; charset=utf-8");
            String result = gson.toJson(userInfo);
            result = result.substring(0, result.length() - 1) + userInfo.scopesAsJson() + "}";

            response.getWriter().print(result);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(403, e.getMessage());
        }
    }

    public void userInfo2(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            com.sap.xs2.security.container.UserInfo info = SecurityContext.getUserInfo();
            com.sap.xs2.security.container.UserInfo info2 = new com.sap.xs2.security.container.UserInfo(info.getAppToken()) {
                @Override
                public String toString() {
                    return this.json.toString();
                }
            };

            response.setContentType("application/json; charset=utf-8");
            response.getWriter().print(info2.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(403, e.getMessage());
        }
    }
}
