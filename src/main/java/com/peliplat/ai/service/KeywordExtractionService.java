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

import java.util.List;

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
        TITLE,           // 搜索具体作品名称
        GENRE,           // 搜索类型/主题
        RECOMMENDATION   // 推荐相似电影
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
                   - RECOMMENDATION: Looking for movies SIMILAR to a specific movie (e.g., "movies like Pirates of the Caribbean", "类似加勒比海盗的电影")

                2. Then extract the keyword in English

                3. Output format MUST be exactly: INTENT|KEYWORD
                   - INTENT must be either "TITLE", "GENRE", or "RECOMMENDATION"
                   - KEYWORD must be in English only

                Examples for TITLE (specific movie search):
                Input: "我想看海贼王" → Output: TITLE|One Piece
                Input: "找盗梦空间" → Output: TITLE|Inception
                Input: "复仇者联盟" → Output: TITLE|Avengers
                Input: "鬼灭之刃" → Output: TITLE|Demon Slayer

                Examples for GENRE (genre/theme search):
                Input: "科幻电影" → Output: GENRE|science fiction
                Input: "搞笑电影" → Output: GENRE|comedy
                Input: "恐怖片" → Output: GENRE|horror
                Input: "动作片" → Output: GENRE|action
                Input: "推荐一些悬疑电影" → Output: GENRE|mystery

                Examples for RECOMMENDATION (similar movie recommendations):
                Input: "类似加勒比海盗的电影" → Output: RECOMMENDATION|Pirates of the Caribbean
                Input: "像盗梦空间那样的电影" → Output: RECOMMENDATION|Inception
                Input: "和复仇者联盟类似的电影" → Output: RECOMMENDATION|Avengers
                Input: "找一些类似哈利波特的电影" → Output: RECOMMENDATION|Harry Potter
                Input: "movies like The Matrix" → Output: RECOMMENDATION|The Matrix

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
                } else if ("RECOMMENDATION".equals(intentStr)) {
                    intent = SearchIntent.RECOMMENDATION;
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

    /**
     * AI验证关键词匹配度
     * 从候选关键词列表中选择最匹配用户查询的关键词
     *
     * @param userQuery 用户查询
     * @param extractedKeyword AI提取的关键词
     * @param candidateKeywords 候选关键词列表（来自自动完成API）
     * @return 最匹配的关键词，如果都不匹配返回null
     */
    public String validateKeywordMatch(String userQuery, String extractedKeyword, List<String> candidateKeywords) {
        if (candidateKeywords == null || candidateKeywords.isEmpty()) {
            logger.warn("⚠️ 候选关键词列表为空");
            return null;
        }

        // 移除"只有1个候选就直接返回"的逻辑，始终使用AI验证匹配度
        try {
            logger.info("🤖 开始AI关键词匹配验证: userQuery={}, extractedKeyword={}, candidates={}",
                       userQuery, extractedKeyword, candidateKeywords);

            String candidatesStr = String.join(", ", candidateKeywords);

            String prompt = String.format("""
                Your task is to select the BEST matching keyword from the candidate list based on user's query.

                User Query: "%s"
                Extracted Keyword: "%s"

                Candidate Keywords (from autocomplete API):
                %s

                RULES:
                1. Analyze which candidate keyword BEST matches the user's intent
                2. If NONE of the candidates match well, output: NONE
                3. If there's a good match, output ONLY the matching keyword from the list
                4. Do NOT output anything else, just the keyword or "NONE"

                Examples:
                Input: User Query: "科幻电影", Extracted: "science fiction", Candidates: ["Sci-Fi", "Drama", "Action"]
                Output: Sci-Fi

                Input: User Query: "搞笑电影", Extracted: "comedy", Candidates: ["Horror", "Thriller", "Mystery"]
                Output: NONE

                Input: User Query: "恐怖片", Extracted: "horror", Candidates: ["Horror", "Suspense", "Thriller"]
                Output: Horror

                Output (keyword or NONE):
                """, userQuery, extractedKeyword, candidatesStr);

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

            logger.info("🤖 AI匹配结果: {}", response);

            if ("NONE".equalsIgnoreCase(response)) {
                logger.warn("⚠️ AI判断所有候选关键词都不匹配用户意图");
                return null;
            }

            // 验证AI返回的关键词是否在候选列表中
            String finalResponse = response;
            boolean isValid = candidateKeywords.stream()
                    .anyMatch(keyword -> keyword.equalsIgnoreCase(finalResponse));

            if (isValid) {
                logger.info("✅ AI选择了最匹配的关键词: {}", response);
                return response;
            } else {
                logger.warn("⚠️ AI返回的关键词不在候选列表中: {}", response);
                return null;
            }

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("❌ AI关键词匹配验证失败", e);
            // 失败时返回第一个候选关键词作为降级方案
            return candidateKeywords.get(0);
        }
    }

    /**
     * 内容验证结果
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * AI验证用户输入是否合法
     * 检测输入长度、是否为无意义内容、是否与电影相关
     *
     * @param userQuery 用户查询
     * @return 验证结果（是否合法 + 提示消息）
     */
    public ValidationResult validateUserInput(String userQuery) {
        // 1. 检查长度
        if (userQuery.length() > 100) {
            logger.warn("⚠️ 用户输入超过100字符: length={}", userQuery.length());
            return new ValidationResult(false,
                "🚫 兄弟，你这是写作文呢？咱这是搜电影的，不是写小说的地方！请把内容控制在100字以内，简洁明了地说你想看啥电影！");
        }

        // 2. AI检测是否为有效的电影查询
        try {
            logger.info("🤖 开始AI输入验证: {}", userQuery);

            String prompt = String.format("""
                Analyze if the user's input is a VALID movie/TV show search query.

                User Input: "%s"

                RULES:
                1. Check if it's related to movies, TV shows, or entertainment
                2. Detect if it's nonsense, gibberish, random characters, or completely unrelated
                3. Output format: VALID or INVALID|reason

                VALID examples:
                - "我想看科幻电影" → VALID
                - "推荐恐怖片" → VALID
                - "One Piece" → VALID
                - "action movies" → VALID
                - "最近有什么好看的" → VALID

                INVALID examples:
                - "asdfghjkl" → INVALID|gibberish
                - "今天天气真好" → INVALID|unrelated
                - "1+1=2" → INVALID|unrelated
                - "帮我做作业" → INVALID|unrelated
                - "你好吗" → INVALID|unrelated
                - Random numbers/symbols → INVALID|gibberish

                Output (VALID or INVALID|reason):
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

            logger.info("🤖 AI验证结果: {}", response);

            if (response.toUpperCase().startsWith("VALID")) {
                logger.info("✅ 输入验证通过");
                return new ValidationResult(true, "OK");
            } else {
                // INVALID|reason格式
                String reason = "unknown";
                if (response.contains("|")) {
                    String[] parts = response.split("\\|");
                    if (parts.length > 1) {
                        reason = parts[1].trim();
                    }
                }

                logger.warn("⚠️ 输入验证失败: reason={}", reason);

                // 根据不同原因返回不同的"骂人"消息
                String message;
                if (reason.toLowerCase().contains("gibberish") || reason.toLowerCase().contains("random")) {
                    message = "🤨 大哥，你这是键盘乱按呢？还是猫在键盘上走了一圈？要搜电影就好好说话，别乱打字！";
                } else if (reason.toLowerCase().contains("unrelated")) {
                    message = "🙄 老铁，这里是搜电影的，不是闲聊的地方！你要找电影就说电影，别扯这些有的没的！";
                } else {
                    message = "😑 这输入我实在看不懂，你确定是在找电影吗？能不能说点人话？";
                }

                return new ValidationResult(false, message);
            }

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            logger.error("❌ AI输入验证失败", e);
            // 验证失败时允许通过，避免影响正常用户
            return new ValidationResult(true, "OK");
        }
    }
}
