package com.hospital.service;

import com.hospital.dao.RegistrationDAO;
import com.hospital.dao.ScheduleDAO;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
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
 * 挂号服务：参数校验与重复挂号等异常分支
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationService 业务逻辑与异常")
class RegistrationServiceTest {

    @Mock
    private RegistrationDAO registrationDAO;

    @Mock
    private ScheduleDAO scheduleDAO;

    private RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService(registrationDAO, scheduleDAO);
    }

    @Nested
    @DisplayName("createRegistration 参数校验")
    class CreateRegistrationValidation {
        @Test
        void 用户ID为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createRegistration(null, 1L));
            assertEquals("用户ID不能为空", ex.getMessage());
            verify(registrationDAO, never()).exists(any(), any());
        }

        @Test
        void 排班ID为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createRegistration(1L, null));
            assertEquals("请选择排班时段", ex.getMessage());
        }

        @Test
        void 已在该时段挂号应抛异常() {
            when(registrationDAO.exists(1L, 10L)).thenReturn(true);
            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createRegistration(1L, 10L));
            assertEquals("您已在该时段挂号，请勿重复挂号", ex.getMessage());
            verify(scheduleDAO, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("completeRegistration / cancelRegistration 参数校验")
    class CompleteAndCancelValidation {
        @Test
        void completeRegistration_id为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.completeRegistration(null));
            assertTrue(ex.getMessage().contains("挂号ID"));
        }

        @Test
        void cancelRegistration_id为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.cancelRegistration(null));
            assertTrue(ex.getMessage().contains("挂号ID"));
        }
    }
}
