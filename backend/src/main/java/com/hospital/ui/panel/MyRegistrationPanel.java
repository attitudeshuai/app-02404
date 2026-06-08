package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.controller.RegistrationController;
import com.hospital.entity.Registration;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 我的挂号面板
 */
public class MyRegistrationPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final MainFrame mainFrame;
    private final RegistrationController regController = new RegistrationController();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Registration> registrationList;

    public MyRegistrationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // 顶部栏
        add(createTopBar(), BorderLayout.NORTH);

        // 内容区
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIConstants.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 表格
        JPanel tablePanel = UIHelper.createCard();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {"挂号时间", "科室", "医生", "就诊日期", "时段", "状态"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIConstants.CARD_COLOR);
        
        JButton refreshBtn = UIHelper.createSecondaryButton("刷新");
        refreshBtn.addActionListener(e -> refresh());
        buttonPanel.add(refreshBtn);

        JButton cancelBtn = UIHelper.createDangerButton("取消挂号");
        cancelBtn.addActionListener(e -> doCancelRegistration());
        buttonPanel.add(cancelBtn);

        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIConstants.PRIMARY_COLOR);
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton backBtn = new JButton("← 返回");
        backBtn.setFont(UIConstants.NORMAL_FONT);
        backBtn.setForeground(Color.WHITE);
        backBtn.setBackground(UIConstants.PRIMARY_COLOR);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> mainFrame.showPatientMain());
        topBar.add(backBtn, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("我的挂号", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);

        return topBar;
    }

    private void doCancelRegistration() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showWarning(this, "请选择要取消的挂号记录");
            return;
        }

        if (registrationList == null || selectedRow >= registrationList.size()) {
            return;
        }

        Registration reg = registrationList.get(selectedRow);
        if (reg.getStatus() != 0) {
            UIHelper.showError(this, "只有待就诊状态的挂号才能取消");
            return;
        }

        if (!UIHelper.showConfirm(this, "确认取消该挂号？")) {
            return;
        }

        Result<Void> result = regController.cancelRegistration(reg.getId());
        if (result.isSuccess()) {
            UIHelper.showSuccess(this, "取消成功");
            refresh();
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        
        if (mainFrame.getCurrentUser() == null) return;
        
        Long userId = mainFrame.getCurrentUser().getId();
        Result<List<Registration>> result = regController.getMyRegistrations(userId);
        if (result.isSuccess() && result.getData() != null) {
            registrationList = result.getData();
            for (Registration r : registrationList) {
                tableModel.addRow(new Object[]{
                    r.getCreateTime() != null ? r.getCreateTime().toString().replace("T", " ") : "",
                    r.getDepartmentName(),
                    r.getDoctorName(),
                    r.getScheduleDate(),
                    r.getTimeSlotText(),
                    r.getStatusText()
                });
            }
        }
    }
}
