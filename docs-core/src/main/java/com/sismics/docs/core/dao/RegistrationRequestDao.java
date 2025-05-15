package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.RegistrationRequest;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * DAO for RegistrationRequest.
 */
public class RegistrationRequestDao {
    /**
     * Creates a new registration request.
     *
     * @param request The registration request to create
     */
    public void create(RegistrationRequest request) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        request.setId(UUID.randomUUID().toString());
        request.setCreateDate(new Date());
        em.persist(request);
    }

    /**
     * Gets all pending registration requests.
     * 
     * @return The list of pending registration requests
     */
    public List<RegistrationRequest> listPendingRequests() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        final TypedQuery<RegistrationRequest> q = em.createQuery("select r from RegistrationRequest r where r.approved = false order by r.createDate", RegistrationRequest.class);
        return q.getResultList();
    }

    /**
     * Finds a registration request by username.
     *
     * @param username The username to search for
     * @return The registration request, or null if not found
     */
    public RegistrationRequest getByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        final TypedQuery<RegistrationRequest> q = em.createQuery("select r from RegistrationRequest r where r.username = :username and r.approved = false", RegistrationRequest.class);
        q.setParameter("username", username);
        return q.getSingleResult();
    }

    /**
     * Deletes a registration request.
     *
     * @param request The registration request to delete
     */
    public void delete(RegistrationRequest request) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.remove(em.contains(request) ? request : em.merge(request));
    }
}
