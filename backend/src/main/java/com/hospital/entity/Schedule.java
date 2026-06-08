package com.hospital.entity;

import java.time.LocalDate;

/**
 * 排班实体类
 */
public class Schedule {
    private Long id;
    private Long doctorId;
    private String doctorName;      // 冗余字段
    private String departmentName;  // 冗余字段
    private LocalDate scheduleDate;
    private Integer timeSlot;       // 0-上午, 1-下午
    private Integer maxCount;
    private Integer currentCount;

    public Schedule() {}

    public Schedule(Long doctorId, LocalDate scheduleDate, Integer timeSlot, Integer maxCount) {
        this.doctorId = doctorId;
        this.scheduleDate = scheduleDate;
        this.timeSlot = timeSlot;
        this.maxCount = maxCount;
        this.currentCount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public Integer getTimeSlot() { return timeSlot; }
    public void setTimeSlot(Integer timeSlot) { this.timeSlot = timeSlot; }

    public Integer getMaxCount() { return maxCount; }
    public void setMaxCount(Integer maxCount) { this.maxCount = maxCount; }

    public Integer getCurrentCount() { return currentCount; }
    public void setCurrentCount(Integer currentCount) { this.currentCount = currentCount; }

    public int getAvailableCount() {
        return maxCount - currentCount;
    }

    public String getTimeSlotText() {
        return timeSlot == 0 ? "上午" : "下午";
    }
}
