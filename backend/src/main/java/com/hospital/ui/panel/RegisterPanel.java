package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.controller.UserController;
import com.hospital.entity.User;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * 注册面板
 */
public class RegisterPanel extends JPanel {
    private final MainFrame mainFrame;
    private final UserController userController = new UserController();
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField realNameField;
    private JTextField idCardField;
    private JTextField phoneField;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.BG_COLOR);

        JPanel card = UIHelper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(450, 550));

        // 标题
        JLabel titleLabel = UIHelper.createTitleLabel("患者注册");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(24));

        // 用户名
        usernameField = addInputField(card, "用 户 名：");
        usernameField.setToolTipText("3～20 个字符");
        card.add(Box.createVerticalStrut(12));

        // 密码
        JPanel pwdPanel = createInputPanel("密　　码：");
        passwordField = UIHelper.createPasswordField();
        passwordField.setMaximumSize(new Dimension(280, 36));
        passwordField.setToolTipText("至少 6 位");
        pwdPanel.add(passwordField);
        card.add(pwdPanel);
        card.add(Box.createVerticalStrut(12));

        // 确认密码
        JPanel confirmPanel = createInputPanel("确认密码：");
        confirmPasswordField = UIHelper.createPasswordField();
        confirmPasswordField.setMaximumSize(new Dimension(280, 36));
        confirmPanel.add(confirmPasswordField);
        card.add(confirmPanel);
        card.add(Box.createVerticalStrut(12));

        // 真实姓名
        realNameField = addInputField(card, "真实姓名：");
        realNameField.setToolTipText("必填，最多 50 字");
        card.add(Box.createVerticalStrut(12));

        // 身份证号
        idCardField = addInputField(card, "身份证号：");
        idCardField.setToolTipText("选填，15 位或 18 位");
        card.add(Box.createVerticalStrut(12));

        // 手机号
        phoneField = addInputField(card, "手 机 号：");
        phoneField.setToolTipText("选填，11 位数字");
        card.add(Box.createVerticalStrut(24));

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(UIConstants.CARD_COLOR);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton registerButton = UIHelper.createPrimaryButton("注 册");
        registerButton.addActionListener(e -> doRegister());
        buttonPanel.add(registerButton);

        JButton backButton = UIHelper.createSecondaryButton("返回登录");
        backButton.addActionListener(e -> {
            clearFields();
            mainFrame.showLogin();
        });
        buttonPanel.add(backButton);

        card.add(buttonPanel);

        add(card);
    }

    private JTextField addInputField(JPanel card, String labelText) {
        JPanel panel = createInputPanel(labelText);
        JTextField field = UIHelper.createTextField();
        field.setMaximumSize(new Dimension(280, 36));
        panel.add(field);
        card.add(panel);
        return field;
    }

    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(UIConstants.CARD_COLOR);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(380, 40));
        
        JLabel label = UIHelper.createLabel(labelText);
        label.setPreferredSize(new Dimension(80, 36));
        label.setMinimumSize(new Dimension(80, 36));
        label.setMaximumSize(new Dimension(80, 36));
        panel.add(label);
        
        return panel;
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String realName = realNameField.getText().trim();
        String idCard = idCardField.getText().trim();
        String phone = phoneField.getText().trim();

        String err = UIHelper.validateUsername(username);
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validateRequired(password, "密码");
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validatePasswordMin(password, "密码");
        if (err != null) { UIHelper.showError(this, err); return; }
        if (!password.equals(confirmPassword)) {
            UIHelper.showError(this, "两次输入的密码不一致");
            return;
        }
        err = UIHelper.validateRequired(realName, "真实姓名");
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validateMaxLength(realName, 50, "真实姓名");
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validateIdCard(idCard);
        if (err != null) { UIHelper.showError(this, err); return; }
        err = UIHelper.validatePhone(phone);
        if (err != null) { UIHelper.showError(this, err); return; }

        User user = new User(username, password, realName, idCard.isEmpty() ? null : idCard, phone.isEmpty() ? null : phone);
        Result<Void> result = userController.register(user);
        
        if (result.isSuccess()) {
            UIHelper.showSuccess(this, "注册成功，请登录");
            clearFields();
            mainFrame.showLogin();
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        realNameField.setText("");
        idCardField.setText("");
        phoneField.setText("");
    }
}
