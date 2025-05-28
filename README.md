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
- **地区智能搜索**：支持"中国历史革命片"、"韩国爱情片"等地区特定搜索
- **智能回退机制**：地区搜索不准确时，AI自动推断具体电影名称并重新搜索
- **热门推荐**：获取本周热门电影列表
- **并发搜索**：同时搜索多部电影，提高查询效率
- **多语言支持**：支持中文、英文、韩文、日文等多种语言显示

### 🎨 现代化前端界面
- **响应式设计**：适配各种设备和屏幕尺寸
- **智能搜索建议**：预设搜索标签，一键快速搜索
- **搜索历史记录**：本地存储搜索历史，支持快速重复搜索
- **高级加载动画**：多阶段加载提示，展示AI处理过程
- **结果统计分析**：显示搜索结果数量、平均评分、类型分布等
- **智能排序功能**：按相关性、评分、年份、标题等多种方式排序
- **动态搜索建议**：基于搜索结果的AI生成建议

### 🛠️ 技术特性
- **AI 工具集成**：使用 Spring AI 的 @Tool 注解实现智能工具调用
- **智能工具路由**：AI自动选择最合适的搜索工具
- **RESTful API**：标准化的 REST 接口设计
- **API 文档**：集成 Swagger UI，便于接口测试和文档查看
- **模块化设计**：清晰的分层架构，易于维护和扩展
- **强化错误处理**：前后端全面的错误处理和用户反馈

## 🏗️ 技术栈

- **Java 17**：现代化的 Java 开发
- **Spring Boot 3.4.0**：最新的 Spring Boot 框架
- **Spring AI 1.0.0-M6**：Spring 官方 AI 集成框架
- **Spring AI Alibaba 1.0.0-M6.1**：阿里云 AI 扩展
- **SpringDoc OpenAPI**：API 文档生成
- **Maven**：项目管理和构建
- **HTML5 + CSS3 + JavaScript**：现代化前端技术栈
- **Font Awesome**：图标库

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

- **电影搜索界面**：http://localhost:10001/movie-search.html
- **应用地址**：http://localhost:10001
- **API 文档**：http://localhost:10001/swagger-ui.html

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

#### 4. AI 工具集成搜索 ⭐ **主要接口**
```http
POST http://localhost:10001/api/movie-tool/chat?userMessage=帮我找几部宫崎骏的动画电影
```

这个接口会使用 AI 理解用户意图，自动调用合适的搜索工具。

**新增智能搜索示例：**
- "中国历史革命片" - AI会自动搜索中国地区的历史战争片
- "韩国最新爱情片" - 自动筛选韩国地区的浪漫电影
- "日本动画电影推荐" - 智能搜索日本动画作品

#### 5. 响应格式

**新增增强响应格式：**
```json
{
  "success": true,
  "query": "中国历史革命片",
  "movies": [...],
  "totalCount": 15,
  "responseTime": 1240,
  "metadata": {
    "averageRating": 7.8,
    "genreCount": 3,
    "topGenres": ["History", "War", "Drama"]
  },
  "suggestions": [
    "更多抗战题材电影",
    "近代历史片推荐",
    "经典战争电影"
  ]
}
```

## 🎯 AI工具功能详解

### 智能工具集合

#### 1. **电影名称搜索** (`searchMovies`)
- 根据准确电影名称搜索
- 支持多语言电影标题
- 最适合已知电影名称的查询

#### 2. **并发批量搜索** (`concurrentSearchMovies`)
- 同时搜索多部电影
- 用逗号分隔多个电影名称
- 每个名称返回最匹配的一部电影

#### 3. **年份类型搜索** (`searchMoviesByYear`)
- 按年份、类型、地区搜索
- 支持组合搜索条件
- 适合宽泛的搜索需求

#### 4. **地区类型搜索** (`searchMoviesByRegionAndGenre`) ⭐ **新功能**
- 专门用于地区特定搜索
- 如"中国历史片"、"韩国爱情片"
- 语言代码决定搜索的电影来源地区

