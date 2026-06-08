package com.hospital.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * UI 工具类
 */
public class UIHelper {

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BUTTON_FONT);
        button.setBackground(UIConstants.PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(UIConstants.BUTTON_SIZE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(UIConstants.PRIMARY_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UIConstants.PRIMARY_COLOR);
            }
        });
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BUTTON_FONT);
        button.setBackground(Color.WHITE);
        button.setForeground(UIConstants.PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setPreferredSize(UIConstants.BUTTON_SIZE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIConstants.BUTTON_FONT);
        button.setBackground(UIConstants.ERROR_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(UIConstants.BUTTON_SIZE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(UIConstants.NORMAL_FONT);
        field.setPreferredSize(UIConstants.INPUT_SIZE);
        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(UIConstants.NORMAL_FONT);
        field.setPreferredSize(UIConstants.INPUT_SIZE);
        return field;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.NORMAL_FONT);
        label.setForeground(UIConstants.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.TITLE_FONT);
        label.setForeground(UIConstants.PRIMARY_COLOR);
        return label;
    }

    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.SUBTITLE_FONT);
        label.setForeground(UIConstants.TEXT_PRIMARY);
        return label;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(UIConstants.CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM, 
                           UIConstants.PADDING_MEDIUM, UIConstants.PADDING_MEDIUM)
        ));
        return card;
    }

    /** 表格网格线边框：每格绘制右下边线，选中行也保留框线 */
    private static final MatteBorder TABLE_CELL_GRID_BORDER = new MatteBorder(0, 0, 1, 1, UIConstants.BORDER_COLOR);

    public static void styleTable(JTable table) {
        table.setFont(UIConstants.NORMAL_FONT);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.BUTTON_FONT);
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(TABLE_CELL_GRID_BORDER);
                return this;
            }
        };
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public static <T> JComboBox<T> createComboBox() {
        JComboBox<T> comboBox = new JComboBox<>();
        comboBox.setFont(UIConstants.NORMAL_FONT);
        comboBox.setPreferredSize(UIConstants.INPUT_SIZE);
        return comboBox;
    }

    /** 创建日期选择器（yyyy-MM-dd），默认今天 */
    public static JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setFont(UIConstants.NORMAL_FONT);
        spinner.setPreferredSize(new Dimension(120, 28));
        return spinner;
    }

    /** 从日期选择器取值转为 LocalDate */
    public static LocalDate getDateFromSpinner(JSpinner spinner) {
        Date d = (Date) spinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** 设置日期选择器的日期 */
    public static void setDateSpinner(JSpinner spinner, LocalDate date) {
        spinner.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    /** 创建日历样式的日期选择器（文本框 + 日历按钮，点击弹出月历），默认今天 */
    public static CalendarDatePicker createCalendarDatePicker() {
        return new CalendarDatePicker(java.time.LocalDate.now());
    }

    public static CalendarDatePicker createCalendarDatePicker(LocalDate initial) {
        return new CalendarDatePicker(initial);
    }

    // ========== 前端输入校验（与数据库/Service 约束一致，提前提示用户） ==========

    /** 非空，trim 后检查 */
    public static String validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) return "请输入" + fieldName;
        return null;
    }

    /** 用户名：3～20 个字符 */
    public static String validateUsername(String value) {
        String e = validateRequired(value, "用户名");
        if (e != null) return e;
        int len = value.trim().length();
        if (len < 3 || len > 20) return "用户名长度应为 3～20 个字符";
        return null;
    }

    /** 密码：至少 6 位（用于登录/注册/绑定账号） */
    public static String validatePasswordMin(String value, String fieldName) {
        if (value == null) return "请输入" + fieldName;
        if (value.length() < 6) return fieldName + "不能少于 6 位";
        return null;
    }

    /** 字符串最大长度 */
    public static String validateMaxLength(String value, int maxLen, String fieldName) {
        if (value == null) return null;
        if (value.length() > maxLen) return fieldName + "不能超过 " + maxLen + " 个字符";
        return null;
    }

    /** 身份证：选填；若填则 15 位或 18 位数字（最后一位可为 X） */
    public static String validateIdCard(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String s = value.trim();
        if (!s.matches("\\d{15}|\\d{17}[0-9Xx]")) return "身份证号应为 15 位或 18 位数字（末位可为 X）";
        return null;
    }

    /** 手机号：选填；若填则 11 位数字 */
    public static String validatePhone(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        if (!value.trim().matches("\\d{11}")) return "手机号应为 11 位数字";
        return null;
    }

    /** 绑定登录名：选填；若填则 3～20 字符，且密码至少 6 位 */
    public static String validateBindAccount(String username, String password) {
        if (username == null) username = "";
        String u = username.trim();
        if (u.isEmpty()) return null;
        if (u.length() < 3 || u.length() > 20) return "绑定登录名长度应为 3～20 个字符";
        if (password == null || password.length() < 6) return "绑定账号时密码不能少于 6 位";
        return null;
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "警告", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "确认", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    private UIHelper() {}
}
