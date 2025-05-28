package com.peliplat.ai.movie;

import com.peliplat.ai.model.MovieDetailVo;
import com.peliplat.ai.model.MovieListResponseVo;
import com.peliplat.ai.service.MovieSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 电影工具类
 * 提供电影搜索、按年份搜索和获取热门电影的功能
 */
public class MovieTools {

    private static final Logger logger = LoggerFactory.getLogger(MovieTools.class);

    private final MovieSearchService movieSearchService;

    public MovieTools(@Qualifier("peliplatMovieSearchService") MovieSearchService movieSearchService) {
        this.movieSearchService = movieSearchService;
    }

    /**
     * 搜索电影
     * 根据查询语句搜索电影
     */
    @Tool(description = "根据影片名称搜索电影，查询多部电影的时候优先调用concurrentSearchMovies。如果查询包含地区信息（如'中国电影'、'韩国电影'），请相应调整language参数", returnDirect = true)
    public Response searchMovies(
            @ToolParam(description = "要搜索的电影的准确或常用标题。如果用户提供的是描述性内容，请先尝试推断出实际的电影标题") String query,
            @ToolParam(description = "电影信息显示的语言代码：中国电影用'zh'，韩国电影用'ko'，日本电影用'ja'，英语电影用'en'，默认为'en'") String language) {

        try {
            if (!StringUtils.hasText(query)) {
                logger.error("Invalid request: query is required");
                return new Response(new ArrayList<>());
            }

            logger.info("电影搜索请求: {}, 语言: {}", query, language);

            String languageCode = language != null ? language : "en";
            // 使用新的ForVo方法
            MovieListResponseVo responseVo = movieSearchService.searchMoviesByQueryForVo(query, languageCode);

            if (responseVo.getResult() == null || responseVo.getResult().isEmpty()) {
                return new Response(new ArrayList<>());
            } else {
                // 直接返回电影对象列表
                return new Response(responseVo.getResult());
            }
        } catch (Exception e) {
            logger.error("电影搜索失败", e);
            return new Response(new ArrayList<>());
        }
    }

    /**
     * 并发搜索多个电影关键词
     * 同时搜索多个关键词，合并结果
     */
    @Tool(description = "根据电影名称查询多部电影,搜索多部电影名称，每个名称只返回最匹配的一部电影。用英文逗号分隔多个电影名称，用英文名称查询效果更佳！", returnDirect = true)
    public Response concurrentSearchMovies(
            @ToolParam(description = "多个电影名称，用逗号分隔，例如'千与千寻,龙猫,泰坦尼克号'") String queries,
            @ToolParam(description = "电影信息显示的语言代码，默认为'en'英语") String language) {

        try {
            if (!StringUtils.hasText(queries)) {
                logger.error("Invalid request: queries is required");
                return new Response(new ArrayList<>());
            }

            // 将查询字符串按逗号分割成列表
            List<String> queryList = Arrays.stream(queries.split(","))
                    .map(String::trim)
                    .filter(q -> !q.isEmpty())
                    .collect(Collectors.toList());

            if (queryList.isEmpty()) {
                logger.error("Invalid request: no valid queries after parsing");
                return new Response(new ArrayList<>());
            }

            logger.info("并发电影搜索请求: {}, 语言: {}", queryList, language);

            String languageCode = language != null ? language : "en";
            // 使用并发搜索方法
            MovieListResponseVo responseVo = movieSearchService.concurrentSearchMoviesForVo(queryList, languageCode);

            if (responseVo.getResult() == null || responseVo.getResult().isEmpty()) {
                return new Response(new ArrayList<>());
            } else {
                // 直接返回电影对象列表
                return new Response(responseVo.getResult());
            }
        } catch (Exception e) {
            logger.error("并发电影搜索失败", e);
            return new Response(new ArrayList<>());
        }
    }

    /**
     * 按年份搜索电影
     * 根据年份和类型搜索电影
     */
    @Tool(description = "按年份、类型、地区搜索电影。当你需要查找特定年份、特定类型或特定地区的电影时使用此功能。对于中国/中文电影，请将language设置为'zh'，对于韩国电影设置为'ko'，日本电影设置为'ja'等", returnDirect = true)
    public Response searchMoviesByYear(
            @ToolParam(description = "电影年份（可选），例如'2024'。可以留空搜索所有年份") String year,
            @ToolParam(description = "电影类型，查询多种类型的时候用逗号分割（可选），支持的类型有：Action, Adventure, Animation, Comedy, Crime, Documentary, Drama, Family, Fantasy, History, Horror, Music, Mystery, Romance, Thriller, War, Western, Sci-Fi, Biography, Film-Noir, Musical。革命片可以使用History,War组合") String genre,
            @ToolParam(description = "电影信息显示的语言代码，重要：中国电影用'zh'，韩国电影用'ko'，日本电影用'ja'，英语电影用'en'。这个参数决定了搜索的电影来源地区") String language) {

        try {

            logger.info("按年份搜索电影请求: 年份={}, 类型={}, 语言={}", year, genre, language);

            String languageCode = language != null ? language : "en";
            // 使用新的ForVo方法
            MovieListResponseVo responseVo = movieSearchService.searchMoviesByYearAndGenreForVo(year, genre,
                    languageCode);

            if (responseVo.getResult() == null || responseVo.getResult().isEmpty()) {
                return new Response(new ArrayList<>());
            } else {
                // 直接返回电影对象列表
                return new Response(responseVo.getResult());
            }
        } catch (Exception e) {
            logger.error("按年份搜索电影失败", e);
            return new Response(new ArrayList<>());
        }
    }

