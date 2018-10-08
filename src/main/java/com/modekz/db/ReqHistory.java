package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@PrimaryKey(columns = {@Column(name = "WAYBILL_ID"), @Column(name = "OBJNR", length = 22)})
public class ReqHistory {
    @Column
    public long Waybill_Id;

    @Column
    public String Objnr;


    public ReqHistory() {

    }

    public ReqHistory(long waybill_id, String objnr) {
        this.Waybill_Id = waybill_id;
        this.Objnr = objnr;
    }

    public long getWaybill_Id() {
        return Waybill_Id;
    }

    public void setWaybill_Id(long waybill_Id) {
        Waybill_Id = waybill_Id;
    }

    public String getObjnr() {
        return Objnr;
    }

    public void setObjnr(String objnr) {
        Objnr = objnr;
    }
}
