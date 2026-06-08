package com.hospital.dao;

import com.hospital.entity.User;
import com.hospital.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * 用户数据访问对象
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public User findByUsername(String username) {
        String sql = "SELECT * FROM sys_user WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("查询用户失败: username={}", username, e);
        }
        return null;
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM sys_user WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("查询用户失败: id={}", id, e);
        }
        return null;
    }

    public int insert(User user) {
        String sql = "INSERT INTO sys_user (username, password, real_name, id_card, phone, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getIdCard());
            ps.setString(5, user.getPhone());
            ps.setInt(6, user.getRole() != null ? user.getRole() : 0);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getLong(1));
                    }
                }
                logger.info("用户注册成功: username={}", user.getUsername());
            }
            return rows;
        } catch (SQLException e) {
            logger.error("插入用户失败: username={}", user.getUsername(), e);
        }
        return 0;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查用户名是否存在失败: username={}", username, e);
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setIdCard(rs.getString("id_card"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getInt("role"));
        Timestamp ts = rs.getTimestamp("create_time");
        if (ts != null) {
            user.setCreateTime(ts.toLocalDateTime());
        }
        return user;
    }
}
