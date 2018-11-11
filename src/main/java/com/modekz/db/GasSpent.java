package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@PrimaryKey(columns = {@Column(name = "\"waybill_id\""), @Column(name = "\"pos\"")})
@Table(name = "\"wb.db::pack.gasspent\"")
public class GasSpent {
    @Id
    @Column(name = "\"waybill_id\"")
    public long Waybill_Id;

    @Id
    @Column(name = "\"pos\"")
    // Relative position is crucial
    public int Pos;

    @Column(name="\"gasmatnr\"",length = 18, nullable = false)
    public String GasMatnr;

    @Column(name = "\"gasbefore\"")
    public double GasBefore;

    @Column(name = "\"gasgive\"")
    public double GasGive;

    @Column(name = "\"gasgiven\"")
    public double GasGiven;

    @Column(name="\"gaslgort\"",length = 4)
    public String GasLgort;

    public long getWaybill_Id() {
        return Waybill_Id;
    }

    public void setWaybill_Id(long waybill_Id) {
        Waybill_Id = waybill_Id;
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

    public double getGasBefore() {
        return GasBefore;
    }

    public void setGasBefore(double GasBefore) {
        this.GasBefore = GasBefore;
    }

    public double getGasGive() {
        return GasGive;
    }

    public void setGasGive(double GasGive) {
        this.GasGive = GasGive;
    }

    public double getGasGiven() {
        return GasGiven;
    }

    public void setGasGiven(double GasGiven) {
        this.GasGiven = GasGiven;
    }

    public String getGasLgort() {
        return GasLgort;
    }

    public void setGasLgort(String gasLgort) {
        GasLgort = gasLgort;
    }
}
