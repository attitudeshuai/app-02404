package com.hospital.ui.dialog;

import com.hospital.entity.Department;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * 科室添加/编辑弹窗
 */
public class DepartmentEditDialog extends JDialog {
    private final JTextField nameField;
    private final JTextField descField;
    private Department result;

    public DepartmentEditDialog(Frame parent, Department existing) {
        super(parent, existing == null ? "添加科室" : "编辑科室", true);
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        content.setBackground(UIConstants.CARD_COLOR);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 12));
        form.setBackground(UIConstants.CARD_COLOR);
        form.add(UIHelper.createLabel("科室名称："));
        nameField = UIHelper.createTextField();
        nameField.setPreferredSize(new Dimension(220, 28));
        form.add(nameField);
        form.add(UIHelper.createLabel("科室描述："));
        descField = UIHelper.createTextField();
        descField.setPreferredSize(new Dimension(220, 28));
        form.add(descField);
        content.add(form, BorderLayout.CENTER);

        if (existing != null) {
            nameField.setText(existing.getName());
            descField.setText(existing.getDescription() != null ? existing.getDescription() : "");
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

    private void onConfirm(Department existing) {
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String err = UIHelper.validateRequired(name, "科室名称");
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validateMaxLength(name, 50, "科室名称");
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validateMaxLength(desc, 200, "科室描述");
        if (err != null) { UIHelper.showError(this, err); return; }
        result = new Department(name, desc.isEmpty() ? null : desc);
        if (existing != null) result.setId(existing.getId());
        dispose();
    }

    /** 确定后返回要保存的实体，取消返回 null */
    public Department getResult() {
        return result;
    }
}
