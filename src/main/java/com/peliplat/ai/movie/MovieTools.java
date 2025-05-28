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
    @Tool(description = "根据影片名称搜索电影,查询多部电影的时候优先调用concurrentSearchMovies", returnDirect = true)
    public Response searchMovies(
            @ToolParam(description = "要搜索的电影的准确或常用标题。如果用户提供的是描述性内容，请先尝试推断出实际的电影标题") String query,
            @ToolParam(description = "电影信息显示的语言代码，默认为'en'英语") String language) {

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
    @Tool(description = "按年份和类型搜索电影,当你需要查找特定年份或特定类型的电影时使用此功能", returnDirect = true)
    public Response searchMoviesByYear(
            @ToolParam(description = "电影年份（可选），例如'2024'") String year,
            @ToolParam(description = "电影类型，查询多种类型的时候用逗号分割（可选），支持的类型有：Action, Adventure, Animation, Comedy, Crime, Documentary, Drama, Family, Fantasy, History, Horror, Music, Mystery, Romance, Thriller, War, Western, Sci-Fi, Biography, Film-Noir, Musical") String genre,
            @ToolParam(description = "电影信息显示的语言代码，默认为'en'英语") String language) {

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
     * 电影工具响应类
     */
    public record Response(List<MovieDetailVo> movies) {
    }
}