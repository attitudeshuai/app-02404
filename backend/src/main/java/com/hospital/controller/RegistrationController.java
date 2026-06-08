package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Registration;
import com.hospital.exception.BusinessException;
import com.hospital.service.RegistrationService;

import java.time.LocalDate;
import java.util.List;

/**
 * 挂号控制器
 */
public class RegistrationController {
    private final RegistrationService registrationService = new RegistrationService();

    public Result<List<Registration>> getMyRegistrations(Long userId) {
        try {
            List<Registration> list = registrationService.getMyRegistrations(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取挂号记录失败");
        }
    }

    public Result<List<Registration>> getAllRegistrations() {
        try {
            List<Registration> list = registrationService.getAllRegistrations();
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取挂号记录失败");
        }
    }

    public Result<List<Registration>> searchRegistrations(LocalDate date, Long deptId, Integer status) {
        try {
            List<Registration> list = registrationService.searchRegistrations(date, deptId, status);
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("搜索挂号记录失败");
        }
    }

    public Result<Void> createRegistration(Long userId, Long scheduleId) {
        try {
            registrationService.createRegistration(userId, scheduleId);
            return Result.success("挂号成功", null);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("挂号失败，请稍后重试");
        }
    }

    public Result<Void> completeRegistration(Long regId) {
        try {
            registrationService.completeRegistration(regId);
            return Result.success("已标记为已完成", null);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("操作失败，请稍后重试");
        }
    }

    public Result<Void> cancelRegistration(Long regId) {
        try {
            registrationService.cancelRegistration(regId);
            return Result.success("取消成功", null);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("取消挂号失败，请稍后重试");
        }
    }

    /** 管理员修改挂号状态：0-待就诊，1-已完成，2-已取消。doctorId 非空时为医生操作，仅能改本人排班下的挂号 */
    public Result<Void> updateStatus(Long regId, Integer status, Long doctorId) {
        try {
            registrationService.updateStatusByAdmin(regId, status, doctorId);
            return Result.success("状态已更新", null);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("修改失败，请稍后重试");
        }
    }

    /** 医生端：查询本人排班下的挂号记录 */
    public Result<List<Registration>> getRegistrationsByDoctorId(Long doctorId) {
        try {
            List<Registration> list = registrationService.getRegistrationsByDoctorId(doctorId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取挂号记录失败");
        }
    }
}
