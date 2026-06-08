package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.controller.DepartmentController;
import com.hospital.controller.DoctorController;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;
import com.hospital.ui.dialog.DoctorEditDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生管理面板：顶部搜索+添加，列表每行可编辑/删除（弹窗）
 */
public class DoctorPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final MainFrame mainFrame;
    private final DoctorController doctorController = new DoctorController();
    private final DepartmentController deptController = new DepartmentController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Doctor> doctorList = new ArrayList<>();

    public DoctorPanel(MainFrame mainFrame) {
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
        toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolPanel.add(UIHelper.createLabel("医生姓名："));
        searchField = UIHelper.createTextField();
        searchField.setPreferredSize(new Dimension(160, 32));
        searchField.setToolTipText("留空显示全部");
        toolPanel.add(searchField);
        JButton searchBtn = UIHelper.createSecondaryButton("搜索");
        searchBtn.addActionListener(e -> loadData());
        toolPanel.add(searchBtn);
        JButton addBtn = UIHelper.createPrimaryButton("添加");
        addBtn.addActionListener(e -> doAdd());
        toolPanel.add(addBtn);
        contentPanel.add(toolPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "姓名", "职称", "所属科室", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column < 0 || column >= getColumnCount() || column == 4) return;
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
        JLabel titleLabel = new JLabel("医生管理", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);
        return topBar;
    }

    private List<Department> loadDepartments() {
        Result<List<Department>> r = deptController.getAllDepartments();
        return (r.isSuccess() && r.getData() != null) ? r.getData() : new ArrayList<>();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        doctorList.clear();
        Result<List<Doctor>> result = doctorController.getAllDoctors();
        if (!result.isSuccess() || result.getData() == null) return;
        String keyword = searchField.getText().trim().toLowerCase();
        List<Doctor> list = result.getData();
        if (!keyword.isEmpty()) {
            list = list.stream()
                .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        }
        doctorList.addAll(list);
        for (Doctor d : list) {
            tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getTitle(), d.getDepartmentName(), d});
        }
    }

    private void doAdd() {
        DoctorEditDialog dlg = new DoctorEditDialog(mainFrame, null, loadDepartments());
        dlg.setVisible(true);
        Doctor result = dlg.getResult();
        if (result == null) return;
        Result<Void> r = doctorController.addDoctor(result);
        if (r.isSuccess()) {
            UIHelper.showSuccess(this, "添加成功");
            loadData();
        } else {
            UIHelper.showError(this, r.getMessage());
        }
    }

    void doEdit(Doctor doctor) {
        if (doctor == null) return;
        DoctorEditDialog dlg = new DoctorEditDialog(mainFrame, doctor, loadDepartments());
        dlg.setVisible(true);
        Doctor result = dlg.getResult();
        if (result == null) return;
        Result<Void> r = doctorController.updateDoctor(result);
        if (r.isSuccess()) {
            UIHelper.showSuccess(this, "更新成功");
            loadData();
        } else {
            UIHelper.showError(this, r.getMessage());
        }
    }

    void doDelete(Doctor doctor) {
        if (doctor == null) return;
        if (!UIHelper.showConfirm(this, "确认删除该医生？")) return;
        Result<Void> r = doctorController.deleteDoctor(doctor.getId());
        if (r.isSuccess()) {
            UIHelper.showSuccess(this, "删除成功");
            loadData();
        } else {
            UIHelper.showError(this, r.getMessage());
        }
    }

    Doctor getDoctorAtRow(int row) {
        if (row < 0 || row >= doctorList.size()) return null;
        return doctorList.get(row);
    }

    @Override
    public void refresh() {
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
        private final java.util.function.Consumer<Doctor> onEdit;
        private final java.util.function.Consumer<Doctor> onDelete;

        OperationCellEditor(java.util.function.Consumer<Doctor> onEdit, java.util.function.Consumer<Doctor> onDelete) {
            this.onEdit = onEdit;
            this.onDelete = onDelete;
            panel.setBorder(new MatteBorder(0, 0, 1, 1, UIConstants.BORDER_COLOR));
            editBtn.setFont(UIConstants.SMALL_FONT);
            delBtn.setFont(UIConstants.SMALL_FONT);
            delBtn.setForeground(UIConstants.ERROR_COLOR);
            editBtn.addActionListener(e -> {
                Doctor d = getDoctorAtRow(editingRow);
                if (d != null) onEdit.accept(d);
                stopCellEditing();
            });
            delBtn.addActionListener(e -> {
                Doctor d = getDoctorAtRow(editingRow);
                if (d != null) onDelete.accept(d);
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
