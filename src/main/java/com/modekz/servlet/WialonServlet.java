package com.modekz.servlet;

import com.modekz.ODataServiceFactory;
import com.modekz.db.WlnVehicle;
import com.modekz.json.ConnWialon;
import com.modekz.json.DbUpdateInfo;
import com.modekz.json.WlnMessageInfo;
import com.modekz.json.WlnSpent;
import com.modekz.rfc.WlnVehicleFm;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.hibersap.session.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/wialon/*"})
public class WialonServlet extends ServletBase {
    // Configuration
    private static ConnWialon connWialon;
    private static Pattern redirectUrl = Pattern.compile("url=([^\"]*)");

    private static double getValue(NodeList columns, int index) {
        return Double.parseDouble(((Element) columns.item(index)).getAttribute("val"));
    }

    public void init() {
        super.initialize("dd.MM.yyyy HH:mm:ss");

        // From environmental variables
        connWialon = gson.fromJson(System.getenv("WIALON_OPT"), ConnWialon.class);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        callByPathInfo(request, response);
    }

    private HttpMethod getAsMethod(String url) throws IOException, ServletException {
        String[] urls = {"",
                "login_action.html?user=" + connWialon.user + "&passw=" + connWialon.password + "&store_cookie=on&lang=ru&action=login",
                null,
                url};

        HttpState initialState = new HttpState();
        HttpClient httpclient = new HttpClient();
        httpclient.setState(initialState);

        httpclient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        Cookie[] cookies = new Cookie[0];

        for (int i = 0; i < urls.length; i++) {
            GetMethod getMethod = new GetMethod(connWialon.host + urls[i]);
            if (httpclient.executeMethod(getMethod) != 200)
                throw new ServletException("Error in " + i + " call");

            switch (i) {
                case 0:
                    cookies = httpclient.getState().getCookies();
                    break;
                case 1:
                    String sBody = getMethod.getResponseBodyAsString();
                    Matcher matcher = redirectUrl.matcher(sBody);
                    if (!matcher.find())
                        throw new ServletException("No redirect url");

                    // Get redirect url
                    urls[2] = matcher.group(1);
                    break;
                case 2:
                    initialState.addCookies(cookies);
                    break;
                case 3:
//                    StringWriter writer = new StringWriter();
//                    InputStream inputStream = getMethod.getResponseBodyAsStream();
//                    IOUtils.copy(inputStream, writer, "UTF-8");
//                    html = writer.toString();
//                    inputStream.close();
                    return getMethod;
            }
        }

        return null;
    }

    @SuppressWarnings("unused")
    public void exportMessages(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String wialonId = request.getParameter("wialonId");
        String sFrom = request.getParameter("from");
        String sTo = request.getParameter("to");

        HttpMethod method = getAsMethod("messages_filter/export_msgs.html?fmt=wln&id=" + wialonId + "&from=" + sFrom + "&to=" + sTo + "&arh=0");
        if (method == null)
            return;

        String html = method.getResponseBodyAsString();
        if (html == null)
            return;

        String[] messages = html.split("\r\n");

        List<WlnMessageInfo.WlnMessage> messageList = new ArrayList<>(messages.length);
        for (String message : messages) {
            String[] parts = message.split(";");
            if (parts.length < 2 || !parts[0].equals("REG"))
                continue;

            double mileage = 0;
            double fuel = 0;
            for (String part : parts[6].split(",")) {
                String[] pair = part.split(":");
                if (pair.length == 0)
                    continue;

                if (pair[0].equals("mileage"))
                    mileage = Double.parseDouble(pair[1]);
                if (pair[0].equals("rs485_fls02"))
                    fuel = Double.parseDouble(pair[1]);

            }

            WlnMessageInfo.WlnMessage wlnMessage = new WlnMessageInfo.WlnMessage(
                    new Date(Long.parseLong(parts[1]) * 1000),
                    Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), mileage, fuel);

            if (wlnMessage.lat > 0 && wlnMessage.lon > 0)
                messageList.add(wlnMessage);
        }

        // And send back to js
        writeJson(response, new WlnMessageInfo(messageList));
    }

    @SuppressWarnings("unused")
    public void loadWlnVehicle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpMethod method = getAsMethod("service.html");
        if (method == null)
            return;

        String html = method.getResponseBodyAsString();
        if (html == null)
            return;

        // All items
        int start = html.indexOf(",\"items\":[{") + ",\"items\":[{(".length() - 3;
        int end = html.indexOf("}],\"classes\":{\"", start) + 2;
        html = html.substring(start, end);

        WlnVehicle[] wlnVehicles = gson.fromJson(html, WlnVehicle[].class);
        List<WlnVehicle> newList = new ArrayList<>(wlnVehicles.length);

        for (WlnVehicle wlnVehicle : wlnVehicles) {
            if (wlnVehicle.cls != 3)
                continue;

            // Copy info
            if (wlnVehicle.pos != null && wlnVehicle.pos.p != null) {
                wlnVehicle.gps_mileage = wlnVehicle.pos.p.gps_mileage;
                wlnVehicle.mileage = wlnVehicle.pos.p.mileage;
                wlnVehicle.rs485_fls02 = wlnVehicle.pos.p.rs485_fls02;
                wlnVehicle.rs485_fls12 = wlnVehicle.pos.p.rs485_fls12;
                wlnVehicle.rs485_fls22 = wlnVehicle.pos.p.rs485_fls22;
            }

            newList.add(wlnVehicle);
        }

        // for DB
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();
        em.getTransaction().begin();

        try (Session session = ODataServiceFactory.getRfcSession().openSession()) {
            // Write to DB
            DbUpdateInfo info = RfcR3Import.persist(request, session, em, newList, new RfcR3Import.R3Clause(WlnVehicle.class) {

                @Override
                String getQuery(HttpServletRequest request, List r3List) {
                    if (r3List == null) // R3 where
                        return "";

                    return "SELECT v FROM WlnVehicle v";
                }

                @Override
                String getKey(Object object) {
                    WlnVehicle wlnVehicle = (WlnVehicle) object;

                    return wlnVehicle.gd;
                }
            });

            // Send to R3
            if (info != null) {
                WlnVehicleFm fm = new WlnVehicleFm(newList);
                session.execute(fm);
                info.dbcnt = fm.dbcnt;
            }

            // Show info
            writeJson(response, info);
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unused")
    public void getSpentByWialon(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String wialonId = request.getParameter("wialonId");
        String sFrom = request.getParameter("from");
        String sTo = request.getParameter("to");

        String fullUrl = "report_templates_filter/export_to_file.html?file_name=&flags=0&gen=1&file_type=xml&page_orientation=landscap&page_size=a4&pack_file=0&att_map=0&object_prop_id=0&coding=utf8&delimiter=semicolon&headers=1&ignore_basis=0&xlsx=1" +
                "&object_id=" + wialonId + "&from=" + sFrom + "&to=" + sTo +
                "&report_template_id=" + connWialon.templateId + // 19
                "&resource_id=" + connWialon.resourceId +        // 291
                "&tz_offset=" + connWialon.tzOffset +            // 134239328
                "&rand=" + (new Date()).getTime();

        HttpMethod method = getAsMethod(fullUrl);
        if (method == null)
            return;

        // Result
        WlnSpent wlnSpent = new WlnSpent();
        try {
            // Read XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream stream = method.getResponseBodyAsStream();
            Document document = builder.parse(stream);
            stream.close();

            // Read info about columns
            int indexOdo = -1, indexMotoHour = -1, indexTopFuel = -1, indexFuel = -1, indexBoothFuel = -1;

            NodeList headers = document.getDocumentElement().getElementsByTagName("header");
            Element header = (Element) headers.item(0);
            NodeList columns = header.getElementsByTagName("col");
            int length = columns.getLength();
            for (int i = 0; i < length; i++) {
                Element column = (Element) columns.item(i);
                String columnName = column.getAttribute("name");

                if (columnName.equals("Пробег"))
                    indexOdo = i;
                else if (columnName.equals("Моточасы"))
                    indexMotoHour = i;
                else if (columnName.equals("Будка") || columnName.equals("Booth"))
                    indexBoothFuel = i;
                else if (columnName.contains("ДАРТ") && columnName.contains("ППУА)"))
                    indexTopFuel = i;
                else if ((columnName.contains("ДАРТ") && columnName.contains("ТС)")) || columnName.equals("Потрачено по ДИРТ"))
                    indexFuel = i;
            }


            NodeList allRows = document.getDocumentElement().getElementsByTagName("row");

            length = allRows.getLength();
            for (int i = 0; i < length; i++) {
                Element el = (Element) allRows.item(i);
                columns = el.getElementsByTagName("col");

                if (indexOdo != -1)
                    wlnSpent.OdoDiff += getValue(columns, indexOdo);

                if (indexMotoHour != -1)
                    wlnSpent.MotoHour += getValue(columns, indexMotoHour);

                if (indexFuel != -1)
                    wlnSpent.Spent1 += getValue(columns, indexFuel);

                if (indexTopFuel != -1)
                    wlnSpent.Spent2 += getValue(columns, indexTopFuel);

                if (indexBoothFuel != -1)
                    wlnSpent.Spent4 += getValue(columns, indexBoothFuel);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw new ServletException(e);
        }

        // And send back to js
        writeJson(response, wlnSpent);
    }
}
