package com.hospital.ui.panel;

import com.hospital.common.RegistrationStatus;
import com.hospital.common.Result;
import com.hospital.controller.DepartmentController;
import com.hospital.controller.RegistrationController;
import com.hospital.entity.Department;
import com.hospital.entity.Registration;
import com.hospital.ui.CalendarDatePicker;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员挂号记录面板
 */
public class AdminRegistrationPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final MainFrame mainFrame;
    private final RegistrationController regController = new RegistrationController();
    private final DepartmentController deptController = new DepartmentController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JCheckBox dateFilterCheck;
    private CalendarDatePicker datePicker;
    private JComboBox<Department> deptComboBox;
    private JComboBox<String> statusComboBox;
    /** 当前表格对应的挂号列表，用于根据选中行取 Registration */
    private List<Registration> registrationList = new ArrayList<>();

    public AdminRegistrationPanel(MainFrame mainFrame) {
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

        // 筛选区
        JPanel filterPanel = UIHelper.createCard();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        dateFilterCheck = new JCheckBox("指定日期");
        dateFilterCheck.setFont(UIConstants.NORMAL_FONT);
        dateFilterCheck.setBackground(UIConstants.CARD_COLOR);
        filterPanel.add(dateFilterCheck);
        datePicker = UIHelper.createCalendarDatePicker();
        datePicker.setPreferredSize(new Dimension(180, 32));
        filterPanel.add(datePicker);

        filterPanel.add(UIHelper.createLabel("科室："));
        deptComboBox = UIHelper.createComboBox();
        deptComboBox.setPreferredSize(new Dimension(120, 32));
        deptComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "全部" : ((Department) value).getName());
            label.setFont(UIConstants.NORMAL_FONT);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            label.setOpaque(true);
            return label;
        });
        filterPanel.add(deptComboBox);

        filterPanel.add(UIHelper.createLabel("状态："));
        statusComboBox = new JComboBox<>(new String[]{"全部", "待就诊", "已完成", "已取消"});
        statusComboBox.setFont(UIConstants.NORMAL_FONT);
        statusComboBox.setPreferredSize(new Dimension(100, 32));
        filterPanel.add(statusComboBox);

        JButton searchBtn = UIHelper.createPrimaryButton("搜索");
        searchBtn.addActionListener(e -> doSearch());
        filterPanel.add(searchBtn);

        JButton resetBtn = UIHelper.createSecondaryButton("重置");
        resetBtn.addActionListener(e -> doReset());
        filterPanel.add(resetBtn);

        contentPanel.add(filterPanel, BorderLayout.NORTH);

        // 表格区
        JPanel tablePanel = UIHelper.createCard();
        tablePanel.setLayout(new BorderLayout());

        String[] columns = {"ID", "患者", "科室", "医生", "就诊日期", "时段", "状态", "挂号时间"};
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

        // 操作按钮：完成 / 取消 / 修改状态
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(UIConstants.CARD_COLOR);
        JButton completeBtn = UIHelper.createPrimaryButton("完成");
        completeBtn.addActionListener(e -> doComplete());
        actionPanel.add(completeBtn);
        JButton cancelBtn = UIHelper.createDangerButton("取消挂号");
        cancelBtn.addActionListener(e -> doCancel());
        actionPanel.add(cancelBtn);
        JButton updateStatusBtn = UIHelper.createSecondaryButton("修改状态");
        updateStatusBtn.addActionListener(e -> doUpdateStatus());
        actionPanel.add(updateStatusBtn);
        tablePanel.add(actionPanel, BorderLayout.SOUTH);

        contentPanel.add(tablePanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void doComplete() {
        Registration reg = getSelectedRegistration();
        if (reg == null) return;
        if (reg.getStatus() != null && reg.getStatus() != 0) {
            UIHelper.showWarning(this, "只有待就诊状态的挂号才能标记为已完成");
            return;
        }
        if (!UIHelper.showConfirm(this, "确认将该挂号标记为「已完成」？")) return;
        Result<Void> result = regController.completeRegistration(reg.getId());
        if (result.isSuccess()) {
            UIHelper.showSuccess(this, "已标记为已完成");
            doSearch();
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }

    private void doCancel() {
        Registration reg = getSelectedRegistration();
        if (reg == null) return;
        if (reg.getStatus() != null && reg.getStatus() != 0) {
            UIHelper.showWarning(this, "只有待就诊状态的挂号才能取消");
            return;
        }
        if (!UIHelper.showConfirm(this, "确认取消该挂号？取消后将释放号源。")) return;
        Result<Void> result = regController.cancelRegistration(reg.getId());
        if (result.isSuccess()) {
            UIHelper.showSuccess(this, "取消成功");
            doSearch();
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }

    private void doUpdateStatus() {
        Registration reg = getSelectedRegistration();
        if (reg == null) return;
        Integer newStatus = showStatusSelectDialog(reg.getStatus());
        if (newStatus == null) return;
        Result<Void> result = regController.updateStatus(reg.getId(), newStatus, null);
        if (result.isSuccess()) {
            UIHelper.showSuccess(this, "状态已更新");
            doSearch();
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }

    /** 弹窗选择新状态，返回 0/1/2 或 null（取消） */
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
        backBtn.addActionListener(e -> mainFrame.showAdminMain());
        topBar.add(backBtn, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("挂号记录", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);

        return topBar;
    }

    private void doSearch() {
        Department dept = (Department) deptComboBox.getSelectedItem();
        int statusIndex = statusComboBox.getSelectedIndex();

        LocalDate date = null;
        if (dateFilterCheck != null && dateFilterCheck.isSelected() && datePicker != null) {
            date = datePicker.getSelectedDate();
        }

        Long deptId = null;
        if (dept != null && deptComboBox.getSelectedIndex() > 0) {
            deptId = dept.getId();
        }

        Integer status = null;
        if (statusIndex > 0) {
            status = statusIndex - 1;
        }

        loadData(date, deptId, status);
    }

    private void doReset() {
        if (dateFilterCheck != null) dateFilterCheck.setSelected(false);
        if (datePicker != null) datePicker.setSelectedDate(LocalDate.now());
        if (deptComboBox.getItemCount() > 0) {
            deptComboBox.setSelectedIndex(0);
        }
        statusComboBox.setSelectedIndex(0);
        loadData(null, null, null);
    }

    private void loadData(LocalDate date, Long deptId, Integer status) {
        tableModel.setRowCount(0);
        registrationList.clear();
        Result<List<Registration>> result = regController.searchRegistrations(date, deptId, status);
        if (result.isSuccess() && result.getData() != null) {
            registrationList.addAll(result.getData());
            for (Registration r : result.getData()) {
                tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getPatientName(),
                    r.getDepartmentName(),
                    r.getDoctorName(),
                    r.getScheduleDate(),
                    r.getTimeSlotText(),
                    r.getStatusText(),
                    r.getCreateTime() != null ? r.getCreateTime().toString().replace("T", " ") : ""
                });
            }
        }
    }

    @Override
    public void refresh() {
        // 刷新科室下拉框
        deptComboBox.removeAllItems();
        deptComboBox.addItem(null); // 全部
        Result<List<Department>> deptResult = deptController.getAllDepartments();
        if (deptResult.isSuccess() && deptResult.getData() != null) {
            for (Department dept : deptResult.getData()) {
                deptComboBox.addItem(dept);
            }
        }
        
        // 加载所有数据
        loadData(null, null, null);
    }
}
