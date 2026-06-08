package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.controller.RegistrationController;
import com.hospital.entity.Doctor;
import com.hospital.entity.Registration;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 医生端：本人排班下的挂号记录，支持修改状态
 */
public class DoctorRegistrationPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final MainFrame mainFrame;
    private final RegistrationController regController = new RegistrationController();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Registration> registrationList = new ArrayList<>();

    public DoctorRegistrationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);
        add(createTopBar(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(UIConstants.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel tablePanel = UIHelper.createCard();
        tablePanel.setLayout(new BorderLayout());
        String[] columns = {"ID", "患者", "科室", "就诊日期", "时段", "状态", "挂号时间"};
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

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(UIConstants.CARD_COLOR);
        JButton updateStatusBtn = UIHelper.createPrimaryButton("修改状态");
        updateStatusBtn.addActionListener(e -> doUpdateStatus());
        actionPanel.add(updateStatusBtn);
        tablePanel.add(actionPanel, BorderLayout.SOUTH);

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
        backBtn.addActionListener(e -> mainFrame.showDoctorMain());
        topBar.add(backBtn, BorderLayout.WEST);
        JLabel titleLabel = new JLabel("我的挂号记录", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);
        return topBar;
    }

    private void doUpdateStatus() {
        Doctor doctor = mainFrame.getCurrentDoctor();
        if (doctor == null || doctor.getId() == null) {
            UIHelper.showError(this, "未获取到医生信息");
            return;
        }
        Registration reg = getSelectedRegistration();
        if (reg == null) return;
        Integer newStatus = showStatusSelectDialog(reg.getStatus());
        if (newStatus == null) return;
        Result<Void> result = regController.updateStatus(reg.getId(), newStatus, doctor.getId());
        if (result.isSuccess()) {
            UIHelper.showSuccess(this, "状态已更新");
            refresh();
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }

    private Integer showStatusSelectDialog(Integer currentStatus) {
        String[] options = {"待就诊", "已完成", "已取消"};
        int currentIndex = currentStatus != null && currentStatus >= 0 && currentStatus <= 2 ? currentStatus : 0;
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(UIConstants.NORMAL_FONT);
        combo.setSelectedIndex(currentIndex);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("新状态："));
        panel.add(combo);
        int result = JOptionPane.showConfirmDialog(this, panel, "修改挂号状态",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;
        return combo.getSelectedIndex();
    }

    private Registration getSelectedRegistration() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showWarning(this, "请先选择一条挂号记录");
            return null;
        }
        if (registrationList == null || selectedRow >= registrationList.size()) return null;
        return registrationList.get(selectedRow);
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        registrationList.clear();
        Doctor doctor = mainFrame.getCurrentDoctor();
        if (doctor == null || doctor.getId() == null) return;
        Result<List<Registration>> result = regController.getRegistrationsByDoctorId(doctor.getId());
        if (result.isSuccess() && result.getData() != null) {
            registrationList.addAll(result.getData());
            for (Registration r : result.getData()) {
                tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getPatientName(),
                    r.getDepartmentName(),
                    r.getScheduleDate(),
                    r.getTimeSlotText(),
                    r.getStatusText(),
                    r.getCreateTime() != null ? r.getCreateTime().toString().replace("T", " ") : ""
                });
            }
        }
    }
}
