package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibersap.annotations.BapiStructure;
import org.hibersap.annotations.Parameter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@BapiStructure
@PrimaryKey(columns = {@Column(name = "WERKS", length = 4), @Column(name = "LGORT", length = 4)})
public class Lgort {
    @Basic
    @Column
    @Parameter("T001L_WERKS")
    public String Werks;

    @Column
    @Parameter("T001L_LGORT")
    public String Lgort;

    @Column(columnDefinition = "NVARCHAR(16)")
    @Parameter("T001L_LGOBE")
    public String Lgobe;

    public String getWerks() {
        return Werks;
    }

    public void setWerks(String werks) {
        Werks = werks;
    }

    public String getLgort() {
        return Lgort;
    }

    public void setLgort(String lgort) {
        Lgort = lgort;
    }

    public String getLgobe() {
        return Lgobe;
    }

    public void setLgobe(String lgobe) {
        Lgobe = lgobe;
    }
}




