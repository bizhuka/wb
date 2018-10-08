package com.modekz.rfc;

import org.hibersap.annotations.*;

import java.util.List;

@Bapi("Z_WB_SET_STATUS")
public class WBSetStatus {

    @Import
    @Parameter("IV_STATUS")
    private final String status;

    @Import
    @Parameter("IV_INACT")
    private final String inact;

    @Table
    @Parameter("IT_OBJNR")
    private final List<Objnr> objnrList;

    public WBSetStatus(String status, String inact, List<Objnr> objnrList) {
        this.status = status;
        this.inact = inact;
        this.objnrList = objnrList;
    }


    @BapiStructure
    public static class Objnr {
        @Parameter("OBJNR")
        String objnr;

        public Objnr() {

        }

        public Objnr(String objnr) {
            this.objnr = objnr;
        }
    }
}
