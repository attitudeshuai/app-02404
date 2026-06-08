# 医院门诊挂号系统

基于 Java Swing + MySQL 的桌面挂号系统，支持**患者端、医生端、管理员端**三端协同。

## 快速启动

### Windows
```bash
scripts\start.bat
```

### Mac / Linux
```bash
chmod +x scripts/start.sh
./scripts/start.sh
```

启动脚本会自动完成：
- 检查 Java 与 Docker 环境
- 检测本地 Maven（有则本地编译运行，无则使用 Docker 内 Maven）
- 启动 `hospital-mysql` 容器并执行 `schema.sql` 初始化数据库
- 编译并启动 Swing 应用

| 服务 | 说明 | 端口 |
|------|------|------|
| MySQL | `hospital-mysql` 容器 | `3306` |
| 应用 | 本地运行的 Swing 程序 | - |

## 环境要求

- **JDK 17+**（必选）
- **Docker Desktop**（必选，用于 MySQL；无本地 Maven 时也用于编译）
- **Maven 3.6+**（可选）

## 题目说明

设计一个医院门诊挂号系统，使用 Java GUI 进行桌面应用开发，后台数据库使用 MySQL。

## API 文档

- 详细接口文档：[`docs/API.md`](docs/API.md)
- 部署说明文档：[`docs/DEPLOYMENT.md`](docs/DEPLOYMENT.md)
- Javadoc：在 `backend` 目录执行 `mvn javadoc:javadoc`，输出目录 `backend/target/site/apidocs/`

## 技术栈

- UI：Java Swing + FlatLaf
- 后端：Java 17 + JDBC
- 数据库：MySQL 8.0（Docker）
- 连接池：HikariCP
- 日志：SLF4J + Logback
- 测试：JUnit 5 + Mockito + jqwik

## 登录账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 医生 | `doctor1` / `doctor2`（或管理员新绑定账号） | `doctor123`（或绑定时设置） |
| 患者 | 自行注册 | 自行设置 |

> 说明：系统使用 **BCrypt** 存储密码，数据库中不保存明文密码。

## 功能模块

### 患者端
- 注册/登录
- 按日期与科室筛选排班并挂号
- 查看个人挂号记录
- 取消挂号（取消后可再次挂同一排班）

### 医生端
- 医生账号登录（需与 `doctor.user_id` 绑定）
- 查看“我的挂号”
- 更新挂号状态（待就诊 / 已完成 / 已取消）

### 管理员端
- 科室管理：新增、搜索、编辑、删除
- 医生管理：新增、编辑、删除，支持绑定医生登录账号
- 排班管理：按医生/日期筛选，新增、编辑、删除（日期为日历选择器）
- 挂号管理：按日期/科室/状态筛选，完成、取消、修改状态

## 系统功能验证步骤（建议按顺序）

### 1) 启动与登录验证
1. 运行启动脚本，确认控制台出现“启动应用成功”日志。
2. 使用管理员账号登录：`admin / admin123`。
3. 退出后使用医生账号登录：`doctor1 / doctor123`。
4. 注册一个新患者账号并登录一次，确认注册与登录链路正常。

**期望结果**
- 管理员和医生均可成功登录。
- 新患者注册后可正常登录。

### 2) 科室与医生管理验证（管理员）
1. 进入“科室管理”，新增一个测试科室（如“测试科室A”）。
2. 编辑该科室描述，再删除该科室。
3. 进入“医生管理”，新增医生并绑定登录账号（如 `doctor_test`）。
4. 使用该绑定账号登录医生端，确认可进入医生界面。

**期望结果**
- 新增/编辑/删除操作成功，列表即时刷新。
- 绑定账号可用于医生登录。

### 3) 排班管理验证（管理员）
1. 在“排班管理”中为某医生新增未来日期排班（使用日历选择器）。
2. 修改该排班的号源上限。
3. 删除该排班并重新新增一条。

**期望结果**
- 日期选择器显示正常，无重叠/遮挡。
- 新增、编辑、删除后列表状态正确。

### 4) 挂号与取消再挂号验证（核心）
1. 使用患者账号登录，选择某条可挂号排班，点击“挂号”。
2. 在“我的挂号”中确认该记录状态为“待就诊”。
3. 执行“取消挂号”，状态变为“已取消”。
4. 再次对**同一排班**执行挂号。

**期望结果**
- 第 1 次挂号成功。
- 取消后可再次挂上同一排班（这是当前约束逻辑的关键验证点）。
- 未取消状态下重复挂同一排班会被拦截。

### 5) 医生/管理员状态流转验证
1. 医生端将某条“待就诊”记录改为“已完成”。
2. 管理员端按状态筛选该记录，确认状态一致。
3. 管理员将状态改回“已取消”或“待就诊”，验证权限与状态切换可用。

**期望结果**
- 医生端与管理员端状态展示一致。
- 状态筛选结果与实际一致。

## 安全说明

### 密码安全（BCrypt）
- `sys_user.password` 存储 BCrypt 哈希（非明文）。
- 注册、医生绑定账号时均在服务层进行 BCrypt 哈希后入库。
- 登录通过 BCrypt 校验，不做明文比对。

### SQL 注入防护
- DAO 层统一使用 `PreparedStatement` 和参数绑定，禁止拼接用户输入 SQL。
- 条件筛选通过固定子句 + 参数列表实现，不拼接用户输入。

### 前端输入校验
- 登录/注册、科室/医生/排班编辑等提交前统一执行 `UIHelper.validate*` 校验。
- 与后端规则保持一致（长度、必填、格式），减少无效请求。

## 数据库设计

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表（`role`: 0 患者 / 1 管理员 / 2 医生） |
| `department` | 科室表 |
| `doctor` | 医生表（`user_id` 可选绑定登录账号） |
| `schedule` | 排班表 |
| `registration` | 挂号记录表（未取消记录唯一，取消后可再次挂号） |

> `registration` 表通过生成列 `active_key` + 唯一索引 `(user_id, schedule_id, active_key)` 保证：未取消记录唯一、取消后可重新挂号。

## 运行测试

```bash
cd backend
mvn test
```

- 单元测试：`*Test.java` / `*Tests.java`
- 属性测试：`*Properties.java`
- 覆盖 UI 校验、时段枚举、服务层校验与异常分支

## 项目结构

```text
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/hospital/
│   │   ├── common/
│   │   ├── controller/
│   │   ├── dao/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── service/
│   │   ├── ui/
│   │   │   ├── panel/
│   │   │   ├── dialog/
│   │   │   ├── CalendarDatePicker.java
│   │   │   ├── UIHelper.java
│   │   │   └── UIConstants.java
│   │   ├── util/
│   │   │   ├── DBUtil.java
│   │   │   └── PasswordUtil.java
│   │   └── Application.java
│   ├── src/main/resources/
│   │   ├── db.properties
│   │   ├── logback.xml
│   │   └── schema.sql
│   └── src/test/java/
├── docs/
│   └── API.md
├── scripts/
│   ├── start.bat
│   └── start.sh
├── docker-compose.yml
└── .gitignore
```

## 重置数据库

```bash
docker compose down -v
# 或：docker-compose down -v
```

然后重新执行启动脚本。

## 停止服务

```bash
docker compose down
# 或：docker-compose down
```
