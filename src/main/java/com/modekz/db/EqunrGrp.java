package com.modekz.db;

import org.hibersap.annotations.Parameter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "\"wb.dbt::pack.equnrgrp\"")
public class EqunrGrp {
    // As replace class number by group
    private static Map<String, String> grpMap;

    @Id
    @Column(name = "\"ktsch\"", length = 7)
    @Parameter("KTSCH")
    public String Ktsch;

    @Column(name = "\"grp\"", length = 40)
    @Parameter("GRP")
    public String Grp;

    private static Map<String, String> getGroupMaps(EntityManager em) {
        if (grpMap != null)
            return grpMap;

        grpMap = new HashMap<>();

        List<EqunrGrp> list = em.createQuery("SELECT p FROM EqunrGrp p", EqunrGrp.class).getResultList();
        for (EqunrGrp equnrGrp : list)
            grpMap.put(equnrGrp.Ktsch, equnrGrp.Grp);

        return grpMap;
    }

    public static String getClassNum(EntityManager em, String clNum) {
        Map<String, String> map = getGroupMaps(em);

        // return group
        if (map.containsKey(clNum))
            return map.get(clNum);

        // Do not change class
        return clNum;
    }

    public String getKtsch() {
        return Ktsch;
    }

    public void setKtsch(String ktsch) {
        Ktsch = ktsch;
    }

    public String getGrp() {
        return Grp;
    }

    public void setGrp(String grp) {
        Grp = grp;
    }
}
