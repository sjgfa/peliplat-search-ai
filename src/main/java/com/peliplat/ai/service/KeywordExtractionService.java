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

/**
 * 关键词提取服务
 * 使用AI从用户查询中提取关键词并翻译成英文
 */
@Service
public class KeywordExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(KeywordExtractionService.class);

    private static final String API_KEY = "sk-126e71dd47804a04980ad84d8323c7c8";
    private static final String MODEL_NAME = "qwen-plus";

    /**
     * 从用户查询中提取关键词并翻译成英文
     *
     * @param userQuery 用户查询（可能是中文或英文）
     * @return 提取的英文关键词，如果提取失败返回null
     */
    public String extractKeyword(String userQuery) {
        try {
            logger.info("🔍 开始提取关键词: {}", userQuery);

            String prompt = String.format("""
                Extract the most relevant movie genre or keyword from the user query and output ONLY in English.

                IMPORTANT RULES:
                1. Output MUST be in English only (never Chinese or other languages)
                2. Return only ONE keyword or short phrase (maximum 2-3 words)
                3. Remove generic words like "电影", "movie", "film"
                4. If input is Chinese, translate to English equivalent
                5. No explanations, quotes, or extra text - just the keyword

                Examples:
                Input: "搞笑电影" → Output: funny
                Input: "科幻电影" → Output: science fiction
                Input: "动作片" → Output: action
                Input: "爱国主义电影" → Output: patriotic
                Input: "恐怖片" → Output: horror
                Input: "浪漫爱情电影" → Output: romantic
                Input: "推荐一些悬疑电影" → Output: mystery
                Input: "thriller movies" → Output: thriller
                Input: "推荐喜剧" → Output: comedy

                User Query: %s

                English Keyword:
                """, userQuery);

            GenerationParam param = GenerationParam.builder()
                    .apiKey(API_KEY)
                    .model(MODEL_NAME)
                    .prompt(prompt)
                    .topP(0.8)
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            String keyword = result.getOutput().getText().trim();

            // 清理结果，去除可能的引号、换行等
            keyword = keyword.replaceAll("[\"'`\n\r]", "").trim();

            logger.info("✅ 提取到关键词: {} → {}", userQuery, keyword);
            return keyword;

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("❌ 提取关键词失败: {}", userQuery, e);
            return null;
        }
    }

    /**
     * 提取多个关键词（用于复杂查询）
     *
     * @param userQuery 用户查询
     * @return 提取的英文关键词数组
     */
    public String[] extractKeywords(String userQuery) {
        try {
            logger.info("🔍 开始提取多个关键词: {}", userQuery);

            String prompt = String.format("""
                Extract ALL relevant movie genres or keywords from the user query and output ONLY in English.

                IMPORTANT RULES:
                1. Output MUST be in English only (never Chinese or other languages)
                2. Return 1-3 most relevant keywords
                3. Separate keywords with commas
                4. Remove generic words like "电影", "movie", "film"
                5. If input is Chinese, translate to English equivalent
                6. No explanations, quotes, or extra text - just keywords

                Examples:
                Input: "推荐一些搞笑的科幻电影" → Output: funny,science fiction
                Input: "最近的动作冒险电影" → Output: action,adventure
                Input: "恐怖悬疑片" → Output: horror,mystery

                User Query: %s

                English Keywords (comma-separated):
                """, userQuery);

            GenerationParam param = GenerationParam.builder()
                    .apiKey(API_KEY)
                    .model(MODEL_NAME)
                    .prompt(prompt)
                    .topP(0.8)
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            String keywordsStr = result.getOutput().getText().trim();

            // 清理结果并分割
            keywordsStr = keywordsStr.replaceAll("[\"'`\n\r]", "").trim();
            String[] keywords = keywordsStr.split("[,，]");

            // 清理每个关键词
            for (int i = 0; i < keywords.length; i++) {
                keywords[i] = keywords[i].trim();
            }

            logger.info("✅ 提取到多个关键词: {} → {}", userQuery, String.join(", ", keywords));
            return keywords;

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("❌ 提取多个关键词失败: {}", userQuery, e);
            return new String[]{};
        }
    }
}
