package com.hospital.service;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.UserDAO;
import com.hospital.entity.Doctor;
import com.hospital.entity.User;
import com.hospital.exception.BusinessException;
import com.hospital.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 医生服务类
 */
public class DoctorService {
    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final UserDAO userDAO = new UserDAO();

    public List<Doctor> getAllDoctors() {
        return doctorDAO.findAll();
    }

    public List<Doctor> getDoctorsByDepartment(Long deptId) {
        return doctorDAO.findByDepartmentId(deptId);
    }

    public Doctor getDoctorById(Long id) {
        return doctorDAO.findById(id);
    }

    /** 根据登录用户ID获取医生信息（医生角色登录后使用） */
    public Doctor getDoctorByUserId(Long userId) {
        return doctorDAO.findByUserId(userId);
    }

    public void addDoctor(Doctor doctor) throws BusinessException {
        validateDoctor(doctor);
        if (doctor.getBindUsername() != null && !doctor.getBindUsername().trim().isEmpty()) {
            createAndBindUser(doctor);
        }
        int rows = doctorDAO.insert(doctor);
        if (rows <= 0) {
            throw new BusinessException("添加医生失败");
        }
        if (doctor.getUserId() != null) {
            logger.info("医生账号已绑定: doctorId={}, username={}", doctor.getId(), doctor.getBindUsername());
        }
    }

    public void updateDoctor(Doctor doctor) throws BusinessException {
        if (doctor.getId() == null) {
            throw new BusinessException("医生ID不能为空");
        }
        validateDoctor(doctor);
        if (doctor.getUserId() == null && doctor.getBindUsername() != null && !doctor.getBindUsername().trim().isEmpty()) {
            createAndBindUser(doctor);
        }
        int rows = doctorDAO.update(doctor);
        if (rows <= 0) {
            throw new BusinessException("更新医生失败");
        }
    }

    /** 创建医生登录账号并绑定到 doctor.userId */
    private void createAndBindUser(Doctor doctor) throws BusinessException {
        String username = doctor.getBindUsername().trim();
        String password = doctor.getBindPassword() != null ? doctor.getBindPassword() : "";
        if (username.length() < 3 || username.length() > 20) {
            throw new BusinessException("绑定登录名长度应为3-20个字符");
        }
        if (password.length() < 6) {
            throw new BusinessException("绑定账号时密码不能少于6位");
        }
        if (userDAO.existsByUsername(username)) {
            throw new BusinessException("该登录名已被使用");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hashPassword(password));
        user.setRealName(doctor.getName());
        user.setRole(2);
        int r = userDAO.insert(user);
        if (r <= 0 || user.getId() == null) {
            throw new BusinessException("创建医生账号失败");
        }
        doctor.setUserId(user.getId());
        if (doctor.getId() != null) {
            logger.info("医生账号已绑定: doctorId={}, username={}", doctor.getId(), username);
        }
    }

    public void deleteDoctor(Long id) throws BusinessException {
        if (id == null) {
            throw new BusinessException("医生ID不能为空");
        }

        if (doctorDAO.hasSchedule(id)) {
            throw new BusinessException("该医生存在排班记录，无法删除");
        }

        int rows = doctorDAO.delete(id);
        if (rows <= 0) {
            throw new BusinessException("删除医生失败");
        }
    }

    private void validateDoctor(Doctor doctor) throws BusinessException {
        if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
            throw new BusinessException("医生姓名不能为空");
        }
        if (doctor.getName().length() > 50) {
            throw new BusinessException("医生姓名不能超过50个字符");
        }
        if (doctor.getDepartmentId() == null) {
            throw new BusinessException("请选择所属科室");
        }
    }
}
