# Daymark

Daymark 打卡系统后端服务。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Spring Boot | 3.5.13 |
| Java | 21 |
| Maven | 3.9+ |
| MyBatis-Plus | 3.5.14 |
| MySQL | 8.x |

## 项目结构

```
Daymark
├── pom.xml
└── src
    ├── main
    │   ├── java/com/nan/daymarkbackend
    │   │   ├── DaymarkBackendApplication.java   # 启动类
    │   │   ├── common                          # 统一响应、状态码、异常处理
    │   │   │   ├── Result.java
    │   │   │   ├── ResultCode.java
    │   │   │   └── exception
    │   │   ├── config                          # MyBatis-Plus、跨域等配置
    │   │   ├── controller                      # 接口层
    │   │   ├── dto                             # 请求参数对象
    │   │   ├── entity                          # 数据库实体
    │   │   ├── mapper                          # MyBatis-Plus Mapper
    │   │   ├── service                         # 业务接口
    │   │   │   └── impl                        # 业务实现
    │   │   └── vo                              # 响应视图对象
    │   └── resources
    │       ├── application.yml                 # 主配置
    │       ├── mapper                          # MyBatis XML 映射文件
    │       └── static                          # 静态资源
    └── test
        └── java/com/nan/daymarkbackend         # 测试
```

## 本地运行

前置条件：JDK 21、Maven 3.9+、MySQL 8.x。

1. 创建数据库：

   ```sql
   CREATE DATABASE daymark DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

2. 启动服务（数据库连接信息可通过环境变量覆盖）：

   ```bash
   mvn spring-boot:run
   ```

   默认连接 `localhost:3306/daymark`，用户名 `root`，密码 `root`。

3. 验证：

   ```bash
   curl http://localhost:8080/api/health
   ```

## 配置说明

`application.yml` 中的数据库配置均支持环境变量覆盖：

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | 服务端口 |
| `MYSQL_HOST` | `localhost` | MySQL 地址 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_DATABASE` | `daymark` | 数据库名 |
| `MYSQL_USERNAME` | `root` | 数据库用户名 |
| `MYSQL_PASSWORD` | `root` | 数据库密码 |

例如：

```bash
MYSQL_PASSWORD=your-password mvn spring-boot:run
```

## 构建与测试

```bash
mvn clean package
java -jar target/daymark-backend-0.0.1-SNAPSHOT.jar
```