#### 5. **智能回退搜索** (`smartMovieSearch`) ⭐ **新功能**
- 当地区搜索结果不准确时自动触发
- AI推断具体电影名称进行精确搜索
- 提供更准确的地区特定电影结果

#### 6. **热门电影获取** (`getPopularMovies`)
- 获取本周热门电影
- 支持多语言显示
- 实时热度排序

### AI工具调用逻辑

1. **智能路由**: AI根据用户查询自动选择最合适的工具
2. **回退机制**: 如果首次搜索结果不理想，自动尝试其他搜索策略
3. **结果优化**: 对搜索结果进行智能排序和过滤

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
│   └── MovieTools.java              # ⭐ AI工具集合
├── model/                            # 数据模型
├── config/                           # 配置类
└── util/                            # 工具类

src/main/resources/static/
└── movie-search.html                 # ⭐ 现代化前端界面
```

## 🎯 使用示例

### 示例 1：地区特定搜索 ⭐ **新功能**
```bash
curl -X POST "http://localhost:10001/api/movie-tool/chat?userMessage=中国历史革命片"
```

### 示例 2：智能多语言搜索
```bash
curl -X POST "http://localhost:10001/api/movie-tool/chat?userMessage=韩国最新爱情片推荐"
```

### 示例 3：自然语言搜索
```bash
curl -X GET "http://localhost:10001/api/movie/query?query=我想看一部关于时间旅行的科幻电影&language=zh"
```

### 示例 4：并发搜索多部电影
```bash
curl -X POST "http://localhost:10001/api/movie-tool/chat?userMessage=搜索：建国大业,集结号,红海行动"
```

### 示例 5：按类型搜索
```bash
curl -X GET "http://localhost:10001/api/movie/search-by-year?year=2024&genre=Comedy,Romance&language=en"
```

## 💡 使用技巧

### 🎯 搜索优化建议

1. **地区搜索**: 使用"中国历史片"、"韩国爱情片"等地区+类型组合
2. **精确搜索**: 已知电影名称时，直接使用电影名称搜索
3. **批量搜索**: 用逗号分隔多个电影名称，一次获取多部电影信息
4. **语言设置**: 中文电影使用'zh'，韩文使用'ko'，日文使用'ja'

### 🚀 前端功能使用

1. **快速搜索**: 点击预设标签进行一键搜索
2. **历史记录**: 查看并重复之前的搜索
3. **智能排序**: 使用下拉菜单按不同标准排序结果
4. **搜索建议**: 查看AI生成的相关搜索建议

## ⚡ 性能特性

- **并发处理**: 支持多个搜索请求并发处理
- **缓存优化**: 智能缓存热门搜索结果
- **响应时间**: 平均响应时间 < 2秒
- **负载均衡**: 支持高并发用户访问

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

### Q: 地区搜索不准确怎么办？
A: 系统具备智能回退机制，会自动推断具体电影名称重新搜索，提供更准确的结果。

### Q: 可以自定义 AI 模型参数吗？
A: 可以，在调用接口时传入温度、最大令牌数等参数进行自定义。

### Q: 前端搜索历史在哪里存储？
A: 搜索历史使用浏览器本地存储（localStorage），不会上传到服务器。

## 🔄 更新日志

### v1.2.0 (最新)
- ✅ 新增地区智能搜索功能
- ✅ 新增智能回退搜索机制
- ✅ 全面更新前端界面，支持现代化UX
- ✅ 新增搜索历史和智能建议功能
- ✅ 强化错误处理和类型安全
- ✅ 新增响应统计和元数据分析

### v1.1.0
- ✅ 修复前端类型错误
- ✅ 增强API响应格式
- ✅ 改进工具描述和参数验证

### v1.0.0
- ✅ 基础电影搜索功能
- ✅ AI聊天集成
- ✅ RESTful API设计

## 📞 支持与反馈

如有问题或建议，请提交 Issue 或联系项目维护者。

---

⭐ 如果这个项目对您有帮助，请给我们一个 Star！

🎬 开始您的智能电影发现之旅：http://localhost:10001/movie-search.html
