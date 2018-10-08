package com.modekz.json;

public class ConnVCAP {
    public Hana[] hana;

    public static class Hana {
        public String name;
        public String instance_name;

        public Credentials credentials;
    }

    public static class Credentials {
        public String url;
        public String user;
        public String password;
    }
}
