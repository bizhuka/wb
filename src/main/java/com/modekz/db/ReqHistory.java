package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@PrimaryKey(columns = {@Column(name = "\"waybill_id\""), @Column(name = "\"objnr\"", length = 22)})
@Table(name = "\"wb.db::pack.reqhistory\"")
public class ReqHistory {
    @Id
    @Column(name = "\"waybill_id\"")
    public long Waybill_Id;

    @Id
    @Column(name = "\"objnr\"", length = 22)
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
