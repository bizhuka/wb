package com.modekz.db;

import com.modekz.ODataServiceFactory;
import com.modekz.db.flag.DelayReason;
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
public class Waybill {
    // For speed
    private static Field[] ownFields = Waybill.class.getDeclaredFields();
    @GeneratedValue
    @Id
    public long id;
    @Basic
    @Column(columnDefinition = "NVARCHAR(150)")
    public String description;
    @Basic
    @Column(length = 18)
    public String equnr;
    @Basic
    @Column(length = 8)
    public String driver;
    @Basic
    @Column(length = 4, nullable = false, updatable = false)
    public String bukrs;
    @Basic
    @Column(columnDefinition = "DATE")
    public Date fromDate;
    @Basic
    @Column(columnDefinition = "DATE")
    public Date toDate;
    @Basic
    @Column(columnDefinition = "TIMESTAMP")
    public Date createDate;
    @Basic
    @Column(columnDefinition = "TIMESTAMP")
    public Date confirmDate;
    @Basic
    @Column(columnDefinition = "TIMESTAMP")
    public Date garageDepDate;
    @Basic
    @Column(columnDefinition = "TIMESTAMP")
    public Date garageArrDate;
    @Basic
    @Column(columnDefinition = "TIMESTAMP")
    public Date closeDate;
    @Basic
    @Column(length = 4, nullable = false, updatable = false)
    public String werks;
    @Basic
    public int status = Status.CREATED;
    @Basic
    public double odoDiff;
    @Basic
    public double motoHour;
    @Basic
    public double gasSpent;
    @Basic
    public double gasTopSpent;
    @Basic
    public int delayReason = DelayReason.NO_DELAY;

    // Who last changed
    @Column(columnDefinition = "NVARCHAR(40)")
    public String changeUser;
    @Column(columnDefinition = "TIMESTAMP")
    public Date changeDate;

    @PrePersist
    @PreUpdate
    public void persist() {
        // Current user info
        try {
            changeDate = new Date();
            changeUser = UserInfo.getCurrentUserInfo(null).email;
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
                    PreparedStatement prepDelete = connection.prepareStatement("DELETE FROM SCHEDULE WHERE WAYBILL_ID = ?");
                    prepDelete.setLong(1, this.id);
                    prepDelete.executeUpdate();
                    break;

                // Close WB
                case Status.CLOSED:
                    connection = ODataServiceFactory.getConnection(em);
                    prepDelete = connection.prepareStatement("DELETE FROM REQHISTORY WHERE WAYBILL_ID = ?");
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

    public double getGasSpent() {
        return gasSpent;
    }

    public void setGasSpent(double gasSpent) {
        this.gasSpent = gasSpent;
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

    public double getGasTopSpent() {
        return gasTopSpent;
    }

    public void setGasTopSpent(double gasTopSpent) {
        this.gasTopSpent = gasTopSpent;
    }
}
