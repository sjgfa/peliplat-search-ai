# Peliplat Search AI - 智能电影搜索系统

基于 DeepSeek AI 模型的智能电影搜索应用，结合自然语言处理和电影数据库，为用户提供智能化的电影发现体验。

## 🌟 功能特性

### 🤖 AI 聊天功能
- **智能对话**：基于 DeepSeek AI 模型的智能聊天
- **流式响应**：支持实时流式对话体验
- **记忆功能**：具备聊天上下文记忆能力
- **参数自定义**：可调节模型温度等参数

### 🎬 智能电影搜索
- **自然语言查询**：支持"我想看今年的爱情片"等自然语言查询
- **多维度搜索**：按年份、类型、关键词等多种方式搜索
- **热门推荐**：获取本周热门电影列表
- **并发搜索**：同时搜索多部电影，提高查询效率
- **多语言支持**：支持中文、英文等多种语言显示

### 🛠️ 技术特性
- **AI 工具集成**：使用 Spring AI 的 @Tool 注解实现智能工具调用
- **RESTful API**：标准化的 REST 接口设计
- **API 文档**：集成 Swagger UI，便于接口测试和文档查看
- **模块化设计**：清晰的分层架构，易于维护和扩展

## 🏗️ 技术栈

- **Java 17**：现代化的 Java 开发
- **Spring Boot 3.4.0**：最新的 Spring Boot 框架
- **Spring AI 1.0.0-M6**：Spring 官方 AI 集成框架
- **Spring AI Alibaba 1.0.0-M6.1**：阿里云 AI 扩展
- **SpringDoc OpenAPI**：API 文档生成
- **Maven**：项目管理和构建

## 🚀 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6+
- DeepSeek API 密钥

### 1. 获取 DeepSeek API 密钥

访问 [DeepSeek 平台](https://platform.deepseek.com/) 注册账号并获取 API 密钥。

### 2. 配置环境

设置环境变量：
```bash
export DEEPSEEK_API_KEY=your-deepseek-api-key-here
```

或者直接在 `src/main/resources/application.yml` 中配置：
```yaml
spring:
  ai:
    openai:
      api-key: your-deepseek-api-key-here
```

### 3. 运行项目

```bash
# 克隆项目
git clone [项目地址]
cd peliplat-search-ai

# 编译运行
mvn clean compile
mvn spring-boot:run
```

### 4. 访问应用

- 应用地址：http://localhost:10001
- API 文档：http://localhost:10001/swagger-ui.html

## 📚 API 使用指南

### 聊天接口

#### 基础聊天
```http
GET http://localhost:10001/client/ai/generate
```

#### 流式聊天
```http
GET http://localhost:10001/client/ai/stream
```

#### 自定义参数聊天
```http
GET http://localhost:10001/client/ai/customOptions
```

### 电影搜索接口

#### 1. 自然语言查询电影
```http
GET http://localhost:10001/api/movie/query?query=我想看今年的爱情片电影&language=zh
```

**参数说明：**
- `query`: 查询语句（必填）
- `language`: 语言代码，默认 'en'（可选）

#### 2. 按年份和类型搜索
```http
GET http://localhost:10001/api/movie/search-by-year?year=2024&genre=Action&language=en
```

**参数说明：**
- `year`: 电影年份（可选）
- `genre`: 电影类型（可选）
- `language`: 语言代码，默认 'en'（可选）

**支持的电影类型：**
Action, Adventure, Animation, Comedy, Crime, Documentary, Drama, Family, Fantasy, History, Horror, Music, Mystery, Romance, Thriller, War, Western, Sci-Fi, Biography, Film-Noir, Musical

#### 3. 获取热门电影
```http
GET http://localhost:10001/api/movie/popular-this-week?language=zh
```

#### 4. AI 工具集成搜索
```http
POST http://localhost:10001/api/movie-tool/chat?userMessage=帮我找几部宫崎骏的动画电影
```

这个接口会使用 AI 理解用户意图，自动调用合适的搜索工具。

## 🔧 配置说明

### application.yml 配置

```yaml
server:
  port: 10001  # 应用端口

spring:
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY:your-deepseek-api-key-here}
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat
      embedding:
        enabled: false

# Swagger UI 配置
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## 📁 项目结构

```
src/main/java/com/peliplat/ai/
├── DeepseekChatModelApplication.java   # 主启动类
├── controller/                         # 控制器层
│   ├── DeepSeekChatClientController.java
│   └── MovieToolController.java
├── service/                           # 服务层
│   ├── MovieSearchService.java
│   └── impl/
├── movie/                            # 电影相关
│   └── MovieTools.java
├── model/                            # 数据模型
├── config/                           # 配置类
└── util/                            # 工具类
```

## 🎯 使用示例

### 示例 1：自然语言搜索
```bash
curl -X GET "http://localhost:10001/api/movie/query?query=我想看一部关于时间旅行的科幻电影&language=zh"
```

### 示例 2：AI 聊天搜索
```bash
curl -X POST "http://localhost:10001/api/movie-tool/chat?userMessage=推荐几部今年的动作片"
```

### 示例 3：按类型搜索
```bash
curl -X GET "http://localhost:10001/api/movie/search-by-year?year=2024&genre=Comedy,Romance&language=en"
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目基于 Apache License 2.0 许可证开源。

## 🙋‍♂️ 常见问题

### Q: 如何获取 DeepSeek API 密钥？
A: 访问 [DeepSeek 平台](https://platform.deepseek.com/) 注册账号并在控制台获取 API 密钥。

### Q: 支持哪些电影数据源？
A: 当前集成了主流电影数据库，支持全球电影信息查询。

### Q: 可以自定义 AI 模型参数吗？
A: 可以，在调用接口时传入温度、最大令牌数等参数进行自定义。

### Q: 如何添加新的搜索功能？
A: 可以在 `MovieTools.java` 中添加新的 `@Tool` 注解方法，或在 `MovieSearchService` 中扩展业务逻辑。

## 📞 支持与反馈

如有问题或建议，请提交 Issue 或联系项目维护者。

---

⭐ 如果这个项目对您有帮助，请给我们一个 Star！
