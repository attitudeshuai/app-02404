-- 医院门诊挂号系统数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS hospital_registration DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital_registration;

-- 用户表
DROP TABLE IF EXISTS registration;
DROP TABLE IF EXISTS schedule;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS department;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(60) NOT NULL COMMENT 'BCrypt 哈希密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(11) COMMENT '手机号',
    role TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0-患者, 1-管理员, 2-医生',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 科室表
CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科室ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '科室名称',
    description VARCHAR(200) COMMENT '科室描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- 医生表（user_id 关联登录账号，可为空表示未绑定）
CREATE TABLE doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    title VARCHAR(50) COMMENT '职称',
    department_id BIGINT NOT NULL COMMENT '所属科室ID',
    user_id BIGINT NULL UNIQUE COMMENT '关联 sys_user.id，医生登录账号',
    FOREIGN KEY (department_id) REFERENCES department(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- 排班表
CREATE TABLE schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排班ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    time_slot TINYINT NOT NULL COMMENT '时段：0-上午, 1-下午',
    max_count INT NOT NULL DEFAULT 20 COMMENT '最大挂号数',
    current_count INT NOT NULL DEFAULT 0 COMMENT '当前挂号数',
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    UNIQUE KEY uk_doctor_date_slot (doctor_id, schedule_date, time_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 挂号记录表（同用户同排班：未取消记录唯一；取消后可再次挂号）
CREATE TABLE registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '挂号ID',
    user_id BIGINT NOT NULL COMMENT '患者ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待就诊, 1-已完成, 2-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
    -- 取消(status=2)时为 NULL，其它状态为 1
    active_key TINYINT
      GENERATED ALWAYS AS (CASE WHEN status = 2 THEN NULL ELSE 1 END) STORED,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (schedule_id) REFERENCES schedule(id),
    UNIQUE KEY uk_user_schedule_active (user_id, schedule_id, active_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号记录表';

-- 初始化管理员账号
INSERT INTO sys_user (username, password, real_name, role) 
VALUES ('admin', '$2a$12$J9wB6d7rKRstd.gjrx1VVeYiJPffSM1xCXDEs6or433j.bDeUM5ju', '系统管理员', 1);
-- 初始化医生登录账号（角色=2）
INSERT INTO sys_user (username, password, real_name, role) VALUES 
('doctor1', '$2a$12$dnlPhSfeo/HeMP/yqMEWmeGy7OGX6GKe3IiNxeWzCZhqgVVq605UO', '张医生', 2),
('doctor2', '$2a$12$dnlPhSfeo/HeMP/yqMEWmeGy7OGX6GKe3IiNxeWzCZhqgVVq605UO', '李医生', 2);

-- 初始化科室数据
INSERT INTO department (name, description) VALUES 
('内科', '内科疾病诊治，包括呼吸内科、消化内科、心血管内科等'),
('外科', '外科手术及治疗，包括普外科、骨科、泌尿外科等'),
('儿科', '儿童疾病诊治，0-14岁儿童常见病、多发病'),
('妇产科', '妇科及产科诊治，妇科疾病、孕产检查、分娩等');

-- 初始化医生数据
INSERT INTO doctor (name, title, department_id, user_id) VALUES 
('张医生', '主任医师', 1, NULL),
('李医生', '副主任医师', 1, NULL),
('王医生', '主任医师', 2, NULL),
('赵医生', '主治医师', 3, NULL),
('陈医生', '副主任医师', 4, NULL);
UPDATE doctor SET user_id = (SELECT id FROM sys_user WHERE username = 'doctor1' LIMIT 1) WHERE name = '张医生' AND department_id = 1 LIMIT 1;
UPDATE doctor SET user_id = (SELECT id FROM sys_user WHERE username = 'doctor2' LIMIT 1) WHERE name = '李医生' AND department_id = 1 LIMIT 1;

-- 初始化排班数据（未来7天）
INSERT INTO schedule (doctor_id, schedule_date, time_slot, max_count) VALUES 
(1, CURDATE(), 0, 20),
(1, CURDATE(), 1, 20),
(2, CURDATE(), 0, 15),
(3, CURDATE(), 1, 20),
(4, CURDATE(), 0, 25),
(5, CURDATE(), 1, 20),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 0, 20),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 15),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 0, 20);

-- 说明：唯一约束基于 (user_id, schedule_id, active_key)，已取消(status=2)记录允许再次挂号。
