package com.modekz.rfc;

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
    @Parameter("IV_CLASS")
    public String N_class;

    @Export
    @Parameter("EV_CONTENT_TYPE")
    public String contentType;

    @Table
    @Parameter("IT_DOC")
    public List<PrintDoc> docs;

    @Table
    @Parameter("IT_REQ")
    public List<PrintReq> reqs;

    @Export
    @Parameter("EV_BIN_DATA")
    public byte[] data;

    public WBPrintDoc(String objid) {
        this.objid = objid;
    }

    public WBPrintDoc(String N_class, List<PrintDoc> docs, List<PrintReq> reqs) {
        this.N_class = N_class;
        this.docs = docs;
        this.reqs = reqs;
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
        public String driver;

        @Parameter("DRIVER_FIO")
        public String driverFio;

        @Parameter("EQKTX")
        public String eqktx;

        @Parameter("LICENSE_NUM")
        public String licenseNum;

        @Parameter("SPEED_MAX")
        public BigDecimal speedMax;
    }

    @BapiStructure
    public static class PrintReq {
        @Parameter("WAYBILL_ID")
        public String waybill_id;

        @Parameter("GSTRP")
        public Date gstrp;

        @Parameter("GLTRP")
        public Date gltrp;

        @Parameter("PLTXT")
        public String pltxt;

        @Parameter("STAND")
        public String stand;

        @Parameter("BEBER")
        public String beber;
    }

}
