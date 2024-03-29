package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.Equipment;
import com.modekz.db.EqunrGrp;
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
        callByPathInfo(request, response);
    }

    @SuppressWarnings("unused")
    public void uploadDriverMedCards(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        try {
            // Result
            DbUpdateInfoPlus info = new DbUpdateInfoPlus(getUploadedFile(request), 4);

            em.getTransaction().begin();
            Connection connection = ODataServiceFactory.getConnection(em);

            PreparedStatement prepStatUpdate = connection.prepareStatement("UPDATE \"wb.db::pack.driver\" SET \"barcode\" = ? WHERE \"stcd3\" = ?");

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
                String equnr = ("ID_" + item.data[1] + "_" + item.data[4]).replaceAll("\\s+", "");
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

                // Delete leading zero
                String clNum = item.data[5];
                if (clNum.startsWith("0"))
                    clNum = clNum.substring(1);

                // Change to group
                eo.setOrigClass(clNum);
                clNum = EqunrGrp.getClassNum(em, clNum);

                // And set it
                eo.setN_class(clNum);

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

//    @SuppressWarnings("unused")
//    public void uploadGrpRoles(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();
//
//        try {
//            // Result
//            DbUpdateInfoPlus info = new DbUpdateInfoPlus(getUploadedFile(request), 3);
//            em.getTransaction().begin();
//
//            Connection connection = ODataServiceFactory.getConnection(em);
//            PreparedStatement prepStatInsert = connection.prepareStatement("INSERT INTO \"wb.db::pack.grouprole\" (\"grprole\", \"indrole\") VALUES(?,?);");
//            PreparedStatement prepStatDelete = connection.prepareStatement("DELETE FROM \"wb.db::pack.grouprole\" WHERE \"grprole\" = ? and \"indrole\" = ?");
//
//            // All items
//            List<GroupRole> grpRoles = GroupRole.getAllGroupRoles();
//            List<DbUpdateInfoPlus.Item> allItems = new ArrayList<>(grpRoles.size() + info.items.size());
//
//            // Copy prev items to show user
//            for (GroupRole groupRole : grpRoles) {
//                String[] lines = {"*", groupRole.grprole, groupRole.indrole};
//                allItems.add(new DbUpdateInfoPlus.Item(lines));
//            }
//
//            // All content
//            for (DbUpdateInfoPlus.Item item : info.items) {
//                // + or -
//                String operation = item.data[0].trim();
//                String grpRole = item.data[1].trim();
//                String indRole = item.data[2].trim();
//
//                DbUpdateInfoPlus.Item findItem = null;
//                for (int i = 0; i < grpRoles.size(); i++) {
//                    GroupRole groupRole = grpRoles.get(i);
//                    if (grpRole.equals(groupRole.grprole) && indRole.equals(groupRole.indrole)) {
//                        findItem = allItems.get(i);
//                        break;
//                    }
//                }
//
//                PreparedStatement prepStat = null;
//                if ("+".equals(operation) && findItem == null)
//                    prepStat = prepStatInsert;
//
//                if ("-".equals(operation) && findItem != null)
//                    prepStat = prepStatDelete;
//
//                if (prepStat == null)
//                    continue;
//
//                // Add to log
//                if (findItem != null)
//                    findItem.data[0] = operation;
//                else
//                    allItems.add(item);
//
//                prepStat.setString(1, grpRole);
//                prepStat.setString(2, indRole);
//                prepStat.addBatch();
//            }
//
//            // Aggregated info
//            int[] insertResults = prepStatInsert.executeBatch();
//            int[] deleteResults = prepStatDelete.executeBatch();
//
//            // Detailed info
//            int insertIndex = 0, deleteIndex = 0;
//            for (DbUpdateInfoPlus.Item item : allItems) {
//                String operation = item.data[0];
//                if ("+".equals(operation)) {
//                    if (insertResults[insertIndex++] > 0) {
//                        item.result = DbUpdateInfoPlus.INSERTED;
//                        info.inserted++;
//                    }
//                } else if ("-".equals(operation)) {
//                    if (deleteResults[deleteIndex++] > 0) {
//                        item.result = DbUpdateInfoPlus.DELETED;
//                        info.deleted++;
//                    }
//                }
//            }
//
//            // And write data back
//            info.items = allItems;
//            writeJson(response, info);
//
//            em.getTransaction().commit();
//        } catch (Exception ex) {
//            throw new ServletException(ex);
//        } finally {
//            em.close();
//        }
//    }
}
