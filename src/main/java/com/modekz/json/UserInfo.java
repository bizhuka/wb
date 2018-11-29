package com.modekz.json;

import com.modekz.servlet.ServletBase;
import com.sap.xs2.security.container.UserInfoException;
import org.apache.commons.lang.SystemUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserInfo {
    public String login;
    public String email;
    public String firstName;
    public String lastName;
    public List<String> scopes;
    public List<String> werks;

    public static UserInfo getCurrentUserInfo(ServletBase servlet) throws UserInfoException, IOException, ServletException {
        // Main result
        UserInfo result;

        // tech info
        final JSONObject[] jsonObjects = new JSONObject[1];

        if (SystemUtils.IS_OS_WINDOWS) {
            if (servlet == null)
                return null;

            String json = servlet.getFileAsString("/json/UserInfo.json");
            result = servlet.gson.fromJson(json, UserInfo.class);

            // For test from local file
            json = servlet.getFileAsString("/json/token.json");
            try {
                jsonObjects[0] = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            result = new UserInfo();

            // From secure origin
            com.sap.xs2.security.container.UserInfo info = new com.sap.xs2.security.container.UserInfo() {
                {
                    jsonObjects[0] = this.json;
                }
            };
            result.login = info.getLogonName();
            result.email = info.getEmail();
            result.firstName = info.getGivenName();
            result.lastName = info.getFamilyName();

            //noinspection deprecation
            List<String> items = Arrays.asList(info.getSystemAttribute("xs.saml.groups"));
            // Read allowed werks
            result.werks = new ArrayList<>(items.size());

            // All scopes and
            for (String item : items)
                if (item.startsWith("Werks_"))
                    result.werks.add(item.substring(6));
        }

        // Read scopes
        result.scopes = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObjects[0].getJSONArray("scope");
            for (int i = 0; i < jsonArray.length(); ++i) {
                String scope = jsonArray.getString(i);
                String[] arr = scope.split("\\.");
                if (arr.length == 2)
                    result.scopes.add(arr[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String scopesAsJson() {
        StringBuilder sb = new StringBuilder();
        for (String group : scopes)
            sb.append(",\"" + group + "\" : true");
        return sb.toString();
    }

    public String werksCondition() {
        StringBuilder sb = new StringBuilder();
        for (String werk : werks)
            sb.append(",'" + werk + "'");
        return "(" + sb.toString().substring(1) + ")";
    }
}
