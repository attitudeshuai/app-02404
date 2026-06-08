package com.hospital.service;

import com.hospital.dao.ScheduleDAO;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * 排班服务类
 */
public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    private final ScheduleDAO scheduleDAO;

    public ScheduleService() {
        this(new ScheduleDAO());
    }

    public ScheduleService(ScheduleDAO scheduleDAO) {
        this.scheduleDAO = scheduleDAO;
    }

    public List<Schedule> getAllSchedules() {
        return scheduleDAO.findAll();
    }

    public List<Schedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleDAO.findByDoctorId(doctorId);
    }

    public List<Schedule> getAvailableSchedules(Long doctorId) {
        return scheduleDAO.findAvailableByDoctorId(doctorId);
    }

    public Schedule getScheduleById(Long id) {
        return scheduleDAO.findById(id);
    }

    public void addSchedule(Schedule schedule) throws BusinessException {
        validateSchedule(schedule);

        if (scheduleDAO.exists(schedule.getDoctorId(), schedule.getScheduleDate(), schedule.getTimeSlot())) {
            throw new BusinessException("该医生在此时段已有排班");
        }

        schedule.setCurrentCount(0);
        int rows = scheduleDAO.insert(schedule);
        if (rows <= 0) {
            throw new BusinessException("添加排班失败");
        }
    }

    public void updateSchedule(Schedule schedule) throws BusinessException {
        if (schedule.getId() == null) {
            throw new BusinessException("排班ID不能为空");
        }
        validateSchedule(schedule);

        if (scheduleDAO.existsExcludeId(schedule.getDoctorId(), schedule.getScheduleDate(), 
                schedule.getTimeSlot(), schedule.getId())) {
            throw new BusinessException("该医生在此时段已有排班");
        }

        int rows = scheduleDAO.update(schedule);
        if (rows <= 0) {
            throw new BusinessException("更新排班失败");
        }
    }

    public void deleteSchedule(Long id) throws BusinessException {
        if (id == null) {
            throw new BusinessException("排班ID不能为空");
        }

        if (scheduleDAO.hasRegistration(id)) {
            throw new BusinessException("该排班存在挂号记录，无法删除");
        }

        int rows = scheduleDAO.delete(id);
        if (rows <= 0) {
            throw new BusinessException("删除排班失败");
        }
    }

    private void validateSchedule(Schedule schedule) throws BusinessException {
        if (schedule.getDoctorId() == null) {
            throw new BusinessException("请选择医生");
        }
        if (schedule.getScheduleDate() == null) {
            throw new BusinessException("请选择排班日期");
        }
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new BusinessException("排班日期不能早于今天");
        }
        if (schedule.getTimeSlot() == null) {
            throw new BusinessException("请选择时段");
        }
        if (schedule.getTimeSlot() != 0 && schedule.getTimeSlot() != 1) {
            throw new BusinessException("时段值无效");
        }
        if (schedule.getMaxCount() == null || schedule.getMaxCount() <= 0) {
            throw new BusinessException("最大挂号数必须大于0");
        }
        if (schedule.getMaxCount() > 100) {
            throw new BusinessException("最大挂号数不能超过100");
        }
    }
}
