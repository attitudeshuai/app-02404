package com.hospital.service;

import com.hospital.dao.DepartmentDAO;
import com.hospital.entity.Department;
import com.hospital.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 科室服务：业务校验与异常处理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentService 业务逻辑与异常")
class DepartmentServiceTest {

    @Mock
    private DepartmentDAO departmentDAO;

    private DepartmentService service;

    @BeforeEach
    void setUp() {
        service = new DepartmentService(departmentDAO);
    }

    @Nested
    @DisplayName("addDepartment")
    class AddDepartment {
        @Test
        void 科室名称为空应抛异常() {
            Department dept = new Department("", "描述");
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addDepartment(dept));
            assertTrue(ex.getMessage().contains("科室名称不能为空"));
            verify(departmentDAO, never()).insert(any());
        }

        @Test
        void 科室名称仅空白应抛异常() {
            Department dept = new Department("  ", null);
            assertThrows(BusinessException.class, () -> service.addDepartment(dept));
            verify(departmentDAO, never()).insert(any());
        }

        @Test
        void 科室名称超过50字应抛异常() {
            Department dept = new Department("a".repeat(51), null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addDepartment(dept));
            assertTrue(ex.getMessage().contains("50"));
            verify(departmentDAO, never()).insert(any());
        }

        @Test
        void 科室名称已存在应抛异常() {
            Department dept = new Department("内科", null);
            when(departmentDAO.existsByName("内科")).thenReturn(true);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addDepartment(dept));
            assertEquals("科室名称已存在", ex.getMessage());
            verify(departmentDAO, never()).insert(any());
        }

        @Test
        void insert返回0应抛异常() {
            Department dept = new Department("内科", "描述");
            when(departmentDAO.existsByName("内科")).thenReturn(false);
            when(departmentDAO.insert(any())).thenReturn(0);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.addDepartment(dept));
            assertTrue(ex.getMessage().contains("添加科室失败"));
        }

        @Test
        void 校验通过且insert成功不抛异常() {
            Department dept = new Department("内科", "描述");
            when(departmentDAO.existsByName("内科")).thenReturn(false);
            when(departmentDAO.insert(any())).thenReturn(1);
            assertDoesNotThrow(() -> service.addDepartment(dept));
        }
    }

    @Nested
    @DisplayName("updateDepartment")
    class UpdateDepartment {
        @Test
        void 科室ID为空应抛异常() {
            Department dept = new Department("内科", null);
            dept.setId(null);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.updateDepartment(dept));
            assertEquals("科室ID不能为空", ex.getMessage());
            verify(departmentDAO, never()).update(any());
        }

        @Test
        void 名称已存在且非本记录应抛异常() {
            Department dept = new Department("心内科", null);
            dept.setId(1L);
            when(departmentDAO.existsByNameExcludeId("心内科", 1L)).thenReturn(true);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.updateDepartment(dept));
            assertEquals("科室名称已存在", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteDepartment")
    class DeleteDepartment {
        @Test
        void 科室ID为空应抛异常() {
            BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteDepartment(null));
            assertEquals("科室ID不能为空", ex.getMessage());
            verify(departmentDAO, never()).delete(anyLong());
        }

        @Test
        void 科室下存在医生应抛异常() {
            when(departmentDAO.hasDoctor(1L)).thenReturn(true);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteDepartment(1L));
            assertTrue(ex.getMessage().contains("存在医生"));
            verify(departmentDAO, never()).delete(anyLong());
        }

        @Test
        void delete返回0应抛异常() {
            when(departmentDAO.hasDoctor(1L)).thenReturn(false);
            when(departmentDAO.delete(1L)).thenReturn(0);
            BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteDepartment(1L));
            assertTrue(ex.getMessage().contains("删除科室失败"));
        }
    }
}
