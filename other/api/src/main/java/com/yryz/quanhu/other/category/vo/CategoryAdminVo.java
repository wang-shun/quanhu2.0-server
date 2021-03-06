package com.yryz.quanhu.other.category.vo;

import com.yryz.common.entity.BaseEntity;

import java.io.Serializable;
import java.util.Date;

public class CategoryAdminVo implements Serializable {

    private Long kid;
    /**
     * 0 表示一级分类
     */
    private Long parentKid;
    private Integer recommend;
    private String categoryName;
    private Integer categoryStatus;
    private Integer categoryType;
    private Integer categorySort;
    /**
     * 下属二级分类数
     */
    private Integer subordinateNum;
    /**
     * 下属达人数
     */
    private Long starNum;

    private Long createUserId;
    /**
     * 创建人
     */
    private String creator;

    private Date createDate;
    private Long lastUpdateUserId;
    private Date lastUpdateDate;

    public Long getKid() {
        return kid;
    }

    public void setKid(Long kid) {
        this.kid = kid;
    }

    public Long getParentKid() {
        return parentKid;
    }

    public void setParentKid(Long parentKid) {
        this.parentKid = parentKid;
    }

    public Integer getRecommend() {
        return recommend;
    }

    public void setRecommend(Integer recommend) {
        this.recommend = recommend;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getCategoryStatus() {
        return categoryStatus;
    }

    public void setCategoryStatus(Integer categoryStatus) {
        this.categoryStatus = categoryStatus;
    }

    public Integer getCategorySort() {
        return categorySort;
    }

    public void setCategorySort(Integer categorySort) {
        this.categorySort = categorySort;
    }

    public Integer getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(Integer categoryType) {
        this.categoryType = categoryType;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Long getLastUpdateUserId() {
        return lastUpdateUserId;
    }

    public void setLastUpdateUserId(Long lastUpdateUserId) {
        this.lastUpdateUserId = lastUpdateUserId;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Integer getSubordinateNum() {
        return subordinateNum;
    }

    public void setSubordinateNum(Integer subordinateNum) {
        this.subordinateNum = subordinateNum;
    }

    public Long getStarNum() {
        return starNum;
    }

    public void setStarNum(Long starNum) {
        this.starNum = starNum;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
