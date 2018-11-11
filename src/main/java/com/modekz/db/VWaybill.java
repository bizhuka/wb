package com.modekz.db;

import com.modekz.db.flag.DelayReason;
import com.modekz.db.flag.Status;
import com.sap.db.annotations.Immutable;

import javax.persistence.*;
import java.util.Date;

@Entity
@Immutable
@Table(name = "\"v_waybill\"")
public class VWaybill {

    @GeneratedValue
    @Id
    @Column(name="\"id\"")
    public long id;

    @Column(name="\"description\"",columnDefinition = "VARCHAR(150)")
    public String description;

    @Column(name="\"equnr\"",length = 18)
    public String equnr;

    @Column(name="\"driver\"",length = 8)
    public String driver;

    @Column(name="\"bukrs\"",length = 4, nullable = false, updatable = false)
    public String bukrs;

    @Column(name="\"fromdate\"",columnDefinition = "DATE")
    public Date fromDate;

    @Column(name="\"todate\"",columnDefinition = "DATE")
    public Date toDate;

    @Column(name="\"createdate\"",columnDefinition = "TIMESTAMP")
    public Date createDate;

    @Column(name="\"confirmdate\"",columnDefinition = "TIMESTAMP")
    public Date confirmDate;

    @Column(name="\"garagedepdate\"",columnDefinition = "TIMESTAMP")
    public Date garageDepDate;

    @Column(name="\"garagearrdate\"",columnDefinition = "TIMESTAMP")
    public Date garageArrDate;

    @Column(name="\"closedate\"",columnDefinition = "TIMESTAMP")
    public Date closeDate;

    @Column(name="\"werks\"",length = 4, nullable = false, updatable = false)
    public String werks;

    @Column(name="\"status\"")
    public int status = Status.CREATED;

    @Column(name="\"ododiff\"")
    public double odoDiff;

    @Column(name="\"motohour\"")
    public double motoHour;

    @Column(name="\"gasspent\"")
    public double gasSpent;

    @Column(name="\"gastopspent\"")
    public double gasTopSpent;

    @Column(name="\"delayreason\"")
    public int delayReason = DelayReason.NO_DELAY;

    // Who last changed
    @Column(name="\"changeuser\"",columnDefinition = "VARCHAR(40)")
    public String changeUser;
    @Column(name="\"changedate\"",columnDefinition = "TIMESTAMP")
    public Date changeDate;

    @Column(name="\"docum\"",length = 20)
    public String docum;

    @Column(name="\"aufnr\"",length = 12)
    public String aufnr;

    @Column(name="\"withnoreqs\"")
    public boolean withNoReqs = false;

    @Column(name="\"req_cnt\"")
    public long Req_Cnt;
    @Column(name="\"sch_cnt\"")
    public long Sch_Cnt;
    @Column(name="\"hist_cnt\"")
    public long Hist_Cnt;
    @Column(name="\"gas_cnt\"")
    public long Gas_Cnt;

    @Column(name="\"eqktx\"",columnDefinition = "VARCHAR(40)")
    public String Eqktx;

    @Column(name="\"fio\"",columnDefinition = "VARCHAR(30)")
    public String Fio;

    @Column(name="\"point\"",length = 12)
    public String Point;

    @Column(name="\"wialonid\"",length = 5)
    public String WialonId;

    @Column(name="\"license_num\"",length = 15)
    public String License_num;

    @Column(name="\"mptyp\"",length = 1)
    public String Mptyp;

    @Column(name="\"tooname\"",columnDefinition = "VARCHAR(50)")
    public String TooName = "-";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEqunr() {
        return equnr;
    }

    public void setEqunr(String equnr) {
        this.equnr = equnr;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getBukrs() {
        return bukrs;
    }

    public void setBukrs(String bukrs) {
        this.bukrs = bukrs;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(Date confirmDate) {
        this.confirmDate = confirmDate;
    }

    public Date getGarageDepDate() {
        return garageDepDate;
    }

    public void setGarageDepDate(Date garageDepDate) {
        this.garageDepDate = garageDepDate;
    }

    public Date getGarageArrDate() {
        return garageArrDate;
    }

    public void setGarageArrDate(Date garageArrDate) {
        this.garageArrDate = garageArrDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public String getWerks() {
        return werks;
    }

    public void setWerks(String werks) {
        this.werks = werks;
    }

    public int getStatus() {
        return status;
    }

    // ----------------

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getEqktx() {
        return Eqktx;
    }

    public void setEqktx(String eqktx) {
        Eqktx = eqktx;
    }

    public String getFio() {
        return Fio;
    }

    public void setFio(String fio) {
        Fio = fio;
    }

    public long getReq_Cnt() {
        return Req_Cnt;
    }

    public void setReq_Cnt(long req_Cnt) {
        Req_Cnt = req_Cnt;
    }

    public long getSch_Cnt() {
        return Sch_Cnt;
    }

    public void setSch_Cnt(long sch_Cnt) {
        Sch_Cnt = sch_Cnt;
    }

    public String getPoint() {
        return Point;
    }

    public void setPoint(String point) {
        Point = point;
    }

    public String getWialonId() {
        return WialonId;
    }

    public void setWialonId(String wialonId) {
        WialonId = wialonId;
    }

    public String getLicense_num() {
        return License_num;
    }

    public void setLicense_num(String license_num) {
        License_num = license_num;
    }

    public double getMotoHour() {
        return motoHour;
    }

    public void setMotoHour(double motoHour) {
        this.motoHour = motoHour;
    }

    public double getOdoDiff() {
        return odoDiff;
    }

    public void setOdoDiff(double odoDiff) {
        this.odoDiff = odoDiff;
    }

    public double getGasSpent() {
        return gasSpent;
    }

    public void setGasSpent(double gasSpent) {
        this.gasSpent = gasSpent;
    }

    public long getHist_Cnt() {
        return Hist_Cnt;
    }

    public void setHist_Cnt(long hist_Cnt) {
        Hist_Cnt = hist_Cnt;
    }

    public long getGas_Cnt() {
        return Gas_Cnt;
    }

    public void setGas_Cnt(long gas_Cnt) {
        Gas_Cnt = gas_Cnt;
    }

    public String getMptyp() {
        return Mptyp;
    }

    public void setMptyp(String mptyp) {
        Mptyp = mptyp;
    }

    public int getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(int delayReason) {
        this.delayReason = delayReason;
    }

    public String getTooName() {
        return TooName;
    }

    public void setTooName(String tooName) {
        TooName = tooName;
    }

    public String getChangeUser() {
        return changeUser;
    }

    public void setChangeUser(String changeUser) {
        this.changeUser = changeUser;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public double getGasTopSpent() {
        return gasTopSpent;
    }

    public void setGasTopSpent(double gasTopSpent) {
        this.gasTopSpent = gasTopSpent;
    }

    public String getDocum() {
        return docum;
    }

    public void setDocum(String docum) {
        this.docum = docum;
    }

    public String getAufnr() {
        return aufnr;
    }

    public void setAufnr(String aufnr) {
        this.aufnr = aufnr;
    }

    public boolean isWithNoReqs() {
        return withNoReqs;
    }

    public void setWithNoReqs(boolean withNoReqs) {
        this.withNoReqs = withNoReqs;
    }
}
