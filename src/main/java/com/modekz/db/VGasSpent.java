package com.modekz.db;

import com.modekz.ODataServiceFactory;
import com.modekz.db.flag.Status;
import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibersap.annotations.Parameter;

import javax.persistence.*;
import javax.servlet.ServletException;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@PrimaryKey(columns = {@Column(name = "\"waybill_id\""), @Column(name = "\"pttype\""), @Column(name = "\"pos\"")})
@Table(name = "\"v_gasspent\"")
@IdClass(GasSpentKey.class)
public class VGasSpent {
    @Id
    @Column(name = "\"waybill_id\"")
    public long Waybill_Id;

    @Id
    @Column(name = "\"pttype\"")
    public int PtType;

    @Id
    @Column(name = "\"pos\"")
    // Relative position is crucial
    public int Pos;

    @Column(name = "\"gasmatnr\"", length = 18, nullable = false)
    public String GasMatnr;

    @Parameter("BEFORE")
    @Column(name = "\"gasbefore\"", columnDefinition = "FLOAT")
    public BigDecimal GasBefore;

    @Parameter("GIVE")
    @Column(name = "\"gasgive\"", columnDefinition = "FLOAT")
    public BigDecimal GasGive;

    @Parameter("GIVEN")
    @Column(name = "\"gasgiven\"", columnDefinition = "FLOAT")
    public BigDecimal GasGiven;

    @Column(name = "\"gaslgort\"", length = 4)
    public String GasLgort;

    @Column(name = "\"license_num\"", length = 15)
    public String License_num;

    @Column(name = "\"equnr\"", length = 18)
    public String equnr;

    @Column(name = "\"anln1\"", length = 12)
    public String Anln1;

    @Column(name = "\"imei\"", columnDefinition = "VARCHAR(40)")
    public String Imei;

    @Column(name = "\"ktschtxt\"", columnDefinition = "VARCHAR(40)")
    public String KtschTxt;

    @Column(name = "\"werks\"", length = 4, nullable = false, updatable = false)
    public String werks;

    @Column(name = "\"id\"")
    public long id;

    @Column(name = "\"fromdate\"", columnDefinition = "DATE")
    public Date fromDate;

    @Column(name = "\"todate\"", columnDefinition = "DATE")
    public Date toDate;

    @Column(name = "\"createdate\"", columnDefinition = "TIMESTAMP")
    public Date createDate;

    @Column(name = "\"ododiff\"")
    public double odoDiff;

    @Column(name = "\"motohour\"")
    public double motoHour;

    @Column(name = "\"description\"", columnDefinition = "VARCHAR(150)")
    public String description;

    @Column(name = "\"eqktx\"", columnDefinition = "VARCHAR(40)")
    public String Eqktx;

    @Column(name = "\"tooname\"", columnDefinition = "VARCHAR(50)")
    public String TooName = "-";

    @Column(name = "\"status\"")
    public int status = Status.CREATED;

    @Column(name = "\"maktx\"", columnDefinition = "VARCHAR(40)")
    public String Maktx;

    @Column(name = "\"pttype_kz\"", columnDefinition = "VARCHAR(40)")
    public String Pttype_kz;

    @Column(name = "\"pttype_ru\"", columnDefinition = "VARCHAR(40)")
    public String Pttype_ru;

    ////////////////////////////////////
    @Column(name = "\"gasspent\"", columnDefinition = "FLOAT")
    public BigDecimal GasSpent;

    @Column(name = "\"gasafter_next\"", columnDefinition = "FLOAT")
    public BigDecimal GasAfterNext;

    @Column(name = "\"gasafter\"", columnDefinition = "FLOAT")
    public BigDecimal GasAfter;

    @Column(name = "\"spent1\"")
    public double spent1;

    @Column(name = "\"spent2\"")
    public double spent2;

    @Column(name = "\"spent4\"")
    public double spent4;

    ////////////////////////////////////
    public long getWaybill_Id() {
        return Waybill_Id;
    }

    public void setWaybill_Id(long waybill_Id) {
        Waybill_Id = waybill_Id;
    }

    public int getPtType() {
        return PtType;
    }

    public void setPtType(int ptType) {
        PtType = ptType;
    }

    public int getPos() {
        return Pos;
    }

    public void setPos(int pos) {
        this.Pos = pos;
    }

    public String getGasMatnr() {
        return GasMatnr;
    }

    public void setGasMatnr(String GasMatnr) {
        this.GasMatnr = GasMatnr;
    }

    public BigDecimal getGasBefore() {
        return GasBefore;
    }

    public void setGasBefore(BigDecimal GasBefore) {
        this.GasBefore = GasBefore;
    }

    public BigDecimal getGasGive() {
        return GasGive;
    }

    public void setGasGive(BigDecimal GasGive) {
        this.GasGive = GasGive;
    }

