package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Doctor;
import com.hospital.exception.BusinessException;
import com.hospital.service.DoctorService;

import java.util.List;

/**
 * 医生控制器
 */
public class DoctorController {
    private final DoctorService doctorService = new DoctorService();

    public Result<List<Doctor>> getAllDoctors() {
        try {
            List<Doctor> list = doctorService.getAllDoctors();
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取医生列表失败");
        }
    }

    public Result<List<Doctor>> getDoctorsByDepartment(Long deptId) {
        try {
            List<Doctor> list = doctorService.getDoctorsByDepartment(deptId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取医生列表失败");
        }
    }

    public Result<Void> addDoctor(Doctor doctor) {
        try {
            doctorService.addDoctor(doctor);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("添加医生失败");
        }
    }

    public Result<Void> updateDoctor(Doctor doctor) {
        try {
            doctorService.updateDoctor(doctor);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("更新医生失败");
        }
    }

    public Result<Void> deleteDoctor(Long id) {
        try {
            doctorService.deleteDoctor(id);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("删除医生失败");
        }
    }

    /** 根据登录用户ID获取当前医生（医生角色登录后使用） */
    public Result<Doctor> getDoctorByUserId(Long userId) {
        try {
            Doctor doctor = doctorService.getDoctorByUserId(userId);
            return doctor != null ? Result.success(doctor) : Result.fail("未绑定医生信息");
        } catch (Exception e) {
            return Result.fail("获取医生信息失败");
        }
    }
}
