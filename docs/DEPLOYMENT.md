# 医院门诊挂号系统部署说明（生产环境）

本文档说明如何在生产环境部署和配置系统，适用于医院内网场景（数据库集中部署，客户端在前台电脑运行）。

## 1. 部署架构建议

推荐采用以下拓扑：

- **MySQL 服务器**：集中部署 1 台（可主从/备份）
- **应用客户端**：每台窗口电脑运行 Swing 客户端（Java 程序）
- **网络**：客户端通过内网访问 MySQL（建议仅开放内网访问）

> 本项目是桌面应用，不是 Web 服务。生产部署重点是数据库可靠性、客户端分发与配置一致性。

## 2. 环境准备

### 2.1 数据库服务器

- Linux/Windows 服务器（建议 Linux）
- Docker + Docker Compose
- 可用磁盘建议 >= 20GB（按业务量调整）
- 内网固定 IP（示例：`10.10.10.20`）

### 2.2 客户端机器

- JDK 17+
- 能访问数据库服务器 `3306` 端口
- 已分发应用程序（源码运行或打包运行）

## 3. 生产数据库部署

在数据库服务器上部署 MySQL（可直接复用项目 `docker-compose.yml`）：

```bash
docker compose up -d
# 或：docker-compose up -d
```

初始化数据库（首次）：

```bash
docker exec -i hospital-mysql mysql -uroot -proot123 --default-character-set=utf8mb4 < backend/src/main/resources/schema.sql
```

检查状态：

```bash
docker ps
docker logs hospital-mysql --tail 100
```

## 4. 生产配置项

核心配置文件：`backend/src/main/resources/db.properties`

重点参数：

- `jdbc.url`：指向生产 MySQL（示例：`jdbc:mysql://10.10.10.20:3306/hospital_registration?...`）
- `jdbc.username`：生产库账号（不要使用 root）
- `jdbc.password`：生产库密码

建议：

- 为系统创建专用数据库账号（如 `hospital_app`），仅授予所需库权限
- 使用强密码并定期轮换
- 不将生产密码提交到 Git（可通过部署脚本按环境覆盖 `db.properties`）

## 5. 应用部署方式

## 5.1 方式 A：客户端源码运行（内部环境）

在每台客户端机器拉取代码后运行：

- Windows：`scripts\start.bat`
- Linux/Mac：`./scripts/start.sh`

适合开发/测试和小规模内部使用。

## 5.2 方式 B：客户端打包运行（推荐生产）

在构建机执行：

```bash
cd backend
mvn clean package
```

将 `backend/target` 下产物分发到客户端机器运行。  
建议进一步使用 `jpackage` 打包成安装程序，便于统一升级与桌面快捷方式管理。

## 6. 安全与合规建议

- 密码存储：系统已使用 **BCrypt** 哈希（数据库不存明文）
- SQL 注入防护：DAO 层使用 `PreparedStatement` 参数化查询
- 网络访问控制：仅允许内网网段访问 MySQL 3306
- 账户权限最小化：应用账号只授予业务库权限
- 日志管理：日志与 `logs/` 目录不提交 Git，按周期归档或清理

## 7. 备份与恢复

### 7.1 逻辑备份（推荐每日）

```bash
docker exec hospital-mysql mysqldump -uroot -proot123 hospital_registration > backup_$(date +%F).sql
```

### 7.2 恢复

```bash
docker exec -i hospital-mysql mysql -uroot -proot123 hospital_registration < backup_2026-03-09.sql
```

> 恢复前请确认目标库状态，避免覆盖现网数据。

## 8. 升级与回滚建议

- 升级前执行数据库备份
- 先在预发布环境验证功能（登录、挂号、取消、再次挂号、状态流转）
- 生产发布后做冒烟测试
- 出现异常时优先回滚应用版本，必要时按备份恢复数据库

## 9. 生产验收清单

- [ ] 管理员、医生、患者登录正常
- [ ] 科室/医生/排班增删改查正常
- [ ] 患者挂号 -> 取消 -> 同排班再次挂号正常
- [ ] 医生端与管理员端状态流转一致
- [ ] 数据库备份任务可执行
- [ ] 日志、`target/` 未进入 Git 仓库
