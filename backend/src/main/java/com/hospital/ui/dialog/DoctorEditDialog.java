package com.hospital.ui.dialog;

import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 医生添加/编辑弹窗
 */
public class DoctorEditDialog extends JDialog {
    private final JTextField nameField;
    private final JTextField titleField;
    private final JComboBox<Department> deptComboBox;
    private final JTextField bindUsernameField;
    private final JPasswordField bindPasswordField;
    private final boolean allowBind;
    private Doctor result;

    public DoctorEditDialog(Frame parent, Doctor existing, List<Department> departments) {
        super(parent, existing == null ? "添加医生" : "编辑医生", true);
        allowBind = existing == null || existing.getUserId() == null;
        setSize(420, allowBind ? 300 : 220);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        content.setBackground(UIConstants.CARD_COLOR);

        int rows = allowBind ? 5 : 3;
        JPanel form = new JPanel(new GridLayout(rows, 2, 8, 12));
        form.setBackground(UIConstants.CARD_COLOR);
        form.add(UIHelper.createLabel("医生姓名："));
        nameField = UIHelper.createTextField();
        nameField.setPreferredSize(new Dimension(220, 28));
        form.add(nameField);
        form.add(UIHelper.createLabel("职称："));
        titleField = UIHelper.createTextField();
        titleField.setPreferredSize(new Dimension(220, 28));
        form.add(titleField);
        form.add(UIHelper.createLabel("所属科室："));
        deptComboBox = UIHelper.createComboBox();
        deptComboBox.setPreferredSize(new Dimension(220, 28));
        for (Department d : departments) deptComboBox.addItem(d);
        form.add(deptComboBox);
        if (allowBind) {
            form.add(UIHelper.createLabel("绑定登录名："));
            bindUsernameField = UIHelper.createTextField();
            bindUsernameField.setPreferredSize(new Dimension(220, 28));
            bindUsernameField.setToolTipText("选填，填写后该医生可用此账号登录");
            form.add(bindUsernameField);
            form.add(UIHelper.createLabel("登录密码："));
            bindPasswordField = UIHelper.createPasswordField();
            bindPasswordField.setPreferredSize(new Dimension(220, 28));
            bindPasswordField.setToolTipText("选填，与绑定登录名一起使用");
            form.add(bindPasswordField);
        } else {
            bindUsernameField = null;
            bindPasswordField = null;
        }
        content.add(form, BorderLayout.CENTER);

        if (existing != null) {
            nameField.setText(existing.getName());
            titleField.setText(existing.getTitle() != null ? existing.getTitle() : "");
            for (int i = 0; i < deptComboBox.getItemCount(); i++) {
                if (deptComboBox.getItemAt(i).getId().equals(existing.getDepartmentId())) {
                    deptComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(UIConstants.CARD_COLOR);
        JButton okBtn = UIHelper.createPrimaryButton("确定");
        okBtn.addActionListener(e -> onConfirm(existing));
        JButton cancelBtn = UIHelper.createSecondaryButton("取消");
        cancelBtn.addActionListener(e -> dispose());
        buttons.add(okBtn);
        buttons.add(cancelBtn);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void onConfirm(Doctor existing) {
        String name = nameField.getText().trim();
        String title = titleField.getText().trim();
        Department dept = (Department) deptComboBox.getSelectedItem();
        String err = UIHelper.validateRequired(name, "医生姓名");
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validateMaxLength(name, 50, "医生姓名");
        if (err != null) { UIHelper.showError(this, err); return; }
        if (title != null && title.length() > 50) {
            UIHelper.showError(this, "职称不能超过 50 个字符");
            return;
        }
        if (dept == null) {
            UIHelper.showError(this, "请选择所属科室");
            return;
        }
        result = new Doctor(name, title.isEmpty() ? null : title, dept.getId());
        if (existing != null) {
            result.setId(existing.getId());
            result.setUserId(existing.getUserId());
        }
        if (allowBind && bindUsernameField != null) {
            String u = bindUsernameField.getText().trim();
            String p = bindPasswordField.getPassword() != null ? new String(bindPasswordField.getPassword()) : "";
            if (!u.isEmpty()) {
                err = UIHelper.validateBindAccount(u, p);
                if (err != null) { UIHelper.showError(this, err); return; }
                result.setBindUsername(u);
                result.setBindPassword(p.isEmpty() ? null : p);
            }
        }
        dispose();
    }

    public Doctor getResult() {
        return result;
    }
}
