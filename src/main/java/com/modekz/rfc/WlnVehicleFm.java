package com.modekz.rfc;

import com.modekz.db.WlnVehicle;
import org.hibersap.annotations.Bapi;
import org.hibersap.annotations.Export;
import org.hibersap.annotations.Parameter;
import org.hibersap.annotations.Table;

import java.util.List;

@Bapi("Z_WB_WIALON_VEHICLE")
public class WlnVehicleFm {
    @Table
    @Parameter("IT_WIALON_VEHICLE")
    public List<WlnVehicle> vehicleList;

    @Export
    @Parameter("EV_DBCNT")
    public int dbcnt;

    public WlnVehicleFm(List<WlnVehicle> newList) {
        this.vehicleList = newList;
    }
}
