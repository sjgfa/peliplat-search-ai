package com.peliplat.ai.model;

/**
 * 媒体基础信息对象
 */
public class MediaBaseVo {

    private String ppId;
    private String title;
    private String originalTitle;
    private String publicYear;
    private String genres;
    private String plot;
    private String mediaType; // 3=电影，4=电视剧
    private String relateType; // movies/series
    private String rating;
    private String poster;
    private String imdbId;
    private String runTime;
    private String languages;
    private String releaseDate;
    private String certificate;

    public String getPpId() {
        return ppId;
    }

    public void setPpId(String ppId) {
        this.ppId = ppId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPublicYear() {
        return publicYear;
    }

    public void setPublicYear(String publicYear) {
        this.publicYear = publicYear;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getRelateType() {
        return relateType;
    }

    public void setRelateType(String relateType) {
        this.relateType = relateType;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @Override
    public String toString() {
        return "MediaBaseVo{" +
                "ppId='" + ppId + '\'' +
                ", title='" + title + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", publicYear='" + publicYear + '\'' +
                ", genres='" + genres + '\'' +
                ", plot='" + (plot != null ? plot.substring(0, Math.min(plot.length(), 50)) + "..." : "null") + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", relateType='" + relateType + '\'' +
                ", rating='" + rating + '\'' +
                ", poster='" + (poster != null ? poster.substring(0, Math.min(poster.length(), 30)) + "..." : "null")
                + '\'' +
                ", imdbId='" + imdbId + '\'' +
                ", runTime='" + runTime + '\'' +
                ", languages='" + languages + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", certificate='" + certificate + '\'' +
                '}';
    }

    /**
     * 返回格式化的详细信息
     */
    public String getFormattedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(title);

        if (originalTitle != null && !originalTitle.isEmpty() && !originalTitle.equals(title)) {
            sb.append(" (").append(originalTitle).append(")");
        }

        sb.append("\n");

        if (rating != null && !rating.isEmpty()) {
            sb.append("评分: ").append(rating).append(" | ");
        }

        if (publicYear != null && !publicYear.isEmpty()) {
            sb.append("年份: ").append(publicYear).append(" | ");
        }

        if (genres != null && !genres.isEmpty()) {
            sb.append("类型: ").append(genres);
        }

        sb.append("\n");

        if (plot != null && !plot.isEmpty()) {
            sb.append("简介: ").append(plot).append("\n");
        }

        if (languages != null && !languages.isEmpty()) {
            sb.append("语言: ").append(languages).append(" | ");
        }

        if (runTime != null && !runTime.isEmpty()) {
            sb.append("片长: ").append(runTime).append("分钟");
        }

        return sb.toString();
    }
}