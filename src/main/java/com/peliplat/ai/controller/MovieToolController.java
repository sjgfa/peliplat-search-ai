package com.peliplat.ai.controller;

import com.peliplat.ai.config.AiModelProperties;
import com.peliplat.ai.model.MediaDetailVo;
import com.peliplat.ai.model.MovieDetailVo;
import com.peliplat.ai.model.MovieListResponseVo;
import com.peliplat.ai.model.SearchResultVo;
import com.peliplat.ai.movie.MovieTools;
import com.peliplat.ai.service.AiModelFactory;
import com.peliplat.ai.service.MovieSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 电影工具控制器 - 支持多模型切换
 * 提供基于@Tool注解的电影查询API以及直接查询API
 */
@RestController
@Tag(name = "电影API", description = "提供电影查询相关的所有服务")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MovieToolController {
    private static final Logger logger = LoggerFactory.getLogger(MovieToolController.class);

    private final AiModelFactory aiModelFactory;
    private final AiModelProperties aiModelProperties;
    private final MovieSearchService movieSearchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public MovieToolController(AiModelFactory aiModelFactory,
                              AiModelProperties aiModelProperties,
                              @Qualifier("peliplatMovieSearchService") MovieSearchService movieSearchService) {
        this.aiModelFactory = aiModelFactory;
        this.aiModelProperties = aiModelProperties;
        this.movieSearchService = movieSearchService;
    }

    /**
     * 电影工具综合接口 - 优化版本
     * 使用所有电影相关的工具方法
     */
    @Operation(summary = "电影工具综合接口（搜索、按年份搜索、热门电影）- 优化版本")
    @PostMapping("/api/movie-tool/chat")
    public ResponseEntity<Map<String, Object>> chatWithMovieTools(
            @Parameter(description = "用户消息内容") @RequestParam("userMessage") String userMessage) {

        try {
            logger.info("收到电影工具请求: {}", userMessage);
            long startTime = System.currentTimeMillis();

            // 系统提示词（优化版）
            String systemPrompt = """
                你是一个专业的电影搜索助手。请按以下步骤精确分析用户查询：
                
                🎯 STEP 1: 查询意图分析
                - 明确电影名称 → 使用 searchMovies 或 concurrentSearchMovies
                - 描述情节/角色 → 推断电影名称，再搜索
                - 询问年份/类型 → 使用 searchMoviesByYear
                - 询问地区电影 → 使用 searchMoviesByRegionAndGenre
                
                🎯 STEP 2: 电影名称推断规则
                - 优先使用英文原名搜索，提高匹配准确性
                - 描述性查询时，推断出3-5个最可能的电影名称
                - 优先考虑知名度高、获奖、经典的电影
                
                🎯 STEP 3: 搜索策略选择
                - 单部电影：searchMovies(准确英文名, 语言代码)
                - 多部电影：concurrentSearchMovies(英文名列表, 语言代码)
                - 按条件筛选：searchMoviesByYear(年份, 类型, 语言代码)
                - 地区特定：searchMoviesByRegionAndGenre(地区代码, 类型, 年份)
                
                🎯 STEP 4: 语言代码精确映射
                - 中国/中文/华语电影 → 'zh'
                - 韩国/韩语电影 → 'ko'
                - 日本/日语电影 → 'ja'
                - 美国/英语/好莱坞电影 → 'en'
                - 其他欧洲电影 → 'en'
                
                ⚠️ 关键规则：
                1. 必须调用工具，不能直接文本回答
                2. 一次只调用一个最合适的工具
                3. 搜索时使用英文电影名称，显示时使用对应语言
                4. 如果是模糊描述，先在内心推断出具体电影名称
                5. 优先选择最匹配的工具类型
                
                🎬 常见推断示例：
                - "黄色海绵动画" → "SpongeBob SquarePants Movie"
                - "泰坦尼克沉船" → "Titanic"
                - "中国功夫片" → searchMoviesByRegionAndGenre('zh', 'Action')
                - "宫崎骏动画" → concurrentSearchMovies("Spirited Away,My Neighbor Totoro,Princess Mononoke", 'ja')
                """;

            // 创建系统消息
            Message systemMsg = new SystemMessage(systemPrompt);

            // 创建用户消息
            Message userMsg = new UserMessage(userMessage);

            // 调用AI工具 - 使用当前激活的模型
            ChatClient currentChatClient = aiModelFactory.getCurrentChatClient();
            String response = currentChatClient.prompt(new Prompt(List.of(systemMsg, userMsg)))
                    .tools(new MovieTools(movieSearchService))
                    .call()
                    .content();

            logger.info("电影工具返回原始结果 (模型: {}): {}", aiModelProperties.getActiveModel(), response);

            try {
                // 处理豆包模型的Function Call响应格式
                MovieTools.Response toolResponse = null;
                
                if (response.contains("<|FunctionCallBegin|>") && response.contains("<|FunctionCallEnd|>")) {
                    // 豆包模型返回的是Function Call格式，需要执行工具调用
                    logger.info("检测到豆包模型Function Call格式，开始处理工具调用");
                    
                    // 提取函数调用信息
                    String functionCallJson = response.substring(
                        response.indexOf("<|FunctionCallBegin|>") + "<|FunctionCallBegin|>".length(),
                        response.indexOf("<|FunctionCallEnd|>")
                    );
                    
                    // 解析函数调用
                    com.fasterxml.jackson.databind.JsonNode[] functionCalls = 
                        objectMapper.readValue(functionCallJson, com.fasterxml.jackson.databind.JsonNode[].class);
                    
                    if (functionCalls.length > 0) {
                        com.fasterxml.jackson.databind.JsonNode functionCall = functionCalls[0];
                        String functionName = functionCall.get("name").asText();
                        com.fasterxml.jackson.databind.JsonNode parameters = functionCall.get("parameters");
                        
                        logger.info("执行工具函数: {} 参数: {}", functionName, parameters.toString());
                        
                        // 执行对应的工具方法
                        MovieTools movieTools = new MovieTools(movieSearchService);
                        
                        if ("searchMovies".equals(functionName)) {
                            String movieName = parameters.has("movieName") ? parameters.get("movieName").asText() : 
                                              parameters.has("movie_name") ? parameters.get("movie_name").asText() : "";
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用searchMovies: movieName={}, languageCode={}", movieName, languageCode);
                            toolResponse = movieTools.searchMovies(movieName, languageCode);
                        } else if ("concurrentSearchMovies".equals(functionName)) {
                            String movieNames = parameters.has("movieNames") ? parameters.get("movieNames").asText() : 
                                               parameters.has("movie_names") ? parameters.get("movie_names").asText() : "";
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用concurrentSearchMovies: movieNames={}, languageCode={}", movieNames, languageCode);
                            toolResponse = movieTools.concurrentSearchMovies(movieNames, languageCode);
                        } else if ("searchMoviesByYear".equals(functionName)) {
                            String year = parameters.has("year") ? parameters.get("year").asText() : null;
                            String genre = parameters.has("genre") ? parameters.get("genre").asText() : null;
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用searchMoviesByYear: year={}, genre={}, languageCode={}", year, genre, languageCode);
                            toolResponse = movieTools.searchMoviesByYear(year, genre, languageCode);
                        } else if ("searchMoviesByRegionAndGenre".equals(functionName)) {
                            String regionCode = parameters.has("regionCode") ? parameters.get("regionCode").asText() : 
                                               parameters.has("region_code") ? parameters.get("region_code").asText() : 
                                               parameters.has("region") ? parameters.get("region").asText() : "en";
                            String genre = parameters.has("genre") ? parameters.get("genre").asText() : null;
                            String year = parameters.has("year") ? parameters.get("year").asText() : null;
                            logger.info("调用searchMoviesByRegionAndGenre: regionCode={}, genre={}, year={}", regionCode, genre, year);
                            toolResponse = movieTools.searchMoviesByRegionAndGenre(regionCode, genre, year);
                        } else if ("getPopularMovies".equals(functionName)) {
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用getPopularMovies: languageCode={}", languageCode);
                            toolResponse = movieTools.getPopularMovies(languageCode);
                        } else if ("smartMovieSearch".equals(functionName)) {
                            String movieNames = parameters.has("movieNames") ? parameters.get("movieNames").asText() : 
                                               parameters.has("movie_names") ? parameters.get("movie_names").asText() : "";
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用smartMovieSearch: movieNames={}, languageCode={}", movieNames, languageCode);
                            toolResponse = movieTools.smartMovieSearch(movieNames, languageCode);
                        } else {
                            logger.warn("未知的工具函数: {}", functionName);
                            toolResponse = new MovieTools.Response(new ArrayList<>());
                        }
                        
                        logger.info("工具调用完成，返回 {} 部电影", 
                            toolResponse != null ? toolResponse.movies().size() : 0);
                    }
                } else {
                    // 尝试直接解析为MovieTools.Response对象
                    toolResponse = objectMapper.readValue(response, MovieTools.Response.class);
                }
                
                long endTime = System.currentTimeMillis();
                
                // 构建增强的响应
                Map<String, Object> enhancedResponse = new HashMap<>();
                enhancedResponse.put("success", true);
                enhancedResponse.put("query", userMessage);
                enhancedResponse.put("movies", toolResponse.movies());
                enhancedResponse.put("totalCount", toolResponse.movies().size());
                enhancedResponse.put("responseTime", endTime - startTime);
                enhancedResponse.put("timestamp", System.currentTimeMillis());
                
                // 添加搜索统计和建议
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("aiProcessed", true);
                metadata.put("searchEngine", "AI-Powered-Multi-Model");
                metadata.put("promptVersion", "v2.0");
                metadata.put("activeModel", aiModelProperties.getActiveModel());
                
                // 添加当前模型信息
                AiModelProperties.ModelConfig currentConfig = aiModelProperties.getActiveModelConfig();
                if (currentConfig != null) {
                    Map<String, Object> modelInfo = new HashMap<>();
                    modelInfo.put("name", aiModelProperties.getActiveModel());
                    modelInfo.put("provider", currentConfig.getProvider());
                    modelInfo.put("model", currentConfig.getModel());
                    modelInfo.put("description", currentConfig.getDescription());
                    modelInfo.put("temperature", currentConfig.getTemperature());
                    metadata.put("modelInfo", modelInfo);
                }
                
                // 分析搜索结果的多样性
                if (!toolResponse.movies().isEmpty()) {
                    Set<String> genres = new HashSet<>();
                    Set<Integer> years = new HashSet<>();
                    double avgRating = toolResponse.movies().stream()
                            .mapToDouble(movie -> {
                                try {
                                    return Double.parseDouble(movie.getRating());
                                } catch (Exception e) {
                                    return 0.0;
                                }
                            })
                            .average().orElse(0.0);
                    
                    toolResponse.movies().forEach(movie -> {
                        if (movie.getGenres() != null) {
                            genres.addAll(Arrays.asList(movie.getGenres().split(", ")));
                        }
                        if (movie.getPublicationYear() != null) {
                            years.add(movie.getPublicationYear());
                        }
                    });
                    
                    metadata.put("genreCount", genres.size());
                    metadata.put("yearRange", years.size());
                    metadata.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
                    metadata.put("topGenres", genres.stream().limit(3).collect(Collectors.toList()));
                }
                
                enhancedResponse.put("metadata", metadata);
                
                // 添加搜索建议
                List<String> suggestions = generateSearchSuggestions(userMessage, toolResponse.movies());
                enhancedResponse.put("suggestions", suggestions);

                return ResponseEntity.ok(enhancedResponse);
            } catch (Exception e) {
                logger.debug("解析电影工具返回结果失败: {}", e.getMessage());
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("query", userMessage);
                errorResponse.put("movies", Collections.emptyList());
                errorResponse.put("error", "Failed to parse AI response");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(errorResponse);
            }
        } catch (Exception e) {
            logger.error("AI工具调用失败: {}", userMessage, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("query", userMessage);
            errorResponse.put("movies", Collections.emptyList());
            errorResponse.put("error", "AI tool call failed: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // ==================================================
    // AI模型管理接口
    // ==================================================
    
    /**
     * 获取所有可用的AI模型
     */
    @Operation(summary = "获取所有可用的AI模型")
    @GetMapping("/api/ai/models")
    public ResponseEntity<Map<String, Object>> getAvailableModels() {
        try {
            Map<String, Map<String, Object>> models = aiModelFactory.getAvailableModels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeModel", aiModelProperties.getActiveModel());
            response.put("models", models);
            response.put("totalCount", models.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取可用模型失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get available models: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 切换AI模型
     */
    @Operation(summary = "切换AI模型")
    @PostMapping("/api/ai/models/switch")
    public ResponseEntity<Map<String, Object>> switchModel(
            @Parameter(description = "要切换到的模型名称") @RequestParam("modelName") String modelName) {
        try {
            boolean success = aiModelFactory.switchModel(modelName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("timestamp", System.currentTimeMillis());
            
            if (success) {
                response.put("message", "模型切换成功");
                response.put("activeModel", aiModelProperties.getActiveModel());
                
                // 添加新模型信息
                AiModelProperties.ModelConfig config = aiModelProperties.getActiveModelConfig();
                if (config != null) {
                    Map<String, Object> modelInfo = new HashMap<>();
                    modelInfo.put("name", aiModelProperties.getActiveModel());
                    modelInfo.put("provider", config.getProvider());
                    modelInfo.put("model", config.getModel());
                    modelInfo.put("description", config.getDescription());
                    modelInfo.put("temperature", config.getTemperature());
                    response.put("modelInfo", modelInfo);
                }
                
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "模型切换失败");
                response.put("error", "Model switch failed, check model configuration and availability");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("切换模型失败: {}", modelName, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "模型切换失败");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 获取当前激活的模型信息
     */
    @Operation(summary = "获取当前激活的模型信息")
    @GetMapping("/api/ai/models/current")
    public ResponseEntity<Map<String, Object>> getCurrentModel() {
        try {
            String activeModel = aiModelProperties.getActiveModel();
            AiModelProperties.ModelConfig config = aiModelProperties.getActiveModelConfig();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeModel", activeModel);
            response.put("timestamp", System.currentTimeMillis());
            
            if (config != null) {
                Map<String, Object> modelInfo = new HashMap<>();
                modelInfo.put("name", activeModel);
                modelInfo.put("provider", config.getProvider());
                modelInfo.put("model", config.getModel());
                modelInfo.put("description", config.getDescription());
                modelInfo.put("temperature", config.getTemperature());
                modelInfo.put("maxTokens", config.getMaxTokens());
                modelInfo.put("enabled", config.getEnabled());
                response.put("modelInfo", modelInfo);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取当前模型信息失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get current model info: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 清除模型缓存
     */
    @Operation(summary = "清除模型缓存")
    @DeleteMapping("/api/ai/models/cache")
    public ResponseEntity<Map<String, Object>> clearModelCache(
            @Parameter(description = "模型名称，不提供则清除所有缓存") @RequestParam(value = "modelName", required = false) String modelName) {
        try {
            if (modelName != null && !modelName.trim().isEmpty()) {
                aiModelFactory.clearModelCache(modelName);
            } else {
                aiModelFactory.clearAllCache();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", modelName != null ? "指定模型缓存已清除" : "所有模型缓存已清除");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("清除模型缓存失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to clear cache: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * 使用指定模型进行电影搜索
     */
    @Operation(summary = "使用指定模型进行电影搜索")
    @PostMapping("/api/movie-tool/chat-with-model")
    public ResponseEntity<Map<String, Object>> chatWithSpecificModel(
            @Parameter(description = "用户消息内容") @RequestParam("userMessage") String userMessage,
            @Parameter(description = "指定使用的模型名称") @RequestParam("modelName") String modelName) {
        
        try {
            logger.info("收到指定模型电影工具请求: {} (模型: {})", userMessage, modelName);
            long startTime = System.currentTimeMillis();
            
            // 获取指定模型的ChatClient
            ChatClient chatClient = aiModelFactory.getChatClient(modelName);
            
            // 系统提示词（优化版）
            String systemPrompt = """
                你是一个专业的电影搜索助手。请按以下步骤精确分析用户查询：
                
                🎯 STEP 1: 查询意图分析
                - 明确电影名称 → 使用 searchMovies 或 concurrentSearchMovies
                - 描述情节/角色 → 推断电影名称，再搜索
                - 询问年份/类型 → 使用 searchMoviesByYear
                - 询问地区电影 → 使用 searchMoviesByRegionAndGenre
                
                🎯 STEP 2: 电影名称推断规则
                - 优先使用英文原名搜索，提高匹配准确性
                - 描述性查询时，推断出3-5个最可能的电影名称
                - 优先考虑知名度高、获奖、经典的电影
                
                🎯 STEP 3: 搜索策略选择
                - 单部电影：searchMovies(准确英文名, 语言代码)
                - 多部电影：concurrentSearchMovies(英文名列表, 语言代码)
                - 按条件筛选：searchMoviesByYear(年份, 类型, 语言代码)
                - 地区特定：searchMoviesByRegionAndGenre(地区代码, 类型, 年份)
                
                🎯 STEP 4: 语言代码精确映射
                - 中国/中文/华语电影 → 'zh'
                - 韩国/韩语电影 → 'ko'
                - 日本/日语电影 → 'ja'
                - 美国/英语/好莱坞电影 → 'en'
                - 其他欧洲电影 → 'en'
                
                ⚠️ 关键规则：
                1. 必须调用工具，不能直接文本回答
                2. 一次只调用一个最合适的工具
                3. 搜索时使用英文电影名称，显示时使用对应语言
                4. 如果是模糊描述，先在内心推断出具体电影名称
                5. 优先选择最匹配的工具类型
                
                🎬 常见推断示例：
                - "黄色海绵动画" → "SpongeBob SquarePants Movie"
                - "泰坦尼克沉船" → "Titanic"
                - "中国功夫片" → searchMoviesByRegionAndGenre('zh', 'Action')
                - "宫崎骏动画" → concurrentSearchMovies("Spirited Away,My Neighbor Totoro,Princess Mononoke", 'ja')
                """;

            // 创建系统消息和用户消息
            Message systemMsg = new SystemMessage(systemPrompt);
            Message userMsg = new UserMessage(userMessage);

            // 调用指定模型的AI工具
            String response = chatClient.prompt(new Prompt(List.of(systemMsg, userMsg)))
                    .tools(new MovieTools(movieSearchService))
                    .call()
                    .content();

            logger.info("指定模型电影工具返回原始结果 (模型: {}): {}", modelName, response);

            try {
                // 处理豆包模型的Function Call响应格式
                MovieTools.Response toolResponse = null;
                
                if (response.contains("<|FunctionCallBegin|>") && response.contains("<|FunctionCallEnd|>")) {
                    // 豆包模型返回的是Function Call格式，需要执行工具调用
                    logger.info("检测到豆包模型Function Call格式，开始处理工具调用");
                    
                    // 提取函数调用信息
                    String functionCallJson = response.substring(
                        response.indexOf("<|FunctionCallBegin|>") + "<|FunctionCallBegin|>".length(),
                        response.indexOf("<|FunctionCallEnd|>")
                    );
                    
                    // 解析函数调用
                    com.fasterxml.jackson.databind.JsonNode[] functionCalls = 
                        objectMapper.readValue(functionCallJson, com.fasterxml.jackson.databind.JsonNode[].class);
                    
                    if (functionCalls.length > 0) {
                        com.fasterxml.jackson.databind.JsonNode functionCall = functionCalls[0];
                        String functionName = functionCall.get("name").asText();
                        com.fasterxml.jackson.databind.JsonNode parameters = functionCall.get("parameters");
                        
                        logger.info("执行工具函数: {} 参数: {}", functionName, parameters.toString());
                        
                        // 执行对应的工具方法
                        MovieTools movieTools = new MovieTools(movieSearchService);
                        
                        if ("searchMovies".equals(functionName)) {
                            String movieName = parameters.has("movieName") ? parameters.get("movieName").asText() : 
                                              parameters.has("movie_name") ? parameters.get("movie_name").asText() : "";
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用searchMovies: movieName={}, languageCode={}", movieName, languageCode);
                            toolResponse = movieTools.searchMovies(movieName, languageCode);
                        } else if ("concurrentSearchMovies".equals(functionName)) {
                            String movieNames = parameters.has("movieNames") ? parameters.get("movieNames").asText() : 
                                               parameters.has("movie_names") ? parameters.get("movie_names").asText() : "";
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用concurrentSearchMovies: movieNames={}, languageCode={}", movieNames, languageCode);
                            toolResponse = movieTools.concurrentSearchMovies(movieNames, languageCode);
                        } else if ("searchMoviesByYear".equals(functionName)) {
                            String year = parameters.has("year") ? parameters.get("year").asText() : null;
                            String genre = parameters.has("genre") ? parameters.get("genre").asText() : null;
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用searchMoviesByYear: year={}, genre={}, languageCode={}", year, genre, languageCode);
                            toolResponse = movieTools.searchMoviesByYear(year, genre, languageCode);
                        } else if ("searchMoviesByRegionAndGenre".equals(functionName)) {
                            String regionCode = parameters.has("regionCode") ? parameters.get("regionCode").asText() : 
                                               parameters.has("region_code") ? parameters.get("region_code").asText() : 
                                               parameters.has("region") ? parameters.get("region").asText() : "en";
                            String genre = parameters.has("genre") ? parameters.get("genre").asText() : null;
                            String year = parameters.has("year") ? parameters.get("year").asText() : null;
                            logger.info("调用searchMoviesByRegionAndGenre: regionCode={}, genre={}, year={}", regionCode, genre, year);
                            toolResponse = movieTools.searchMoviesByRegionAndGenre(regionCode, genre, year);
                        } else if ("getPopularMovies".equals(functionName)) {
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用getPopularMovies: languageCode={}", languageCode);
                            toolResponse = movieTools.getPopularMovies(languageCode);
                        } else if ("smartMovieSearch".equals(functionName)) {
                            String movieNames = parameters.has("movieNames") ? parameters.get("movieNames").asText() : 
                                               parameters.has("movie_names") ? parameters.get("movie_names").asText() : "";
                            String languageCode = parameters.has("languageCode") ? parameters.get("languageCode").asText() : 
                                                 parameters.has("language") ? parameters.get("language").asText() : "en";
                            logger.info("调用smartMovieSearch: movieNames={}, languageCode={}", movieNames, languageCode);
                            toolResponse = movieTools.smartMovieSearch(movieNames, languageCode);
                        } else {
                            logger.warn("未知的工具函数: {}", functionName);
                            toolResponse = new MovieTools.Response(new ArrayList<>());
                        }
                        
                        logger.info("工具调用完成，返回 {} 部电影", 
                            toolResponse != null ? toolResponse.movies().size() : 0);
                    }
                } else {
                    // 尝试直接解析为MovieTools.Response对象
                    toolResponse = objectMapper.readValue(response, MovieTools.Response.class);
                }
                
                long endTime = System.currentTimeMillis();
                
                // 构建增强的响应
                Map<String, Object> enhancedResponse = new HashMap<>();
                enhancedResponse.put("success", true);
                enhancedResponse.put("query", userMessage);
                enhancedResponse.put("movies", toolResponse.movies());
                enhancedResponse.put("totalCount", toolResponse.movies().size());
                enhancedResponse.put("responseTime", endTime - startTime);
                enhancedResponse.put("timestamp", System.currentTimeMillis());
                
                // 添加模型信息
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("aiProcessed", true);
                metadata.put("searchEngine", "AI-Powered-Multi-Model");
                metadata.put("promptVersion", "v2.0");
                metadata.put("usedModel", modelName);
                
                // 添加指定模型信息
                AiModelProperties.ModelConfig config = aiModelProperties.getModelConfig(modelName);
                if (config != null) {
                    Map<String, Object> modelInfo = new HashMap<>();
                    modelInfo.put("name", modelName);
                    modelInfo.put("provider", config.getProvider());
                    modelInfo.put("model", config.getModel());
                    modelInfo.put("description", config.getDescription());
                    modelInfo.put("temperature", config.getTemperature());
                    metadata.put("modelInfo", modelInfo);
                }
                
                // 分析搜索结果的多样性
                if (!toolResponse.movies().isEmpty()) {
                    Set<String> genres = new HashSet<>();
                    Set<Integer> years = new HashSet<>();
                    double avgRating = toolResponse.movies().stream()
                            .mapToDouble(movie -> {
                                try {
                                    return Double.parseDouble(movie.getRating());
                                } catch (Exception e) {
                                    return 0.0;
                                }
                            })
                            .average().orElse(0.0);
                    
                    toolResponse.movies().forEach(movie -> {
                        if (movie.getGenres() != null) {
                            genres.addAll(Arrays.asList(movie.getGenres().split(", ")));
                        }
                        if (movie.getPublicationYear() != null) {
                            years.add(movie.getPublicationYear());
                        }
                    });
                    
                    metadata.put("genreCount", genres.size());
                    metadata.put("yearRange", years.size());
                    metadata.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
                    metadata.put("topGenres", genres.stream().limit(3).collect(Collectors.toList()));
                }
                
                enhancedResponse.put("metadata", metadata);
                
                // 添加搜索建议
                List<String> suggestions = generateSearchSuggestions(userMessage, toolResponse.movies());
                enhancedResponse.put("suggestions", suggestions);

                return ResponseEntity.ok(enhancedResponse);
            } catch (Exception e) {
                logger.debug("解析指定模型返回结果失败: {}", e.getMessage());
                
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("query", userMessage);
                errorResponse.put("usedModel", modelName);
                errorResponse.put("movies", Collections.emptyList());
                errorResponse.put("error", "Failed to parse AI response");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(errorResponse);
            }
        } catch (Exception e) {
            logger.error("指定模型AI工具调用失败: {} (模型: {})", userMessage, modelName, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("query", userMessage);
            errorResponse.put("usedModel", modelName);
            errorResponse.put("movies", Collections.emptyList());
            errorResponse.put("error", "AI tool call failed: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ==================================================
    // 直接调用接口端点
    // ==================================================

    /**
     * 通过自然语言查询电影
     */
    @Operation(summary = "通过API查询电影(直接调用接口)")
    @GetMapping("/api/movie/query")
    public ResponseEntity<Map<String, Object>> queryMovies(
            @Parameter(description = "查询语句，如'我想看今年的爱情片电影'") @RequestParam("query") String query,
            @Parameter(description = "语言代码，默认en") @RequestParam(value = "language", required = false, defaultValue = "en") String language) {

        try {
            MovieListResponseVo responseVo = movieSearchService.searchMoviesByQueryForVo(query, language);

            // 创建返回结果Map
            Map<String, Object> result = new HashMap<>();
            result.put("retCode", responseVo.getRetCode());
            result.put("message", responseVo.getMessage());
            result.put("totalCount", responseVo.getTotalCount());
            result.put("count", responseVo.getCount());
            result.put("movies", responseVo.getResult());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to search movies by query: {}", query, e);

            // 创建错误返回
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("retCode", 500);
            errorResult.put("message", "查询失败: " + e.getMessage());
            errorResult.put("movies", new ArrayList<>());

            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 根据年份和类型搜索电影
     */
    @Operation(summary = "根据年份和类型搜索电影")
    @GetMapping("/api/movie/search-by-year")
    public ResponseEntity<Map<String, Object>> searchMoviesByYear(
            @Parameter(description = "电影年份，如'2024'") @RequestParam("year") String year,
            @Parameter(description = "电影类型，如'Action'（可选）") @RequestParam(value = "genre", required = false) String genre,
            @Parameter(description = "语言代码，默认en") @RequestParam(value = "language", required = false, defaultValue = "en") String language) {

        try {
            MovieListResponseVo responseVo = movieSearchService.searchMoviesByYearAndGenreForVo(year, genre, language);

            // 创建返回结果Map
            Map<String, Object> result = new HashMap<>();
            result.put("retCode", responseVo.getRetCode());
            result.put("message", responseVo.getMessage());
            result.put("totalCount", responseVo.getTotalCount());
            result.put("count", responseVo.getCount());
            result.put("movies", responseVo.getResult());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to search movies by year: {} and genre: {}", year, genre, e);

            // 创建错误返回
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("retCode", 500);
            errorResult.put("message", "查询失败: " + e.getMessage());
            errorResult.put("movies", new ArrayList<>());

            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 获取本周热门电影
     */
    @Operation(summary = "获取本周热门电影")
    @GetMapping("/api/movie/popular-this-week")
    public ResponseEntity<Map<String, Object>> getPopularMoviesThisWeek(
            @Parameter(description = "语言代码，默认en") @RequestParam(value = "language", required = false, defaultValue = "en") String language) {

        try {
            MovieListResponseVo responseVo = movieSearchService.getPopularMoviesThisWeekForVo(language);

            // 创建返回结果Map
            Map<String, Object> result = new HashMap<>();
            result.put("retCode", responseVo.getRetCode());
            result.put("message", responseVo.getMessage());
            result.put("totalCount", responseVo.getTotalCount());
            result.put("count", responseVo.getCount());
            result.put("movies", responseVo.getResult());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Failed to get popular movies", e);

            // 创建错误返回
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("retCode", 500);
            errorResult.put("message", "查询失败: " + e.getMessage());
            errorResult.put("movies", new ArrayList<>());

            return ResponseEntity.status(500).body(errorResult);
        }
    }
    
    /**
     * 生成搜索建议
     */
    private List<String> generateSearchSuggestions(String originalQuery, List<MovieDetailVo> results) {
        List<String> suggestions = new ArrayList<>();
        
        if (results.isEmpty()) {
            suggestions.add("尝试更通用的关键词");
            suggestions.add("检查拼写是否正确");
            suggestions.add("浏览热门电影");
            suggestions.add("尝试不同的电影类型");
        } else {
            // 基于结果生成相关建议
            Set<String> genres = new HashSet<>();
            Set<Integer> years = new HashSet<>();
            
            results.forEach(movie -> {
                if (movie.getGenres() != null) {
                    String[] movieGenres = movie.getGenres().split(", ");
                    for (String genre : movieGenres) {
                        genres.add(genre.trim());
                    }
                }
                if (movie.getPublicationYear() != null) {
                    years.add(movie.getPublicationYear());
                }
            });
            
            // 添加类型建议
            genres.stream().limit(2).forEach(genre -> 
                suggestions.add("更多 " + genre + " 类型电影"));
            
            // 添加年份建议
            years.stream().limit(2).forEach(year -> 
                suggestions.add(year + " 年的电影"));
            
            suggestions.add("相似电影推荐");
            suggestions.add("同类型高评分电影");
        }
        
        return suggestions.stream().limit(4).collect(Collectors.toList());
    }
}