    /**
     * 获取本周热门电影
     * 获取平台上的热门电影列表
     */
    @Tool(description = "获取本周热门电影。当你需要了解当前流行或热门的电影时使用此功能。支持的电影类型参考按年份搜索功能。", returnDirect = true)
    public Response getPopularMovies(
            @ToolParam(description = "电影信息显示的语言代码，默认为'en'英语") String language) {

        try {
            logger.info("获取本周热门电影请求，语言: {}", language);

            String languageCode = language != null ? language : "en";
            // 使用新的ForVo方法
            MovieListResponseVo responseVo = movieSearchService.getPopularMoviesThisWeekForVo(languageCode);

            if (responseVo.getResult() == null || responseVo.getResult().isEmpty()) {
                return new Response(new ArrayList<>());
            } else {
                // 直接返回电影对象列表
                return new Response(responseVo.getResult());
            }
        } catch (Exception e) {
            logger.error("获取本周热门电影失败", e);
            return new Response(new ArrayList<>());
        }
    }

    /**
     * 按地区和类型搜索电影
     * 专门用于搜索特定地区的特定类型电影，如中国历史片、韩国爱情片等
     */
    @Tool(description = "按地区和类型搜索电影。当用户询问特定地区的特定类型电影时首先使用，如'中国历史革命片'、'韩国爱情片'等。如果返回的结果不是该地区的电影或结果不准确，请立即使用smartMovieSearch工具根据具体电影名称重新搜索", returnDirect = true)
    public Response searchMoviesByRegionAndGenre(
            @ToolParam(description = "地区/国家，影响搜索的电影来源：中国用'zh'，韩国用'ko'，日本用'ja'，美国/英语地区用'en'等") String region,
            @ToolParam(description = "电影类型，支持的类型有：Action, Adventure, Animation, Comedy, Crime, Documentary, Drama, Family, Fantasy, History, Horror, Music, Mystery, Romance, Thriller, War, Western, Sci-Fi, Biography, Film-Noir, Musical。革命片可以使用History,War组合") String genre,
            @ToolParam(description = "电影年份（可选），例如'2024'。可以留空搜索所有年份") String year) {

        try {
            logger.info("按地区和类型搜索电影请求: 地区={}, 类型={}, 年份={}", region, genre, year);

            String languageCode = region != null ? region : "en";
            // 使用按年份搜索的方法，但重点是地区和类型
            MovieListResponseVo responseVo = movieSearchService.searchMoviesByYearAndGenreForVo(year, genre, languageCode);

            if (responseVo.getResult() == null || responseVo.getResult().isEmpty()) {
                return new Response(new ArrayList<>());
            } else {
                // 直接返回电影对象列表
                return new Response(responseVo.getResult());
            }
        } catch (Exception e) {
            logger.error("按地区和类型搜索电影失败", e);
            return new Response(new ArrayList<>());
        }
    }

    /**
     * 智能电影搜索工具
     * 当地区+类型搜索结果不理想时，自动推断具体电影名称并批量搜索
     */
    @Tool(description = "智能电影搜索。当用户查询特定地区特定类型电影（如'中国历史革命片'）但通过地区搜索结果不准确时，请根据查询内容推断出3-5个具体的代表性电影英文名称，然后调用此工具进行批量搜索。例如：中国历史革命片可以搜索'The Founding of a Republic,Beginning of the Great Revival,Assembly,Battle of Changping,Red Cliff'", returnDirect = true)
    public Response smartMovieSearch(
            @ToolParam(description = "根据用户查询推断出的具体电影英文名称，用逗号分隔。例如'The Founding of a Republic,Beginning of the Great Revival,Assembly'。请推断该地区该类型最具代表性的电影") String movieNames,
            @ToolParam(description = "电影信息显示的语言代码：中国电影用'zh'，韩国电影用'ko'，日本电影用'ja'，英语电影用'en'") String language) {

        try {
            if (!StringUtils.hasText(movieNames)) {
                logger.error("Invalid request: movieNames is required for smart search");
                return new Response(new ArrayList<>());
            }

            logger.info("智能电影搜索请求: 电影名称={}, 语言={}", movieNames, language);

            String languageCode = language != null ? language : "en";
            
            // 将电影名称分割成列表
            List<String> movieList = Arrays.stream(movieNames.split(","))
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.toList());

            if (movieList.isEmpty()) {
                logger.error("No valid movie names after parsing");
                return new Response(new ArrayList<>());
            }

            // 使用并发搜索方法
            MovieListResponseVo responseVo = movieSearchService.concurrentSearchMoviesForVo(movieList, languageCode);

            if (responseVo.getResult() == null || responseVo.getResult().isEmpty()) {
                return new Response(new ArrayList<>());
            } else {
                // 直接返回电影对象列表
                return new Response(responseVo.getResult());
            }
        } catch (Exception e) {
            logger.error("智能电影搜索失败", e);
            return new Response(new ArrayList<>());
        }
    }

    /**
     * 电影工具响应类
     */
    public record Response(List<MovieDetailVo> movies) {
    }
}