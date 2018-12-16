package com.modekz.db;

import com.modekz.ODataServiceFactory;
import com.modekz.db.flag.Status;
import com.modekz.json.UserInfo;

import javax.persistence.*;
import javax.servlet.ServletException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

//@Cacheable(false) global option

@Entity
@Table(name = "\"wb.db::pack.waybill\"")
public class Waybill {
    // For speed
    private static Field[] ownFields = Waybill.class.getDeclaredFields();
    @GeneratedValue
    @Id
    @Column(name="\"id\"")
    public long id;
    
    @Column(name="\"description\"",columnDefinition = "VARCHAR(150)")
    public String description;
    
    @Column(name="\"equnr\"",length = 18)
    public String equnr;
    
    @Column(name="\"driver\"",length = 8)
    public String driver;
    
    @Column(name="\"bukrs\"",length = 4, nullable = false, updatable = false)
    public String bukrs;
    
    @Column(name="\"fromdate\"",columnDefinition = "DATE")
    public Date fromDate;
    
    @Column(name="\"todate\"",columnDefinition = "DATE")
    public Date toDate;
    
    @Column(name="\"createdate\"",columnDefinition = "TIMESTAMP")
    public Date createDate;
    
    @Column(name="\"confirmdate\"",columnDefinition = "TIMESTAMP")
    public Date confirmDate;
    
    @Column(name="\"garagedepdate\"",columnDefinition = "TIMESTAMP")
    public Date garageDepDate;
    
    @Column(name="\"garagearrdate\"",columnDefinition = "TIMESTAMP")
    public Date garageArrDate;
    
    @Column(name="\"closedate\"",columnDefinition = "TIMESTAMP")
    public Date closeDate;
    
    @Column(name="\"werks\"",length = 4, nullable = false, updatable = false)
    public String werks;

    @Column(name="\"status\"")
    public int status = Status.CREATED;

    @Column(name="\"ododiff\"")
    public double odoDiff;

    @Column(name="\"motohour\"")
    public double motoHour;

    @Column(name="\"spent1\"")
    public double spent1;

    @Column(name="\"spent2\"")
    public double spent2;

    @Column(name="\"spent4\"")
    public double spent4;

    @Column(name="\"delayreason\"")
    public int delayReason = Status.DR_NO_DELAY;

    // Who last changed
    @Column(name="\"changeuser\"",columnDefinition = "VARCHAR(40)")
    public String changeUser;
    @Column(name="\"changedate\"",columnDefinition = "TIMESTAMP")
    public Date changeDate;

    @Column(name="\"docum\"",length = 20)
    public String docum;

    @Column(name="\"aufnr\"",length = 12)
    public String aufnr;

    @Column(name="\"withnoreqs\"")
    public boolean withNoReqs = false;

    @PrePersist
    @PreUpdate
    public void persist() {
        // Current user info
        try {
            changeDate = new Date();
            UserInfo curUser = UserInfo.getCurrentUserInfo(null);
            if (curUser != null)
                changeUser = curUser.email;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Set current time if time == 1
            for (Field field : ownFields) {
                if (field.getType() != Date.class)
                    continue;

                // First char to upper case
                String name = field.getName();
                name = name.substring(0, 1).toUpperCase() + name.substring(1);

                Method methodGet = Waybill.class.getMethod("get" + name);
                Method methodSet = Waybill.class.getMethod("set" + name, Date.class);

                Date date = (Date) methodGet.invoke(this);
                if (date == null)
                    continue;

                // Set current time
                if (date.getTime() != 1)
                    continue;

                // Set current date
                methodSet.invoke(this, new Date());
            }

            // New items or delete
            updateSchedule();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSchedule() throws ServletException {
        if (status != Status.CREATED && status != Status.IN_PROCESS && //AGREED &&
                status != Status.REJECTED &&
                status != Status.CLOSED)
            return;

        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();
        em.getTransaction().begin();
        try {
            switch (this.status) {
                // Create and confirm
                case Status.CREATED:
                case Status.IN_PROCESS: //AGREED:
                    // Modify schedule
                    Date from = this.getFromDate();
                    while (from.before(this.toDate) || from.equals(this.toDate)) {
                        Schedule schedule = new Schedule(this.werks, this.equnr, from, this.id);
                        // Insert or update
                        em.merge(schedule);

                        // Next date
                        from = new Date(from.getTime() + (1000 * 3600 * 24));
                    }
                    break;

                // Cancel WB
                case Status.REJECTED:
                    Connection connection = ODataServiceFactory.getConnection(em);
                    PreparedStatement prepDelete = connection.prepareStatement("DELETE FROM \"wb.db::pack.schedule\" WHERE \"waybill_id\" = ?");
                    prepDelete.setLong(1, this.id);
                    prepDelete.executeUpdate();
                    break;

                // Close WB
                case Status.CLOSED:
                    connection = ODataServiceFactory.getConnection(em);
                    prepDelete = connection.prepareStatement("DELETE FROM \"wb.db::pack.reqhistory\" WHERE \"waybill_id\" = ?");
                    prepDelete.setLong(1, this.id);
                    prepDelete.executeUpdate();
                    break;
            }
            em.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEqunr() {
        return equnr;
    }

    public void setEqunr(String equnr) {
        this.equnr = equnr;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getBukrs() {
        return bukrs;
    }

    public void setBukrs(String bukrs) {
        this.bukrs = bukrs;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(Date confirmDate) {
        this.confirmDate = confirmDate;
    }

    public Date getGarageDepDate() {
        return garageDepDate;
    }

    public void setGarageDepDate(Date garageDepDate) {
        this.garageDepDate = garageDepDate;
    }

    public Date getGarageArrDate() {
        return garageArrDate;
    }

    public void setGarageArrDate(Date garageArrDate) {
        this.garageArrDate = garageArrDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public String getWerks() {
        return werks;
    }

    public void setWerks(String werks) {
        this.werks = werks;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public double getMotoHour() {
        return motoHour;
    }

    public void setMotoHour(double motoHour) {
        this.motoHour = motoHour;
    }

    public double getOdoDiff() {
        return odoDiff;
    }

    public void setOdoDiff(double odoDiff) {
        this.odoDiff = odoDiff;
    }

    public double getSpent1() {
        return spent1;
    }

    public void setSpent1(double spent1) {
        this.spent1 = spent1;
    }

    public double getSpent2() {
        return spent2;
    }

    public void setSpent2(double spent2) {
        this.spent2 = spent2;
    }

    public double getSpent4() {
        return spent4;
    }

    public void setSpent4(double spent4) {
        this.spent4 = spent4;
    }

    public int getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(int delayReason) {
        this.delayReason = delayReason;
    }

    public String getChangeUser() {
        return changeUser;
    }

    public void setChangeUser(String changeUser) {
        this.changeUser = changeUser;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getDocum() {
        return docum;
    }

    public void setDocum(String docum) {
        this.docum = docum;
    }

    public String getAufnr() {
        return aufnr;
    }

    public void setAufnr(String aufnr) {
        this.aufnr = aufnr;
    }

    public boolean isWithNoReqs() {
        return withNoReqs;
    }

    public void setWithNoReqs(boolean withNoReqs) {
        this.withNoReqs = withNoReqs;
    }
}
