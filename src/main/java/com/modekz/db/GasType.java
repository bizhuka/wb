package com.modekz.db;

import org.hibersap.annotations.BapiStructure;
import org.hibersap.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@BapiStructure
public class GasType {
    @Id
    @Column(length = 18)
    @Parameter("MARA_MATNR")
    public String Matnr;

    @Column(columnDefinition = "NVARCHAR(40)")
    @Parameter("MAKT_MAKTX")
    public String Maktx;

    @Column(columnDefinition = "NVARCHAR(30)")
    @Parameter("T006A_MSEHL")
    public String Msehl;

    public String getMatnr() {
        return Matnr;
    }

    public void setMatnr(String matnr) {
        this.Matnr = matnr;
    }

    public String getMaktx() {
        return Maktx;
    }

    public void setMaktx(String maktx) {
        this.Maktx = maktx;
    }

    public String getMsehl() {
        return Msehl;
    }

    public void setMsehl(String msehl) {
        this.Msehl = msehl;
    }
}
