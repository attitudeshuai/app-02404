#!/bin/bash

echo "========================================"
echo "   医院门诊挂号系统 一键启动脚本"
echo "========================================"
echo

# 切换到项目根目录
cd "$(dirname "$0")/.."

if [ ! -d "backend/src/main/java" ]; then
    echo "[错误] 未找到项目目录，请确认在 scripts 文件夹下运行 start.sh"
    exit 1
fi

# 检查 Java 环境
if ! command -v java &> /dev/null; then
    echo "[错误] 未检测到 Java 环境，请安装 JDK 17+"
    exit 1
fi

# 检测本地 Maven：有则用本地，无则用 Docker 中的 Maven
USE_LOCAL_MAVEN=0
if command -v mvn &>/dev/null; then
    USE_LOCAL_MAVEN=1
fi

# 检查 Docker 环境（MySQL 必选；无本地 Maven 时编译也依赖 Docker）
if ! command -v docker &> /dev/null; then
    echo "[错误] 未检测到 Docker 环境，请安装 Docker"
    exit 1
fi

if [ "$USE_LOCAL_MAVEN" -eq 0 ]; then
    echo "[信息] 未检测到本地 Maven，将使用 Docker 中的 Maven 编译"
fi

# 兼容新版 docker compose（无连字符）与旧版 docker-compose
DOCKER_COMPOSE="docker-compose"
if docker compose version &>/dev/null; then
    DOCKER_COMPOSE="docker compose"
fi

# 检查 MySQL 容器是否运行
CONTAINER_ID=$(docker ps -q -f name=hospital-mysql)

if [ -z "$CONTAINER_ID" ]; then
    # 若存在已停止的同名容器，先删除
    if docker ps -a -q -f name=hospital-mysql | grep -q .; then
        echo "[信息] 发现已停止的 MySQL 容器，正在移除以便重新启动..."
        docker rm -f hospital-mysql 2>/dev/null || true
    fi
    echo "[信息] 正在启动 MySQL 容器..."
    $DOCKER_COMPOSE up -d

    echo "[信息] 等待 MySQL 启动完成（最多 90 秒）..."
    for i in $(seq 1 30); do
        if docker exec hospital-mysql mysql -uroot -proot123 -e "SELECT 1" &>/dev/null; then
            echo "[信息] MySQL 已就绪"
            break
        fi
        if [ "$i" -eq 30 ]; then
            echo "[警告] MySQL 启动超时，将仍尝试初始化数据库..."
        fi
        sleep 3
    done

    echo "[信息] 正在初始化数据库..."
    docker exec -i hospital-mysql mysql -uroot -proot123 --default-character-set=utf8mb4 < backend/src/main/resources/schema.sql
    if [ $? -ne 0 ]; then
        echo "[警告] 数据库初始化可能失败，请检查"
    else
        echo "[成功] 数据库初始化完成"
    fi
else
    echo "[信息] MySQL 容器已在运行"
fi

echo

if [ "$USE_LOCAL_MAVEN" -eq 1 ]; then
    echo "[信息] 使用本地 Maven 编译并启动应用..."
    echo "========================================"
    echo
    mvn -f backend/pom.xml compile exec:java -q
    exit $?
fi

echo "[信息] 使用 Docker 中的 Maven 编译..."
docker run --rm -v "$(pwd):/app" -w /app maven:3.9-eclipse-temurin-17 mvn -f backend/pom.xml compile dependency:copy-dependencies -DincludeScope=runtime -q
if [ $? -ne 0 ]; then
    echo "[错误] Docker Maven 编译失败"
    exit 1
fi
echo "[成功] 编译完成"
echo
echo "[信息] 正在启动应用..."
echo "========================================"
echo
java -cp "backend/target/classes:backend/target/dependency/*" com.hospital.Application
exit $?
