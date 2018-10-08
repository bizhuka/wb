package com.modekz.rfc;

import com.modekz.db.*;
import org.hibersap.annotations.Bapi;
import org.hibersap.annotations.Import;
import org.hibersap.annotations.Parameter;
import org.hibersap.annotations.Table;

import java.util.List;

@Bapi("Z_WB_READ")
public class WBRead {
    @Import
    @Parameter("IV_METHOD")
    private final String method;

    @Import
    @Parameter("IV_WHERE")
    private String where;

    @Table
    @Parameter("EQUIPMENT")
    public List<Equipment> equipmentList;

    @Table
    @Parameter("SCHEDULE")
    public List<Schedule> scheduleList;

    @Table
    @Parameter("REQ_HEADER")
    public List<ReqHeader> reqHeaderList;

    @Table
    @Parameter("DRIVER")
    public List<Driver> driverList;

    @Table
    @Parameter("WERK")
    public List<Werk> werkList;

    @Table
    @Parameter("GAS_TYPE")
    public List<GasType> gasTypeList;


    public WBRead(String method, String where) {
        this.method = method;
        this.where = where;
    }
}
