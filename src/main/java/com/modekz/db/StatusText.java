package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibersap.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "\"wb.db::pack.statustext\"")
@PrimaryKey(columns = {@Column(name = "\"stype\"", length = 2), @Column(name = "\"id\"")})
public class StatusText {
    @Column(name = "\"stype\"")
    @Parameter("STATUS_TYPE")
    public String Stype;

    @Column(name = "\"id\"")
    @Parameter("STATUS_ID")
    public int Id;

    @Column(name = "\"kz\"", columnDefinition = "VARCHAR(40)")
    @Parameter("KZ")
    public String Kz;

    @Column(name = "\"ru\"", columnDefinition = "VARCHAR(40)")
    @Parameter("RU")
    public String Ru;

    public String getStype() {
        return Stype;
    }

    public void setStype(String stype) {
        Stype = stype;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getKz() {
        return Kz;
    }

    public void setKz(String kz) {
        Kz = kz;
    }

    public String getRu() {
        return Ru;
    }

    public void setRu(String ru) {
        Ru = ru;
    }
}
