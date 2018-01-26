package com.yryz.quanhu.support.activity.service;


import com.yryz.common.response.PageList;
import com.yryz.quanhu.support.activity.dto.AdminActivityVoteDetailDto;
import com.yryz.quanhu.support.activity.dto.AdminActivityVoteRecordDto;
import com.yryz.quanhu.support.activity.dto.AdminConfigObjectDto;
import com.yryz.quanhu.support.activity.vo.AdminActivityInfoVo1;
import com.yryz.quanhu.support.activity.vo.AdminActivityVoteDetailVo;
import com.yryz.quanhu.support.activity.vo.AdminActivityVoteRecordVo;

import java.util.List;


/**
 * 投票类活动参与内容管理
 */
public interface AdminIActivityParticipationService {

	AdminActivityInfoVo1 detail(Long id);

	/**
	 * 参与活动列表
	 */
	List<AdminActivityVoteDetailVo> list(AdminActivityVoteDetailDto adminActivityVoteDetailDto);

	/**
	 * 投票用户数据
	 */
	List<AdminActivityVoteRecordVo> voteList(AdminActivityVoteRecordDto adminActivityVoteRecordDto);


	/**
	 * 增加票数
	 */
	Integer addVote(Long id, Integer count);

	Integer updateStatus(Long id, Byte status);

	//public ListPage<CustInfo> selectUser(String type, String nickName, String circle, Integer pageNo, Integer pageSize);

	public AdminConfigObjectDto getVoteConfig(Long infoId);

    public String saveVoteDetail(AdminActivityVoteDetailDto voteDetailDto);

	/*List<CircleInfo> circleList();*/

	PageList adminlist(AdminActivityVoteRecordDto adminActivityVoteRecordDto);

	PageList adminlistDetail(AdminActivityVoteDetailDto adminActivityVoteDetailDto);
}
