package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibersap.annotations.BapiStructure;
import org.hibersap.annotations.Parameter;

import javax.persistence.*;

@Entity
@BapiStructure
@PrimaryKey(columns = {@Column(name = "\"werks\"", length = 4), @Column(name = "\"lgort\"", length = 4)})
@Table(name = "\"wb.dbt::pack.lgort\"")
public class Lgort {
    @Id
    @Column(name = "\"werks\"", length = 4)
    @Parameter("T001L_WERKS")
    public String Werks;

    @Id
    @Column(name = "\"lgort\"", length = 4)
    @Parameter("T001L_LGORT")
    public String Lgort;

    @Column(name="\"lgobe\"",columnDefinition = "VARCHAR(16)")
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




