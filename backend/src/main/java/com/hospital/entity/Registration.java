package com.hospital.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 挂号记录实体类
 */
public class Registration {
    private Long id;
    private Long userId;
    private String patientName;     // 冗余字段
    private Long scheduleId;
    private String doctorName;      // 冗余字段
    private String departmentName;  // 冗余字段
    private LocalDate scheduleDate;
    private Integer timeSlot;
    private Integer status;         // 0-待就诊, 1-已完成, 2-已取消
    private LocalDateTime createTime;

    public Registration() {}

    public Registration(Long userId, Long scheduleId) {
        this.userId = userId;
        this.scheduleId = scheduleId;
        this.status = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public Integer getTimeSlot() { return timeSlot; }
    public void setTimeSlot(Integer timeSlot) { this.timeSlot = timeSlot; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getStatusText() {
        return switch (status) {
            case 0 -> "待就诊";
            case 1 -> "已完成";
            case 2 -> "已取消";
            default -> "未知";
        };
    }

    public String getTimeSlotText() {
        return timeSlot == 0 ? "上午" : "下午";
    }
}
