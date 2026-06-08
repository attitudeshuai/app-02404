package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.controller.DepartmentController;
import com.hospital.controller.DoctorController;
import com.hospital.controller.RegistrationController;
import com.hospital.controller.ScheduleController;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 挂号面板
 */
public class BookingPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final MainFrame mainFrame;
    private final DepartmentController deptController = new DepartmentController();
    private final DoctorController doctorController = new DoctorController();
    private final ScheduleController scheduleController = new ScheduleController();
    private final RegistrationController regController = new RegistrationController();

    private JComboBox<Department> deptComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private List<Schedule> scheduleList;

    public BookingPanel(MainFrame mainFrame) {
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

        // 选择区域
        JPanel selectPanel = UIHelper.createCard();
        selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));

        selectPanel.add(UIHelper.createLabel("选择科室："));
        deptComboBox = UIHelper.createComboBox();
        deptComboBox.setPreferredSize(new Dimension(150, 32));
        deptComboBox.addActionListener(e -> onDeptSelected());
        selectPanel.add(deptComboBox);

        selectPanel.add(Box.createHorizontalStrut(20));
        selectPanel.add(UIHelper.createLabel("选择医生："));
        doctorComboBox = UIHelper.createComboBox();
        doctorComboBox.setPreferredSize(new Dimension(150, 32));
        doctorComboBox.addActionListener(e -> onDoctorSelected());
        selectPanel.add(doctorComboBox);

        contentPanel.add(selectPanel, BorderLayout.NORTH);

        // 排班表格
        JPanel tablePanel = UIHelper.createCard();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            new EmptyBorder(10, 10, 10, 10)
        ));

        String[] columns = {"日期", "时段", "最大号源", "已挂号", "剩余号源"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(tableModel);
        UIHelper.styleTable(scheduleTable);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 挂号按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIConstants.CARD_COLOR);
        JButton bookBtn = UIHelper.createPrimaryButton("确认挂号");
        bookBtn.addActionListener(e -> doBooking());
        buttonPanel.add(bookBtn);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(UIConstants.BG_COLOR);
        centerWrapper.setBorder(new EmptyBorder(20, 0, 0, 0));
        centerWrapper.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(centerWrapper, BorderLayout.CENTER);

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

        JLabel titleLabel = new JLabel("门诊挂号", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);

        return topBar;
    }

    private void onDeptSelected() {
        Department dept = (Department) deptComboBox.getSelectedItem();
        if (dept == null) return;

        doctorComboBox.removeAllItems();
        tableModel.setRowCount(0);

        Result<List<Doctor>> result = doctorController.getDoctorsByDepartment(dept.getId());
        if (result.isSuccess() && result.getData() != null) {
            for (Doctor doctor : result.getData()) {
                doctorComboBox.addItem(doctor);
            }
        }
    }

    private void onDoctorSelected() {
        Doctor doctor = (Doctor) doctorComboBox.getSelectedItem();
        if (doctor == null) return;

        tableModel.setRowCount(0);
        Result<List<Schedule>> result = scheduleController.getAvailableSchedules(doctor.getId());
        if (result.isSuccess() && result.getData() != null) {
            scheduleList = result.getData();
            for (Schedule s : scheduleList) {
                tableModel.addRow(new Object[]{
                    s.getScheduleDate(),
                    s.getTimeSlotText(),
                    s.getMaxCount(),
                    s.getCurrentCount(),
                    s.getAvailableCount()
                });
            }
        }
    }

    private void doBooking() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow < 0) {
            UIHelper.showWarning(this, "请选择一个排班时段");
            return;
        }

        if (scheduleList == null || selectedRow >= scheduleList.size()) {
            return;
        }

        Schedule schedule = scheduleList.get(selectedRow);
        if (schedule.getAvailableCount() <= 0) {
            UIHelper.showError(this, "该时段号源已满");
            return;
        }

        if (!UIHelper.showConfirm(this, "确认挂号？\n医生：" + schedule.getDoctorName() + 
                "\n日期：" + schedule.getScheduleDate() + " " + schedule.getTimeSlotText())) {
            return;
        }

        Long userId = mainFrame.getCurrentUser().getId();
        Result<Void> result = regController.createRegistration(userId, schedule.getId());
        if (result.isSuccess()) {
            UIHelper.showSuccess(this, "挂号成功！");
            onDoctorSelected(); // 刷新表格
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }

    @Override
    public void refresh() {
        deptComboBox.removeAllItems();
        doctorComboBox.removeAllItems();
        tableModel.setRowCount(0);

        Result<List<Department>> result = deptController.getAllDepartments();
        if (result.isSuccess() && result.getData() != null) {
            for (Department dept : result.getData()) {
                deptComboBox.addItem(dept);
            }
        }
    }
}
