package com.hospital.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 日历样式的日期选择器：显示日期标签 +「日历」按钮，点击后弹出月历选择。
 * 使用 JLabel 显示日期，避免 JTextField.setEditable(false) 被 Look and Feel 渲染成灰色。
 */
public class CalendarDatePicker extends JPanel {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] WEEK_HEADERS = {"日", "一", "二", "三", "四", "五", "六"};

    private final JLabel dateLabel;
    private LocalDate selectedDate;

    public CalendarDatePicker() {
        this(LocalDate.now());
    }

    public CalendarDatePicker(LocalDate initial) {
        setLayout(new BorderLayout(4, 0));
        setBackground(UIConstants.CARD_COLOR);
        setOpaque(true);
        selectedDate = initial != null ? initial : LocalDate.now();
        dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(UIConstants.NORMAL_FONT);
        dateLabel.setOpaque(true);
        dateLabel.setBackground(Color.WHITE);
        dateLabel.setForeground(UIConstants.TEXT_PRIMARY);
        dateLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        dateLabel.setToolTipText("点击此处或「日历」按钮选择日期");
        dateLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dateLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEnabled()) showCalendar();
            }
        });
        refreshDateText();
        JButton calBtn = new JButton("日历");
        calBtn.setFont(UIConstants.SMALL_FONT);
        calBtn.setPreferredSize(new Dimension(56, 32));
        calBtn.setOpaque(true);
        calBtn.setBackground(Color.WHITE);
        calBtn.setForeground(UIConstants.TEXT_PRIMARY);
        calBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        calBtn.setBorderPainted(true);
        calBtn.setContentAreaFilled(true);
        calBtn.setFocusPainted(false);
        calBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calBtn.addActionListener(e -> showCalendar());
        add(dateLabel, BorderLayout.CENTER);
        add(calBtn, BorderLayout.EAST);
        setMinimumSize(new Dimension(180, 32));
    }

    private void refreshDateText() {
        dateLabel.setText(selectedDate.format(FMT));
    }

    private void showCalendar() {
        CalendarDialog dlg = new CalendarDialog(selectedDate);
        dlg.setVisible(true);
        LocalDate picked = dlg.getSelectedDate();
        if (picked != null) {
            selectedDate = picked;
            refreshDateText();
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        if (date != null) {
            this.selectedDate = date;
            refreshDateText();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dateLabel.setEnabled(enabled);
        for (Component c : getComponents()) {
            if (c != dateLabel) c.setEnabled(enabled);
        }
    }

    /** 弹窗内的月历 */
    private static class CalendarDialog extends JDialog {
        private LocalDate selectedDate;
        private JLabel monthYearLabel;
        private YearMonth currentMonth;
        private final JButton[][] dayButtons = new JButton[6][7];

        CalendarDialog(LocalDate initial) {
            super((Frame) null, "选择日期", true);
            selectedDate = initial;
            currentMonth = YearMonth.from(initial);
            setSize(320, 280);
            setLocationRelativeTo(null);
            setResizable(false);
            setLayout(new BorderLayout(12, 12));
            getContentPane().setBackground(UIConstants.BG_COLOR);
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(UIConstants.BG_COLOR);
            JButton prev = new JButton("◀");
            prev.setFont(UIConstants.SMALL_FONT);
            prev.addActionListener(e -> { currentMonth = currentMonth.minusMonths(1); buildCalendar(); });
            JButton next = new JButton("▶");
            next.setFont(UIConstants.SMALL_FONT);
            next.addActionListener(e -> { currentMonth = currentMonth.plusMonths(1); buildCalendar(); });
            monthYearLabel = new JLabel("", SwingConstants.CENTER);
            monthYearLabel.setFont(UIConstants.BUTTON_FONT);
            JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
            nav.setBackground(UIConstants.BG_COLOR);
            nav.add(prev);
            nav.add(monthYearLabel);
            nav.add(next);
            top.add(nav, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
            grid.setBackground(UIConstants.BG_COLOR);
            for (int i = 0; i < 7; i++) {
                JLabel h = new JLabel(WEEK_HEADERS[i], SwingConstants.CENTER);
                h.setFont(UIConstants.SMALL_FONT);
                h.setForeground(UIConstants.TEXT_SECONDARY);
                grid.add(h);
            }
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 7; col++) {
                    JButton btn = new JButton("");
                    btn.setFont(UIConstants.SMALL_FONT);
                    btn.setMargin(new Insets(2, 4, 2, 4));
                    dayButtons[row][col] = btn;
                    grid.add(btn);
                }
            }
            add(grid, BorderLayout.CENTER);
            buildCalendar();
        }

        private void buildCalendar() {
            monthYearLabel.setText(currentMonth.getYear() + "年" + currentMonth.getMonthValue() + "月");
            int len = currentMonth.lengthOfMonth();
            LocalDate first = currentMonth.atDay(1);
            DayOfWeek firstDow = first.getDayOfWeek();
            int startOffset = firstDow.getValue() % 7;

            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 7; col++) {
                    int dayIndex = row * 7 + col - startOffset;
                    JButton btn = dayButtons[row][col];
                    btn.setVisible(true);
                    if (dayIndex < 0 || dayIndex >= len) {
                        btn.setText("");
                        btn.setEnabled(false);
                        btn.setBackground(null);
                        continue;
                    }
                    int day = dayIndex + 1;
                    LocalDate d = currentMonth.atDay(day);
                    btn.setText(String.valueOf(day));
                    btn.setEnabled(true);
                    if (d.equals(selectedDate)) {
                        btn.setBackground(UIConstants.PRIMARY_LIGHT);
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setBackground(Color.WHITE);
                        btn.setForeground(UIConstants.TEXT_PRIMARY);
                    }
                    while (btn.getActionListeners().length > 0) btn.removeActionListener(btn.getActionListeners()[0]);
                    LocalDate pick = d;
                    btn.addActionListener(e -> {
                        selectedDate = pick;
                        dispose();
                    });
                }
            }
        }

        LocalDate getSelectedDate() {
            return selectedDate;
        }
    }
}
