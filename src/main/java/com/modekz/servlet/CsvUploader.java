package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.Equipment;
import com.modekz.json.DbUpdateInfo;
import com.modekz.json.DbUpdateInfoPlus;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet(urlPatterns = {"/csv/*"})
public class CsvUploader extends ServletBase {

    public void init() {
        super.initialize(null);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        callCaseMethod(request, response);
    }

    @SuppressWarnings("unused")
    public void uploadDriverMedCards(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        try {
            // Result
            DbUpdateInfoPlus info = new DbUpdateInfoPlus(getUploadedFile(request), 4);

            em.getTransaction().begin();
            Connection connection = ODataServiceFactory.getConnection(em);

            PreparedStatement prepStatUpdate = connection.prepareStatement("UPDATE DRIVER SET BARCODE = ? WHERE STCD3 = ?");

            // All content
            for (DbUpdateInfoPlus.Item item : info.items) {
                prepStatUpdate.setString(1, item.data[3]);
                prepStatUpdate.setString(2, item.data[0]);
                prepStatUpdate.addBatch();
            }

            // Aggregated info
            int[] results = prepStatUpdate.executeBatch();
            info.updated = DbUpdateInfo.countModified(results);

            // Detailed info
            for (int i = 0; i < results.length; i++) {
                int result = results[i];
                if (result > 0)
                    info.items.get(i).result = DbUpdateInfoPlus.UPDATED;
            }

            writeJson(response, info);

            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unused")
    public void uploadEquipment(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        try {
            // Result
            DbUpdateInfoPlus info = new DbUpdateInfoPlus(getUploadedFile(request), 6);
            em.getTransaction().begin();

            // All content
            for (DbUpdateInfoPlus.Item item : info.items) {

                // Works + Plate
                String equnr = "ID_" + item.data[1] + "_" + item.data[4];
                Equipment eo = em.find(Equipment.class, equnr);
                if (eo == null) {
                    eo = new Equipment();
                    eo.setEqunr(equnr);
                    item.result = DbUpdateInfoPlus.INSERTED;
                    info.inserted++;
                } else {
                    item.result = DbUpdateInfoPlus.UPDATED;
                    info.updated++;
                }

                eo.setBukrs(item.data[0]);
                eo.setSwerk(item.data[1]);
                eo.setEqktx(item.data[2]);
                eo.setTooName(item.data[3]);
                eo.setLicense_num(item.data[4]);
                eo.setN_class(item.data[5]);

                em.merge(eo);
            }
            writeJson(response, info);

            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }
    }

}
