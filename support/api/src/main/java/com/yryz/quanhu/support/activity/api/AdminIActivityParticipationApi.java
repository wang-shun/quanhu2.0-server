package com.yryz.quanhu.support.activity.api;


import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
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
public interface AdminIActivityParticipationApi {

	Response<AdminActivityInfoVo1> detail(Long id);

	/**
	 * 参与活动列表
	 */
	Response<List<AdminActivityVoteDetailVo>> list(AdminActivityVoteDetailDto adminActivityVoteDetailDto);

	/**
	 * 投票用户数据
	 */
	Response<List<AdminActivityVoteRecordVo>> voteList(AdminActivityVoteRecordDto adminActivityVoteRecordDto);


	/**
	 * 增加票数
	 */
	Response<Integer> addVote(Long id, Integer count);

	Response<Integer> updateStatus(Long id, Byte status);

	//public ListPage<CustInfo> selectUser(String type, String nickName, String circle, Integer pageNo, Integer pageSize);

	Response<AdminConfigObjectDto> getVoteConfig(Long infoId);

	Response<String> saveVoteDetail(AdminActivityVoteDetailDto voteDetailDto);

	/*List<CircleInfo> circleList();*/

	Response<PageList> adminlist(AdminActivityVoteRecordDto adminActivityVoteRecordDto);

	Response<PageList> adminlistDetail(AdminActivityVoteDetailDto adminActivityVoteDetailDto);
}
