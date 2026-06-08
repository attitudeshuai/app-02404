package com.hospital.controller;

import com.hospital.common.Result;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
import com.hospital.service.ScheduleService;

import java.util.List;

/**
 * 排班控制器
 */
public class ScheduleController {
    private final ScheduleService scheduleService = new ScheduleService();

    public Result<List<Schedule>> getAllSchedules() {
        try {
            List<Schedule> list = scheduleService.getAllSchedules();
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取排班列表失败");
        }
    }

    public Result<List<Schedule>> getAvailableSchedules(Long doctorId) {
        try {
            List<Schedule> list = scheduleService.getAvailableSchedules(doctorId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.fail("获取可用排班失败");
        }
    }

    public Result<Void> addSchedule(Schedule schedule) {
        try {
            scheduleService.addSchedule(schedule);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("添加排班失败");
        }
    }

    public Result<Void> updateSchedule(Schedule schedule) {
        try {
            scheduleService.updateSchedule(schedule);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("更新排班失败");
        }
    }

    public Result<Void> deleteSchedule(Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("删除排班失败");
        }
    }
}
