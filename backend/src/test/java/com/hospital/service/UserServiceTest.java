package com.hospital.service;

import com.hospital.dao.UserDAO;
import com.hospital.entity.User;
import com.hospital.exception.BusinessException;
import com.hospital.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务：登录/注册校验与异常处理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 业务逻辑与异常")
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userDAO);
    }

    @Nested
    @DisplayName("login")
    class Login {
        @Test
        void 用户名为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.login("", "pass"));
            assertEquals("用户名不能为空", ex.getMessage());
            verify(userDAO, never()).findByUsername(any());
        }

        @Test
        void 用户名为null应抛异常() {
            assertThrows(BusinessException.class, () -> service.login(null, "pass"));
        }

        @Test
        void 密码为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.login("user1", ""));
            assertEquals("密码不能为空", ex.getMessage());
        }

        @Test
        void 用户不存在应返回null() {
            when(userDAO.findByUsername("nobody")).thenReturn(null);
            assertNull(service.login("nobody", "any"));
        }

        @Test
        void 密码错误应返回null() {
            User user = new User();
            user.setUsername("u1");
            user.setPassword(PasswordUtil.hashPassword("correct"));
            when(userDAO.findByUsername("u1")).thenReturn(user);
            assertNull(service.login("u1", "wrong"));
        }

        @Test
        void 用户名密码正确应返回用户() {
            User user = new User();
            user.setUsername("u1");
            user.setPassword(PasswordUtil.hashPassword("correct"));
            user.setRole(0);
            when(userDAO.findByUsername("u1")).thenReturn(user);
            User result = service.login("u1", "correct");
            assertNotNull(result);
            assertEquals("u1", result.getUsername());
        }
    }

    @Nested
    @DisplayName("register")
    class Register {
        @Test
        void 用户名为空应抛异常() {
            User user = new User("", "pass123", "张三", null, null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertTrue(ex.getMessage().contains("用户名"));
        }

        @Test
        void 用户名长度不足3应抛异常() {
            User user = new User("ab", "pass123", "张三", null, null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertTrue(ex.getMessage().contains("3-20"));
        }

        @Test
        void 密码少于6位应抛异常() {
            User user = new User("user1", "12345", "张三", null, null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertTrue(ex.getMessage().contains("6"));
        }

        @Test
        void 姓名为空应抛异常() {
            User user = new User("user1", "pass123", "", null, null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertTrue(ex.getMessage().contains("姓名"));
        }

        @Test
        void 身份证格式错误应抛异常() {
            User user = new User("user1", "pass123", "张三", "123", null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertTrue(ex.getMessage().contains("身份证"));
        }

        @Test
        void 手机号格式错误应抛异常() {
            User user = new User("user1", "pass123", "张三", null, "138");
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertTrue(ex.getMessage().contains("手机号"));
        }

        @Test
        void 用户名已存在应抛异常() {
            User user = new User("exist", "pass123", "张三", null, null);
            when(userDAO.existsByUsername("exist")).thenReturn(true);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertEquals("用户名已存在", ex.getMessage());
            verify(userDAO, never()).insert(any());
        }

        @Test
        void insert返回0应抛异常() {
            User user = new User("newuser", "pass123", "张三", null, null);
            when(userDAO.existsByUsername("newuser")).thenReturn(false);
            when(userDAO.insert(any())).thenReturn(0);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.register(user));
            assertTrue(ex.getMessage().contains("注册失败"));
        }

        @Test
        void 注册时密码应转换为bcrypt哈希() {
            User user = new User("newuser", "pass123", "张三", null, null);
            when(userDAO.existsByUsername("newuser")).thenReturn(false);
            when(userDAO.insert(any())).thenReturn(1);

            assertDoesNotThrow(() -> service.register(user));
            assertNotEquals("pass123", user.getPassword());
            assertTrue(PasswordUtil.matches("pass123", user.getPassword()));
        }
    }

    @Nested
    @DisplayName("isValidIdCard / isValidPhone")
    class FormatValidation {
        @Test
        void 18位身份证末位X合法() {
            assertTrue(service.isValidIdCard("12345678901234567X"));
        }

        @Test
        void 15位数字身份证不合法() {
            assertFalse(service.isValidIdCard("123456789012345"));
        }

        @Test
        void 11位1开头手机号合法() {
            assertTrue(service.isValidPhone("13800138000"));
        }

        @Test
        void 非1开头手机号不合法() {
            assertFalse(service.isValidPhone("23800138000"));
        }
    }
}
