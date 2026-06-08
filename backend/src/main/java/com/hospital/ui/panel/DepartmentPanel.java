package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.controller.DepartmentController;
import com.hospital.entity.Department;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;
import com.hospital.ui.dialog.DepartmentEditDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 科室管理面板：顶部搜索+添加，列表每行可编辑/删除（弹窗）
 */
public class DepartmentPanel extends JPanel implements MainFrame.RefreshablePanel {
    private final MainFrame mainFrame;
    private final DepartmentController deptController = new DepartmentController();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Department> departmentList = new ArrayList<>();

    public DepartmentPanel(MainFrame mainFrame) {
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

        // 顶部：搜索 + 添加
        JPanel toolPanel = UIHelper.createCard();
        toolPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolPanel.add(UIHelper.createLabel("科室名称："));
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

        // 表格
        JPanel tablePanel = UIHelper.createCard();
        tablePanel.setLayout(new BorderLayout());
        String[] columns = {"ID", "科室名称", "科室描述", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                // 操作列仅用于按钮，不写入模型；避免列索引越界导致 ArrayIndexOutOfBoundsException
                if (column < 0 || column >= getColumnCount() || column == 3) return;
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
        JLabel titleLabel = new JLabel("科室管理", SwingConstants.CENTER);
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.CENTER);
        return topBar;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        departmentList.clear();
        Result<List<Department>> result = deptController.getAllDepartments();
        if (!result.isSuccess() || result.getData() == null) return;
        applyListToTable(result.getData());
    }

    /** 在后台拉取列表并刷新表格，完成后可显示成功提示（用于添加/编辑/删除后的刷新，避免界面卡顿） */
    private void loadDataInBackground() {
        loadDataInBackground(null);
    }

    private void loadDataInBackground(String successMessage) {
        new SwingWorker<Result<List<Department>>, Void>() {
            @Override
            protected Result<List<Department>> doInBackground() {
                return deptController.getAllDepartments();
            }
            @Override
            protected void done() {
                try {
                    Result<List<Department>> result = get();
                    if (result.isSuccess() && result.getData() != null) {
                        tableModel.setRowCount(0);
                        departmentList.clear();
                        applyListToTable(result.getData());
                        if (successMessage != null) {
                            UIHelper.showSuccess(DepartmentPanel.this, successMessage);
                        }
                    }
                } catch (Exception ignored) { }
            }
        }.execute();
    }

    private void applyListToTable(List<Department> list) {
        String keyword = searchField.getText().trim().toLowerCase();
        if (!keyword.isEmpty()) {
            list = list.stream()
                .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        }
        departmentList.addAll(list);
        for (Department d : list) {
            tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getDescription(), d});
        }
    }

    private void doAdd() {
        DepartmentEditDialog dlg = new DepartmentEditDialog(mainFrame, null);
        dlg.setVisible(true);
        Department toAdd = dlg.getResult();
        if (toAdd == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Result<?>, Void>() {
            @Override
            protected Result<?> doInBackground() {
                return deptController.addDepartment(toAdd);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Result<?> r = get();
                    if (r.isSuccess()) {
                        loadDataInBackground("添加成功");
                    } else {
                        UIHelper.showError(DepartmentPanel.this, r.getMessage());
                    }
                } catch (Exception e) {
                    setCursor(Cursor.getDefaultCursor());
                    UIHelper.showError(DepartmentPanel.this, "添加失败");
                }
            }
        }.execute();
    }

    void doEdit(Department dept) {
        if (dept == null) return;
        DepartmentEditDialog dlg = new DepartmentEditDialog(mainFrame, dept);
        dlg.setVisible(true);
        Department toUpdate = dlg.getResult();
        if (toUpdate == null) return;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Result<?>, Void>() {
            @Override
            protected Result<?> doInBackground() {
                return deptController.updateDepartment(toUpdate);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Result<?> r = get();
                    if (r.isSuccess()) {
                        loadDataInBackground("更新成功");
                    } else {
                        UIHelper.showError(DepartmentPanel.this, r.getMessage());
                    }
                } catch (Exception e) {
                    setCursor(Cursor.getDefaultCursor());
                    UIHelper.showError(DepartmentPanel.this, "更新失败");
                }
            }
        }.execute();
    }

    void doDelete(Department dept) {
        if (dept == null) return;
        if (!UIHelper.showConfirm(this, "确认删除该科室？")) return;
        Long idToDelete = dept.getId();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Result<?>, Void>() {
            @Override
            protected Result<?> doInBackground() {
                return deptController.deleteDepartment(idToDelete);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Result<?> r = get();
                    if (r.isSuccess()) {
                        loadDataInBackground("删除成功");
                    } else {
                        UIHelper.showError(DepartmentPanel.this, r.getMessage());
                    }
                } catch (Exception e) {
                    setCursor(Cursor.getDefaultCursor());
                    UIHelper.showError(DepartmentPanel.this, "删除失败");
                }
            }
        }.execute();
    }

    Department getDepartmentAtRow(int row) {
        if (row < 0 || row >= departmentList.size()) return null;
        return departmentList.get(row);
    }

    @Override
    public void refresh() {
        loadData();
    }

    /** 操作列渲染：显示 编辑 删除 */
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

    /** 操作列编辑器：点击后弹出编辑/删除 */
    private class OperationCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        private final JButton editBtn = new JButton("编辑");
        private final JButton delBtn = new JButton("删除");
        private int editingRow = -1;
        private final java.util.function.Consumer<Department> onEdit;
        private final java.util.function.Consumer<Department> onDelete;

        OperationCellEditor(java.util.function.Consumer<Department> onEdit, java.util.function.Consumer<Department> onDelete) {
            this.onEdit = onEdit;
            this.onDelete = onDelete;
            panel.setBorder(new MatteBorder(0, 0, 1, 1, UIConstants.BORDER_COLOR));
            editBtn.setFont(UIConstants.SMALL_FONT);
            delBtn.setFont(UIConstants.SMALL_FONT);
            delBtn.setForeground(UIConstants.ERROR_COLOR);
            editBtn.addActionListener(e -> {
                Department dept = getDepartmentAtRow(editingRow);
                if (dept != null) onEdit.accept(dept);
                stopCellEditing();
            });
            delBtn.addActionListener(e -> {
                Department dept = getDepartmentAtRow(editingRow);
                if (dept != null) onDelete.accept(dept);
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
