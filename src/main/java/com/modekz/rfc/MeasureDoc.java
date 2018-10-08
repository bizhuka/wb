package com.modekz.rfc;

import org.hibersap.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Bapi("Z_WB_MEASURE_DOC")
public class MeasureDoc {
    @Import
    @Parameter("IV_DIS_MODE")
    public String disMode = "N"; // As background task

    @Import
    @Parameter("IV_MEASUREMENT_POINT")
    public String point;

    @Import
    @Parameter("IV_EQUNR")
    public String equnr;

    @Import
    @Parameter("IV_WERKS")
    public String werks;

    @Import
    @Parameter("IV_GSTRP")
    public String gstrp;

    @Import
    @Parameter("IV_GLTRP")
    public String gltrp;

    @Import
    @Parameter("IV_SHORT_TEXT")
    public String text;

    @Import
    @Parameter("IV_ODO_DIFF")
    public String odoDiff;

    @Import
    @Parameter("IV_MOTO_HOUR")
    public String motoHour;

    @Table
    @Parameter("IT_GAS_SPENT_POS")
    public List<SpentPos> spents;

    @Table
    @Parameter("CT_MESSAGE")
    public List<Message> messages;

    @BapiStructure
    public static class SpentPos {
        @Parameter("MATNR")
        public String matnr;

        @Parameter("MENGE")
        public BigDecimal menge;

        @Parameter("LGORT")
        public String lgort;
    }

    @BapiStructure
    public static class Message {
        @Parameter("MESSAGE_TYPE")
        public String messageType;

        @Parameter("MESSAGE")
        public String message;
    }
}
