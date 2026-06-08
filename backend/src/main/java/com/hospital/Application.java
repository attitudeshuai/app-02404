package com.hospital;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.exception.GlobalExceptionHandler;
import com.hospital.ui.MainFrame;
import com.hospital.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * 医院门诊挂号系统 - 应用入口
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // 安装全局异常处理器
        GlobalExceptionHandler.install();

        // 检查数据库连接
        if (!DBUtil.isInitialized()) {
            String error = DBUtil.getInitError();
            System.err.println("数据库连接失败: " + error);
            JOptionPane.showMessageDialog(null,
                "数据库连接失败！\n\n" + error + "\n\n请检查:\n1. MySQL服务是否启动\n2. db.properties配置是否正确\n3. 数据库是否已初始化",
                "数据库错误",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 设置 FlatLaf 主题
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception e) {
            logger.error("设置 Look and Feel 失败", e);
        }

        // 在 EDT 线程中启动 GUI
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                logger.info("医院门诊挂号系统启动成功");
            } catch (Exception e) {
                logger.error("启动应用失败", e);
                JOptionPane.showMessageDialog(null, 
                    "启动失败：" + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
