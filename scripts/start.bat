@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
title 医院门诊挂号系统

:: 切换到项目根目录（脚本所在目录的上一级）
cd /d "%~dp0.."
if not exist "backend\src\main\java" (
    echo [错误] 未找到项目目录，请确认在 scripts 文件夹下运行 start.bat
    pause
    exit /b 1
)

echo ========================================
echo    医院门诊挂号系统 一键启动脚本
echo ========================================
echo.

call :main
pause
exit /b 0

:main
:: 检查 Java 环境
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Java 环境，请安装 JDK 17+
    exit /b 1
)

:: 检测本地 Maven：有则用本地，无则用 Docker 中的 Maven
set "USE_LOCAL_MAVEN=0"
mvn -version >nul 2>&1
if not errorlevel 1 set "USE_LOCAL_MAVEN=1"

:: 检查 Docker 环境（MySQL 必选；无本地 Maven 时编译也依赖 Docker）
docker --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Docker 环境，请安装 Docker Desktop
    exit /b 1
)
if "!USE_LOCAL_MAVEN!"=="0" (
    echo [信息] 未检测到本地 Maven，将使用 Docker 中的 Maven 编译
)

:: 兼容新版 docker compose（无连字符）与旧版 docker-compose
set "DOCKER_COMPOSE=docker-compose"
docker compose version >nul 2>&1
if not errorlevel 1 set "DOCKER_COMPOSE=docker compose"

:: 检查 MySQL 容器是否运行
docker ps -q -f name=hospital-mysql >nul 2>&1
for /f %%i in ('docker ps -q -f name^=hospital-mysql') do set CONTAINER_ID=%%i

if "!CONTAINER_ID!"=="" (
    :: 若存在已停止的同名容器，先删除以避免名称冲突（数据在 volume 中会保留）
    docker ps -a -q -f name=hospital-mysql >nul 2>&1
    for /f %%j in ('docker ps -a -q -f name^=hospital-mysql 2^>nul') do (
        echo [信息] 发现已停止的 MySQL 容器，正在移除以便重新启动...
        docker rm -f hospital-mysql >nul 2>&1
    )
    echo [信息] 正在启动 MySQL 容器...
    %DOCKER_COMPOSE% up -d
    
    call :wait_for_mysql
    
    echo [信息] 正在初始化数据库...
    docker exec -i hospital-mysql mysql -uroot -proot123 --default-character-set=utf8mb4 < backend\src\main\resources\schema.sql
    if errorlevel 1 (
        echo [警告] 数据库初始化可能失败，请检查
    ) else (
        echo [成功] 数据库初始化完成
    )
) else (
    echo [信息] MySQL 容器已在运行
)
goto :skip_mysql_wait

:wait_for_mysql
echo [信息] 等待 MySQL 启动完成（最多 90 秒）...
for /l %%k in (1,1,30) do (
    docker exec hospital-mysql mysql -uroot -proot123 -e "SELECT 1" >nul 2>&1
    if not errorlevel 1 (
        echo [信息] MySQL 已就绪
        exit /b 0
    )
    timeout /t 3 /nobreak >nul
)
echo [警告] MySQL 启动超时，将仍尝试初始化数据库...
exit /b 0

:skip_mysql_wait

echo.

if "!USE_LOCAL_MAVEN!"=="1" (
    echo [信息] 使用本地 Maven 编译并启动应用...
    echo ========================================
    echo.
    mvn -f backend\pom.xml compile exec:java -q
    if errorlevel 1 exit /b 1
) else (
    echo [信息] 使用 Docker 中的 Maven 编译...
    docker run --rm -v "%cd%:/app" -w /app maven:3.9-eclipse-temurin-17 mvn -f backend/pom.xml compile dependency:copy-dependencies -DincludeScope=runtime -q
    if errorlevel 1 (
        echo [错误] Docker Maven 编译失败
        exit /b 1
    )
    echo [成功] 编译完成
    echo.
    echo [信息] 正在启动应用...
    echo ========================================
    echo.
    java -cp "backend\target\classes;backend\target\dependency\*" com.hospital.Application
    if errorlevel 1 exit /b 1
)

exit /b 0
