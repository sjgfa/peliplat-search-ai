//package com.peliplat.ai.controller;
//
//import com.peliplat.ai.model.MediaDetailVo;
//import com.peliplat.ai.model.MovieDetailVo;
//import com.peliplat.ai.model.MovieListResponseVo;
//import com.peliplat.ai.model.SearchResultVo;
//import com.peliplat.ai.movie.MovieTools;
//import com.peliplat.ai.service.MovieSearchService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
//import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
//import org.springframework.ai.chat.memory.InMemoryChatMemory;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.ai.chat.messages.SystemMessage;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.ai.openai.OpenAiChatOptions;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * 电影工具控制器
// * 提供基于@Tool注解的电影查询API以及直接查询API
// */
//@RestController
//@Tag(name = "电影API", description = "提供电影查询相关的所有服务")
//@CrossOrigin(origins = "*", allowedHeaders = "*")
//public class MovieToolController_backup {
//    private static final Logger logger = LoggerFactory.getLogger(MovieToolController.class);
//
//    private final ChatClient deepSeekChatClient;
//    private final MovieSearchService movieSearchService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Autowired
//    public MovieToolController(OpenAiChatModel chatModel,
//            @Qualifier("peliplatMovieSearchService") MovieSearchService movieSearchService) {
//        this.movieSearchService = movieSearchService;
//
//        // 构建ChatClient，与原MovieController保持一致
//        this.deepSeekChatClient = ChatClient.builder(chatModel)
////                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
//                // 实现 Logger 的 Advisor
////                .defaultAdvisors(new SimpleLoggerAdvisor())
//                // 设置 ChatClient 中 ChatModel 的 Options 参数
////                .defaultOptions(OpenAiChatOptions.builder().temperature(0.7d).build())
//                .build();
//    }
//
//    // ==================================================
//    // 直接调用接口端点
//    // ==================================================
//
//    /**
//     * 通过自然语言查询电影
//     */
//    @Operation(summary = "通过API查询电影(直接调用接口)")
//    @GetMapping("/api/movie/query")
//    public ResponseEntity<Map<String, Object>> queryMovies(
//            @Parameter(description = "查询语句，如'我想看今年的爱情片电影'") @RequestParam("query") String query,
//            @Parameter(description = "语言代码，默认en") @RequestParam(value = "language", required = false, defaultValue = "en") String language) {
//
//        try {
//            MovieListResponseVo responseVo = movieSearchService.searchMoviesByQueryForVo(query, language);
//
//            // 创建返回结果Map
//            Map<String, Object> result = new HashMap<>();
//            result.put("retCode", responseVo.getRetCode());
//            result.put("message", responseVo.getMessage());
//            result.put("totalCount", responseVo.getTotalCount());
//            result.put("count", responseVo.getCount());
//            result.put("movies", responseVo.getResult());
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            logger.error("Failed to search movies by query: {}", query, e);
//
//            // 创建错误返回
//            Map<String, Object> errorResult = new HashMap<>();
//            errorResult.put("retCode", 500);
//            errorResult.put("message", "查询失败: " + e.getMessage());
//            errorResult.put("movies", new ArrayList<>());
//
//            return ResponseEntity.status(500).body(errorResult);
//        }
//    }
//
//    /**
//     * 根据年份和类型搜索电影
//     */
//    @Operation(summary = "根据年份和类型搜索电影")
//    @GetMapping("/api/movie/search-by-year")
//    public ResponseEntity<Map<String, Object>> searchMoviesByYear(
//            @Parameter(description = "电影年份，如'2024'") @RequestParam("year") String year,
//            @Parameter(description = "电影类型，如'Action'（可选）") @RequestParam(value = "genre", required = false) String genre,
//            @Parameter(description = "语言代码，默认en") @RequestParam(value = "language", required = false, defaultValue = "en") String language) {
//
//        try {
//            MovieListResponseVo responseVo = movieSearchService.searchMoviesByYearAndGenreForVo(year, genre, language);
//
//            // 创建返回结果Map
//            Map<String, Object> result = new HashMap<>();
//            result.put("retCode", responseVo.getRetCode());
//            result.put("message", responseVo.getMessage());
//            result.put("totalCount", responseVo.getTotalCount());
//            result.put("count", responseVo.getCount());
//            result.put("movies", responseVo.getResult());
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            logger.error("Failed to search movies by year: {} and genre: {}", year, genre, e);
//
//            // 创建错误返回
//            Map<String, Object> errorResult = new HashMap<>();
//            errorResult.put("retCode", 500);
//            errorResult.put("message", "查询失败: " + e.getMessage());
//            errorResult.put("movies", new ArrayList<>());
//
//            return ResponseEntity.status(500).body(errorResult);
//        }
//    }
//
//    /**
//     * 获取本周热门电影
//     */
//    @Operation(summary = "获取本周热门电影")
//    @GetMapping("/api/movie/popular-this-week")
//    public ResponseEntity<Map<String, Object>> getPopularMoviesThisWeek(
//            @Parameter(description = "语言代码，默认en") @RequestParam(value = "language", required = false, defaultValue = "en") String language) {
//
//        try {
//            MovieListResponseVo responseVo = movieSearchService.getPopularMoviesThisWeekForVo(language);
//
//            // 创建返回结果Map
//            Map<String, Object> result = new HashMap<>();
//            result.put("retCode", responseVo.getRetCode());
//            result.put("message", responseVo.getMessage());
//            result.put("totalCount", responseVo.getTotalCount());
//            result.put("count", responseVo.getCount());
//            result.put("movies", responseVo.getResult());
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            logger.error("Failed to get popular movies", e);
//
//            // 创建错误返回
//            Map<String, Object> errorResult = new HashMap<>();
//            errorResult.put("retCode", 500);
//            errorResult.put("message", "查询失败: " + e.getMessage());
//            errorResult.put("movies", new ArrayList<>());
//
//            return ResponseEntity.status(500).body(errorResult);
//        }
//    }
//
//    // ==================================================
//    // @Tool注解方式的API端点
//    // ==================================================
//
//    /**
//     * 电影工具综合接口
//     * 使用所有电影相关的工具方法
//     */
//    @Operation(summary = "电影工具综合接口（搜索、按年份搜索、热门电影）")
//    @PostMapping("/api/movie-tool/chat")
//    public ResponseEntity<Map<String, Object>> chatWithMovieTools(
//            @Parameter(description = "用户消息内容") @RequestParam("userMessage") String userMessage) {
//
//        try {
//            logger.info("收到电影工具请求: {}", userMessage);
//            long startTime = System.currentTimeMillis();
//
//            // 系统提示词（非常重要）
//            String systemPrompt = "你是一个智能电影助手。当用户想要搜索电影时，请仔细分析用户的输入。\n" +
//                    "如果用户提供的是电影的描述、角色特征（例如‘一部关于黄色海绵的电影’）或主题，而不是一个明确的电影标题，\n" +
//                    "请尝试推断出最可能的实际电影名称，然后再使用工具进行搜索。\n" +
//                    "例如，如果用户说‘找一部黄色海绵的动画片’，你应该尝试搜索《海绵宝宝》相关的电影，而不是直接搜索‘黄色海绵’。\n" +
//                    "如果无法准确推断，可以向用户提问以获取更多信息。,你总会给我总结出电影名称的列表来给我查询，而且你只会调用一次工具,工具的结果会直接返回给用户" +
//                    "下面是必须遵循的规则" +
//                    "- 1.你必须用英文去查询 " +
//                    "- 2.你必须理解用户的意图再去调用工具，用户可能描述的是很模糊的概念,你理解用户要看的电影后用电影名称查询" +
//                    "- 3.你必须调用工具" +
//                    "- 4.你能总结出电影名称的,用根据电影名称查询电影的接口来查询";
//
//            // 创建系统消息
//            Message systemMsg = new SystemMessage(systemPrompt);
//
//            // 创建用户消息
//            Message userMsg = new UserMessage(userMessage);
//
//            // 把系统提示词也放进去
//            String response = deepSeekChatClient.prompt(new Prompt(List.of(systemMsg, userMsg)))
//                    .tools(new MovieTools(movieSearchService))
//                    .call()
//                    .content();
//
//            logger.info("电影工具返回原始结果: {}", response);
//
//            try {
//                // 尝试将结果直接解析为MovieTools.Response对象
//                MovieTools.Response toolResponse = objectMapper.readValue(response, MovieTools.Response.class);
//
//                long endTime = System.currentTimeMillis();
//
//                // 构建增强的响应
//                Map<String, Object> enhancedResponse = new HashMap<>();
//                enhancedResponse.put("success", true);
//                enhancedResponse.put("query", userMessage);
//                enhancedResponse.put("movies", toolResponse.movies());
//                enhancedResponse.put("totalCount", toolResponse.movies().size());
//                enhancedResponse.put("responseTime", endTime - startTime);
//                enhancedResponse.put("timestamp", System.currentTimeMillis());
//
//                // 添加搜索统计和建议
//                Map<String, Object> metadata = new HashMap<>();
//                metadata.put("aiProcessed", true);
//                metadata.put("searchEngine", "AI-Powered");
//
//                // 分析搜索结果的多样性
//                if (!toolResponse.movies().isEmpty()) {
//                    Set<String> genres = new HashSet<>();
//                    Set<Integer> years = new HashSet<>();
//                    double avgRating = toolResponse.movies().stream()
//                            .mapToDouble(movie -> {
//                                try {
//                                    return Double.parseDouble(movie.getRating());
//                                } catch (Exception e) {
//                                    return 0.0;
//                                }
//                            })
//                            .average().orElse(0.0);
//
//                    toolResponse.movies().forEach(movie -> {
//                        if (movie.getGenres() != null) {
//                            genres.addAll(Arrays.asList(movie.getGenres().split(", ")));
//                        }
//                        if (movie.getPublicationYear() != null) {
//                            years.add(movie.getPublicationYear());
//                        }
//                    });
//
//                    metadata.put("genreCount", genres.size());
//                    metadata.put("yearRange", years.size());
//                    metadata.put("averageRating", Math.round(avgRating * 10.0) / 10.0);
//                    metadata.put("topGenres", genres.stream().limit(3).collect(Collectors.toList()));
//                }
//
//                enhancedResponse.put("metadata", metadata);
//
//                // 添加搜索建议
//                List<String> suggestions = generateSearchSuggestions(userMessage, toolResponse.movies());
//                enhancedResponse.put("suggestions", suggestions);
//
//                // 从Response中获取电影列表并直接返回
//                return ResponseEntity.ok(enhancedResponse);
//            } catch (Exception e) {
//                // 如果解析失败，返回错误响应
//                logger.debug("解析电影工具返回结果失败: {}", e.getMessage());
//
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("success", false);
//                errorResponse.put("query", userMessage);
//                errorResponse.put("movies", Collections.emptyList());
//                errorResponse.put("error", "Failed to parse AI response");
//                errorResponse.put("timestamp", System.currentTimeMillis());
//
//                return ResponseEntity.ok(errorResponse);
//            }
//        } catch (Exception e) {
//            logger.error("AI工具调用失败: {}", userMessage, e);
//
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("success", false);
//            errorResponse.put("query", userMessage);
//            errorResponse.put("movies", Collections.emptyList());
//            errorResponse.put("error", "AI tool call failed: " + e.getMessage());
//            errorResponse.put("timestamp", System.currentTimeMillis());
//
//            return ResponseEntity.status(500).body(errorResponse);
//        }
//    }
//
//    /**
//     * 生成搜索建议
//     */
//    private List<String> generateSearchSuggestions(String originalQuery, List<MovieDetailVo> results) {
//        List<String> suggestions = new ArrayList<>();
//
//        if (results.isEmpty()) {
//            // 如果没有结果，提供一些通用建议
//            suggestions.add("Try more general terms");
//            suggestions.add("Check spelling");
//            suggestions.add("Browse popular movies");
//            suggestions.add("Try different genres");
//        } else {
//            // 基于结果生成相关建议
//            Set<String> genres = new HashSet<>();
//            Set<Integer> years = new HashSet<>();
//
//            results.forEach(movie -> {
//                if (movie.getGenres() != null) {
//                    String[] movieGenres = movie.getGenres().split(", ");
//                    for (String genre : movieGenres) {
//                        genres.add(genre.trim());
//                    }
//                }
//                if (movie.getPublicationYear() != null) {
//                    years.add(movie.getPublicationYear());
//                }
//            });
//
//            // 添加类型建议
//            genres.stream().limit(2).forEach(genre ->
//                suggestions.add("More " + genre + " movies"));
//
//            // 添加年份建议
//            years.stream().limit(2).forEach(year ->
//                suggestions.add("Movies from " + year));
//
//            // 添加相关建议
//            suggestions.add("Similar movies");
//            suggestions.add("Top rated in this genre");
//        }
//
//        return suggestions.stream().limit(4).collect(Collectors.toList());
//    }
//
//}