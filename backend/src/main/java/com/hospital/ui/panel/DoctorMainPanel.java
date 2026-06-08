package com.hospital.ui.panel;

import com.hospital.entity.Doctor;
import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 医生主界面
 */
public class DoctorMainPanel extends JPanel {
    private final MainFrame mainFrame;

    public DoctorMainPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(UIConstants.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        cardsPanel.setBackground(UIConstants.BG_COLOR);

        JPanel regCard = createFunctionCard("我的挂号记录", "查看本人排班下的挂号并修改状态",
            e -> mainFrame.showDoctorRegistration());
        cardsPanel.add(regCard);

        contentPanel.add(cardsPanel);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIConstants.PRIMARY_COLOR);
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20));

        Doctor doctor = mainFrame.getCurrentDoctor();
        String name = doctor != null ? doctor.getName() : "医生";
        JLabel titleLabel = new JLabel("医院门诊挂号系统 - 医生端（" + name + "）");
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.setFont(UIConstants.SMALL_FONT);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(UIConstants.PRIMARY_DARK);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> mainFrame.logout());
        rightPanel.add(logoutBtn);
        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createFunctionCard(String title, String desc, java.awt.event.ActionListener action) {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(280, 200));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel titleLabel = UIHelper.createSubtitleLabel(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(UIConstants.PRIMARY_COLOR);
        card.add(Box.createVerticalStrut(30));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(20));
        JLabel descLabel = new JLabel("<html><center>" + desc + "</center></html>");
        descLabel.setFont(UIConstants.NORMAL_FONT);
        descLabel.setForeground(UIConstants.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(descLabel);
        card.add(Box.createVerticalStrut(30));
        JButton enterBtn = UIHelper.createPrimaryButton("进入");
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterBtn.addActionListener(action);
        card.add(enterBtn);
        return card;
    }
}
