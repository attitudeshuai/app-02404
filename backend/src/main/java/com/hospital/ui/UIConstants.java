package com.hospital.ui;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * UI 常量类（跨平台：字体在 Windows/macOS/Linux 下自动选择可用中文字体）
 */
public class UIConstants {
    // 颜色常量
    public static final Color PRIMARY_COLOR = new Color(0x1E88E5);      // 医疗蓝
    public static final Color PRIMARY_DARK = new Color(0x1565C0);       // 深蓝
    public static final Color PRIMARY_LIGHT = new Color(0x64B5F6);      // 浅蓝
    public static final Color SUCCESS_COLOR = new Color(0x4CAF50);      // 成功绿
    public static final Color ERROR_COLOR = new Color(0xF44336);        // 错误红
    public static final Color WARNING_COLOR = new Color(0xFF9800);      // 警告橙
    public static final Color BG_COLOR = new Color(0xF5F5F5);           // 背景灰
    public static final Color CARD_COLOR = Color.WHITE;                 // 卡片白
    public static final Color TEXT_PRIMARY = new Color(0x212121);       // 主文字
    public static final Color TEXT_SECONDARY = new Color(0x757575);     // 次要文字
    public static final Color BORDER_COLOR = new Color(0xE0E0E0);       // 边框色

    /** 跨平台 UI 字体名：优先微软雅黑/PingFang/Noto 等，最后回退到 SansSerif */
    private static final String UI_FONT_NAME = resolveUiFontName();

    // 字体常量（使用统一解析的字体名，保证各平台显示一致）
    public static final Font TITLE_FONT = new Font(UI_FONT_NAME, Font.BOLD, 24);
    public static final Font SUBTITLE_FONT = new Font(UI_FONT_NAME, Font.BOLD, 18);
    public static final Font NORMAL_FONT = new Font(UI_FONT_NAME, Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font(UI_FONT_NAME, Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font(UI_FONT_NAME, Font.BOLD, 14);

    /**
     * 按优先级选择当前系统可用的中文字体名，保证 Windows/macOS/Linux 下均有合适回退。
     */
    private static String resolveUiFontName() {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        String[] candidates = {
                "微软雅黑", "Microsoft YaHei UI", "Microsoft YaHei",
                "PingFang SC", "PingFang TC", "Hiragino Sans GB",
                "Noto Sans CJK SC", "Noto Sans SC", "Source Han Sans SC",
                "WenQuanYi Micro Hei", "WenQuanYi Zen Hei", "Droid Sans Fallback",
                "SansSerif"
        };
        for (String name : candidates) {
            if (available.contains(name)) return name;
        }
        return "SansSerif";
    }

    // 尺寸常量
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_LARGE = 24;
    public static final int BORDER_RADIUS = 8;
    public static final Dimension BUTTON_SIZE = new Dimension(120, 36);
    public static final Dimension INPUT_SIZE = new Dimension(200, 32);

    // 窗口尺寸
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;

    private UIConstants() {}
}
