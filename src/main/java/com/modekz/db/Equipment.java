package com.modekz.db;

import org.hibersap.annotations.Parameter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "\"wb.db::pack.equipment\"")
public class Equipment {
    @Column(name="\"baujj\"",length = 4)
    @Parameter("EQUI_BAUJJ")
    public String Baujj;

    @Column(name="\"baumm\"",length = 2)
    @Parameter("EQUI_BAUMM")
    public String Baumm;

    @Column(name="\"bukrs\"",length = 4)
    @Parameter("T001K_BUKRS")
    public String Bukrs;

    @Column(name="\"datbi\"",columnDefinition = "DATE")
    @Parameter("EQUZ_DATBI")
    public Date Datbi;

    @Column(name="\"engine_type\"",length = 10)
    @Parameter("FLEET_ENGINE_TYPE")
    public String Engine_type;

    @Column(name="\"eqart\"",columnDefinition = "VARCHAR(10)")
    @Parameter("EQUI_EQART")
    public String Eqart;

    @Column(name="\"eqktx\"",columnDefinition = "VARCHAR(40)")
    @Parameter("EQKT_EQKTX")
    public String Eqktx;

    @Id
    @Column(name="\"equnr\"",length = 18)
    @Parameter("EQUI_EQUNR")
    public String Equnr;

//    @Column(name="\"expelled\"",length = 1)
//    @Parameter("_MARK")
//    public String Expelled;

    @Column(name="\"fleet_num\"",length = 18)
    @Parameter("FLEET_FLEET_NUM")
    public String Fleet_num;

    @Column(name="\"fuel_pri\"",length = 12)
    @Parameter("FLEET_FUEL_PRI")
    public String Fuel_pri;

    @Column(name="\"fuel_sec\"",length = 12)
    @Parameter("FLEET_FUEL_SEC")
    public String Fuel_sec;

    @Column(name="\"gernr\"",length = 18)
    @Parameter("EQUI_GERNR")
    public String Gernr;

    @Column(name="\"herld\"",length = 3)
    @Parameter("EQUI_HERLD")
    public String Herld;

    @Column(name="\"herst\"",columnDefinition = "VARCHAR(30)")
    @Parameter("EQUI_HERST")
    public String Herst;

    @Column(name="\"inbdt\"",columnDefinition = "DATE")
    @Parameter("EQUI_INBDT")
    public Date Inbdt;

    @Column(name="\"license_num\"",length = 15)
    @Parameter("FLEET_LICENSE_NUM")
    public String License_num;

    @Column(name="\"n_class\"",length = 15)
    @Parameter("KLAH_CLASS")
    public String N_class;

    @Column(name="\"orig_class\"",length = 15)
    @Parameter("_ORIG_CLASS")
    public String OrigClass;

    @Column(name = "\"ktschtxt\"", columnDefinition = "VARCHAR(40)")
    @Parameter("_T435T_TXT")
    public String KtschTxt;

    @Column(name="\"point\"",length = 12)
    @Parameter("IMPTT_POINT")
    public String Point;

    @Column(name="\"pltxt\"",columnDefinition = "VARCHAR(40)")
    @Parameter("IFLOTX_PLTXT")
    public String Pltxt;

    @Column(name="\"mptyp\"",length = 1)
    @Parameter("IMPTT_MPTYP")
    public String Mptyp;

    @Column(name="\"imei\"",columnDefinition = "VARCHAR(40)")
    @Parameter("WV_IMEI")
    public String Imei;

    @Column(name="\"wialonid\"",length = 5)
    @Parameter("WV_ID")
    public String WialonId;

    @Column(name="\"speed_max\"",length = 5)
    @Parameter("FLEET_SPEED_MAX")
    public BigDecimal Speed_max;

    @Column(name="\"swerk\"",length = 4)
    @Parameter("ILOA_SWERK")
    public String Swerk;

    @Column(name="\"tplnr\"",length = 30)
    @Parameter("ILOA_TPLNR")
    public String Tplnr;

    @Column(name="\"anln1\"",length = 12)
    @Parameter("ILOA_ANLNR")
    public String Anln1;

    @Column(name="\"typbz\"",columnDefinition = "VARCHAR(20)")
    @Parameter("EQUI_TYPBZ")
    public String Typbz;

    @Column(name="\"petrolmode\"",columnDefinition = "VARCHAR(3)")
    @Parameter("WV_PETROL_MODE")
    public String PetrolMode;

    @Column(name="\"tooname\"",columnDefinition = "VARCHAR(50)")
    public String TooName = "-";

    @Column(name="\"nodriverdate\"",columnDefinition = "TIMESTAMP")
    public Date NoDriverDate;

