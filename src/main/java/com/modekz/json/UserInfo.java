package com.modekz.json;

import com.modekz.db.GroupRole;
import com.modekz.servlet.ServletBase;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfoException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;

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
    public List<String> groups;
    public List<String> werks;

    public static UserInfo getCurrentUserInfo(ServletBase servlet) throws UserInfoException, IOException, ServletException {
        UserInfo result;
        if (SystemUtils.IS_OS_WINDOWS) {
            String context = IOUtils.toString(
                    servlet.getServletContext().getResourceAsStream("/json/UserInfo.json"), "UTF-8");
            result = servlet.gson.fromJson(context, UserInfo.class);
        } else {
            // From secure origin
            com.sap.xs2.security.container.UserInfo info = SecurityContext.getUserInfo();
            result = new UserInfo();
            result.login = info.getLogonName();
            result.email = info.getEmail();
            result.firstName = info.getGivenName();
            result.lastName = info.getFamilyName();

            //noinspection deprecation
            result.groups = Arrays.asList(info.getSystemAttribute("xs.saml.groups"));
        }
        // All items
        List<GroupRole> grpRoles = GroupRole.getAllGroupRoles();

        // Copy to temp var
        List<String> items = result.groups;

        // Split
        result.groups = new ArrayList<>(items.size());
        result.werks = new ArrayList<>();

        // All groups and
        for (String item : items)
            if (item.startsWith("Werks_"))
                result.werks.add(item.substring(6));
            else if (item.startsWith("_Wb")) {
                for (GroupRole groupRole : grpRoles)
                    if (item.equals(groupRole.GrpRole))
                        result.groups.add(groupRole.IndRole);
            } else
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
