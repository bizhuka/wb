package com.modekz.db;

import com.sap.db.annotations.Immutable;
import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibersap.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Immutable
@Table(name = "\"v_driver\"")
@PrimaryKey(columns = {@Column(name = "\"bukrs\"", length = 4), @Column(name = "\"pernr\"", length = 8)})
public class VDriver {
    @Id
    @Column(name = "\"bukrs\"")
    @Parameter("DR_BE")
    public String Bukrs;

    @Parameter("DR_DATBEG")
    @Column(name = "\"datbeg\"", columnDefinition = "DATE")
    public Date Datbeg;

    @Parameter("DR_FIO")
    @Column(name = "\"fio\"", columnDefinition = "VARCHAR(30)")
    public String Fio;

    @Id
    @Column(name = "\"pernr\"")
    @Parameter("DR_TN")
    public String Pernr;

    @Column(name = "\"podr\"", columnDefinition = "VARCHAR(50)")
    @Parameter("DR_PODR")
    public String Podr;

    @Column(name = "\"post\"", columnDefinition = "VARCHAR(120)")
    @Parameter("DR_POST")
    public String Post;

    @Parameter("DR_STCD3")
    @Column(name = "\"stcd3\"", length = 18)
    public String Stcd3;

    @Column(name = "\"barcode\"", length = 32)
    public String Barcode;

    @Column(name = "\"validdate\"", columnDefinition = "TIMESTAMP")
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
