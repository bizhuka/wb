package com.modekz.db;

import org.hibersap.annotations.BapiStructure;
import org.hibersap.annotations.Parameter;

import javax.persistence.*;

@Entity
@BapiStructure
@Table(name = "\"wb.dbt::pack.wlnvehicle\"")
public class WlnVehicle {
    // Unique guid
    @Id
    @Column(name="\"gd\"",length = 32)
    @Parameter("GUID")
    public String gd;

    // Text
    @Column(name="\"nm\"",columnDefinition = "VARCHAR(50)")
    @Parameter("TEXT")
    public String nm;

    // Class always 3
    @Transient
    public int cls;

    // Tech id for request
    @Column(name="\"id\"",length = 5)
    @Parameter("ID")
    public String id;

    // IMEI or sim card number
    @Column(name="\"uid\"",length = 20)
    @Parameter("IMEI")
    public String uid;

    // Copy from pos.p
    @Column(name="\"mileage\"")
    public Double mileage;
    @Column(name="\"gps_mileage\"")
    public Double gps_mileage;
    @Column(name="\"rs485_fls02\"")
    public Double rs485_fls02;
    @Column(name="\"rs485_fls12\"")
    public Double rs485_fls12;
    @Column(name="\"rs485_fls22\"")
    public Double rs485_fls22;

    @Transient
    public Pos pos;

    public String getGd() {
        return gd;
    }

    public void setGd(String gd) {
        this.gd = gd;
    }

    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public Double getGps_mileage() {
        return gps_mileage;
    }

    public void setGps_mileage(Double gps_mileage) {
        this.gps_mileage = gps_mileage;
    }

    public Double getRs485_fls02() {
        return rs485_fls02;
    }

    public void setRs485_fls02(Double rs485_fls02) {
        this.rs485_fls02 = rs485_fls02;
    }

    public Double getRs485_fls12() {
        return rs485_fls12;
    }

    public void setRs485_fls12(Double rs485_fls12) {
        this.rs485_fls12 = rs485_fls12;
    }

    public Double getRs485_fls22() {
        return rs485_fls22;
    }

    public void setRs485_fls22(Double rs485_fls22) {
        this.rs485_fls22 = rs485_fls22;
    }

    public static class Pos {
        public P p;
    }

    public static class P {
        public Double mileage;
        public Double gps_mileage;

        public Double rs485_fls02;
        public Double rs485_fls12;
        public Double rs485_fls22;
    }
}
