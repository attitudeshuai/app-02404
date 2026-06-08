package com.hospital.service;

import com.hospital.dao.DepartmentDAO;
import com.hospital.entity.Department;
import com.hospital.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 科室服务类
 */
public class DepartmentService {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);
    private final DepartmentDAO departmentDAO;

    public DepartmentService() {
        this(new DepartmentDAO());
    }

    public DepartmentService(DepartmentDAO departmentDAO) {
        this.departmentDAO = departmentDAO;
    }

    public List<Department> getAllDepartments() {
        return departmentDAO.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentDAO.findById(id);
    }

    public void addDepartment(Department dept) throws BusinessException {
        validateDepartment(dept);

        if (departmentDAO.existsByName(dept.getName())) {
            throw new BusinessException("科室名称已存在");
        }

        int rows = departmentDAO.insert(dept);
        if (rows <= 0) {
            throw new BusinessException("添加科室失败");
        }
    }

    public void updateDepartment(Department dept) throws BusinessException {
        if (dept.getId() == null) {
            throw new BusinessException("科室ID不能为空");
        }
        validateDepartment(dept);

        if (departmentDAO.existsByNameExcludeId(dept.getName(), dept.getId())) {
            throw new BusinessException("科室名称已存在");
        }

        int rows = departmentDAO.update(dept);
        if (rows <= 0) {
            throw new BusinessException("更新科室失败");
        }
    }

    public void deleteDepartment(Long id) throws BusinessException {
        if (id == null) {
            throw new BusinessException("科室ID不能为空");
        }

        if (departmentDAO.hasDoctor(id)) {
            throw new BusinessException("该科室下存在医生，无法删除");
        }

        int rows = departmentDAO.delete(id);
        if (rows <= 0) {
            throw new BusinessException("删除科室失败");
        }
    }

    private void validateDepartment(Department dept) throws BusinessException {
        if (dept.getName() == null || dept.getName().trim().isEmpty()) {
            throw new BusinessException("科室名称不能为空");
        }
        if (dept.getName().length() > 50) {
            throw new BusinessException("科室名称不能超过50个字符");
        }
    }
}
