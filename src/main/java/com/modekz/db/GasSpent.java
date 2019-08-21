package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibersap.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@PrimaryKey(columns = {@Column(name = "\"waybill_id\""), @Column(name = "\"pttype\""), @Column(name = "\"pos\"")})
@Table(name = "\"wb.db::pack.gasspent\"")
public class GasSpent {
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
}
