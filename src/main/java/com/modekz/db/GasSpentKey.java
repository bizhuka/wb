package com.modekz.db;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class GasSpentKey implements Serializable {
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

    public GasSpentKey(long waybill_Id, int ptType, int pos) {
        Waybill_Id = waybill_Id;
        PtType = ptType;
        Pos = pos;
    }

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
}
