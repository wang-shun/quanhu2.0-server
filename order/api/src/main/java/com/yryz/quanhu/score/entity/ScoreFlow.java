package com.yryz.quanhu.score.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lsn on 2017/8/28.
 */
public class ScoreFlow implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -3577121435443411098L;

	private Long id;

	private int consumeFlag;
	
    private String custId;

    private String eventCode;
    
    private String eventName;

    private Integer newScore;

    private Long allScore;

    private Date createTime;

    private Date updateTime;
    
    public ScoreFlow(){
    	
    }
    
    public ScoreFlow(String custId , String eventCode , int newScore){
    	this.custId = custId ;
    	this.eventCode = eventCode;
    	this.newScore = newScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getEventCode() {
		return eventCode;
	}

	public void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public Integer getNewScore() {
        return newScore;
    }

    public void setNewScore(Integer newScore) {
        this.newScore = newScore;
    }

    public Long getAllScore() {
        return allScore;
    }

    public void setAllScore(Long allScore) {
        this.allScore = allScore;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

	public int getConsumeFlag() {
		return consumeFlag;
	}

	public void setConsumeFlag(int consumeFlag) {
		this.consumeFlag = consumeFlag;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
}
