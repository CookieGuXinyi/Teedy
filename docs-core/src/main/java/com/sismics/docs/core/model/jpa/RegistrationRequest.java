package com.sismics.docs.core.model.jpa;

import com.sismics.util.context.ThreadLocalContext;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.RegistrationRequestDao;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * RegistrationRequest entity.
 * Represents a registration request made by a guest user.
 */
@Entity
@Table(name = "T_REGISTRATION_REQUEST")
public class RegistrationRequest {
    @Id
    @Column(name = "REQ_ID_C")
    private String id;

    @Column(name = "REQ_USERNAME_C", nullable = false, unique = true)
    private String username;

    @Column(name = "REQ_PASSWORD_C", nullable = false)
    private String password;

    @Column(name = "REQ_EMAIL_C", nullable = false, unique = true)
    private String email;

    @Column(name = "REQ_CREATEDATE_D", nullable = false)
    private Date createDate;

    @Column(name = "REQ_APPROVED_B", nullable = false)
    private boolean approved = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public RegistrationRequest(){
    }

    public RegistrationRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.createDate = new Date();
    }

    /**
     * Lists all pending registration requests.
     *
     * @return List of pending registration requests
     */
    public List<RegistrationRequest> listPendingRequests() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.createQuery("select r from RegistrationRequest r", RegistrationRequest.class)
                 .getResultList();
    }

    /**
     * Approves or rejects a registration request.
     *
     * @param username The username of the request
     * @param approve True to approve, false to reject
     * @param principal The principal for user creation
     */
    public void approveOrRejectRequest(String username, boolean approve, String principal) {
        RegistrationRequestDao registrationRequestDao = new RegistrationRequestDao();
        RegistrationRequest request = registrationRequestDao.getByUsername(username);
        if (request == null) {
            return;
        }

        if (approve) {
            // Create the user
            User user = new User();
            user.setRoleId(Constants.DEFAULT_USER_ROLE);
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setEmail(request.getEmail());
            user.setStorageQuota(104857600L); // 设置默认配额 100MB（单位视你数据库定义）
            user.setOnboarding(true);

            UserDao userDao = new UserDao();
            try {
                userDao.create(user, principal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Delete the request
        registrationRequestDao.delete(request);
    }
}