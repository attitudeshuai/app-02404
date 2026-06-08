package com.hospital.ui.dialog;

import com.hospital.common.TimeSlot;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.ui.CalendarDatePicker;
import com.hospital.ui.UIConstants;
import com.hospital.ui.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 排班添加/编辑弹窗
 */
public class ScheduleEditDialog extends JDialog {
    private final JComboBox<Doctor> doctorComboBox;
    private final CalendarDatePicker datePicker;
    private final JComboBox<TimeSlot> timeSlotComboBox;
    private final JTextField maxCountField;
    private Schedule result;

    public ScheduleEditDialog(Frame parent, Schedule existing, List<Doctor> doctors) {
        super(parent, existing == null ? "添加排班" : "编辑排班", true);
        setSize(420, 240);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        content.setBackground(UIConstants.CARD_COLOR);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 12));
        form.setBackground(UIConstants.CARD_COLOR);
        form.add(UIHelper.createLabel("医生："));
        doctorComboBox = UIHelper.createComboBox();
        doctorComboBox.setPreferredSize(new Dimension(220, 28));
        for (Doctor d : doctors) doctorComboBox.addItem(d);
        form.add(doctorComboBox);
        form.add(UIHelper.createLabel("日期："));
        datePicker = UIHelper.createCalendarDatePicker();
        datePicker.setPreferredSize(new Dimension(220, 32));
        form.add(datePicker);
        form.add(UIHelper.createLabel("时段："));
        timeSlotComboBox = new JComboBox<>(TimeSlot.values());
        timeSlotComboBox.setFont(UIConstants.NORMAL_FONT);
        timeSlotComboBox.setPreferredSize(new Dimension(220, 28));
        form.add(timeSlotComboBox);
        form.add(UIHelper.createLabel("最大号源："));
        maxCountField = UIHelper.createTextField();
        maxCountField.setPreferredSize(new Dimension(220, 28));
        maxCountField.setToolTipText("1～100 的整数");
        form.add(maxCountField);
        content.add(form, BorderLayout.CENTER);

        if (existing != null) {
            datePicker.setSelectedDate(existing.getScheduleDate());
            timeSlotComboBox.setSelectedItem(TimeSlot.fromCode(existing.getTimeSlot()));
            maxCountField.setText(String.valueOf(existing.getMaxCount()));
            for (int i = 0; i < doctorComboBox.getItemCount(); i++) {
                if (doctorComboBox.getItemAt(i).getId().equals(existing.getDoctorId())) {
                    doctorComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            datePicker.setSelectedDate(LocalDate.now());
            maxCountField.setText("20");
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

    private void onConfirm(Schedule existing) {
        Doctor doctor = (Doctor) doctorComboBox.getSelectedItem();
        LocalDate date = datePicker.getSelectedDate();
        TimeSlot timeSlot = (TimeSlot) timeSlotComboBox.getSelectedItem();
        String maxStr = maxCountField.getText().trim();
        if (doctor == null) {
            UIHelper.showError(this, "请选择医生");
            return;
        }
        int maxCount;
        try {
            maxCount = Integer.parseInt(maxStr);
            if (maxCount <= 0 || maxCount > 100) {
                UIHelper.showError(this, "最大号源请填写 1～100");
                return;
            }
        } catch (NumberFormatException e) {
            UIHelper.showError(this, "最大号源必须是数字");
            return;
        }
        result = new Schedule(doctor.getId(), date, timeSlot.getCode(), maxCount);
        if (existing != null) result.setId(existing.getId());
        dispose();
    }

    public Schedule getResult() {
        return result;
    }
}
