package com.hospital.service;

import com.hospital.dao.UserDAO;
import com.hospital.entity.User;
import com.hospital.exception.BusinessException;
import com.hospital.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 用户服务类
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAO());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // 身份证号正则：18位，最后一位可以是X
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");
    // 手机号正则：11位，以1开头
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            logger.warn("登录失败: username={}", username);
            return null;
        }

        logger.info("用户登录成功: username={}, role={}", username, user.getRole());
        return user;
    }

    public void register(User user) throws BusinessException {
        validateUser(user);

        if (userDAO.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        user.setRole(0); // 默认为患者
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        int rows = userDAO.insert(user);
        if (rows <= 0) {
            throw new BusinessException("注册失败，请稍后重试");
        }
    }

    private void validateUser(User user) throws BusinessException {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            throw new BusinessException("用户名长度应为3-20个字符");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (user.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        if (user.getRealName() == null || user.getRealName().trim().isEmpty()) {
            throw new BusinessException("姓名不能为空");
        }
        if (user.getIdCard() != null && !user.getIdCard().isEmpty()) {
            if (!isValidIdCard(user.getIdCard())) {
                throw new BusinessException("身份证号格式不正确");
            }
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            if (!isValidPhone(user.getPhone())) {
                throw new BusinessException("手机号格式不正确");
            }
        }
    }

    public boolean isValidIdCard(String idCard) {
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }
        // 简单校验：检查前17位是否为数字
        return true;
    }

    public boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}
