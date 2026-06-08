package com.hospital.common;

/**
 * 时段枚举
 */
public enum TimeSlot {
    MORNING(0, "上午"),
    AFTERNOON(1, "下午");

    private final int code;
    private final String desc;

    TimeSlot(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static TimeSlot fromCode(int code) {
        for (TimeSlot slot : values()) {
            if (slot.code == code) return slot;
        }
        return MORNING;
    }

    @Override
    public String toString() { return desc; }
}