    @PrePersist
    @PreUpdate
    public void persist() {
        if (NoDriverDate == null)
            return;

        // Set current date
        if (NoDriverDate.getTime() == 1)
            this.setNoDriverDate(new Date());
    }

    public String getBaujj() {
        return Baujj;
    }

    public void setBaujj(String baujj) {
        Baujj = baujj;
    }

    public String getBaumm() {
        return Baumm;
    }

    public void setBaumm(String baumm) {
        Baumm = baumm;
    }

    public String getBukrs() {
        return Bukrs;
    }

    public void setBukrs(String bukrs) {
        Bukrs = bukrs;
    }

    public String getMptyp() {
        return Mptyp;
    }

    public void setMptyp(String mptyp) {
        Mptyp = mptyp;
    }

    public Date getDatbi() {
        return Datbi;
    }

    public void setDatbi(Date datbi) {
        Datbi = datbi;
    }

    public String getEngine_type() {
        return Engine_type;
    }

    public void setEngine_type(String engine_type) {
        Engine_type = engine_type;
    }

    public String getEqart() {
        return Eqart;
    }

    public void setEqart(String eqart) {
        Eqart = eqart;
    }

    public String getEqktx() {
        return Eqktx;
    }

    public void setEqktx(String eqktx) {
        Eqktx = eqktx;
    }

    public String getEqunr() {
        return Equnr;
    }

    public void setEqunr(String equnr) {
        Equnr = equnr;
    }

    public String getFleet_num() {
        return Fleet_num;
    }

    public void setFleet_num(String fleet_num) {
        Fleet_num = fleet_num;
    }

    public String getFuel_pri() {
        return Fuel_pri;
    }

    public void setFuel_pri(String fuel_pri) {
        Fuel_pri = fuel_pri;
    }

    public String getFuel_sec() {
        return Fuel_sec;
    }

    public void setFuel_sec(String fuel_sec) {
        Fuel_sec = fuel_sec;
    }

    public String getGernr() {
        return Gernr;
    }

    public void setGernr(String gernr) {
        Gernr = gernr;
    }

    public String getHerld() {
        return Herld;
    }

    public void setHerld(String herld) {
        Herld = herld;
    }

    public String getHerst() {
        return Herst;
    }

    public void setHerst(String herst) {
        Herst = herst;
    }

    public Date getInbdt() {
        return Inbdt;
    }

    public void setInbdt(Date inbdt) {
        Inbdt = inbdt;
    }

    public String getLicense_num() {
        return License_num;
    }

    public void setLicense_num(String license_num) {
        License_num = license_num;
    }

    public String getN_class() {
        return N_class;
    }

    public void setN_class(String n_class) {
        N_class = n_class;
    }

    public String getPltxt() {
        return Pltxt;
    }

    public void setPltxt(String pltxt) {
        Pltxt = pltxt;
    }

    public String getPoint() {
        return Point;
    }

    public void setPoint(String point) {
        Point = point;
    }

    public BigDecimal getSpeed_max() {
        return Speed_max;
    }

    public void setSpeed_max(BigDecimal speed_max) {
        Speed_max = speed_max;
    }

    public String getSwerk() {
        return Swerk;
    }

    public void setSwerk(String swerk) {
        Swerk = swerk;
    }

    public String getTplnr() {
        return Tplnr;
    }

    public void setTplnr(String tplnr) {
        Tplnr = tplnr;
    }

    public String getTypbz() {
        return Typbz;
    }

    public void setTypbz(String typbz) {
        Typbz = typbz;
    }

    public String getImei() {
        return Imei;
    }

    public void setImei(String imei) {
        Imei = imei;
    }

    public String getWialonId() {
        return WialonId;
    }

    public void setWialonId(String wialonId) {
        WialonId = wialonId;
    }

    public String getTooName() {
        return TooName;
    }

    public void setTooName(String tooName) {
        TooName = tooName;
    }

    public Date getNoDriverDate() {
        return NoDriverDate;
    }

    public void setNoDriverDate(Date noDriverDate) {
        NoDriverDate = noDriverDate;
    }

    public String getAnln1() {
        return Anln1;
    }

    public void setAnln1(String anln1) {
        Anln1 = anln1;
    }

    public String getKtschTxt() {
        return KtschTxt;
    }

    public void setKtschTxt(String ktschTxt) {
        KtschTxt = ktschTxt;
    }

    public String getOrigClass() {
        return OrigClass;
    }

    public void setOrigClass(String origClass) {
        OrigClass = origClass;
    }

    public String getPetrolMode() {
        return PetrolMode;
    }

    public void setPetrolMode(String petrolMode) {
        PetrolMode = petrolMode;
    }

//    public String getExpelled() {
//        return Expelled;
//    }
//
//    public void setExpelled(String expelled) {
//        Expelled = expelled;
//    }
}
