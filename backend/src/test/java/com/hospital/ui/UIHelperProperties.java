package com.hospital.ui;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.constraints.StringLength;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * UIHelper 校验方法的 jqwik 属性测试（属性：满足条件的输入应通过校验）
 */
class UIHelperProperties {

    @Property
    @Label("用户名：长度在 3～20 之间的非空字符串应通过校验")
    void username_validLength_shouldPass(
            @ForAll @StringLength(min = 3, max = 20) String username) {
        assertNull(UIHelper.validateUsername(username));
    }

    @Property
    @Label("密码：长度 >= 6 的字符串应通过 validatePasswordMin")
    void password_atLeast6_shouldPass(
            @ForAll @StringLength(min = 6, max = 50) String password) {
        assertNull(UIHelper.validatePasswordMin(password, "密码"));
    }

    @Property
    @Label("最大长度：长度 <= maxLen 的字符串应通过 validateMaxLength")
    void maxLength_underLimit_shouldPass(
            @ForAll @StringLength(max = 50) String value,
            @ForAll @IntRange(min = 50, max = 200) int maxLen) {
        assertNull(UIHelper.validateMaxLength(value, maxLen, "字段"));
    }

    @Property
    @Label("身份证：15 位数字应通过")
    void idCard_15digits_shouldPass(
            @ForAll @StringLength(15) @NumericChars String idCard) {
        assertNull(UIHelper.validateIdCard(idCard));
    }

    @Property
    @Label("手机号：11 位数字应通过")
    void phone_11digits_shouldPass(
            @ForAll @StringLength(11) @NumericChars String phone) {
        assertNull(UIHelper.validatePhone(phone));
    }

    @Property
    @Label("绑定账号：登录名 3～20 且密码 >= 6 应通过")
    void bindAccount_valid_shouldPass(
            @ForAll @StringLength(min = 3, max = 20) String username,
            @ForAll @StringLength(min = 6, max = 30) String password) {
        assertNull(UIHelper.validateBindAccount(username, password));
    }
}
