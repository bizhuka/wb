package com.modekz.db.flag;

public class Status {
    // Waybill
    public static final int NOT_CREATED = 0;
    public static final int CREATED = 10;
    //public static final int AGREED = 20;
    public static final int REJECTED = 30;
    public static final int IN_PROCESS = 40;
    public static final int ARRIVED = 50;
    public static final int CLOSED = 60;

    // WB delay reason
    public static final int DR_NO_DELAY = 1000;
    public static final int DR_NO_CAR = 2000;
    public static final int DR_REPAIR = 3000;

    // Request
    public static final int RC_NEW = 100;
    public static final int RC_SET = 200;

    // Request waybill_id field
    public static final int WB_ID_NULL = -1;
    public static final int WB_ID_REJECTED = -2;
}
