package com.modekz.rfc;

import com.modekz.db.GasSpent;
import org.hibersap.annotations.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Bapi("Z_WB_PRINT_DOC")
public class WBPrintDoc {
    @Import
    @Parameter("IV_OBJID")
    public String objid;

    @Import
    @Parameter("IV_WAYBILL_ID")
    public String waybillId;

    @Import
    @Parameter("IV_CLASS")
    public String N_class;

    @Export
    @Parameter("EV_CONTENT_TYPE")
    public String contentType;

    @Export
    @Parameter("EV_FILENAME")
    public String filename;

    @Table
    @Parameter("IT_DOC")
    public List<PrintDoc> docs;

    @Table
    @Parameter("IT_REQ")
    public List<PrintReq> reqs;

    @Table
    @Parameter("IT_GAS")
    public List<GasSpent> gasSpents;

    @Export
    @Parameter("EV_BIN_DATA")
    public byte[] data;

    public WBPrintDoc(String objid) {
        this.objid = objid;
    }

    public WBPrintDoc(long waybillId, String N_class, List<PrintDoc> docs, List<PrintReq> reqs, List<GasSpent> gasSpents) {
        this.waybillId = String.valueOf(waybillId);
        this.N_class = N_class;
        this.docs = docs;
        this.reqs = reqs;
        this.gasSpents = gasSpents;
    }

    @BapiStructure
    public static class PrintDoc {
        @Parameter("ID")
        public String id;

        @Parameter("DATUM")
        public Date datum;

        @Parameter("BUKRS_NAME")
        public String bukrsName;

        @Parameter("PLTXT")
        public String pltxt;

        @Parameter("DRIVER")
        public int driver;

        @Parameter("DRIVER_FIO")
        public String driverFio;

        @Parameter("EQKTX")
        public String eqktx;

        @Parameter("LICENSE_NUM")
        public String licenseNum;

        @Parameter("SPEED_MAX")
        public BigDecimal speedMax;

        @Parameter("FROM_DATE")
        public Date fromDate;

        @Parameter("TO_DATE")
        public Date toDate;

        @Parameter("TOO_NAME")
        public String tooName;

        @Parameter("TYPBZ")
        public String typbz;

        @Parameter("ANLN1")
        public String anln1;

        // Watermarks
        @Parameter("WM_KZ1")
        public String k1;
        @Parameter("WM_RU1")
        public String r1;

        @Parameter("WM_KZ2")
        public String k2;
        @Parameter("WM_RU2")
        public String r2;

        @Parameter("WM_KZ3")
        public String k3;
        @Parameter("WM_RU3")
        public String r3;

        @Parameter("WM_KZ4")
        public String k4;
        @Parameter("WM_RU4")
        public String r4;

        @Parameter("WM_KZ5")
        public String k5;
        @Parameter("WM_RU5")
        public String r5;
    }

    @BapiStructure
    public static class PrintReq {
        @Parameter("NUM")
        public String num;

        @Parameter("WAYBILL_ID")
        public String waybill_id;

        @Parameter("GSTRP")
        public Date gstrp;

        @Parameter("GLTRP")
        public Date gltrp;

        @Parameter("DATE_DIFF")
        public String dateDiff;

        @Parameter("DAUNO")
        public String duration;

        @Parameter("PLTXT")
        public String pltxt;

        @Parameter("STAND")
        public String stand;

        @Parameter("BEBER")
        public String beber;

        @Parameter("ILATX")
        public String ilatx;

        @Parameter("LTXA1")
        public String ltxa1;
    }

}
