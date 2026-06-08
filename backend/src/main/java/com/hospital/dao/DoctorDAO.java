package com.hospital.dao;

import com.hospital.entity.Doctor;
import com.hospital.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 医生数据访问对象
 */
public class DoctorDAO {
    private static final Logger logger = LoggerFactory.getLogger(DoctorDAO.class);

    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.*, dept.name as department_name FROM doctor d " +
                     "LEFT JOIN department dept ON d.department_id = dept.id ORDER BY d.id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("查询所有医生失败", e);
        }
        return list;
    }

    public List<Doctor> findByDepartmentId(Long deptId) {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.*, dept.name as department_name FROM doctor d " +
                     "LEFT JOIN department dept ON d.department_id = dept.id " +
                     "WHERE d.department_id = ? ORDER BY d.id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("按科室查询医生失败: deptId={}", deptId, e);
        }
        return list;
    }

    public Doctor findById(Long id) {
        String sql = "SELECT d.*, dept.name as department_name FROM doctor d " +
                     "LEFT JOIN department dept ON d.department_id = dept.id WHERE d.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("查询医生失败: id={}", id, e);
        }
        return null;
    }

    /** 根据登录用户ID查询医生（用于医生角色登录后获取当前医生信息） */
    public Doctor findByUserId(Long userId) {
        if (userId == null) return null;
        String sql = "SELECT d.*, dept.name as department_name FROM doctor d " +
                     "LEFT JOIN department dept ON d.department_id = dept.id WHERE d.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("按用户ID查询医生失败: userId={}", userId, e);
        }
        return null;
    }

    public int insert(Doctor doctor) {
        String sql = "INSERT INTO doctor (name, title, department_id, user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getTitle());
            ps.setLong(3, doctor.getDepartmentId());
            ps.setObject(4, doctor.getUserId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        doctor.setId(rs.getLong(1));
                    }
                }
                logger.info("医生添加成功: name={}", doctor.getName());
            }
            return rows;
        } catch (SQLException e) {
            logger.error("插入医生失败: name={}", doctor.getName(), e);
        }
        return 0;
    }

    public int update(Doctor doctor) {
        String sql = "UPDATE doctor SET name = ?, title = ?, department_id = ?, user_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getName());
            ps.setString(2, doctor.getTitle());
            ps.setLong(3, doctor.getDepartmentId());
            ps.setObject(4, doctor.getUserId());
            ps.setLong(5, doctor.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("医生更新成功: id={}", doctor.getId());
            }
            return rows;
        } catch (SQLException e) {
            logger.error("更新医生失败: id={}", doctor.getId(), e);
        }
        return 0;
    }

    public int delete(Long id) {
        String sql = "DELETE FROM doctor WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("医生删除成功: id={}", id);
            }
            return rows;
        } catch (SQLException e) {
            logger.error("删除医生失败: id={}", id, e);
        }
        return 0;
    }

    public boolean hasSchedule(Long doctorId) {
        String sql = "SELECT COUNT(*) FROM schedule WHERE doctor_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查医生是否有排班失败: doctorId={}", doctorId, e);
        }
        return false;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setId(rs.getLong("id"));
        doctor.setName(rs.getString("name"));
        doctor.setTitle(rs.getString("title"));
        doctor.setDepartmentId(rs.getLong("department_id"));
        try {
            long uid = rs.getLong("user_id");
            if (!rs.wasNull()) doctor.setUserId(uid);
        } catch (SQLException ignored) {}
        try {
            doctor.setDepartmentName(rs.getString("department_name"));
        } catch (SQLException ignored) {}
        return doctor;
    }
}
