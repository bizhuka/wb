package com.modekz.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Login {
    @Id
    @Column(length = 30)
    public String Id;

    @Column(length = 150)
    public String WerksList;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getWerksList() {
        return WerksList;
    }

    public void setWerksList(String werksList) {
        WerksList = werksList;
    }
}
