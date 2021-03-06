/**
 * Copyright (c) 2018-2019 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * 
 * Created on 2018年1月18日
 * Id: RrzOrderPayInfoDao.java, 2018年1月18日 上午11:17:56 yehao
 */
package com.yryz.quanhu.order.dao.persistence;

import java.util.List;

import com.yryz.quanhu.order.vo.WithdrawCashDto;
import org.apache.ibatis.annotations.Mapper;

import com.yryz.quanhu.order.entity.RrzOrderPayInfo;

/**
 * @author yehao
 * @version 2.0
 * @date 2018年1月18日 上午11:17:56
 * @Description 支付订单DAO
 */
@Mapper
public interface RrzOrderPayInfoDao {
	
	/**
	 * 添加
	 * @param rrzOrderPayInfo
	 */
	public void insert(RrzOrderPayInfo rrzOrderPayInfo);
	
	/**
	 * 更新
	 * @param rrzOrderPayInfo
	 */
	public void update(RrzOrderPayInfo rrzOrderPayInfo);
	
	/**
	 * 单一查询
	 * @param orderId
	 * @return
	 */
	public RrzOrderPayInfo get(String orderId);
	
	/**
	 * 获取列表
	 * @param rrzOrderPayInfo
	 * @return
	 */
	public List<RrzOrderPayInfo> getList(RrzOrderPayInfo rrzOrderPayInfo);
	
	/**
	 * 获取列表(PC端)
	 * @param rrzOrderPayInfo
	 * @return
	 */
	public List<RrzOrderPayInfo> getListWeb(RrzOrderPayInfo rrzOrderPayInfo);

	/**
	 * 获取提现申请订单列表
	 * @param withdrawCashDto
	 * @return
	 */
	List<RrzOrderPayInfo> getWithdrawCashList(WithdrawCashDto withdrawCashDto);

}
