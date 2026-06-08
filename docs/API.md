# 医院门诊挂号系统 API 文档

本文档描述后端**控制器层**与**服务层**的对外接口，便于维护、扩展与对接 UI 或其它调用方。

---

## 1. 通用约定

### 1.1 统一响应 `Result<T>`

所有 Controller 方法均返回 `com.hospital.common.Result<T>`：

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 是否成功 |
| message | String | 提示信息（成功或失败原因） |
| data | T | 业务数据，无数据时为 null |

- **成功**：`Result.success()` 或 `Result.success(data)`，可选 `Result.success("自定义消息", data)`。
- **失败**：`Result.fail("错误信息")`，调用方通过 `result.isSuccess()` 判断，失败时从 `result.getMessage()` 取提示。

### 1.2 业务异常 `BusinessException`

- Service 层在参数不合法或业务规则不满足时抛出 `BusinessException(message)`。
- Controller 会捕获并将 `e.getMessage()` 放入 `Result.fail(message)`，不向上抛出。
- 常见原因：必填为空、长度/格式不符、名称重复、关联数据存在（如科室下有医生无法删除）等。

### 1.3 调用关系

```
UI (Panel/Dialog)  →  Controller  →  Service  →  DAO  →  数据库
```

- **Controller**：供 UI 或其它模块调用，返回 `Result<T>`，不抛业务异常。
- **Service**：业务逻辑与校验，抛出 `BusinessException`；Controller 负责捕获并转成 Result。

---

## 2. Controller 层 API（供 UI 调用）

### 2.1 UserController

用户登录与注册。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| login | username: String, password: String | Result\<User\> | 登录。成功时 data 为当前用户；失败时 success=false，message 为“用户名或密码错误”或校验提示。 |
| register | user: User | Result\<Void\> | 注册。user 需包含 username、password、realName；可选 idCard、phone。失败时 message 为校验或“用户名已存在”等。 |

**User 主要字段**：id, username, password, realName, idCard, phone, role（0 患者 / 1 管理员 / 2 医生）, createTime。

---

### 2.2 DepartmentController

科室管理。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| getAllDepartments | - | Result\<List\<Department\>\> | 获取全部科室，按 id 排序。 |
| addDepartment | dept: Department | Result\<Void\> | 新增科室。dept 需 name（必填）、description（可选）。失败：科室名称为空/超长/已存在。 |
| updateDepartment | dept: Department | Result\<Void\> | 更新科室。dept 需 id、name、description。失败：id 为空、名称已存在等。 |
| deleteDepartment | id: Long | Result\<Void\> | 删除科室。失败：id 为空、该科室下存在医生。 |

**Department**：id, name, description。

---

### 2.3 DoctorController

医生管理及医生端查询。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| getAllDoctors | - | Result\<List\<Doctor\>\> | 获取全部医生（含科室名等）。 |
| getDoctorsByDepartment | deptId: Long | Result\<List\<Doctor\>\> | 按科室筛选医生。 |
| addDoctor | doctor: Doctor | Result\<Void\> | 新增医生。必填：name、departmentId；可选：title、bindUsername/bindPassword（绑定登录账号）。失败：姓名/科室为空、绑定账号格式不符、登录名已存在等。 |
| updateDoctor | doctor: Doctor | Result\<Void\> | 更新医生。必填 id、name、departmentId。未绑定时可填 bindUsername/bindPassword 进行绑定。 |
| deleteDoctor | id: Long | Result\<Void\> | 删除医生。失败：该医生存在排班记录。 |
| getDoctorByUserId | userId: Long | Result\<Doctor\> | 根据用户 ID 查医生（医生登录后获取当前医生信息）。 |

**Doctor**：id, name, title, departmentId, departmentName, userId（绑定登录账号 id）, bindUsername/bindPassword（仅用于创建/绑定，不持久化密码）。

---

### 2.4 ScheduleController

排班管理及患者端可选排班查询。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| getAllSchedules | - | Result\<List\<Schedule\>\> | 获取全部排班。 |
| getAvailableSchedules | doctorId: Long | Result\<List\<Schedule\>\> | 某医生下仍有号源的排班（可挂号）。 |
| addSchedule | schedule: Schedule | Result\<Void\> | 新增排班。必填：doctorId、scheduleDate、timeSlot（0 上午/1 下午）、maxCount（1–100）。失败：日期早于今天、该医生该时段已有排班等。 |
| updateSchedule | schedule: Schedule | Result\<Void\> | 更新排班。必填 id、doctorId、scheduleDate、timeSlot、maxCount。 |
| deleteSchedule | id: Long | Result\<Void\> | 删除排班。失败：该排班存在挂号记录。 |

