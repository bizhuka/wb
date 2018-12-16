package com.modekz.db;

import com.modekz.ODataServiceFactory;
import com.modekz.db.flag.Status;
import org.hibersap.annotations.Parameter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "\"wb.db::pack.reqheader\"")
public class ReqHeader {
    @Column(name = "\"aufnr\"", length = 12)
    @Parameter("AFIH_AUFNR")
    public String Aufnr;

    @Column(name = "\"beber\"", length = 3)
    @Parameter("ILOA_BEBER")
    public String Beber;

    @Column(name = "\"equnr\"", length = 18)
    @Parameter("AFIH_EQUNR")
    public String Equnr;

    @Column(name = "\"gltrp\"", columnDefinition = "DATE")
    @Parameter("AFKO_GLTRP")
    public Date Gltrp;

    @Column(name = "\"gstrp\"", columnDefinition = "DATE")
    @Parameter("AFKO_GSTRP")
    public Date Gstrp;

    @Column(name = "\"waybill_id\"")
    public long Waybill_Id = Status.WB_ID_NULL;

    @Column(name = "\"ilart\"", columnDefinition = "VARCHAR(3)")
    @Parameter("AFIH_ILART")
    public String Ilart;

    @Column(name = "\"ilatx\"", columnDefinition = "VARCHAR(30)")
    @Parameter("T353I_ILATX")
    public String Ilatx;

    @Column(name = "\"ingpr\"", length = 3)
    @Parameter("AFIH_INGPR")
    public String Ingpr;

    @Column(name = "\"innam\"", columnDefinition = "VARCHAR(18)")
    @Parameter("T024I_INNAM")
    public String Innam;

    @Column(name = "\"iwerk\"", length = 4)
    @Parameter("AFIH_IWERK")
    public String Iwerk;

    @Column(name = "\"ktsch\"", length = 7)
    @Parameter("AFVC_KTSCH")
    public String Ktsch;

    @Column(name = "\"ltxa1\"", columnDefinition = "VARCHAR(40)")
    @Parameter("AFVC_LTXA1")
    public String Ltxa1;

    @Id
    @Column(name = "\"objnr\"", length = 22)
    @Parameter("AFVC_OBJNR")
    public String Objnr;

    @Column(name = "\"pltxt\"", columnDefinition = "VARCHAR(40)")
    @Parameter("IFLOTX_PLTXT")
    public String Pltxt;

    @Column(name = "\"priok\"", length = 1)
    @Parameter("AFIH_PRIOK")
    public String Priok;

    @Column(name = "\"priokx\"", columnDefinition = "VARCHAR(20)")
    @Parameter("T356_PRIOKX")
    public String Priokx;

    @Column(name = "\"stand\"", columnDefinition = "VARCHAR(40)")
    @Parameter("T499S_KTEXT")
    public String Stand;

    @Column(name = "\"tplnr\"", length = 30)
    @Parameter("ILOA_TPLNR")
    public String Tplnr;

    @Column(name = "\"hours\"", columnDefinition = "VARCHAR(11)")
    @Parameter("_HOURS")
    public String Hours;

    @Column(name = "\"duration\"", columnDefinition = "FLOAT")
    @Parameter("AFVV_DAUNO")
    public BigDecimal Duration;

    @Column(name = "\"ktschtxt\"", columnDefinition = "VARCHAR(40)")
    @Parameter("T435T_TXT")
    public String KtschTxt;

    @Column(name = "\"reason\"", columnDefinition = "VARCHAR(100)")
    public String reason;

    @Column(name = "\"statusreason\"")
    public int statusReason = Status.RC_NEW;
    @Column(name = "\"fromdate\"", columnDefinition = "TIMESTAMP")
    public Date fromDate;
    @Column(name = "\"todate\"", columnDefinition = "TIMESTAMP")
    public Date toDate;

    @Column(name = "\"fing\"", columnDefinition = "VARCHAR(14)")
    @Parameter("T357_FING")
    public String Fing;

    @PreUpdate
    public void persist() {
        if (this.Waybill_Id == Status.WB_ID_NULL)
            return;

        EntityManager em = null;
        try {
            em = ODataServiceFactory.getEmf().createEntityManager();
            em.getTransaction().begin();

            Waybill waybill = em.find(Waybill.class, this.Waybill_Id);

            // Only if is cancelled
            if (waybill.status < Status.ARRIVED) {
                // Insert for history
                em.merge(new ReqHistory(Waybill_Id, Objnr));
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null)
                em.close();
        }
    }

    public String getAufnr() {
        return Aufnr;
    }

    public void setAufnr(String aufnr) {
        Aufnr = aufnr;
    }

    public String getBeber() {
        return Beber;
    }

    public void setBeber(String beber) {
        Beber = beber;
    }

    public String getEqunr() {
        return Equnr;
    }

    public void setEqunr(String equnr) {
        Equnr = equnr;
    }

    public Date getGltrp() {
        return Gltrp;
    }

    public void setGltrp(Date gltrp) {
        Gltrp = gltrp;
    }

    public Date getGstrp() {
        return Gstrp;
    }

    public void setGstrp(Date gstrp) {
        Gstrp = gstrp;
    }

    public String getIlart() {
        return Ilart;
    }

    public void setIlart(String ilart) {
        Ilart = ilart;
    }

    public String getIlatx() {
        return Ilatx;
    }

    public void setIlatx(String ilatx) {
        Ilatx = ilatx;
    }

    public String getIngpr() {
        return Ingpr;
    }

    public void setIngpr(String ingpr) {
        Ingpr = ingpr;
    }

    public String getInnam() {
        return Innam;
    }

    public void setInnam(String innam) {
        Innam = innam;
    }

    public String getIwerk() {
        return Iwerk;
    }

    public void setIwerk(String iwerk) {
        Iwerk = iwerk;
    }

    public String getKtsch() {
        return Ktsch;
    }

    public void setKtsch(String ktsch) {
        Ktsch = ktsch;
    }

    public String getLtxa1() {
        return Ltxa1;
    }

    public void setLtxa1(String ltxa1) {
        Ltxa1 = ltxa1;
    }

    public String getObjnr() {
        return Objnr;
    }

    public void setObjnr(String objnr) {
        Objnr = objnr;
    }

    public String getPltxt() {
        return Pltxt;
    }

    public void setPltxt(String pltxt) {
        Pltxt = pltxt;
    }

    public String getPriok() {
        return Priok;
    }

    public void setPriok(String priok) {
        Priok = priok;
    }

    public String getPriokx() {
        return Priokx;
    }

    public void setPriokx(String priokx) {
        Priokx = priokx;
    }

    public String getStand() {
        return Stand;
    }

    public void setStand(String stand) {
        Stand = stand;
    }

    public String getTplnr() {
        return Tplnr;
    }

    public void setTplnr(String tplnr) {
        Tplnr = tplnr;
    }

    public long getWaybill_Id() {
        return Waybill_Id;
    }

    public void setWaybill_Id(long waybill_Id) {
        Waybill_Id = waybill_Id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(int statusReason) {
        this.statusReason = statusReason;
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

    public BigDecimal getDuration() {
        return Duration;
    }

    public void setDuration(BigDecimal duration) {
        Duration = duration;
    }

    public String getKtschTxt() {
        return KtschTxt;
    }

    public void setKtschTxt(String ktschTxt) {
        KtschTxt = ktschTxt;
    }

    public String getFing() {
        return Fing;
    }

    public void setFing(String fing) {
        this.Fing = fing;
    }

    public String getHours() {
        return Hours;
    }

    public void setHours(String hours) {
        Hours = hours;
    }
}
