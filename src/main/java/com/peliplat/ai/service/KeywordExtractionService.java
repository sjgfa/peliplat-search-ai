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
     * 搜索意图类型
     */
    public enum SearchIntent {
        TITLE,    // 搜索具体作品名称
        GENRE     // 搜索类型/主题
    }

    /**
     * 意图识别结果
     */
    public static class IntentResult {
        private SearchIntent intent;
        private String keyword;

        public IntentResult(SearchIntent intent, String keyword) {
            this.intent = intent;
            this.keyword = keyword;
        }

        public SearchIntent getIntent() {
            return intent;
        }

        public String getKeyword() {
            return keyword;
        }
    }

    /**
     * 识别用户搜索意图并提取关键词（智能版）
     *
     * @param userQuery 用户查询
     * @return 意图识别结果（包含意图类型和关键词）
     */
    public IntentResult extractKeywordWithIntent(String userQuery) {
        try {
            logger.info("🤖 开始AI意图识别: {}", userQuery);

            String prompt = String.format("""
                Analyze the user's search query and determine their intent, then extract the keyword.

                IMPORTANT RULES:
                1. First, determine if user is searching for:
                   - TITLE: A specific movie/TV show/anime name (e.g., "One Piece", "Inception", "Avengers")
                   - GENRE: A movie genre/theme/type (e.g., "horror", "comedy", "action")

                2. Then extract the keyword in English

                3. Output format MUST be exactly: INTENT|KEYWORD
                   - INTENT must be either "TITLE" or "GENRE"
                   - KEYWORD must be in English only

                Examples:
                Input: "我想看海贼王" → Output: TITLE|One Piece
                Input: "找盗梦空间" → Output: TITLE|Inception
                Input: "复仇者联盟" → Output: TITLE|Avengers
                Input: "鬼灭之刃" → Output: TITLE|Demon Slayer
                Input: "科幻电影" → Output: GENRE|science fiction
                Input: "搞笑电影" → Output: GENRE|comedy
                Input: "恐怖片" → Output: GENRE|horror
                Input: "动作片" → Output: GENRE|action
                Input: "推荐一些悬疑电影" → Output: GENRE|mystery

                User Query: %s

                Output (INTENT|KEYWORD):
                """, userQuery);

            GenerationParam param = GenerationParam.builder()
                    .apiKey(API_KEY)
                    .model(MODEL_NAME)
                    .prompt(prompt)
                    .topP(0.8)
                    .build();

            Generation generation = new Generation();
            GenerationResult result = generation.call(param);

            String response = result.getOutput().getText().trim();
            response = response.replaceAll("[\"'`\n\r]", "").trim();

            logger.info("🤖 AI原始响应: {}", response);

            // 解析响应 INTENT|KEYWORD
            String[] parts = response.split("\\|");
            if (parts.length == 2) {
                String intentStr = parts[0].trim().toUpperCase();
                String keyword = parts[1].trim();

                SearchIntent intent;
                if ("TITLE".equals(intentStr)) {
                    intent = SearchIntent.TITLE;
                } else if ("GENRE".equals(intentStr)) {
                    intent = SearchIntent.GENRE;
                } else {
                    // 默认按GENRE处理
                    logger.warn("⚠️ 无法识别意图类型: {}, 默认使用GENRE", intentStr);
                    intent = SearchIntent.GENRE;
                }

                logger.info("✅ 意图识别成功: {} → 意图={}, 关键词={}", userQuery, intent, keyword);
                return new IntentResult(intent, keyword);
            } else {
                logger.error("❌ AI响应格式错误: {}", response);
                return null;
            }

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("❌ AI意图识别失败: {}", userQuery, e);
            return null;
        }
    }

    /**
     * 从用户查询中提取关键词并翻译成英文（旧版本，保持兼容）
     *
     * @param userQuery 用户查询（可能是中文或英文）
     * @return 提取的英文关键词，如果提取失败返回null
     */
    public String extractKeyword(String userQuery) {
        try {
            logger.info("🔍 开始提取关键词: {}", userQuery);

            String prompt = String.format("""
                Analyze the user query and extract the most relevant keyword. You MUST distinguish between specific titles and genres.

                IMPORTANT RULES:
                1. Output MUST be in English only (never Chinese or other languages)
                2. **Distinguish between TITLE and GENRE**:
                   - If user mentions a SPECIFIC movie/TV show/anime title → translate to its ORIGINAL English/international name
                   - If user asks for a GENRE/theme → extract the genre keyword
                3. Remove generic words like "电影", "movie", "film", "我想看", "推荐"
                4. No explanations, quotes, or extra text - just the keyword

                Examples (TITLES - translate to original name):
                Input: "我想看海贼王" → Output: One Piece
                Input: "找复仇者联盟" → Output: Avengers
                Input: "盗梦空间" → Output: Inception
                Input: "权力的游戏" → Output: Game of Thrones
                Input: "流浪地球" → Output: Wandering Earth
                Input: "哪吒之魔童降世" → Output: Ne Zha
                Input: "鬼灭之刃" → Output: Demon Slayer
                Input: "进击的巨人" → Output: Attack on Titan

                Examples (GENRES - extract genre):
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
                Analyze the user query and extract ALL relevant keywords. You MUST distinguish between specific titles and genres.

                IMPORTANT RULES:
                1. Output MUST be in English only (never Chinese or other languages)
                2. **Distinguish between TITLE and GENRE**:
                   - If user mentions a SPECIFIC movie/TV show/anime title → translate to its ORIGINAL English/international name
                   - If user asks for GENRES/themes → extract genre keywords
                3. Return 1-3 most relevant keywords, separated by commas
                4. Remove generic words like "电影", "movie", "film", "我想看", "推荐"
                5. No explanations, quotes, or extra text - just keywords

                Examples (TITLES):
                Input: "我想看海贼王" → Output: One Piece
                Input: "找一些像盗梦空间的电影" → Output: Inception
                Input: "复仇者联盟系列" → Output: Avengers

                Examples (GENRES):
                Input: "推荐一些搞笑的科幻电影" → Output: funny,science fiction
                Input: "最近的动作冒险电影" → Output: action,adventure
                Input: "恐怖悬疑片" → Output: horror,mystery

                Examples (MIXED):
                Input: "像海贼王一样的冒险动漫" → Output: One Piece,adventure
                Input: "复仇者联盟那样的超级英雄电影" → Output: Avengers,superhero

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