**Schedule**：id, doctorId, doctorName, departmentName, scheduleDate, timeSlot, maxCount, currentCount, 以及剩余号源等展示字段。

---

### 2.5 RegistrationController

挂号记录：患者端、医生端、管理员端。

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| getMyRegistrations | userId: Long | Result\<List\<Registration\>\> | 患者端：当前用户的挂号列表。 |
| getAllRegistrations | - | Result\<List\<Registration\>\> | 全部挂号记录。 |
| searchRegistrations | date: LocalDate, deptId: Long, status: Integer | Result\<List\<Registration\>\> | 管理员端：按日期、科室、状态筛选。任一参数可为 null 表示不筛。status：0 待就诊/1 已完成/2 已取消。 |
| createRegistration | userId: Long, scheduleId: Long | Result\<Void\> | 患者挂号。失败：已在该时段挂号、号源已满、排班不存在等。 |
| completeRegistration | regId: Long | Result\<Void\> | 将挂号标记为“已完成”。仅待就诊可操作。 |
| cancelRegistration | regId: Long | Result\<Void\> | 患者取消挂号。仅待就诊可取消。 |
| updateStatus | regId: Long, status: Integer, doctorId: Long | Result\<Void\> | 管理员/医生修改状态。status：0/1/2。doctorId 非空时为医生操作，仅能改本人排班下的挂号。 |
| getRegistrationsByDoctorId | doctorId: Long | Result\<List\<Registration\>\> | 医生端：本人排班下的挂号列表。 |

**Registration**：id, userId, scheduleId, status, createTime, patientName, doctorName, departmentName, scheduleDate, timeSlot 等。

---

## 3. Service 层 API（业务逻辑）

Service 供 Controller 调用；直接调用 Service 时需自行捕获 `BusinessException`。

### 3.1 UserService

- **login(username, password)** → User 或 null（用户名/密码错误时抛 `BusinessException` 或返回 null，见实现）。
- **register(user)** → void，throws BusinessException。校验：用户名 3–20 字、密码≥6、姓名必填、身份证/手机格式可选。
- **isValidIdCard(idCard)** / **isValidPhone(phone)** → boolean，格式校验工具。

### 3.2 DepartmentService

- **getAllDepartments()** / **getDepartmentById(id)** → List / Department。
- **addDepartment(dept)** / **updateDepartment(dept)** / **deleteDepartment(id)** → void，throws BusinessException。校验与规则见 Controller 说明。

### 3.3 DoctorService

- **getAllDoctors()** / **getDoctorsByDepartment(deptId)** / **getDoctorById(id)** / **getDoctorByUserId(userId)** → List/Doctor。
- **addDoctor(doctor)** / **updateDoctor(doctor)** / **deleteDoctor(id)** → void，throws BusinessException。含绑定账号时的用户名/密码校验。

### 3.4 ScheduleService

- **getAllSchedules()** / **getSchedulesByDoctor(doctorId)** / **getAvailableSchedules(doctorId)** / **getScheduleById(id)** → List/Schedule。
- **addSchedule(schedule)** / **updateSchedule(schedule)** / **deleteSchedule(id)** → void，throws BusinessException。校验：医生、日期（不早于今天）、时段 0/1、maxCount 1–100，以及同医生同时段唯一性。

### 3.5 RegistrationService

- **getMyRegistrations(userId)** / **getAllRegistrations()** / **searchRegistrations(date, deptId, status)** / **getRegistrationsByDoctorId(doctorId)** / **getRegistrationById(id)** → List/Registration。
- **createRegistration(userId, scheduleId)** / **completeRegistration(regId)** / **cancelRegistration(regId)** / **updateStatusByAdmin(regId, status, doctorId)** → void，throws BusinessException。create 内含号源检查与事务。

---

## 4. 实体与枚举速查

| 类型 | 说明 |
|------|------|
| **User.role** | 0 患者，1 管理员，2 医生。 |
| **Registration.status** | 0 待就诊，1 已完成，2 已取消。 |
| **Schedule.timeSlot** | 0 上午，1 下午。 |
| **TimeSlot** | 枚举 MORNING(0)/AFTERNOON(1)，getCode()、getDesc()、fromCode(int)。 |

---

## 5. 生成 Javadoc

在 `backend` 目录执行：

```bash
mvn javadoc:javadoc
```

生成的 HTML 在 `backend/target/site/apidocs/`。可在 IDE 中查看类/方法上的 Javadoc 注释以获取更多实现细节。

---

## 6. 文档维护说明

- 新增或修改 Controller/Service 的**对外方法**时，请同步更新本文档（方法签名、参数含义、返回值及常见失败原因）。
- 详细入参校验规则见各 Service 实现及单元测试（如 `DepartmentServiceTest`、`UserServiceTest`）。
