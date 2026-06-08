package com.hospital.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务异常类行为
 */
@DisplayName("BusinessException 异常处理")
class BusinessExceptionTest {

    @Test
    void 仅message时getMessage正确() {
        BusinessException e = new BusinessException("科室名称已存在");
        assertEquals("科室名称已存在", e.getMessage());
        assertNull(e.getCode());
    }

    @Test
    void code与message时均可获取() {
        BusinessException e = new BusinessException("ERR_001", "业务错误");
        assertEquals("业务错误", e.getMessage());
        assertEquals("ERR_001", e.getCode());
    }

    @Test
    void 带cause时cause可获取() {
        RuntimeException cause = new RuntimeException("原因");
        BusinessException e = new BusinessException("包装消息", cause);
        assertEquals("包装消息", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    void 是RuntimeException子类可无需声明抛出() {
        assertTrue(RuntimeException.class.isAssignableFrom(BusinessException.class));
    }
}
