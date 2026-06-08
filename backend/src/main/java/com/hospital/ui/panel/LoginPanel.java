package com.hospital.ui.panel;

import com.hospital.common.Result;
import com.hospital.controller.DoctorController;
import com.hospital.controller.UserController;
import com.hospital.entity.Doctor;
import com.hospital.entity.User;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 登录面板
 */
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private final UserController userController = new UserController();
    private final DoctorController doctorController = new DoctorController();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton patientRadio;
    private JRadioButton adminRadio;
    private JRadioButton doctorRadio;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.BG_COLOR);

        JPanel card = UIHelper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 450));

        // 标题
        JLabel titleLabel = UIHelper.createTitleLabel("医院门诊挂号系统");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(30));

        // 用户名
        JPanel usernamePanel = createInputPanel("用户名：");
        usernameField = UIHelper.createTextField();
        usernameField.setMaximumSize(new Dimension(280, 36));
        usernameField.setToolTipText("3～20 个字符");
        usernamePanel.add(usernameField);
        card.add(usernamePanel);
        card.add(Box.createVerticalStrut(16));

        // 密码
        JPanel passwordPanel = createInputPanel("密　码：");
        passwordField = UIHelper.createPasswordField();
        passwordField.setMaximumSize(new Dimension(280, 36));
        passwordField.setToolTipText("至少 6 位");
        passwordPanel.add(passwordField);
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(16));

        // 角色选择
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rolePanel.setBackground(UIConstants.CARD_COLOR);
        rolePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        patientRadio = new JRadioButton("患者", true);
        adminRadio = new JRadioButton("管理员");
        doctorRadio = new JRadioButton("医生");
        patientRadio.setFont(UIConstants.NORMAL_FONT);
        adminRadio.setFont(UIConstants.NORMAL_FONT);
        doctorRadio.setFont(UIConstants.NORMAL_FONT);
        patientRadio.setBackground(UIConstants.CARD_COLOR);
        adminRadio.setBackground(UIConstants.CARD_COLOR);
        doctorRadio.setBackground(UIConstants.CARD_COLOR);

        ButtonGroup group = new ButtonGroup();
        group.add(patientRadio);
        group.add(adminRadio);
        group.add(doctorRadio);

        rolePanel.add(patientRadio);
        rolePanel.add(Box.createHorizontalStrut(15));
        rolePanel.add(adminRadio);
        rolePanel.add(Box.createHorizontalStrut(15));
        rolePanel.add(doctorRadio);
        card.add(rolePanel);
        card.add(Box.createVerticalStrut(24));

        // 登录按钮
        JButton loginButton = UIHelper.createPrimaryButton("登 录");
        loginButton.setMaximumSize(new Dimension(280, 40));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> doLogin());
        card.add(loginButton);
        card.add(Box.createVerticalStrut(16));

        // 注册链接
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        linkPanel.setBackground(UIConstants.CARD_COLOR);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel registerLabel = new JLabel("还没有账号？");
        registerLabel.setFont(UIConstants.SMALL_FONT);
        registerLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        JButton registerLink = new JButton("立即注册");
        registerLink.setFont(UIConstants.SMALL_FONT);
        registerLink.setForeground(UIConstants.PRIMARY_COLOR);
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> mainFrame.showRegister());
        
        linkPanel.add(registerLabel);
        linkPanel.add(registerLink);
        card.add(linkPanel);

        add(card);
    }

    private JPanel createInputPanel(String labelText) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(UIConstants.CARD_COLOR);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(320, 40));
        
        JLabel label = UIHelper.createLabel(labelText);
        label.setPreferredSize(new Dimension(70, 36));
        label.setMinimumSize(new Dimension(70, 36));
        label.setMaximumSize(new Dimension(70, 36));
        panel.add(label);
        
        return panel;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String err = UIHelper.validateUsername(username);
        if (err != null) {
            UIHelper.showError(this, err);
            return;
        }
        err = UIHelper.validateRequired(password, "密码");
        if (err != null) {
            UIHelper.showError(this, err);
            return;
        }

        Result<User> result = userController.login(username, password);
        if (result.isSuccess()) {
            User user = result.getData();
            mainFrame.setCurrentUser(user);
            
            // 清空输入
            usernameField.setText("");
            passwordField.setText("");
            
            // 根据所选角色跳转
            if (adminRadio.isSelected()) {
                if (user.isAdmin()) {
                    mainFrame.showAdminMain();
                } else {
                    UIHelper.showError(this, "该账号不是管理员");
                }
            } else if (doctorRadio.isSelected()) {
                if (user.isDoctor()) {
                    Result<Doctor> docResult = doctorController.getDoctorByUserId(user.getId());
                    if (docResult.isSuccess() && docResult.getData() != null) {
                        mainFrame.setCurrentDoctor(docResult.getData());
                        mainFrame.showDoctorMain();
                    } else {
                        UIHelper.showError(this, docResult != null ? docResult.getMessage() : "该账号未绑定医生信息");
                    }
                } else {
                    UIHelper.showError(this, "该账号不是医生账号");
                }
            } else {
                if (user.isPatient()) {
                    mainFrame.showPatientMain();
                } else {
                    UIHelper.showError(this, "该账号不是患者账号");
                }
            }
        } else {
            UIHelper.showError(this, result.getMessage());
        }
    }
}
