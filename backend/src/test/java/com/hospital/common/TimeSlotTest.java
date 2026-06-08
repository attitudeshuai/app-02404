package com.hospital.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TimeSlot 枚举的单元测试
 */
@DisplayName("时段 TimeSlot")
class TimeSlotTest {

    @Test
    void fromCode返回对应时段() {
        assertEquals(TimeSlot.MORNING, TimeSlot.fromCode(0));
        assertEquals(TimeSlot.AFTERNOON, TimeSlot.fromCode(1));
    }

    @Test
    void 非法code回退到上午() {
        assertEquals(TimeSlot.MORNING, TimeSlot.fromCode(-1));
        assertEquals(TimeSlot.MORNING, TimeSlot.fromCode(2));
    }

    @Test
    void getCode与描述正确() {
        assertEquals(0, TimeSlot.MORNING.getCode());
        assertEquals(1, TimeSlot.AFTERNOON.getCode());
        assertEquals("上午", TimeSlot.MORNING.getDesc());
        assertEquals("下午", TimeSlot.AFTERNOON.getDesc());
    }

    @Test
    void toString返回描述() {
        assertEquals("上午", TimeSlot.MORNING.toString());
        assertEquals("下午", TimeSlot.AFTERNOON.toString());
    }
}
