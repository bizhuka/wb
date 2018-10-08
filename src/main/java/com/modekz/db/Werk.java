package com.modekz.db;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import org.hibersap.annotations.BapiStructure;
import org.hibersap.annotations.Parameter;

@Entity
@BapiStructure
public class Werk {
    @Basic
    @Column(length = 4)
    @Parameter("T001K_BUKRS")
    public String Bukrs;

    @Column(columnDefinition = "NVARCHAR(25)")
    @Parameter("T001_BUTXT")
    public String Butxt;

    @Column(columnDefinition = "NVARCHAR(30)")
    @Parameter("T001W_NAME1")
    public String Name1;

    @Column(length = 4)
    @Parameter("T001W_WERKS")
    @Id
    public String Werks;

    public String getBukrs() {
        return Bukrs;
    }

    public void setBukrs(String bukrs) {
        Bukrs = bukrs;
    }

    public String getName1() {
        return Name1;
    }

    public void setName1(String name1) {
        Name1 = name1;
    }

    public String getWerks() {
        return Werks;
    }

    public void setWerks(String werks) {
        Werks = werks;
    }

    public String getButxt() {
        return Butxt;
    }

    public void setButxt(String butxt) {
        Butxt = butxt;
    }
}
