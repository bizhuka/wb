package com.modekz.json;

import com.modekz.servlet.ServletBase;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfoException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    public String login;
    public String email;
    public String firstName;
    public String lastName;
    public List<String> groups;
    public List<String> werks;

    public static UserInfo getCurrentUserInfo(ServletBase servlet) throws UserInfoException, IOException {
        if (SystemUtils.IS_OS_WINDOWS) {
            String context = IOUtils.toString(
                    servlet.getServletContext().getResourceAsStream("/json/UserInfo.json"), "UTF-8");
            return servlet.gson.fromJson(context, UserInfo.class);
        }

        // From secure origin
        com.sap.xs2.security.container.UserInfo info = SecurityContext.getUserInfo();
        UserInfo result = new UserInfo();
        result.login = info.getLogonName();
        result.email = info.getEmail();
        result.firstName = info.getGivenName();
        result.lastName = info.getFamilyName();

        //noinspection deprecation
        String[] items = info.getSystemAttribute("xs.saml.groups");
        result.groups = new ArrayList<>(items.length);
        result.werks = new ArrayList<>();

        // All groups and
        for (String item : items)
            if (item.startsWith("Werks_"))
                result.werks.add(item.substring(6));
            else
                result.groups.add(item);

        return result;
    }

    public boolean hasGroup(String group) {
        return groups.indexOf(group) >= 0;
    }

    public String groupsAsJson() {
        StringBuilder sb = new StringBuilder();
        for (String group : groups)
            sb.append(",\"" + group + "\" : true");
        return sb.toString();
    }
}
