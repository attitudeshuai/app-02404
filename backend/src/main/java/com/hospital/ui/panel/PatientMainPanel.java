package com.hospital.ui.panel;

import com.hospital.ui.MainFrame;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 患者主界面
 */
public class PatientMainPanel extends JPanel {
    private final MainFrame mainFrame;

    public PatientMainPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_COLOR);

        // 顶部栏
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // 中间内容区
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(UIConstants.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        cardsPanel.setBackground(UIConstants.BG_COLOR);

        // 挂号卡片
        JPanel bookingCard = createFunctionCard("挂号", "选择科室和医生进行挂号", 
            e -> mainFrame.showBooking());
        cardsPanel.add(bookingCard);

        // 我的挂号卡片
        JPanel myRegCard = createFunctionCard("我的挂号", "查看和管理我的挂号记录", 
            e -> mainFrame.showMyRegistration());
        cardsPanel.add(myRegCard);

        contentPanel.add(cardsPanel);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIConstants.PRIMARY_COLOR);
        topBar.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("医院门诊挂号系统 - 患者端");
        titleLabel.setFont(UIConstants.SUBTITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("欢迎您");
        welcomeLabel.setFont(UIConstants.NORMAL_FONT);
        welcomeLabel.setForeground(Color.WHITE);
        rightPanel.add(welcomeLabel);

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
