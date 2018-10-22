package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.Equipment;
import com.modekz.db.GroupRole;
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
import java.util.ArrayList;
import java.util.List;

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

    @SuppressWarnings("unused")
    public void uploadGrpRoles(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        try {
            // Result
            DbUpdateInfoPlus info = new DbUpdateInfoPlus(getUploadedFile(request), 3);
            em.getTransaction().begin();

            Connection connection = ODataServiceFactory.getConnection(em);
            PreparedStatement prepStatInsert = connection.prepareStatement("INSERT INTO GroupRole (GrpRole, IndRole) VALUES(?,?);");
            PreparedStatement prepStatDelete = connection.prepareStatement("DELETE FROM GroupRole WHERE GrpRole = ? AND IndRole = ?");

            // All items
            List<GroupRole> grpRoles = GroupRole.getAllGroupRoles();
            List<DbUpdateInfoPlus.Item> allItems = new ArrayList<>(grpRoles.size() + info.items.size());

            // Copy prev items to show user
            for (GroupRole groupRole : grpRoles) {
                String[] lines = {"*", groupRole.GrpRole, groupRole.IndRole};
                allItems.add(new DbUpdateInfoPlus.Item(lines));
            }

            // All content
            for (DbUpdateInfoPlus.Item item : info.items) {
                // + or -
                String operation = item.data[0].trim();
                String grpRole = item.data[1].trim();
                String indRole = item.data[2].trim();

                boolean bFind = false;
                for (GroupRole groupRole : grpRoles)
                    if (grpRole.equals(groupRole.GrpRole) && indRole.equals(groupRole.IndRole)) {
                        bFind = true;
                        break;
                    }

                PreparedStatement prepStat = null;
                if ("+".equals(operation) && !bFind)
                    prepStat = prepStatInsert;

                if ("-".equals(operation) && bFind)
                    prepStat = prepStatInsert;

                if (prepStat == null)
                    continue;

                // Add to log
                allItems.add(item);

                prepStat.setString(1, grpRole);
                prepStat.setString(2, indRole);
                prepStat.addBatch();
            }

            // Aggregated info
            int[] insertResults = prepStatInsert.executeBatch();
            int[] deleteResults = prepStatDelete.executeBatch();

            // Detailed info
            int insertIndex = 0, deleteIndex = 0;
            for (DbUpdateInfoPlus.Item item : allItems) {
                String operation = item.data[0];
                if ("+".equals(operation)) {
                    if (insertResults[insertIndex++] > 0) {
                        item.result = DbUpdateInfoPlus.INSERTED;
                        info.inserted++;
                    }
                } else if ("-".equals(operation)) {
                    if (deleteResults[deleteIndex++] > 0) {
                        item.result = DbUpdateInfoPlus.DELETED;
                        info.deleted++;
                    }
                }
            }

            // And write data back
            info.items = allItems;
            writeJson(response, info);

            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            em.close();
        }
    }


}
