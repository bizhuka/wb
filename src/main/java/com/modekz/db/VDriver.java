package com.modekz.db;

import com.sap.db.annotations.Immutable;
import org.hibersap.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Immutable
@Table(name = "V_DRIVER")
public class VDriver {
    @Id
    @Parameter("DR_BE")
    public String Bukrs;

    @Parameter("DR_DATBEG")
    @Column(columnDefinition = "DATE")
    public Date Datbeg;

    @Parameter("DR_FIO")
    @Column(columnDefinition = "NVARCHAR(30)")
    public String Fio;

    @Id
    @Parameter("DR_TN")
    public String Pernr;

    @Column(columnDefinition = "NVARCHAR(50)")
    @Parameter("DR_PODR")
    public String Podr;

    @Column(columnDefinition = "NVARCHAR(120)")
    @Parameter("DR_POST")
    public String Post;

    @Parameter("DR_STCD3")
    @Column(length = 18)
    public String Stcd3;

    @Column(length = 32)
    public String Barcode;

    @Column(columnDefinition = "TIMESTAMP")
    public Date ValidDate;

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public Date getValidDate() {
        return ValidDate;
    }

    public void setValidDate(Date validDate) {
        ValidDate = validDate;
    }

    public String getBukrs() {
        return Bukrs;
    }

    public void setBukrs(String bukrs) {
        Bukrs = bukrs;
    }

    public Date getDatbeg() {
        return Datbeg;
    }

    public void setDatbeg(Date datbeg) {
        Datbeg = datbeg;
    }

    public String getFio() {
        return Fio;
    }

    public void setFio(String fio) {
        Fio = fio;
    }

    public String getPernr() {
        return Pernr;
    }

    public void setPernr(String pernr) {
        Pernr = pernr;
    }

    public String getPodr() {
        return Podr;
    }

    public void setPodr(String podr) {
        Podr = podr;
    }

    public String getPost() {
        return Post;
    }

    public void setPost(String post) {
        Post = post;
    }

    public String getStcd3() {
        return Stcd3;
    }

    public void setStcd3(String stcd3) {
        Stcd3 = stcd3;
    }
}
