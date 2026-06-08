package com.hospital.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * 全局异常处理器
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("线程 [{}] 发生未捕获异常", t.getName(), e);
        
        SwingUtilities.invokeLater(() -> {
            String message;
            if (e instanceof BusinessException) {
                message = e.getMessage();
            } else {
                message = "系统发生错误，请联系管理员";
            }
            JOptionPane.showMessageDialog(null, message, "错误", JOptionPane.ERROR_MESSAGE);
        });
    }

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        logger.info("全局异常处理器已安装");
    }
}
