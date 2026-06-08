package com.hospital.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UIHelper 输入校验方法的单元测试
 */
@DisplayName("UIHelper 输入校验")
class UIHelperTest {

    @Nested
    @DisplayName("validateRequired")
    class ValidateRequired {
        @Test
        void null返回错误() {
            assertNotNull(UIHelper.validateRequired(null, "科室名称"));
            assertTrue(UIHelper.validateRequired(null, "科室名称").contains("科室名称"));
        }

        @Test
        void 空字符串或仅空白返回错误() {
            assertNotNull(UIHelper.validateRequired("", "用户名"));
            assertNotNull(UIHelper.validateRequired("  ", "密码"));
        }

        @Test
        void 非空返回null() {
            assertNull(UIHelper.validateRequired("abc", "用户名"));
            assertNull(UIHelper.validateRequired("  abc  ", "姓名"));
        }
    }

    @Nested
    @DisplayName("validateUsername")
    class ValidateUsername {
        @Test
        void 空或null返回错误() {
            assertNotNull(UIHelper.validateUsername(null));
            assertNotNull(UIHelper.validateUsername(""));
            assertNotNull(UIHelper.validateUsername("  "));
        }

        @Test
        void 少于3字符返回错误() {
            assertNotNull(UIHelper.validateUsername("ab"));
            assertNotNull(UIHelper.validateUsername("a"));
        }

        @Test
        void 超过20字符返回错误() {
            assertNotNull(UIHelper.validateUsername("a".repeat(21)));
        }

        @Test
        void 3到20字符返回null() {
            assertNull(UIHelper.validateUsername("abc"));
            assertNull(UIHelper.validateUsername("a".repeat(20)));
        }
    }

    @Nested
    @DisplayName("validatePasswordMin")
    class ValidatePasswordMin {
        @Test
        void null返回错误() {
            assertNotNull(UIHelper.validatePasswordMin(null, "密码"));
        }

        @Test
        void 少于6位返回错误() {
            assertNotNull(UIHelper.validatePasswordMin("12345", "密码"));
        }

        @Test
        void 至少6位返回null() {
            assertNull(UIHelper.validatePasswordMin("123456", "密码"));
            assertNull(UIHelper.validatePasswordMin("1234567", "密码"));
        }
    }

    @Nested
    @DisplayName("validateMaxLength")
    class ValidateMaxLength {
        @Test
        void null返回null() {
            assertNull(UIHelper.validateMaxLength(null, 50, "姓名"));
        }

        @Test
        void 超过最大长度返回错误() {
            assertNotNull(UIHelper.validateMaxLength("a".repeat(51), 50, "科室名称"));
        }

        @Test
        void 等于或小于最大长度返回null() {
            assertNull(UIHelper.validateMaxLength("a".repeat(50), 50, "科室名称"));
            assertNull(UIHelper.validateMaxLength("abc", 50, "科室名称"));
        }
    }

    @Nested
    @DisplayName("validateIdCard")
    class ValidateIdCard {
        @Test
        void 空或null返回null() {
            assertNull(UIHelper.validateIdCard(null));
            assertNull(UIHelper.validateIdCard(""));
            assertNull(UIHelper.validateIdCard("  "));
        }

        @Test
        void 15位数字通过() {
            assertNull(UIHelper.validateIdCard("123456789012345"));
        }

        @Test
        void 18位数字或末位X通过() {
            assertNull(UIHelper.validateIdCard("123456789012345678"));
            assertNull(UIHelper.validateIdCard("12345678901234567X"));
            assertNull(UIHelper.validateIdCard("12345678901234567x"));
        }

        @Test
        void 非法格式返回错误() {
            assertNotNull(UIHelper.validateIdCard("123"));
            assertNotNull(UIHelper.validateIdCard("12345678901234567A"));
        }
    }

    @Nested
    @DisplayName("validatePhone")
    class ValidatePhone {
        @Test
        void 空或null返回null() {
            assertNull(UIHelper.validatePhone(null));
            assertNull(UIHelper.validatePhone(""));
        }

        @Test
        void 11位数字通过() {
            assertNull(UIHelper.validatePhone("13800138000"));
        }

        @Test
        void 非11位或非数字返回错误() {
            assertNotNull(UIHelper.validatePhone("138"));
            assertNotNull(UIHelper.validatePhone("138001380001"));
            assertNotNull(UIHelper.validatePhone("1380013800a"));
        }
    }

    @Nested
    @DisplayName("validateBindAccount")
    class ValidateBindAccount {
        @Test
        void 登录名为空返回null() {
            assertNull(UIHelper.validateBindAccount("", "123456"));
            assertNull(UIHelper.validateBindAccount("  ", "123456"));
        }

        @Test
        void 登录名3到20且密码至少6位返回null() {
            assertNull(UIHelper.validateBindAccount("abc", "123456"));
        }

        @Test
        void 登录名过短或过长返回错误() {
            assertNotNull(UIHelper.validateBindAccount("ab", "123456"));
            assertNotNull(UIHelper.validateBindAccount("a".repeat(21), "123456"));
        }

        @Test
        void 填了登录名但密码不足6位返回错误() {
            assertNotNull(UIHelper.validateBindAccount("doctor1", "12345"));
        }
    }
}
