package com.hospital.service;

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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 排班服务：校验与业务异常
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService 业务逻辑与异常")
class ScheduleServiceTest {

    @Mock
    private ScheduleDAO scheduleDAO;

    private ScheduleService service;

    @BeforeEach
    void setUp() {
        service = new ScheduleService(scheduleDAO);
    }

    @Nested
    @DisplayName("addSchedule validateSchedule")
    class AddScheduleValidation {
        @Test
        void 未选择医生应抛异常() {
            Schedule s = new Schedule(null, LocalDate.now(), 0, 20);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertEquals("请选择医生", ex.getMessage());
            verify(scheduleDAO, never()).insert(any());
        }

        @Test
        void 排班日期为空应抛异常() {
            Schedule s = new Schedule(1L, null, 0, 20);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertEquals("请选择排班日期", ex.getMessage());
        }

        @Test
        void 排班日期早于今天应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now().minusDays(1), 0, 20);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertEquals("排班日期不能早于今天", ex.getMessage());
        }

        @Test
        void 时段为空应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now(), 0, 20);
            s.setTimeSlot(null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertEquals("请选择时段", ex.getMessage());
        }

        @Test
        void 时段无效应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now(), 0, 20);
            s.setTimeSlot(2);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertEquals("时段值无效", ex.getMessage());
        }

        @Test
        void 最大挂号数为0应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now(), 0, 0);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertTrue(ex.getMessage().contains("大于0"));
        }

        @Test
        void 最大挂号数超过100应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now(), 0, 101);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertTrue(ex.getMessage().contains("100"));
        }

        @Test
        void 该医生该时段已有排班应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now(), 0, 20);
            when(scheduleDAO.exists(1L, LocalDate.now(), 0)).thenReturn(true);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertTrue(ex.getMessage().contains("已有排班"));
            verify(scheduleDAO, never()).insert(any());
        }

        @Test
        void insert返回0应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now(), 0, 20);
            when(scheduleDAO.exists(1L, LocalDate.now(), 0)).thenReturn(false);
            when(scheduleDAO.insert(any())).thenReturn(0);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addSchedule(s));
            assertTrue(ex.getMessage().contains("添加排班失败"));
        }
    }

    @Nested
    @DisplayName("updateSchedule")
    class UpdateSchedule {
        @Test
        void 排班ID为空应抛异常() {
            Schedule s = new Schedule(1L, LocalDate.now(), 0, 20);
            s.setId(null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.updateSchedule(s));
            assertEquals("排班ID不能为空", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteSchedule")
    class DeleteSchedule {
        @Test
        void 排班ID为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteSchedule(null));
            assertEquals("排班ID不能为空", ex.getMessage());
        }

        @Test
        void 存在挂号记录应抛异常() {
            when(scheduleDAO.hasRegistration(1L)).thenReturn(true);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteSchedule(1L));
            assertTrue(ex.getMessage().contains("挂号记录"));
            verify(scheduleDAO, never()).delete(anyLong());
        }
    }
}
