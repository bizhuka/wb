package com.modekz.servlet;

import com.modekz.ODataServiceFactory;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(urlPatterns = {"/gen_entity"})
public class GenerateEntity extends ServletBase {
    public void init() {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();

        String lastColumnMark = ";};";
        try {
            em.getTransaction().begin();
            Connection conn = ODataServiceFactory.getConnection(em);

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT c.table_name, c.column_name, c.ordinal_position, c.is_nullable, c.data_type, c.udt_name, c.character_maximum_length as len, ccu.constraint_name as is_key \n" +
                            "FROM information_schema.columns as c\n" +
                            "  LEFT JOIN information_schema.constraint_column_usage AS ccu on -- USING (constraint_schema, constraint_name)\n" +
                            "   c.table_name = ccu.table_name AND c.column_name = ccu.column_name AND ccu.constraint_name like '%_pkey'\n" +
                            "WHERE c.table_schema = 'public' AND c.table_name NOT LIKE 'v_%' AND c.table_name <> 'sequence' \n" +
                            "ORDER BY c.table_name, c.ordinal_position;");

            String prevTable = null;
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                String table = rs.getString("table_name");
                String column = rs.getString("column_name");
                String type = rs.getString("udt_name");

                if (!table.equals(prevTable)) {
                    sb.append(lastColumnMark).append("\n");
                    sb.append("entity ").append(table.replace("wb.dbt::pack.", "")).append(" {");
                } else
                    sb.append(";");
                prevTable = table;

                switch (type) {
                    case "varchar":
                        type = "String(" + rs.getInt("len") + ")";
                        break;
                    case "date":
                        type = "LocalDate";
                        break;
                    case "time":
                        type = "LocalTime";
                        break;
                    case "timestamp":
                        type = "UTCTimestamp";
                        break;
                    case "numeric":
                        type = "BinaryFloat"; //"Decimal(20,3)";
                        break;
                    case "int4":
                        type = "Integer";
                        break;
                    case "int8":
                        type = "Integer64";
                        break;
                    case "float8":
                        type = "DecimalFloat";
                        break;
                    case "bool":
                        type = "Boolean";
                        break;
                    default:
                        System.err.println(column);
                        System.err.println(type);
                        break;
                }

                // Add type
                column = column + " : " + type;
                if (rs.getString("is_key") != null)
                    column = "key " + column;

                sb.append(column);
            }

            // And return
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().print(sb.toString().substring(4) + lastColumnMark);
            response.getWriter().print("<br>Insert to Web-IDE!");
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            em.close();
        }
    }
}
