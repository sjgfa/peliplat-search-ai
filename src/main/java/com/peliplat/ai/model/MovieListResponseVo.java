package com.peliplat.ai.model;

import java.util.List;

/**
 * 电影列表响应包装类
 */
public class MovieListResponseVo {
    private Integer retCode;
    private String message;
    private String show;
    private Integer totalPage;
    private Integer totalCount;
    private Integer count;
    private List<MovieDetailVo> result;

    public Integer getRetCode() {
        return retCode;
    }

    public void setRetCode(Integer retCode) {
        this.retCode = retCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<MovieDetailVo> getResult() {
        return result;
    }

    public void setResult(List<MovieDetailVo> result) {
        this.result = result;
    }
}