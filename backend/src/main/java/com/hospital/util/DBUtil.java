package com.hospital.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接池工具类
 */
public class DBUtil {
    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);
    private static HikariDataSource dataSource;
    private static String initError = null;
    private static boolean initialized = false;

    static {
        try {
            initDataSource();
            initialized = true;
        } catch (Exception e) {
            initError = e.getMessage();
            logger.error("初始化数据库连接池失败", e);
            // 打印到控制台，方便调试
            System.err.println("========================================");
            System.err.println("数据库连接失败: " + e.getMessage());
            System.err.println("请检查:");
            System.err.println("1. MySQL 服务是否已启动");
            System.err.println("2. db.properties 中的用户名密码是否正确");
            System.err.println("3. 数据库 hospital_registration 是否已创建");
            System.err.println("========================================");
            e.printStackTrace();
        }
    }

    private static void initDataSource() throws IOException {
        Properties props = new Properties();
        try (InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new IOException("找不到 db.properties 配置文件");
            }
            props.load(is);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));
        
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimumIdle", "5")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("hikari.maxLifetime", "1800000")));

        dataSource = new HikariDataSource(config);
        logger.info("数据库连接池初始化成功");
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized || dataSource == null) {
            throw new SQLException("数据库未初始化: " + (initError != null ? initError : "未知错误"));
        }
        return dataSource.getConnection();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static String getInitError() {
        return initError;
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    logger.warn("关闭资源失败", e);
                }
            }
        }
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("数据库连接池已关闭");
        }
    }
}
