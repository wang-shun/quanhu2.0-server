package com.yryz.quanhu.resource.release.config.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangheng
 * This class was generated by MyBatis Generator.
 * @table qh_release_config
*/
public class ReleaseConfig implements Serializable{
    private static final long serialVersionUID = 1L;

    /**
    * 主键
    */
    private Long id;

    /**
    * 分类ID
    */
    private Long classifyId;

    /**
    * 属性key
    */
    private String propertyKey;

    /**
    * 属性别名
    */
    private String propertyAlias;

    /**
    * 是否存在（0：不存在，1：存在;默认：0）
    */
    private Byte enabled;

    /**
    * 是否必填（0：不存在，1：存在;默认：0）
    */
    private Byte required;

    /**
    * 校验下限(字符为长度，资源为个数)（范围：0~字段长度）（包含关系）
    */
    private Integer lowerLimit;

    /**
    * 校验上限
    */
    private Integer upperLimit;

    /**
    * 输入提示信息
    */
    private String inputPrompt;

    /**
    * 错误提示信息
    */
    private String errorPrompt;

    /**
    * 校验正则表达式
    */
    private String checkRegexp;

    /**
    * 创建时间
    */
    private Date createDate;

    /**
    * 创建用户user_id
    */
    private Long createUserId;

    /**
    * 最后更新时间
    */
    private Date lastUpdateDate;

    /**
    * 最后更新用户user_id
    */
    private Long lastUpdateUserId;

    /**
    * 租户id
    */
    private String appId;

    /**
    * 版本号
    */
    private Integer revision;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyAlias() {
        return propertyAlias;
    }

    public void setPropertyAlias(String propertyAlias) {
        this.propertyAlias = propertyAlias;
    }

    public Byte getEnabled() {
        return enabled;
    }

    public void setEnabled(Byte enabled) {
        this.enabled = enabled;
    }

    public Byte getRequired() {
        return required;
    }

    public void setRequired(Byte required) {
        this.required = required;
    }

    public Integer getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Integer lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Integer getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Integer upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getInputPrompt() {
        return inputPrompt;
    }

    public void setInputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
    }

    public String getErrorPrompt() {
        return errorPrompt;
    }

    public void setErrorPrompt(String errorPrompt) {
        this.errorPrompt = errorPrompt;
    }

    public String getCheckRegexp() {
        return checkRegexp;
    }

    public void setCheckRegexp(String checkRegexp) {
        this.checkRegexp = checkRegexp;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getLastUpdateUserId() {
        return lastUpdateUserId;
    }

    public void setLastUpdateUserId(Long lastUpdateUserId) {
        this.lastUpdateUserId = lastUpdateUserId;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", classifyId=").append(classifyId);
        sb.append(", propertyKey=").append(propertyKey);
        sb.append(", propertyAlias=").append(propertyAlias);
        sb.append(", enabled=").append(enabled);
        sb.append(", required=").append(required);
        sb.append(", lowerLimit=").append(lowerLimit);
        sb.append(", upperLimit=").append(upperLimit);
        sb.append(", inputPrompt=").append(inputPrompt);
        sb.append(", errorPrompt=").append(errorPrompt);
        sb.append(", checkRegexp=").append(checkRegexp);
        sb.append(", createDate=").append(createDate);
        sb.append(", createUserId=").append(createUserId);
        sb.append(", lastUpdateDate=").append(lastUpdateDate);
        sb.append(", lastUpdateUserId=").append(lastUpdateUserId);
        sb.append(", appId=").append(appId);
        sb.append(", revision=").append(revision);
        sb.append("]");
        return sb.toString();
    }
}