package com.peliplat.ai.service;

import com.peliplat.ai.model.MediaBaseVo;
import com.peliplat.ai.model.MovieListResponseVo;

import java.util.List;
import java.util.Map;

/**
 * 电影搜索服务接口
 * 根据用户的自然语言对话，查询符合条件的电影列表
 */
public interface MovieSearchService {

    /**
     * 根据用户查询条件搜索电影
     * 
     * @param query    用户查询条件
     * @param language 语言
     * @return 电影内容列表（字符串形式）
     */
    List<String> searchMoviesByQuery(String query, String language);

    /**
     * 根据用户查询条件搜索电影（返回结构化数据）
     * 
     * @param query    用户查询条件
     * @param language 语言
     * @return 电影列表响应对象
     */
    MovieListResponseVo searchMoviesByQueryForVo(String query, String language);


    /**
     * 根据年份和类型搜索电影（返回结构化数据）
     * 
     * @param year     年份
     * @param genre    电影类型
     * @param language 语言
     * @return 电影列表响应对象
     */
    MovieListResponseVo searchMoviesByYearAndGenreForVo(String year, String genre, String language);


    /**
     * 获取本周热门电影（返回结构化数据）
     * 
     * @param language 语言
     * @return 热门电影列表响应对象
     */
    MovieListResponseVo getPopularMoviesThisWeekForVo(String language);

    /**
     * 并发搜索多个查询条件的电影，并合并结果
     * 
     * @param queries  查询条件列表
     * @param language 语言
     * @return 合并后的电影列表响应对象
     */
    MovieListResponseVo concurrentSearchMoviesForVo(List<String> queries, String language);
}