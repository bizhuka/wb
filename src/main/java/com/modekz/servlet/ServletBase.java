package com.modekz.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URLEncoder;

public class ServletBase extends HttpServlet {
    Gson gson;

    void initialize(String pattern) {
        gson = pattern == null ?
                new Gson() :
                new GsonBuilder().setDateFormat(pattern).create();
    }

    void callCaseMethod(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        // Detect method
        String name = request.getPathInfo().substring(1);

        try {
            Method method = this.getClass().getMethod(name, HttpServletRequest.class, HttpServletResponse.class);
            method.invoke(this, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    String requestAsString(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = request.getReader();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);

        return sb.toString();
    }

    void writeJson(HttpServletResponse response, Object object) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().print(gson.toJson(object));
    }

    void sendFile(HttpServletResponse response, byte[] data, String contentType, String fileName) throws IOException {
        response.setContentType(contentType);
        response.setContentLength(data.length);


        // Show in place or as file
        if (fileName != null)
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    URLEncoder.encode(fileName, "UTF-8") + "\"");

        OutputStream os = response.getOutputStream();
        os.write(data, 0, data.length);
        os.close();
    }

    String camelCase(String in) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : in.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
