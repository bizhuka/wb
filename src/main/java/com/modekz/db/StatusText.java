package com.modekz.db;

import org.eclipse.persistence.annotations.PrimaryKey;
import org.hibersap.annotations.Parameter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "\"wb.db::pack.statustext\"")
@PrimaryKey(columns = {@Column(name = "\"stype\"", length = 2), @Column(name = "\"id\"")})
public class StatusText {
    @Id
    @Column(name = "\"stype\"")
    @Parameter("STATUS_TYPE")
    public String Stype;

    @Id
    @Column(name = "\"id\"")
    @Parameter("STATUS_ID")
    public int Id;

    @Column(name = "\"kz\"", columnDefinition = "VARCHAR(40)")
    @Parameter("KZ")
    public String Kz;

    @Column(name = "\"ru\"", columnDefinition = "VARCHAR(40)")
    @Parameter("RU")
    public String Ru;

    @Column(name = "\"messagetype\"", columnDefinition = "VARCHAR(40)")
    @Parameter("MESSAGE_TYPE")
    public String MessageType;

    @Column(name = "\"intile\"", columnDefinition = "VARCHAR(1)")
    @Parameter("IN_TILE")
    public String InTile;

    public static List<StatusText> getStatusList(EntityManager em){
        return  em.createQuery("SELECT s FROM StatusText s", StatusText.class).getResultList();
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        MessageType = messageType;
    }

    public String getStype() {
        return Stype;
    }

    public void setStype(String stype) {
        Stype = stype;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getKz() {
        return Kz;
    }

    public void setKz(String kz) {
        Kz = kz;
    }

    public String getRu() {
        return Ru;
    }

    public void setRu(String ru) {
        Ru = ru;
    }

    public String getInTile() {
        return InTile;
    }

    public void setInTile(String inTile) {
        InTile = inTile;
    }
}
