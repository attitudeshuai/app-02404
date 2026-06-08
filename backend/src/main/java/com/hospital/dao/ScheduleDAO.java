package com.hospital.dao;

import com.hospital.entity.Schedule;
import com.hospital.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 排班数据访问对象
 */
public class ScheduleDAO {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleDAO.class);

    public List<Schedule> findAll() {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT s.*, d.name as doctor_name, dept.name as department_name " +
                     "FROM schedule s " +
                     "LEFT JOIN doctor d ON s.doctor_id = d.id " +
                     "LEFT JOIN department dept ON d.department_id = dept.id " +
                     "ORDER BY s.schedule_date DESC, s.time_slot";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("查询所有排班失败", e);
        }
        return list;
    }

    public List<Schedule> findByDoctorId(Long doctorId) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT s.*, d.name as doctor_name, dept.name as department_name " +
                     "FROM schedule s " +
                     "LEFT JOIN doctor d ON s.doctor_id = d.id " +
                     "LEFT JOIN department dept ON d.department_id = dept.id " +
                     "WHERE s.doctor_id = ? ORDER BY s.schedule_date, s.time_slot";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按医生查询排班失败: doctorId={}", doctorId, e);
        }
        return list;
    }

    public List<Schedule> findAvailableByDoctorId(Long doctorId) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT s.*, d.name as doctor_name, dept.name as department_name " +
                     "FROM schedule s " +
                     "LEFT JOIN doctor d ON s.doctor_id = d.id " +
                     "LEFT JOIN department dept ON d.department_id = dept.id " +
                     "WHERE s.doctor_id = ? AND s.schedule_date >= CURDATE() " +
                     "AND s.current_count < s.max_count " +
                     "ORDER BY s.schedule_date, s.time_slot";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("查询可用排班失败: doctorId={}", doctorId, e);
        }
        return list;
    }

    public Schedule findById(Long id) {
        String sql = "SELECT s.*, d.name as doctor_name, dept.name as department_name " +
                     "FROM schedule s " +
                     "LEFT JOIN doctor d ON s.doctor_id = d.id " +
                     "LEFT JOIN department dept ON d.department_id = dept.id " +
                     "WHERE s.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("查询排班失败: id={}", id, e);
        }
        return null;
    }

    public int insert(Schedule schedule) {
        String sql = "INSERT INTO schedule (doctor_id, schedule_date, time_slot, max_count, current_count) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, schedule.getDoctorId());
            ps.setDate(2, Date.valueOf(schedule.getScheduleDate()));
            ps.setInt(3, schedule.getTimeSlot());
            ps.setInt(4, schedule.getMaxCount());
            ps.setInt(5, schedule.getCurrentCount() != null ? schedule.getCurrentCount() : 0);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        schedule.setId(rs.getLong(1));
                    }
                }
                logger.info("排班添加成功: doctorId={}, date={}", schedule.getDoctorId(), schedule.getScheduleDate());
            }
            return rows;
        } catch (SQLException e) {
            logger.error("插入排班失败", e);
        }
        return 0;
    }

    public int update(Schedule schedule) {
        String sql = "UPDATE schedule SET doctor_id = ?, schedule_date = ?, time_slot = ?, max_count = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, schedule.getDoctorId());
            ps.setDate(2, Date.valueOf(schedule.getScheduleDate()));
            ps.setInt(3, schedule.getTimeSlot());
            ps.setInt(4, schedule.getMaxCount());
            ps.setLong(5, schedule.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("排班更新成功: id={}", schedule.getId());
            }
            return rows;
        } catch (SQLException e) {
            logger.error("更新排班失败: id={}", schedule.getId(), e);
        }
        return 0;
    }

    public int delete(Long id) {
        String sql = "DELETE FROM schedule WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("排班删除成功: id={}", id);
            }
            return rows;
        } catch (SQLException e) {
            logger.error("删除排班失败: id={}", id, e);
        }
        return 0;
    }

    public boolean exists(Long doctorId, LocalDate date, Integer timeSlot) {
        String sql = "SELECT COUNT(*) FROM schedule WHERE doctor_id = ? AND schedule_date = ? AND time_slot = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, timeSlot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查排班是否存在失败", e);
        }
        return false;
    }

    public boolean existsExcludeId(Long doctorId, LocalDate date, Integer timeSlot, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM schedule WHERE doctor_id = ? AND schedule_date = ? AND time_slot = ? AND id != ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, timeSlot);
            ps.setLong(4, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查排班是否存在失败", e);
        }
        return false;
    }

    public boolean hasRegistration(Long scheduleId) {
        String sql = "SELECT COUNT(*) FROM registration WHERE schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查排班是否有挂号记录失败: scheduleId={}", scheduleId, e);
        }
        return false;
    }

    /** 使用调用方传入的连接，供事务内使用（不关闭 conn） */
    public int incrementCount(Connection conn, Long scheduleId) {
        String sql = "UPDATE schedule SET current_count = current_count + 1 WHERE id = ? AND current_count < max_count";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, scheduleId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("增加挂号数失败: scheduleId={}", scheduleId, e);
        }
        return 0;
    }

    public int incrementCount(Long scheduleId) {
        try (Connection conn = DBUtil.getConnection()) {
            return incrementCount(conn, scheduleId);
        } catch (SQLException e) {
            logger.error("增加挂号数失败: scheduleId={}", scheduleId, e);
            return 0;
        }
    }

    /** 使用调用方传入的连接，供事务内使用（不关闭 conn） */
    public int decrementCount(Connection conn, Long scheduleId) {
        String sql = "UPDATE schedule SET current_count = current_count - 1 WHERE id = ? AND current_count > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, scheduleId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("减少挂号数失败: scheduleId={}", scheduleId, e);
        }
        return 0;
    }

    public int decrementCount(Long scheduleId) {
        try (Connection conn = DBUtil.getConnection()) {
            return decrementCount(conn, scheduleId);
        } catch (SQLException e) {
            logger.error("减少挂号数失败: scheduleId={}", scheduleId, e);
            return 0;
        }
    }

    private Schedule mapRow(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setId(rs.getLong("id"));
        schedule.setDoctorId(rs.getLong("doctor_id"));
        schedule.setScheduleDate(rs.getDate("schedule_date").toLocalDate());
        schedule.setTimeSlot(rs.getInt("time_slot"));
        schedule.setMaxCount(rs.getInt("max_count"));
        schedule.setCurrentCount(rs.getInt("current_count"));
        try {
            schedule.setDoctorName(rs.getString("doctor_name"));
            schedule.setDepartmentName(rs.getString("department_name"));
        } catch (SQLException ignored) {}
        return schedule;
    }
}
