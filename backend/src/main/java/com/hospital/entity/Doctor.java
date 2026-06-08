package com.hospital.entity;

/**
 * 医生实体类
 */
public class Doctor {
    private Long id;
    private String name;
    private String title;
    private Long departmentId;
    private Long userId;           // 关联 sys_user.id，医生登录账号
    private String departmentName;  // 冗余字段，用于显示
    /** 绑定账号时填写的登录名（不持久化） */
    private String bindUsername;
    /** 绑定账号时填写的密码（不持久化） */
    private String bindPassword;

    public Doctor() {}

    public Doctor(String name, String title, Long departmentId) {
        this.name = name;
        this.title = title;
        this.departmentId = departmentId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getBindUsername() { return bindUsername; }
    public void setBindUsername(String bindUsername) { this.bindUsername = bindUsername; }
    public String getBindPassword() { return bindPassword; }
    public void setBindPassword(String bindPassword) { this.bindPassword = bindPassword; }

    @Override
    public String toString() {
        return name + " (" + title + ")";
    }
}