    public BigDecimal getGasGiven() {
        return GasGiven;
    }

    public void setGasGiven(BigDecimal GasGiven) {
        this.GasGiven = GasGiven;
    }

    public String getGasLgort() {
        return GasLgort;
    }

    public void setGasLgort(String gasLgort) {
        GasLgort = gasLgort;
    }

    public String getLicense_num() {
        return License_num;
    }

    public void setLicense_num(String license_num) {
        License_num = license_num;
    }

    public String getEqunr() {
        return equnr;
    }

    public void setEqunr(String equnr) {
        this.equnr = equnr;
    }

    public String getAnln1() {
        return Anln1;
    }

    public void setAnln1(String anln1) {
        Anln1 = anln1;
    }

    public String getImei() {
        return Imei;
    }

    public void setImei(String imei) {
        Imei = imei;
    }

    public String getKtschTxt() {
        return KtschTxt;
    }

    public void setKtschTxt(String ktschTxt) {
        KtschTxt = ktschTxt;
    }

    public String getWerks() {
        return werks;
    }

    public void setWerks(String werks) {
        this.werks = werks;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getOdoDiff() {
        return odoDiff;
    }

    public void setOdoDiff(double odoDiff) {
        this.odoDiff = odoDiff;
    }

    public double getMotoHour() {
        return motoHour;
    }

    public void setMotoHour(double motoHour) {
        this.motoHour = motoHour;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEqktx() {
        return Eqktx;
    }

    public void setEqktx(String eqktx) {
        Eqktx = eqktx;
    }

    public String getTooName() {
        return TooName;
    }

    public void setTooName(String tooName) {
        TooName = tooName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMaktx() {
        return Maktx;
    }

    public void setMaktx(String maktx) {
        Maktx = maktx;
    }

    public String getPttype_kz() {
        return Pttype_kz;
    }

    public void setPttype_kz(String pttype_kz) {
        Pttype_kz = pttype_kz;
    }

    public String getPttype_ru() {
        return Pttype_ru;
    }

    public void setPttype_ru(String pttype_ru) {
        Pttype_ru = pttype_ru;
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public double getSpent1() {
        return spent1;
    }

    public void setSpent1(double spent1) {
        this.spent1 = spent1;
    }

    public double getSpent2() {
        return spent2;
    }

    public void setSpent2(double spent2) {
        this.spent2 = spent2;
    }

    public double getSpent4() {
        return spent4;
    }

    public void setSpent4(double spent4) {
        this.spent4 = spent4;
    }

    public VGasSpent getPrevItem() {
        // No need
        if (this.Pos == 0)
            return null;

        EntityManager em = null;
        VGasSpent result = null;
        try {
            em = ODataServiceFactory.getEmf().createEntityManager();
            em.getTransaction().begin();
            result = em.find(VGasSpent.class, new GasSpentKey(Waybill_Id, PtType, Pos - 1));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null)
                em.close();
        }
        return result;
    }

    public BigDecimal getGasSpent() {
        if (this.Pos == 0)
            return GasSpent;
        VGasSpent prevItem = getPrevItem();
        if (prevItem == null)
            return GasSpent;

        BigDecimal total = GasBefore.add(GasGiven);

        BigDecimal prevGasAfterNext = prevItem.getGasAfterNext();
        if (prevGasAfterNext.compareTo(BigDecimal.valueOf(0)) > 0)
            return BigDecimal.valueOf(0);

        prevGasAfterNext = prevGasAfterNext.abs();
        if (total.compareTo(prevGasAfterNext) > 0)
            return prevGasAfterNext;

        return total.subtract(prevGasAfterNext);
    }

    public BigDecimal getGasAfter() {
        if (this.Pos == 0)
            return GasAfter;

        BigDecimal gasAfterNext = getGasAfterNext();
        if (gasAfterNext.compareTo(BigDecimal.valueOf(0)) < 0)
            return BigDecimal.valueOf(0);

        return gasAfterNext;
    }

    public BigDecimal getGasAfterNext() {
        if (this.Pos == 0)
            return GasAfterNext;
        VGasSpent prevItem = getPrevItem();
        if (prevItem == null)
            return GasAfterNext;

        BigDecimal total = GasBefore.add(GasGiven);
        BigDecimal prevGasAfterNext = prevItem.getGasAfterNext();
        if (prevGasAfterNext.compareTo(BigDecimal.valueOf(0)) > 0)
            return total;

        return prevGasAfterNext.add(total);
    }

    public void setGasAfter(BigDecimal gasAfter) {
        GasAfter = gasAfter;
    }

    public void setGasSpent(BigDecimal gasSpent) {
        GasSpent = gasSpent;
    }

    public void setGasAfterNext(BigDecimal gasAfterNext) {
        GasAfterNext = gasAfterNext;
    }
}
