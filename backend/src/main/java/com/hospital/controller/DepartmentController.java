package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Department;
import com.hospital.exception.BusinessException;
import com.hospital.service.DepartmentService;

import java.util.List;

/**
 * 科室控制器
 */
public class DepartmentController {
    private final DepartmentService departmentService = new DepartmentService();

    public Result<List<Department>> getAllDepartments() {
        try {
            List<Department> list = departmentService.getAllDepartments();
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取科室列表失败");
        }
    }

    public Result<Void> addDepartment(Department dept) {
        try {
            departmentService.addDepartment(dept);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("添加科室失败");
        }
    }

    public Result<Void> updateDepartment(Department dept) {
        try {
            departmentService.updateDepartment(dept);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("更新科室失败");
        }
    }

    public Result<Void> deleteDepartment(Long id) {
        try {
            departmentService.deleteDepartment(id);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("删除科室失败");
        }
    }
}
