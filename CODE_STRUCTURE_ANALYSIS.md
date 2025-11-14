# PeliPlat AI Movie Search 项目代码结构梳理

## 📋 项目概览

**项目名称**: Spring AI Alibaba DeepSeek Chat Example  
**主要功能**: 基于Spring AI的多模型电影搜索系统  
**技术栈**: Spring Boot 3.4.0 + Spring AI 1.0.0-M6 + 多AI模型集成  
**端口**: 10001  

---

## 🏗️ 项目架构

```
peliplat-search-ai/
├── 📁 src/main/java/com/peliplat/ai/
│   ├── 🚀 DeepseekChatModelApplication.java     # Spring Boot启动类
│   ├── 📁 config/                               # 配置类
│   ├── 📁 controller/                           # 控制器层
│   ├── 📁 model/                               # 数据模型
│   ├── 📁 movie/                               # 电影工具类
│   ├── 📁 service/                             # 服务层
│   └── 📁 util/                                # 工具类
├── 📁 src/main/resources/
│   ├── 📄 application.yml                       # 主配置文件
│   └── 📁 static/                              # 静态资源
└── 📄 pom.xml                                  # Maven依赖配置
```

---

## 🔧 核心组件分析

### 1. 🚀 启动类 (`DeepseekChatModelApplication.java`)
```java
@SpringBootApplication
public class DeepseekChatModelApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeepseekChatModelApplication.class, args);
    }
}
```
**职责**: 标准的Spring Boot启动类，负责应用程序启动

---

### 2. 🎛️ 配置层 (`config/`)

#### AiModelProperties.java
- **功能**: AI模型配置属性类
- **支持模型**: DeepSeek、GPT-4、GPT-3.5、Claude、Ollama、通义千问、豆包
- **配置项**: API密钥、基础URL、模型名称、温度、最大token数等

#### RestTemplateConfig.java 
- **功能**: HTTP客户端配置
- **特性**: 自定义RestTemplate Bean配置

#### SwaggerConfiguration.java
- **功能**: API文档配置
- **访问路径**: `/swagger-ui.html`

---

### 3. 🎮 控制器层 (`controller/`)

#### MovieToolController.java (主控制器 - 853行)
**核心接口**:

##### AI电影工具接口
- `POST /api/movie-tool/chat` - 综合AI电影搜索
- `POST /api/movie-tool/chat-with-model` - 指定模型搜索

##### AI模型管理接口  
- `GET /api/ai/models` - 获取所有可用模型
- `POST /api/ai/models/switch` - 切换AI模型
- `GET /api/ai/models/current` - 获取当前模型信息
- `DELETE /api/ai/models/cache` - 清除模型缓存

##### 直接调用接口
- `GET /api/movie/query` - 自然语言查询电影
- `GET /api/movie/search-by-year` - 按年份搜索
- `GET /api/movie/popular-this-week` - 获取热门电影

**特色功能**:
- ✅ 多模型支持与切换
- ✅ 豆包模型Function Call格式处理
- ✅ 搜索结果增强分析
- ✅ 搜索建议生成
- ✅ 错误处理和日志记录

#### MovieToolController_backup.java
**功能**: 控制器备份版本

---

### 4. 📊 数据模型层 (`model/`)

#### 核心模型类:
- **MovieDetailVo.java** - 电影详细信息VO
- **MovieListResponseVo.java** - 电影列表响应VO  
- **MediaDetailVo.java** - 媒体详细信息VO
- **PersonVo.java** - 人员信息VO
- **SearchResultVo.java** - 搜索结果VO
- **MediaBaseVo.java** - 媒体基础VO

**数据结构特点**:
- 完整的电影信息包装
- 支持多语言显示
- 包含评分、年份、类型等详细信息

---

### 5. 🛠️ 服务层 (`service/`)

#### AiModelFactory.java
**功能**: AI模型工厂类
**职责**:
- 管理多个AI模型的ChatClient实例
- 提供模型切换功能
- 模型缓存管理
- 支持模型的动态配置

#### CustomDoubaoModel.java  
**功能**: 自定义豆包模型处理
**特点**: 专门处理豆包模型的特殊响应格式

#### MovieSearchService.java (接口)
**定义**: 电影搜索服务接口

#### PeliplatMovieSearchServiceImpl.java (实现类)
**功能**: PeliPlat电影搜索服务实现
**集成**: 与PeliPlat电影数据库API集成
**方法**: 
- 按查询搜索电影
- 按年份和类型搜索
- 获取热门电影等

---

### 6. 🔧 电影工具 (`movie/`)

