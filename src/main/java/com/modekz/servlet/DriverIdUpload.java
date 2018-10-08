package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.json.DbUpdateInfo;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

@WebServlet(urlPatterns = {"/driverIdUpload"})
public class DriverIdUpload extends ServletBase {

    public void init() {
        super.initialize(null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            Connection connection = ODataServiceFactory.getConnection(em);

            PreparedStatement prepStatUpdate = connection.prepareStatement("UPDATE DRIVER SET BARCODE = ? WHERE STCD3 = ?");

            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(request);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                String name = item.getFieldName();
                if (!name.endsWith("id_driver_uploader"))
                    continue;

                //regular form field
                Scanner scan = new Scanner(item.openStream(), "UTF-8");
                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    String[] parts = line.split(";");
                    if (parts.length != 4)
                        continue;

                    prepStatUpdate.setString(1, parts[3]);
                    prepStatUpdate.setString(2, parts[0]);
                    prepStatUpdate.addBatch();
                }
            }

            DbUpdateInfo info = new DbUpdateInfo();
            info.updated = DbUpdateInfo.countModified(prepStatUpdate.executeBatch());
            writeJson(response, info);

            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }
    }

}
