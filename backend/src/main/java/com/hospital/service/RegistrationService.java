package com.hospital.service;

import com.hospital.dao.RegistrationDAO;
import com.hospital.dao.ScheduleDAO;
import com.hospital.entity.Registration;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
import com.hospital.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * 挂号服务类
 */
public class RegistrationService {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private final RegistrationDAO registrationDAO;
    private final ScheduleDAO scheduleDAO;

    public RegistrationService() {
        this(new RegistrationDAO(), new ScheduleDAO());
    }

    public RegistrationService(RegistrationDAO registrationDAO, ScheduleDAO scheduleDAO) {
        this.registrationDAO = registrationDAO;
        this.scheduleDAO = scheduleDAO;
    }

    public List<Registration> getMyRegistrations(Long userId) {
        return registrationDAO.findByUserId(userId);
    }

    public List<Registration> getAllRegistrations() {
        return registrationDAO.findAll();
    }

    public List<Registration> searchRegistrations(LocalDate date, Long deptId, Integer status) {
        return registrationDAO.findByCondition(date, deptId, status);
    }

    /** 医生端：查询该医生排班下的挂号记录 */
    public List<Registration> getRegistrationsByDoctorId(Long doctorId) {
        if (doctorId == null) return List.of();
        return registrationDAO.findByDoctorId(doctorId);
    }

    public Registration getRegistrationById(Long id) {
        return registrationDAO.findById(id);
    }

    public void createRegistration(Long userId, Long scheduleId) throws BusinessException {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (scheduleId == null) {
            throw new BusinessException("请选择排班时段");
        }

        // 检查是否重复挂号
        if (registrationDAO.exists(userId, scheduleId)) {
            throw new BusinessException("您已在该时段挂号，请勿重复挂号");
        }

        // 检查号源
        Schedule schedule = scheduleDAO.findById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班信息不存在");
        }
        if (schedule.getCurrentCount() >= schedule.getMaxCount()) {
            throw new BusinessException("该时段号源已满");
        }

        // 使用事务确保数据一致性
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 增加挂号数（使用同一连接，与下文 insert 在同一事务内）
            int updated = scheduleDAO.incrementCount(conn, scheduleId);
            if (updated <= 0) {
                throw new BusinessException("号源已满或排班不存在");
            }

            // 创建挂号记录（同一 conn，保证号源与挂号记录一致）
            Registration reg = new Registration(userId, scheduleId);
            int inserted = registrationDAO.insert(conn, reg);
            if (inserted <= 0) {
                throw new BusinessException("挂号失败");
            }

            conn.commit();
            logger.info("挂号成功: userId={}, scheduleId={}", userId, scheduleId);
        } catch (BusinessException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { logger.error("回滚事务失败", ex); }
            }
            throw e;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { logger.error("回滚事务失败", ex); }
            }
            logger.error("挂号失败", e);
            throw new BusinessException("挂号失败，请稍后重试");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
            DBUtil.close(conn);
        }
    }

    /**
     * 管理员将挂号记录标记为已完成（仅待就诊可操作，不释放号源）
     */
    public void completeRegistration(Long regId) throws BusinessException {
        if (regId == null) {
            throw new BusinessException("挂号ID不能为空");
        }
        Registration reg = registrationDAO.findById(regId);
        if (reg == null) {
            throw new BusinessException("挂号记录不存在");
        }
        if (reg.getStatus() != 0) {
            throw new BusinessException("只有待就诊状态的挂号才能标记为已完成");
        }
        int updated = registrationDAO.updateStatus(regId, 1);
        if (updated <= 0) {
            throw new BusinessException("操作失败");
        }
        logger.info("挂号已完成: regId={}", regId);
    }

    public void cancelRegistration(Long regId) throws BusinessException {
        if (regId == null) {
            throw new BusinessException("挂号ID不能为空");
        }

        Registration reg = registrationDAO.findById(regId);
        if (reg == null) {
            throw new BusinessException("挂号记录不存在");
        }

        // 只有待就诊状态才能取消
        if (reg.getStatus() != 0) {
            throw new BusinessException("只有待就诊状态的挂号才能取消");
        }

        // 使用事务确保数据一致性
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 更新状态为已取消（使用同一连接）
            int updated = registrationDAO.updateStatus(conn, regId, 2);
            if (updated <= 0) {
                throw new BusinessException("取消挂号失败");
            }

            // 恢复号源（同一 conn，与上面 update 在同一事务内）
            scheduleDAO.decrementCount(conn, reg.getScheduleId());

            conn.commit();
            logger.info("取消挂号成功: regId={}", regId);
        } catch (BusinessException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { logger.error("回滚事务失败", ex); }
            }
            throw e;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { logger.error("回滚事务失败", ex); }
            }
            logger.error("取消挂号失败", e);
            throw new BusinessException("取消挂号失败，请稍后重试");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {}
            }
            DBUtil.close(conn);
        }
    }

    /**
     * 管理员/医生修改挂号状态（可设为 0-待就诊 / 1-已完成 / 2-已取消），并同步号源。
     * @param doctorId 若不为 null 表示医生操作，仅允许修改本人排班下的挂号
     */
    public void updateStatusByAdmin(Long regId, Integer newStatus, Long doctorId) throws BusinessException {
        if (regId == null) {
            throw new BusinessException("挂号ID不能为空");
        }
        if (newStatus == null || newStatus < 0 || newStatus > 2) {
            throw new BusinessException("状态值无效");
        }
        Registration reg = registrationDAO.findById(regId);
        if (reg == null) {
            throw new BusinessException("挂号记录不存在");
        }
        if (doctorId != null) {
            Schedule schedule = scheduleDAO.findById(reg.getScheduleId());
            if (schedule == null || !doctorId.equals(schedule.getDoctorId())) {
                throw new BusinessException("无权修改该挂号记录");
            }
        }
        int oldStatus = reg.getStatus() != null ? reg.getStatus() : 0;
        if (oldStatus == newStatus) {
            return;
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 改为已取消：若原为待就诊或已完成，释放号源
            if (newStatus == 2) {
                if (oldStatus == 0 || oldStatus == 1) {
                    scheduleDAO.decrementCount(conn, reg.getScheduleId());
                }
                int u = registrationDAO.updateStatus(conn, regId, 2);
                if (u <= 0) throw new BusinessException("更新失败");
            }
            // 改为待就诊或已完成：若原为已取消，需占回号源
            else if (oldStatus == 2) {
                int inc = scheduleDAO.incrementCount(conn, reg.getScheduleId());
                if (inc <= 0) {
                    throw new BusinessException("该时段号源已满，无法恢复为该状态");
                }
                int u = registrationDAO.updateStatus(conn, regId, newStatus);
                if (u <= 0) throw new BusinessException("更新失败");
            }
            // 待就诊 <-> 已完成：不涉及号源
            else {
                int u = registrationDAO.updateStatus(conn, regId, newStatus);
                if (u <= 0) throw new BusinessException("更新失败");
            }

            conn.commit();
            logger.info("管理员修改挂号状态: regId={}, {} -> {}", regId, oldStatus, newStatus);
        } catch (BusinessException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { logger.error("回滚事务失败", ex); }
            }
            throw e;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { logger.error("回滚事务失败", ex); }
            }
            logger.error("修改挂号状态失败", e);
            throw new BusinessException("修改失败，请稍后重试");
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                DBUtil.close(conn);
            }
        }
    }
}
