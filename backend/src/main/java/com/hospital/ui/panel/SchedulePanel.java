package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.common.TimeSlot;
import com.hospital.controller.DoctorController;
import com.hospital.controller.ScheduleController;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.ui.CalendarDatePicker;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;
import com.hospital.ui.dialog.ScheduleEditDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 排班管理面板：顶部筛选+添加，列表每行可编辑/删除（弹窗）
 */
public class SchedulePanel extends JPanel implements MainFrame.RefreshablePanel {
    private final MainFrame mainFrame;
    private final ScheduleController scheduleController = new ScheduleController();
    private final DoctorController doctorController = new DoctorController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<Doctor> doctorFilterCombo;
    private JCheckBox dateFilterCheck;
    private CalendarDatePicker dateFilterPicker;
    private List<Schedule> scheduleList = new ArrayList<>();

    public SchedulePanel(MainFrame mainFrame) {
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

        JPanel toolPanel = UIHelper.createCard();
        toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        toolPanel.add(UIHelper.createLabel("医生："));
        doctorFilterCombo = UIHelper.createComboBox();
        doctorFilterCombo.setPreferredSize(new Dimension(150, 32));
        toolPanel.add(doctorFilterCombo);
        dateFilterCheck = new JCheckBox("按日期");
        dateFilterCheck.setFont(UIConstants.NORMAL_FONT);
        dateFilterCheck.setBackground(UIConstants.CARD_COLOR);
        toolPanel.add(dateFilterCheck);
        dateFilterPicker = UIHelper.createCalendarDatePicker();
        dateFilterPicker.setPreferredSize(new Dimension(180, 32));
        toolPanel.add(dateFilterPicker);
        JButton searchBtn = UIHelper.createSecondaryButton("搜索");
        searchBtn.addActionListener(e -> loadData());
        toolPanel.add(searchBtn);
        JButton addBtn = UIHelper.createPrimaryButton("添加");
        addBtn.addActionListener(e -> doAdd());
        toolPanel.add(addBtn);
        contentPanel.add(toolPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "医生", "科室", "日期", "时段", "最大号源", "已挂号", "剩余", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column < 0 || column >= getColumnCount() || column == 8) return;
                super.setValueAt(aValue, row, column);
            }
        };
        table = new JTable(tableModel);
        UIHelper.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumn("操作").setCellRenderer(new OperationCellRenderer());
        table.getColumn("操作").setCellEditor(new OperationCellEditor(this::doEdit, this::doDelete));
        table.getColumn("操作").setPreferredWidth(120);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        JPanel tablePanel = UIHelper.createCard();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
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
        backBtn.addActionListener(e -> mainFrame.showAdminMain());
        topBar.add(backBtn, BorderLayout.WEST);
        JLabel titleLabel = new JLabel("排班管理", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);
        return topBar;
    }

    private List<Doctor> loadDoctors() {
        Result<List<Doctor>> r = doctorController.getAllDoctors();
        return (r.isSuccess() && r.getData() != null) ? r.getData() : new ArrayList<>();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        scheduleList.clear();
        Result<List<Schedule>> result = scheduleController.getAllSchedules();
        if (!result.isSuccess() || result.getData() == null) return;
        List<Schedule> list = result.getData();
        Doctor filterDoctor = (Doctor) doctorFilterCombo.getSelectedItem();
        if (filterDoctor != null && filterDoctor.getId() != null) {
            list = list.stream().filter(s -> filterDoctor.getId().equals(s.getDoctorId())).toList();
        }
        if (dateFilterCheck != null && dateFilterCheck.isSelected() && dateFilterPicker != null) {
            java.time.LocalDate filterDate = dateFilterPicker.getSelectedDate();
            list = list.stream().filter(s -> filterDate.equals(s.getScheduleDate())).toList();
        }
        scheduleList.addAll(list);
        for (Schedule s : list) {
            tableModel.addRow(new Object[]{
                s.getId(), s.getDoctorName(), s.getDepartmentName(),
                s.getScheduleDate(), s.getTimeSlotText(),
                s.getMaxCount(), s.getCurrentCount(), s.getAvailableCount(), s
            });
        }
    }

    private void doAdd() {
        List<Doctor> doctors = loadDoctors();
        if (doctors.isEmpty()) {
            UIHelper.showWarning(this, "请先添加医生");
            return;
        }
        ScheduleEditDialog dlg = new ScheduleEditDialog(mainFrame, null, doctors);
        dlg.setVisible(true);
        Schedule result = dlg.getResult();
        if (result == null) return;
        Result<Void> r = scheduleController.addSchedule(result);
        if (r.isSuccess()) {
            UIHelper.showSuccess(this, "添加成功");
            loadData();
        } else {
            UIHelper.showError(this, r.getMessage());
        }
    }

    void doEdit(Schedule schedule) {
        if (schedule == null) return;
        ScheduleEditDialog dlg = new ScheduleEditDialog(mainFrame, schedule, loadDoctors());
        dlg.setVisible(true);
        Schedule result = dlg.getResult();
        if (result == null) return;
        Result<Void> r = scheduleController.updateSchedule(result);
        if (r.isSuccess()) {
            UIHelper.showSuccess(this, "更新成功");
            loadData();
        } else {
            UIHelper.showError(this, r.getMessage());
        }
    }

    void doDelete(Schedule schedule) {
        if (schedule == null) return;
        if (!UIHelper.showConfirm(this, "确认删除该排班？")) return;
        Result<Void> r = scheduleController.deleteSchedule(schedule.getId());
        if (r.isSuccess()) {
            UIHelper.showSuccess(this, "删除成功");
            loadData();
        } else {
            UIHelper.showError(this, r.getMessage());
        }
    }

    Schedule getScheduleAtRow(int row) {
        if (row < 0 || row >= scheduleList.size()) return null;
        return scheduleList.get(row);
    }

    @Override
    public void refresh() {
        doctorFilterCombo.removeAllItems();
        doctorFilterCombo.addItem(null);
        for (Doctor d : loadDoctors()) {
            doctorFilterCombo.addItem(d);
        }
        doctorFilterCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "全部" : ((Doctor) value).getName());
            l.setFont(UIConstants.NORMAL_FONT);
            if (isSelected) {
                l.setBackground(list.getSelectionBackground());
                l.setForeground(list.getSelectionForeground());
            } else {
                l.setBackground(list.getBackground());
                l.setForeground(list.getForeground());
            }
            l.setOpaque(true);
            return l;
        });
        if (dateFilterCheck != null) dateFilterCheck.setSelected(false);
        if (dateFilterPicker != null) dateFilterPicker.setSelectedDate(java.time.LocalDate.now());
        loadData();
    }

    private static class OperationCellRenderer implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            p.setBorder(new MatteBorder(0, 0, 1, 1, UIConstants.BORDER_COLOR));
            JButton editBtn = new JButton("编辑");
            editBtn.setFont(UIConstants.SMALL_FONT);
            JButton delBtn = new JButton("删除");
            delBtn.setFont(UIConstants.SMALL_FONT);
            delBtn.setForeground(UIConstants.ERROR_COLOR);
            p.add(editBtn);
            p.add(delBtn);
            return p;
        }
    }

    private class OperationCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        private final JButton editBtn = new JButton("编辑");
        private final JButton delBtn = new JButton("删除");
        private int editingRow = -1;
        private final java.util.function.Consumer<Schedule> onEdit;
        private final java.util.function.Consumer<Schedule> onDelete;

        OperationCellEditor(java.util.function.Consumer<Schedule> onEdit, java.util.function.Consumer<Schedule> onDelete) {
            this.onEdit = onEdit;
            this.onDelete = onDelete;
            panel.setBorder(new MatteBorder(0, 0, 1, 1, UIConstants.BORDER_COLOR));
            editBtn.setFont(UIConstants.SMALL_FONT);
            delBtn.setFont(UIConstants.SMALL_FONT);
            delBtn.setForeground(UIConstants.ERROR_COLOR);
            editBtn.addActionListener(e -> {
                Schedule s = getScheduleAtRow(editingRow);
                if (s != null) onEdit.accept(s);
                stopCellEditing();
            });
            delBtn.addActionListener(e -> {
                Schedule s = getScheduleAtRow(editingRow);
                if (s != null) onDelete.accept(s);
                stopCellEditing();
            });
            panel.add(editBtn);
            panel.add(delBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
            editingRow = row;
            panel.setBackground(t.getBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }
}
