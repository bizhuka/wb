package com.modekz.db;

import com.modekz.ODataServiceFactory;
import org.eclipse.persistence.annotations.PrimaryKey;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import java.util.List;

@Entity
@PrimaryKey(columns = {@Column(name = "GrpRole", columnDefinition = "VARCHAR(40)"),
        @Column(name = "IndRole", columnDefinition = "VARCHAR(40)")})
public class GroupRole {
    @Column
    public String GrpRole;

    @Column
    public String IndRole;

    public static List<GroupRole> getAllGroupRoles() throws ServletException {
        EntityManager em = ODataServiceFactory.getEmf().createEntityManager();
        TypedQuery<GroupRole> query = em.createQuery("SELECT g FROM GroupRole g", GroupRole.class);

        // Get and close
        List<GroupRole> result = query.getResultList();
        em.close();

        return result;
    }

    public String getGrpRole() {
        return GrpRole;
    }

    public void setGrpRole(String grpRole) {
        GrpRole = grpRole;
    }

    public String getIndRole() {
        return IndRole;
    }

    public void setIndRole(String indRole) {
        IndRole = indRole;
    }
}
