package com.hospital.common;

/**
 * 用户角色枚举
 */
public enum UserRole {
    PATIENT(0, "患者"),
    ADMIN(1, "管理员"),
    DOCTOR(2, "医生");

    private final int code;
    private final String desc;

    UserRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static UserRole fromCode(int code) {
        for (UserRole role : values()) {
            if (role.code == code) return role;
        }
        return PATIENT;
    }
}