#### MovieTools.java
**功能**: 电影搜索工具类，使用Spring AI的@Tool注解
**工具方法**:
- `@Tool searchMovies()` - 单部电影搜索
- `@Tool concurrentSearchMovies()` - 并发搜索多部电影  
- `@Tool searchMoviesByYear()` - 按年份搜索
- `@Tool searchMoviesByRegionAndGenre()` - 按地区和类型搜索
- `@Tool getPopularMovies()` - 获取热门电影
- `@Tool smartMovieSearch()` - 智能电影搜索

**特点**: 
- 支持多语言查询 (zh/en/ko/ja)
- 智能电影名称推断
- 并发搜索优化

---

### 7. 🎨 前端页面 (`static/`)

#### movie-search.html
**功能**: 主电影搜索界面
**特性**:
- 🎨 现代化UI设计
- 🔄 模型切换功能
- 📱 响应式布局  
- ⚡ 实时搜索结果展示

#### debug-ai-query.html
**功能**: AI查询调试界面
**用途**: 开发和测试AI查询功能

#### test-ai-search.html  
**功能**: AI搜索测试页面
**用途**: 功能测试和验证

---

## ⚙️ 配置文件分析 (`application.yml`)

### 核心配置项:

#### 服务端配置
```yaml
server:
  port: 10001
```

#### Spring AI配置
```yaml
spring:
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
```

#### AI模型配置
```yaml
ai:
  active-model: doubao  # 当前激活模型
  models:
    deepseek:     # DeepSeek配置
    gpt4:         # GPT-4配置  
    claude:       # Claude配置
    doubao:       # 豆包配置
    # ... 更多模型
```

**支持的AI模型**:
1. **DeepSeek Chat** - 高性能中文对话模型
2. **OpenAI GPT-4** - 强大的多模态AI模型  
3. **OpenAI GPT-3.5** - 快速高效的对话模型
4. **Anthropic Claude-3** - 安全可靠的AI助手
5. **Ollama本地模型** - 私有部署AI模型
6. **阿里通义千问** - 中文优化大模型
7. **字节豆包1.5 Pro** - 高性能中文大模型

---

## 📦 Maven依赖 (`pom.xml`)

### 关键依赖:
- **Spring Boot**: 3.4.0
- **Spring AI**: 1.0.0-M6  
- **Spring AI Alibaba**: 1.0.0-M6.1
- **Java版本**: 17
- **Swagger**: API文档生成

---

## 🔄 系统工作流程

### 1. 用户查询流程
```
用户输入 → MovieToolController → AI模型选择 → MovieTools工具调用 → PeliPlat API → 结果返回
```

### 2. AI处理链路
```
自然语言查询 → AI理解分析 → 工具函数调用 → 电影搜索 → 结果增强 → JSON响应
```

### 3. 模型切换流程
```
前端模型选择 → AiModelFactory → 配置验证 → ChatClient切换 → 缓存更新
```

---

## 🎯 核心特性

### ✅ 已实现功能
- [x] **多AI模型支持** - 7个主流AI模型集成
- [x] **动态模型切换** - 运行时无缝切换AI模型
- [x] **智能电影搜索** - AI理解自然语言查询
- [x] **多语言支持** - 中英日韩电影搜索
- [x] **并发优化** - 多部电影同时搜索
- [x] **Function Call处理** - 支持豆包等模型特殊格式
- [x] **搜索结果增强** - 智能分析和建议生成
- [x] **现代化前端** - 响应式设计和实时交互

### 🔧 架构优势
- **模块化设计** - 清晰的分层架构
- **可扩展性** - 易于添加新的AI模型
- **配置化** - 模型参数外部化配置
- **错误处理** - 完善的异常处理机制
- **缓存优化** - 模型实例缓存管理

---

## 🚀 启动和使用

### 启动命令
```bash
mvn spring-boot:run
```

### 访问地址
- **主应用**: http://localhost:10001/movie-search.html
- **API文档**: http://localhost:10001/swagger-ui.html
- **调试页面**: http://localhost:10001/debug-ai-query.html

---

## 📈 性能特点

- **响应式设计** - 支持移动端和桌面端
- **异步处理** - 非阻塞的AI查询处理
- **缓存机制** - AI模型实例缓存复用
- **并发搜索** - 多电影同时查询优化
- **错误恢复** - 优雅的降级处理

---

## 🔮 技术亮点

1. **Spring AI集成** - 使用最新的Spring AI框架
2. **多模型架构** - 支持主流AI提供商
3. **工具函数** - @Tool注解的函数调用
4. **智能推断** - AI理解描述性查询
5. **现代前端** - CSS3动画和响应式设计

---

*📅 文档生成时间: 2025-08-29*  
*🔄 最后更新: 代码回滚后的完整梳理*