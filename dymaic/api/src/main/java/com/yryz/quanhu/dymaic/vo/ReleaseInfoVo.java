package com.yryz.quanhu.dymaic.vo;

import com.yryz.common.entity.GenericEntity;

/**
 * @author wangheng
 * This class was generated by MyBatis Generator.
 * @table qh_release_info
*/
public class ReleaseInfoVo extends GenericEntity {

	private static final long serialVersionUID = 1280737198072120936L;

	/**
    * 分类ID
    */
    private Long classifyId;

    /**
    * 封面图
    */
    private String coverPlanUrl;

    /**
    * 标题
    */
    private String title;

    /**
    * 简介、描述
    */
    private String description;

    /**
    * 音频url
    */
    private String audioUrl;

    /**
    * 视频url
    */
    private String videoUrl;

    /**
    * 视频首帧图url
    */
    private String videoThumbnailUrl;

    /**
    * 内容标签(关键字)
    */
    private String contentLabel;

    /**
    * 地区 二级城市、地区编码
    */
    private String cityCode;

    /**
    * gps数据,(x,y坐标)
    */
    private String gps;

    /**
    * 私圈id
    */
    private Long coterieId;

    /**
    * 内容价格
    */
    private Long contentPrice;

    /**
    * 上下架状态(10：上架,11：下架)
    */
    private Byte shelveFlag;

    /**
    * 删除状态(10：未删除,11：删除)
    */
    private Byte delFlag;

    /**
    * 推荐状态(10：非推荐，11：推荐)
    */
    private Byte recommend;

    /**
    * 排列顺序
    */
    private Integer sort;

    /**
    * 功能枚举
    */
    private String moduleEnum;

    /**
    * 租户id
    */
    private String appId;

    /**
    * 版本号
    */
    private Integer revision;

    /**
     * 内容源对象（支持图文混排）
     */
    private String contentSource;

    /**
    * 正文（只有文字）
    */
    private String content;

    /**
    * 图片url
    */
    private String imgUrl;

    /**
    * 扩展
    */
    private String extend;
    
    public Long getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }

    public String getCoverPlanUrl() {
        return coverPlanUrl;
    }

    public void setCoverPlanUrl(String coverPlanUrl) {
        this.coverPlanUrl = coverPlanUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoThumbnailUrl() {
        return videoThumbnailUrl;
    }

    public void setVideoThumbnailUrl(String videoThumbnailUrl) {
        this.videoThumbnailUrl = videoThumbnailUrl;
    }

    public String getContentLabel() {
        return contentLabel;
    }

    public void setContentLabel(String contentLabel) {
        this.contentLabel = contentLabel;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public Long getCoterieId() {
        return coterieId;
    }

    public void setCoterieId(Long coterieId) {
        this.coterieId = coterieId;
    }

    public Long getContentPrice() {
        return contentPrice;
    }

    public void setContentPrice(Long contentPrice) {
        this.contentPrice = contentPrice;
    }

    public Byte getShelveFlag() {
        return shelveFlag;
    }

    public void setShelveFlag(Byte shelveFlag) {
        this.shelveFlag = shelveFlag;
    }

    public Byte getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Byte delFlag) {
        this.delFlag = delFlag;
    }

    public Byte getRecommend() {
        return recommend;
    }

    public void setRecommend(Byte recommend) {
        this.recommend = recommend;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getModuleEnum() {
        return moduleEnum;
    }

    public void setModuleEnum(String moduleEnum) {
        this.moduleEnum = moduleEnum;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public String getContentSource() {
        return contentSource;
    }

    public void setContentSource(String contentSource) {
        this.contentSource = contentSource;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

	@Override
	public String toString() {
		return "ReleaseInfoVo [classifyId=" + classifyId + ", coverPlanUrl=" + coverPlanUrl + ", title=" + title
				+ ", description=" + description + ", audioUrl=" + audioUrl + ", videoUrl=" + videoUrl
				+ ", videoThumbnailUrl=" + videoThumbnailUrl + ", contentLabel=" + contentLabel + ", cityCode="
				+ cityCode + ", gps=" + gps + ", coterieId=" + coterieId + ", contentPrice=" + contentPrice
				+ ", shelveFlag=" + shelveFlag + ", delFlag=" + delFlag + ", recommend=" + recommend + ", sort=" + sort
				+ ", moduleEnum=" + moduleEnum + ", appId=" + appId + ", revision=" + revision + ", contentSource="
				+ contentSource + ", content=" + content + ", imgUrl=" + imgUrl + ", extend=" + extend + "]";
	}
    
}