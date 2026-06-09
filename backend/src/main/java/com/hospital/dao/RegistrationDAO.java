package com.hospital.dao;

import com.hospital.entity.Registration;
import com.hospital.exception.BusinessException;
import com.hospital.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 挂号记录数据访问对象
 */
public class RegistrationDAO {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationDAO.class);

    private static final String BASE_SQL = 
        "SELECT r.*, u.real_name as patient_name, d.name as doctor_name, " +
        "dept.name as department_name, s.schedule_date, s.time_slot " +
        "FROM registration r " +
        "LEFT JOIN sys_user u ON r.user_id = u.id " +
        "LEFT JOIN schedule s ON r.schedule_id = s.id " +
        "LEFT JOIN doctor d ON s.doctor_id = d.id " +
        "LEFT JOIN department dept ON d.department_id = dept.id ";

    public List<Registration> findByUserId(Long userId) {
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SQL + "WHERE r.user_id = ? ORDER BY r.create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按用户查询挂号记录失败: userId={}", userId, e);
        }
        return list;
    }

    public List<Registration> findAll() {
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SQL + "ORDER BY r.create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("查询所有挂号记录失败", e);
        }
        return list;
    }

    /** 按医生ID查询其排班下的所有挂号记录（医生端“我的挂号”） */
    public List<Registration> findByDoctorId(Long doctorId) {
        if (doctorId == null) return new ArrayList<>();
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SQL + "WHERE s.doctor_id = ? ORDER BY s.schedule_date DESC, s.time_slot, r.create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按医生查询挂号记录失败: doctorId={}", doctorId, e);
        }
        return list;
    }

    public List<Registration> findByCondition(LocalDate date, Long deptId, Integer status) {
        List<Registration> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SQL);
        sql.append("WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        if (date != null) {
            sql.append("AND s.schedule_date = ? ");
            params.add(Date.valueOf(date));
        }
        if (deptId != null) {
            sql.append("AND d.department_id = ? ");
            params.add(deptId);
        }
        if (status != null) {
            sql.append("AND r.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY r.create_time DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按条件查询挂号记录失败", e);
        }
        return list;
    }

    public Registration findById(Long id) {
        String sql = BASE_SQL + "WHERE r.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("查询挂号记录失败: id={}", id, e);
        }
        return null;
    }

    /** MySQL 重复键错误码（uk_user_schedule_active 冲突时） */
    private static final int MYSQL_ER_DUP_ENTRY = 1062;

    /** 使用调用方传入的连接，供事务内使用（不关闭 conn） */
    public int insert(Connection conn, Registration reg) {
        String sql = "INSERT INTO registration (user_id, schedule_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, reg.getUserId());
            ps.setLong(2, reg.getScheduleId());
            ps.setInt(3, reg.getStatus() != null ? reg.getStatus() : 0);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        reg.setId(rs.getLong(1));
                    }
                }
                logger.info("挂号成功: userId={}, scheduleId={}", reg.getUserId(), reg.getScheduleId());
            }
            return rows;
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_ER_DUP_ENTRY) {
                throw new BusinessException("您已在该时段挂号，请勿重复挂号");
            }
            logger.error("插入挂号记录失败", e);
        }
        return 0;
    }

    public int insert(Registration reg) {
        try (Connection conn = DBUtil.getConnection()) {
            return insert(conn, reg);
        } catch (SQLException e) {
            logger.error("插入挂号记录失败", e);
            return 0;
        }
    }

    /** 使用调用方传入的连接，供事务内使用（不关闭 conn） */
    public int updateStatus(Connection conn, Long id, Integer status) {
        String sql = "UPDATE registration SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setLong(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("挂号状态更新成功: id={}, status={}", id, status);
            }
            return rows;
        } catch (SQLException e) {
            logger.error("更新挂号状态失败: id={}", id, e);
        }
        return 0;
    }

    public int updateStatus(Long id, Integer status) {
        try (Connection conn = DBUtil.getConnection()) {
            return updateStatus(conn, id, status);
        } catch (SQLException e) {
            logger.error("更新挂号状态失败: id={}", id, e);
            return 0;
        }
    }

    public boolean exists(Long userId, Long scheduleId) {
        String sql = "SELECT COUNT(*) FROM registration WHERE user_id = ? AND schedule_id = ? AND status != 2";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查挂号记录是否存在失败", e);
        }
        return false;
    }

    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration reg = new Registration();
        reg.setId(rs.getLong("id"));
        reg.setUserId(rs.getLong("user_id"));
        reg.setScheduleId(rs.getLong("schedule_id"));
        reg.setStatus(rs.getInt("status"));
        Timestamp ts = rs.getTimestamp("create_time");
        if (ts != null) {
            reg.setCreateTime(ts.toLocalDateTime());
        }
        try {
            reg.setPatientName(rs.getString("patient_name"));
            reg.setDoctorName(rs.getString("doctor_name"));
            reg.setDepartmentName(rs.getString("department_name"));
            Date date = rs.getDate("schedule_date");
            if (date != null) {
                reg.setScheduleDate(date.toLocalDate());
            }
            reg.setTimeSlot(rs.getInt("time_slot"));
        } catch (SQLException ignored) {}
        return reg;
    }
}
