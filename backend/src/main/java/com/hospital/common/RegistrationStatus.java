package com.hospital.common;

/**
 * 挂号状态枚举
 */
public enum RegistrationStatus {
    PENDING(0, "待就诊"),
    COMPLETED(1, "已完成"),
    CANCELLED(2, "已取消");

    private final int code;
    private final String desc;

    RegistrationStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static RegistrationStatus fromCode(int code) {
        for (RegistrationStatus status : values()) {
            if (status.code == code) return status;
        }
        return PENDING;
    }

    @Override
    public String toString() { return desc; }
}
