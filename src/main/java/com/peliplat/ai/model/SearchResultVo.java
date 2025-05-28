package com.peliplat.ai.model;

import java.util.List;

/**
 * 搜索结果包装类
 */
public class SearchResultVo {
    private ResultWrapper result;

    public ResultWrapper getResult() {
        return result;
    }

    public void setResult(ResultWrapper result) {
        this.result = result;
    }

    /**
     * 结果列表包装类
     */
    public static class ResultWrapper {
        private List<SearchItem> list;

        public List<SearchItem> getList() {
            return list;
        }

        public void setList(List<SearchItem> list) {
            this.list = list;
        }
    }

    /**
     * 搜索项目
     */
    public static class SearchItem {
        private String relateType;
        private String highLight;
        private MediaDetailVo details;

        public String getRelateType() {
            return relateType;
        }

        public void setRelateType(String relateType) {
            this.relateType = relateType;
        }

        public String getHighLight() {
            return highLight;
        }

        public void setHighLight(String highLight) {
            this.highLight = highLight;
        }

        public MediaDetailVo getDetails() {
            return details;
        }

        public void setDetails(MediaDetailVo details) {
            this.details = details;
        }
    }
}