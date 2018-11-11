package com.modekz.db;

import org.hibersap.annotations.Parameter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "\"wb.db::pack.schedule\"")
public class Schedule {
    @Id
    @Column(name="\"werks\"",length = 4)
    @Parameter("WERKS")
    public String Werks;

    @Id
    @Column(name="\"datum\"",columnDefinition = "DATE")
    @Parameter("DATUM")
    public Date Datum;

    @Id
    @Column(name="\"equnr\"",length = 18)
    @Parameter("EQUNR")
    public String Equnr;

    @Column(name="\"ilart\"",columnDefinition = "VARCHAR(3)")
    @Parameter("ILART")
    public String Ilart;

    @Column(name="\"waybill_id\"")
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
