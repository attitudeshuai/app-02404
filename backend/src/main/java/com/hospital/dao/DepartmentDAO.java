package com.hospital.dao;

import com.hospital.entity.Department;
import com.hospital.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 科室数据访问对象
 */
public class DepartmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentDAO.class);

    public List<Department> findAll() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM department ORDER BY id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("查询所有科室失败", e);
        }
        return list;
    }

    public Department findById(Long id) {
        String sql = "SELECT * FROM department WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("查询科室失败: id={}", id, e);
        }
        return null;
    }

    public int insert(Department dept) {
        String sql = "INSERT INTO department (name, description) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        dept.setId(rs.getLong(1));
                    }
                }
                logger.info("科室添加成功: name={}", dept.getName());
            }
            return rows;
        } catch (SQLException e) {
            logger.error("插入科室失败: name={}", dept.getName(), e);
        }
        return 0;
    }

    public int update(Department dept) {
        String sql = "UPDATE department SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getDescription());
            ps.setLong(3, dept.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("科室更新成功: id={}", dept.getId());
            }
            return rows;
        } catch (SQLException e) {
            logger.error("更新科室失败: id={}", dept.getId(), e);
        }
        return 0;
    }

    public int delete(Long id) {
        String sql = "DELETE FROM department WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                logger.info("科室删除成功: id={}", id);
            }
            return rows;
        } catch (SQLException e) {
            logger.error("删除科室失败: id={}", id, e);
        }
        return 0;
    }

    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM department WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查科室名称是否存在失败: name={}", name, e);
        }
        return false;
    }

    public boolean existsByNameExcludeId(String name, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM department WHERE name = ? AND id != ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查科室名称是否存在失败: name={}", name, e);
        }
        return false;
    }

    public boolean hasDoctor(Long deptId) {
        String sql = "SELECT COUNT(*) FROM doctor WHERE department_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, deptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查科室是否有医生失败: deptId={}", deptId, e);
        }
        return false;
    }

    private Department mapRow(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setId(rs.getLong("id"));
        dept.setName(rs.getString("name"));
        dept.setDescription(rs.getString("description"));
        return dept;
    }
}
