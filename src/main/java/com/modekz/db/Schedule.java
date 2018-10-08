package com.modekz.db;

import org.hibersap.annotations.Parameter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Schedule {
    @Id
    @Column(length = 4)
    @Parameter("WERKS")
    public String Werks;

    @Id
    @Column(columnDefinition = "DATE")
    @Parameter("DATUM")
    public Date Datum;

    @Id
    @Column(length = 18)
    @Parameter("EQUNR")
    public String Equnr;

    @Column(columnDefinition = "NVARCHAR(3)")
    @Parameter("ILART")
    public String Ilart;

    @Basic
    public long Waybill_Id = -1;

    public Schedule() {

    }

    public Schedule(String werks, String equnr, Date datum, long waybill_id) {
        this.Werks = werks;
        this.Equnr = equnr;
        this.Datum = datum;
        this.Waybill_Id = waybill_id;
    }

    public String getWerks() {
        return Werks;
    }

    public void setWerks(String werks) {
        Werks = werks;
    }

    public Date getDatum() {
        return Datum;
    }

    public void setDatum(Date datum) {
        Datum = datum;
    }

    public String getEqunr() {
        return Equnr;
    }

    public void setEqunr(String equnr) {
        Equnr = equnr;
    }

    public String getIlart() {
        return Ilart;
    }

    public void setIlart(String ilart) {
        Ilart = ilart;
    }

    public long getWaybill_Id() {
        return Waybill_Id;
    }

    public void setWaybill_Id(long waybill_Id) {
        Waybill_Id = waybill_Id;
    }
}
