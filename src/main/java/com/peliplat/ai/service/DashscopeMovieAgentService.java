package com.peliplat.ai.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 阿里云DashScope电影推荐AI服务
 * 负责调用AI API进行电影推荐，提取电影名称
 */
@Service
public class DashscopeMovieAgentService {

    private static final Logger logger = LoggerFactory.getLogger(DashscopeMovieAgentService.class);

    private static final String API_KEY = "sk-126e71dd47804a04980ad84d8323c7c8";
    private static final String MODEL_NAME = "qwen-plus";
    
    /**
     * 电影推荐结果包装类
     */
    public static class MovieRecommendationResult {
        private List<String> movieNames;
        private String explanation;
        private boolean hasMoreContent;
        
        public MovieRecommendationResult(List<String> movieNames, String explanation, boolean hasMoreContent) {
            this.movieNames = movieNames;
            this.explanation = explanation;
            this.hasMoreContent = hasMoreContent;
        }
        
        // Getters
        public List<String> getMovieNames() { return movieNames; }
        public String getExplanation() { return explanation; }
        public boolean isHasMoreContent() { return hasMoreContent; }
    }
    
    /**
     * 调用AI获取电影推荐
     * @param userPrompt 用户查询内容
     * @return 异步结果包含电影名称列表和解释内容
     */
    public CompletableFuture<MovieRecommendationResult> streamMovieRecommendation(String userPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("🎬 开始调用AI进行电影推荐，用户查询: {}", userPrompt);

                // 构建prompt，判断用户意图并提取电影名称
                String prompt = String.format("""
                    You are a movie search intent analyzer. Analyze if the user wants to search for specific movie(s) or wants general recommendations.

                    IMPORTANT RULES:
                    1. If user wants to search for SPECIFIC movie(s) by name: return the movie title(s) in English
                    2. If user wants RECOMMENDATIONS or asks about a GENRE: return "no"
                    3. For specific movies, translate Chinese names to official English titles
                    4. No explanations, no quotes, no extra text

                    Examples:
                    Input: "海绵宝宝" → Output: SpongeBob SquarePants (specific movie search)
                    Input: "搞笑电影" → Output: no (genre recommendation)
                    Input: "推荐一些科幻电影" → Output: no (asking for recommendations)
                    Input: "哈利波特" → Output: Harry Potter (specific movie search)
                    Input: "好看的动作片" → Output: no (genre recommendation)
                    Input: "复仇者联盟" → Output: Avengers (specific movie search)
                    Input: "恐怖片推荐" → Output: no (asking for recommendations)

                    User Query: %s

                    Output:
                    """, userPrompt);

                // 构建AI调用参数
                GenerationParam param = GenerationParam.builder()
                        .apiKey(API_KEY)
                        .model(MODEL_NAME)
                        .prompt(prompt)
                        .topP(0.8)
                        .build();

                Generation generation = new Generation();
                GenerationResult result = generation.call(param);

                String fullResponse = result.getOutput().getText().trim();
                logger.debug("📥 收到AI响应: {}", fullResponse);

                // 清理响应（去除引号、换行等）
                String cleanResponse = fullResponse.replaceAll("[\"'`\n\r]", "").trim();

                List<String> extractedMovies = new ArrayList<>();

                // 判断AI的返回
                if ("no".equalsIgnoreCase(cleanResponse)) {
                    // AI识别到用户不是想搜索具体电影，而是要推荐
                    logger.info("✅ AI判断：用户想要推荐电影，不是搜索具体电影");
                    return new MovieRecommendationResult(extractedMovies, "", false);
                } else {
                    // AI提取到了具体的电影名称
                    if (!cleanResponse.isEmpty()) {
                        extractedMovies.add(cleanResponse);
                    }
                    logger.info("✅ AI判断：用户想搜索具体电影 - {}", cleanResponse);
                    return new MovieRecommendationResult(extractedMovies, "", true);
                }

            } catch (NoApiKeyException e) {
                logger.error("❌ API密钥错误", e);
                return new MovieRecommendationResult(new ArrayList<>(), "API密钥配置错误", false);
            } catch (InputRequiredException e) {
                logger.error("❌ 输入参数错误", e);
                return new MovieRecommendationResult(new ArrayList<>(), "输入参数不完整", false);
            } catch (ApiException e) {
                logger.error("❌ API调用失败", e);
                return new MovieRecommendationResult(new ArrayList<>(), "AI服务暂时不可用", false);
            } catch (Exception e) {
                logger.error("❌ AI调用出现未知错误", e);
                return new MovieRecommendationResult(new ArrayList<>(), "服务出现异常，请稍后重试", false);
            }
        });
    }
    
    /**
     * 从响应文本中提取电影名称数组
     * 支持多种格式：["电影1", "电影2"] 或 [电影1, 电影2] 等
     */
    private List<String> extractMovieNames(String text) {
        List<String> movieNames = new ArrayList<>();
        
        try {
            // 匹配电影数组的正则表达式 - 支持带引号和不带引号的格式
            Pattern arrayPattern = Pattern.compile("\\[([^\\]]+)\\]");
            Matcher matcher = arrayPattern.matcher(text);
            
            if (matcher.find()) {
                String arrayContent = matcher.group(1);
                logger.debug("📋 提取到数组内容: {}", arrayContent);
                
                // 分割电影名称，支持多种分隔符
                String[] movies = arrayContent.split("[,，]");
                
                for (String movie : movies) {
                    // 清理电影名称：去除引号、空格等
                    String cleanMovie = movie.trim()
                            .replaceAll("^\"|\"$", "")  // 去除首尾双引号
                            .replaceAll("^'|'$", "")    // 去除首尾单引号
                            .replaceAll("^[\\u201C\\u201D]|[\\u201C\\u201D]$", "")    // 去除中文引号
                            .trim();
                    
                    if (!cleanMovie.isEmpty()) {
                        movieNames.add(cleanMovie);
                        logger.debug("🎬 提取到电影: {}", cleanMovie);
                    }
                }
            }
            
            logger.info("📊 总共提取到 {} 部电影: {}", movieNames.size(), movieNames);
            
        } catch (Exception e) {
            logger.warn("⚠️ 提取电影名称时发生错误", e);
        }
        
        return movieNames;
    }
    
    /**
     * 从完整响应中提取解释说明部分
     * 去除电影数组，保留推荐理由和详细说明
     */
    private String extractExplanation(String fullText) {
        try {
            // 查找第一个数组结束位置
            int arrayEndIndex = fullText.indexOf("]");
            if (arrayEndIndex != -1 && arrayEndIndex + 1 < fullText.length()) {
                String explanation = fullText.substring(arrayEndIndex + 1).trim();
                
                // 去除可能的换行符开头
                if (explanation.startsWith("\n")) {
                    explanation = explanation.substring(1).trim();
                }
                
                return explanation;
            }
        } catch (Exception e) {
            logger.warn("⚠️ 提取解释内容时发生错误", e);
        }
        
        // 如果提取失败，返回原文本
        return fullText;
    }
    
    /**
     * 简单的同步调用方法（用于测试）
     */
    public MovieRecommendationResult getMovieRecommendation(String userPrompt) {
        try {
            return streamMovieRecommendation(userPrompt).get();
        } catch (Exception e) {
            logger.error("❌ 同步获取电影推荐失败", e);
            return new MovieRecommendationResult(new ArrayList<>(), "获取推荐失败", false);
        }
    }

    /**
     * 推荐多部电影（用于用户想要推荐而不是搜索具体电影时）
     * @param userQuery 用户查询
     * @return 推荐的电影名称列表
     */
    public List<String> recommendMovies(String userQuery) {
        try {
            logger.info("🎬 开始推荐电影，用户查询: {}", userQuery);

            String prompt = String.format("""
                You are a professional movie recommendation expert. Based on the user's query, recommend 5-10 most relevant movies.

                IMPORTANT RULES:
                1. Output ONLY movie titles in English, one per line
                2. Movie titles MUST be the official English titles
                3. No numbering, no explanations, no extra text
                4. Return 5-10 movies that best match the user's preferences

                Examples:
                User Query: "搞笑电影"
                Output:
                The Grand Budapest Hotel
                Superbad
                Airplane!
                Monty Python and the Holy Grail
                Hot Fuzz

                User Query: "科幻电影"
                Output:
                Interstellar
                The Matrix
                Inception
                Blade Runner 2049
                Arrival

                User Query: %s

                Output:
                """, userQuery);

            GenerationParam param = GenerationParam.builder()
                    .apiKey(API_KEY)
                    .model(MODEL_NAME)
                    .prompt(prompt)
                    .topP(0.8)
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            String fullResponse = result.getOutput().getText().trim();
            logger.debug("📥 收到AI推荐: {}", fullResponse);

            // 按行分割电影名称
            List<String> movieList = new ArrayList<>();
            String[] lines = fullResponse.split("\n");
            for (String line : lines) {
                String cleanLine = line.trim().replaceAll("^[0-9]+[.、]\\s*", "").replaceAll("[\"'`]", "").trim();
                if (!cleanLine.isEmpty() && !cleanLine.matches("^[Oo]utput:?.*")) {
                    movieList.add(cleanLine);
                }
            }

            logger.info("✅ AI推荐完成，共推荐 {} 部电影", movieList.size());
            return movieList;

        } catch (Exception e) {
            logger.error("❌ AI推荐电影失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从多个电影标题中过滤出符合用户意图的电影
     * @param userQuery 用户原始查询
     * @param movieTitles 电影标题列表
     * @return 符合条件的电影标题列表
     */
    public List<String> filterMatchingMovies(String userQuery, List<String> movieTitles) {
        try {
            logger.info("🎯 开始过滤符合条件的电影，用户查询: {}, 候选电影数: {}", userQuery, movieTitles.size());

            // 构建候选电影列表字符串
            StringBuilder candidateList = new StringBuilder();
            for (int i = 0; i < movieTitles.size(); i++) {
                candidateList.append(i + 1).append(". ").append(movieTitles.get(i)).append("\n");
            }

            String prompt = String.format("""
                You are a movie filtering expert. Based on the user's query, filter out movies that match the user's intent from the candidate list.

                IMPORTANT RULES:
                1. Return ALL movie titles that match the user's intent (not just one)
                2. Filter OUT movies that are clearly NOT related to the user's query
                3. Return movie titles one per line, using EXACT titles from the candidate list
                4. No numbering, no explanations, no extra text
                5. If user searches for a specific movie/series, include ALL related movies (sequels, prequels, spin-offs)

                Examples:
                User Query: "海绵宝宝" (SpongeBob)
                Candidates include: "The SpongeBob Movie", "SpongeBob SquarePants: Sponge on the Run", "Finding Nemo", "The Lion King"
                Output:
                The SpongeBob Movie
                SpongeBob SquarePants: Sponge on the Run

                User Query: "哈利波特" (Harry Potter)
                Candidates include: "Harry Potter and the Sorcerer's Stone", "Harry Potter and the Chamber of Secrets", "Lord of the Rings"
                Output:
                Harry Potter and the Sorcerer's Stone
                Harry Potter and the Chamber of Secrets

                User Query: %s

                Candidate Movies:
                %s

                Matching Movies (one per line):
                """, userQuery, candidateList.toString());

            GenerationParam param = GenerationParam.builder()
                    .apiKey(API_KEY)
                    .model(MODEL_NAME)
                    .prompt(prompt)
                    .topP(0.8)
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            String fullResponse = result.getOutput().getText().trim();
            logger.debug("📥 收到AI过滤结果: {}", fullResponse);

            // 按行分割电影名称
            List<String> filteredMovies = new ArrayList<>();
            String[] lines = fullResponse.split("\n");
            for (String line : lines) {
                String cleanLine = line.trim()
                    .replaceAll("^[0-9]+[.、]\\s*", "")  // 移除编号
                    .replaceAll("[\"'`]", "")            // 移除引号
                    .trim();

                // 检查是否是候选列表中的电影
                if (!cleanLine.isEmpty() && movieTitles.contains(cleanLine)) {
                    filteredMovies.add(cleanLine);
                }
            }

            logger.info("✅ AI过滤完成，保留 {} 部电影: {}", filteredMovies.size(), filteredMovies);

            // 如果AI没有返回任何结果，返回所有候选
            if (filteredMovies.isEmpty()) {
                logger.warn("⚠️ AI未返回任何匹配电影，返回所有候选");
                return movieTitles;
            }

            return filteredMovies;

        } catch (Exception e) {
            logger.error("❌ AI过滤电影失败", e);
            // 失败时返回所有候选
            return movieTitles;
        }
    }
}