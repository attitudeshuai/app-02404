package com.hospital.ui;

import com.hospital.entity.Doctor;
import com.hospital.entity.User;
import com.hospital.ui.panel.*;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗口框架
 */
public class MainFrame extends JFrame {
    private static final String LOGIN_PANEL = "login";
    private static final String REGISTER_PANEL = "register";
    private static final String PATIENT_MAIN_PANEL = "patientMain";
    private static final String ADMIN_MAIN_PANEL = "adminMain";
    private static final String BOOKING_PANEL = "booking";
    private static final String MY_REGISTRATION_PANEL = "myRegistration";
    private static final String DEPARTMENT_PANEL = "department";
    private static final String DOCTOR_PANEL = "doctor";
    private static final String SCHEDULE_PANEL = "schedule";
    private static final String ADMIN_REGISTRATION_PANEL = "adminRegistration";
    private static final String DOCTOR_MAIN_PANEL = "doctorMain";
    private static final String DOCTOR_REGISTRATION_PANEL = "doctorRegistration";

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private User currentUser;
    private Doctor currentDoctor;

    public MainFrame() {
        initFrame();
        initPanels();
    }

    private void initFrame() {
        setTitle("医院门诊挂号系统");
        setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 700));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(UIConstants.BG_COLOR);
        add(mainPanel);
    }

    private void initPanels() {
        addPanel(new LoginPanel(this), LOGIN_PANEL);
        addPanel(new RegisterPanel(this), REGISTER_PANEL);
        addPanel(new PatientMainPanel(this), PATIENT_MAIN_PANEL);
        addPanel(new AdminMainPanel(this), ADMIN_MAIN_PANEL);
        addPanel(new BookingPanel(this), BOOKING_PANEL);
        addPanel(new MyRegistrationPanel(this), MY_REGISTRATION_PANEL);
        addPanel(new DepartmentPanel(this), DEPARTMENT_PANEL);
        addPanel(new DoctorPanel(this), DOCTOR_PANEL);
        addPanel(new SchedulePanel(this), SCHEDULE_PANEL);
        addPanel(new AdminRegistrationPanel(this), ADMIN_REGISTRATION_PANEL);
        addPanel(new DoctorMainPanel(this), DOCTOR_MAIN_PANEL);
        addPanel(new DoctorRegistrationPanel(this), DOCTOR_REGISTRATION_PANEL);
    }

    private void addPanel(Component panel, String cardName) {
        panel.setName(cardName);
        mainPanel.add(panel, cardName);
    }

    public void showLogin() {
        cardLayout.show(mainPanel, LOGIN_PANEL);
    }

    public void showRegister() {
        cardLayout.show(mainPanel, REGISTER_PANEL);
    }

    public void showPatientMain() {
        cardLayout.show(mainPanel, PATIENT_MAIN_PANEL);
    }

    public void showAdminMain() {
        cardLayout.show(mainPanel, ADMIN_MAIN_PANEL);
    }

    public void showBooking() {
        refreshPanel(BOOKING_PANEL);
        cardLayout.show(mainPanel, BOOKING_PANEL);
    }

    public void showMyRegistration() {
        refreshPanel(MY_REGISTRATION_PANEL);
        cardLayout.show(mainPanel, MY_REGISTRATION_PANEL);
    }

    public void showDepartment() {
        refreshPanel(DEPARTMENT_PANEL);
        cardLayout.show(mainPanel, DEPARTMENT_PANEL);
    }

    public void showDoctor() {
        refreshPanel(DOCTOR_PANEL);
        cardLayout.show(mainPanel, DOCTOR_PANEL);
    }

    public void showSchedule() {
        refreshPanel(SCHEDULE_PANEL);
        cardLayout.show(mainPanel, SCHEDULE_PANEL);
    }

    public void showAdminRegistration() {
        refreshPanel(ADMIN_REGISTRATION_PANEL);
        cardLayout.show(mainPanel, ADMIN_REGISTRATION_PANEL);
    }

    public void showDoctorMain() {
        cardLayout.show(mainPanel, DOCTOR_MAIN_PANEL);
    }

    public void showDoctorRegistration() {
        refreshPanel(DOCTOR_REGISTRATION_PANEL);
        cardLayout.show(mainPanel, DOCTOR_REGISTRATION_PANEL);
    }

    private void refreshPanel(String panelName) {
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof RefreshablePanel
                    && panelName != null && panelName.equals(comp.getName())) {
                ((RefreshablePanel) comp).refresh();
                return;
            }
        }
    }

    public void logout() {
        currentUser = null;
        currentDoctor = null;
        showLogin();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public Doctor getCurrentDoctor() {
        return currentDoctor;
    }

    public void setCurrentDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
    }

    public interface RefreshablePanel {
        void refresh();
    }
}
