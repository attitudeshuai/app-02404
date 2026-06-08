package com.hospital.entity;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String idCard;
    private String phone;
    private Integer role;  // 0-患者, 1-管理员, 2-医生
    private LocalDateTime createTime;

    public User() {}

    public User(String username, String password, String realName, String idCard, String phone) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.idCard = idCard;
        this.phone = phone;
        this.role = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public boolean isAdmin() { return role != null && role == 1; }
    public boolean isPatient() { return role == null || role == 0; }
    public boolean isDoctor() { return role != null && role == 2; }
}
