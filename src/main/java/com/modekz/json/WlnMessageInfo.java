package com.modekz.json;

import java.util.Date;
import java.util.List;

public class WlnMessageInfo {
    public final List<WlnMessage> messages;
    public final int count;
    public final Date from;
    public final Date to;
    public final double mileage;
    public final double diff;

    public WlnMessageInfo(List<WlnMessage> messages) {
        this.messages = messages;
        count = messages.size();

        if (count >= 1) {
            WlnMessage beg = messages.get(0);
            WlnMessage end = messages.get(count - 1);

            from = beg.date;
            to = end.date;

            mileage = round(end.mileage, 3);
            diff = round(end.mileage - beg.mileage, 3);
        } else {
            from = null;
            to = null;
            mileage = 0;
            diff = 0;
        }
    }

    static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static class WlnMessage {
        public Date date;
        public double lon;
        public double lat;
        public double mileage;
        public double fuel;

        public WlnMessage(Date date, double lon, double lat, double mileage, double fuel) {
            this.date = date;
            this.lon = lon;
            this.lat = lat;
            this.mileage = mileage;
            this.fuel = fuel;
        }
    }
}
