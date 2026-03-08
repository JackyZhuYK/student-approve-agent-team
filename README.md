# Student Approval Agent Team System

学生审批 Agent 团队系统 - 一个基于多 Agent 协作的审批流程管理平台。

## 技术栈

### 后端
- **Java 17** + **Spring Boot 3.2**
- **Spring Data JPA** + PostgreSQL / H2
- **Spring Security** + JWT 认证
- **Maven** 构建工具
- **Lombok** + MapStruct

### 前端
- **Vue 3.4** + TypeScript
- **Element Plus** UI 组件库
- **Pinia** 状态管理
- **Vue Router** 路由
- **Axios** API 客户端
- **Vite** 构建工具

### 基础设施
- **PostgreSQL 15** (生产数据库)
- **H2** (开发/测试数据库)
- **Redis 7** (缓存/会话)
- **Docker** + **Docker Compose** (容器化部署)

## 快速开始

### 前提条件
- Java 17+
- Node.js 20+
- Docker & Docker Compose (可选)

### 方式一：使用 Docker Compose (推荐)

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

访问地址：
- 前端：http://localhost:5173
- 后端 API: http://localhost:8080/api
- H2 Console (开发): http://localhost:8080/api/h2-console

### 方式二：本地开发

#### 启动后端

```bash
cd backend

# 使用 H2 内存数据库运行
mvn spring-boot:run

# 或者使用 PostgreSQL (需要先启动数据库)
docker-compose up -d postgres redis
export DATABASE_URL=jdbc:postgresql://localhost:5432/student_approval
mvn spring-boot:run
```

#### 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build
```

## 默认账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 审批人 | approver | approver123 |

## 核心功能

### 1. 用户认证模块
- 用户登录
- JWT 令牌认证
- RBAC 权限控制

### 2. 申请管理模块
- 提交申请
- 查看申请列表
- 查看申请详情
- 撤回申请

### 3. 审批管理模块
- 待办审批列表
- 审批通过/驳回
- 批量审批
- 审批历史记录

### 4. Agent 协作模块
- 多 Agent 角色模拟
- Agent 审批日志
- 流程引擎配置

## API 端点

### 认证
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/refresh` - 刷新令牌

### 申请
- `GET /api/applications` - 获取我的申请列表
- `GET /api/applications/all` - 获取所有申请 (管理员)
- `GET /api/applications/pending` - 获取待处理申请
- `GET /api/applications/:id` - 获取申请详情
- `POST /api/applications` - 创建新申请
- `POST /api/applications/:id/withdraw` - 撤回申请

### 审批
- `GET /api/approvals/pending` - 获取待办审批
- `GET /api/approvals/application/:id` - 获取申请审批记录
- `POST /api/approvals/application/:id` - 提交审批
- `POST /api/approvals/application/:id/agent/:role` - Agent 审批

## 数据库配置

### 开发环境 (H2)
```yaml
DATABASE_URL: jdbc:h2:mem:studentdb
DATABASE_USER: sa
DATABASE_PASSWORD: ''
```

### 生产环境 (PostgreSQL)
```yaml
DATABASE_URL: jdbc:postgresql://localhost:5432/student_approval
DATABASE_USER: student
DATABASE_PASSWORD: student123
```

## 项目结构

```
student-approve-agent-team/
├── backend/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/agentteam/
│   │       ├── config/        # 配置类
│   │       ├── controller/    # REST 控制器
│   │       ├── dto/           # 数据传输对象
│   │       ├── entity/        # JPA 实体
│   │       ├── repository/    # 数据访问层
│   │       ├── security/      # 安全相关
│   │       └── service/       # 业务逻辑层
│   ├── src/main/resources/
│   │   └── db/migration/      # 数据库迁移脚本
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API 客户端
│   │   ├── components/        # 公共组件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia 状态管理
│   │   ├── views/             # 页面组件
│   │   └── tests/             # 测试文件
│   └── package.json
├── tests/e2e/                  # E2E 测试
├── docker-compose.yml          # Docker 编排
└── .env.example                # 环境变量示例
```

## 测试

### 后端测试
```bash
cd backend
mvn test
```

### 前端测试
```bash
cd frontend
npm run test
```

### E2E 测试
```bash
# 需要安装 Playwright
npx playwright test
```

## 安全建议

参考 [SECURITY.md](./SECURITY.md) 了解详细的安全配置建议。

## 开发计划

- [x] 项目初始化
- [x] 后端基础架构
- [x] 前端基础架构
- [x] 用户认证
- [x] 申请管理
- [x] 审批管理
- [x] 基础测试
- [ ] Agent 协作功能增强
- [ ] 流程引擎可视化
- [ ] 数据统计看板
- [ ] 通知系统

## License

MIT
