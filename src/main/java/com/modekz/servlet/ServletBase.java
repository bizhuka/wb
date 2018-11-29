package com.modekz.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

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
    public Gson gson;

    void initialize(String pattern) {
        gson = pattern == null ?
                new Gson() :
                new GsonBuilder().setDateFormat(pattern).create();
    }

    void callByServletPath(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        callByName(request.getServletPath().substring(1), request, response);
    }

    void callByPathInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        callByName(request.getPathInfo().substring(1), request, response);
    }

    private void callByName(String name, HttpServletRequest request, HttpServletResponse response) throws ServletException {
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
        if (fileName != null && !fileName.equals(""))
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    URLEncoder.encode(fileName, "UTF-8") + "\"");

        OutputStream os = response.getOutputStream();
        os.write(data, 0, data.length);
        os.close();
    }

    String getUploadedFile(HttpServletRequest request) throws IOException, FileUploadException {
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(request);
        while (iterator.hasNext()) {
            FileItemStream item = iterator.next();
            String name = item.getFieldName();
            if (!name.endsWith("id_csv_uploader"))
                continue;

            return IOUtils.toString(item.openStream(), "UTF-8");
        }

        return null;
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

    public String getFileAsString(String path) throws IOException {
        return IOUtils.toString(this.getServletContext().getResourceAsStream(path), "UTF-8");
    }
}
