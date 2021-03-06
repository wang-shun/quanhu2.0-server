package com.yryz.quanhu.other.activity.provider;

import com.alibaba.dubbo.config.annotation.Service;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.quanhu.other.activity.api.ActivityCandidateApi;
import com.yryz.quanhu.other.activity.dto.ActivityVoteDto;
import com.yryz.quanhu.other.activity.service.ActivityCandidateService;
import com.yryz.quanhu.other.activity.service.ActivityVoteService;
import com.yryz.quanhu.other.activity.vo.ActivityVoteConfigVo;
import com.yryz.quanhu.other.activity.vo.ActivityVoteDetailVo;
import com.yryz.quanhu.other.activity.vo.ActivityVoteInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Service(interfaceClass=ActivityCandidateApi.class)
public class ActivityCandidateProvider implements ActivityCandidateApi {

    private static final Logger logger = LoggerFactory.getLogger(ActivityCandidateProvider.class);

    @Autowired
    private ActivityCandidateService activityCandidateService;

    @Autowired
    private ActivityVoteService activityVoteService;

    /**
     * 增加参与者
     * @param activityVoteDto
     * @return
     * */
    public Response<Map<String, Object>> join(ActivityVoteDto activityVoteDto) {
        try {
            Assert.notNull(activityVoteDto.getActivityInfoId(), "activityInfoId不能为空");
            activityCandidateService.join(activityVoteDto);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("kid", activityVoteDto.getKid().toString());
            return ResponseUtils.returnObjectSuccess(resultMap);
        } catch (Exception e) {
            logger.error("增加参与者 失败", e);
            return ResponseUtils.returnException(e);
        }
    }

    /**
     * 参与投票活动
     * @param   activityInfoId
     * @return
     * */
    public Response<ActivityVoteConfigVo> config(Long activityInfoId) {
        try {
            Assert.notNull(activityInfoId, "activityInfoId不能为空");
            ActivityVoteConfigVo activityVoteConfigVo = new ActivityVoteConfigVo();
            ActivityVoteInfoVo activityVoteInfoVo = activityVoteService.getVoteInfo(activityInfoId);
            if(activityVoteInfoVo == null) {
                throw QuanhuException.busiError("活动已关闭或不存在");
            }
            activityVoteConfigVo.setActivityInfoId(activityInfoId);
            activityVoteConfigVo.setConfigSources(activityVoteInfoVo.getConfigSources());
            return ResponseUtils.returnObjectSuccess(activityVoteConfigVo);
        } catch (Exception e) {
            logger.error("参与投票活动 失败", e);
            return ResponseUtils.returnException(e);
        }
    }

    /**
     * 获取参与者详情
     * @param   activityVoteDto
     * @return
     * */
    public Response<ActivityVoteDetailVo> detail(ActivityVoteDto activityVoteDto) {
        try {
            //Assert.notNull(activityVoteDto.getActivityInfoId(), "activityInfoId不能为空");
            Assert.notNull(activityVoteDto.getCandidateId(), "candidateId不能为空");
            Assert.notNull(activityVoteDto.getOtherFlag(), "otherFlag不能为空");
            return ResponseUtils.returnObjectSuccess(activityCandidateService.detail(activityVoteDto));
        } catch (Exception e) {
            logger.error("获取参与者详情 失败", e);
            return ResponseUtils.returnException(e);
        }
    }

    /**
     * 参与者列表
     * @param   activityVoteDto
     * @return
     * */
    public Response<PageList<ActivityVoteDetailVo>> list(ActivityVoteDto activityVoteDto) {
        try {
            Assert.notNull(activityVoteDto.getActivityInfoId(), "activityInfoId不能为空");
            Assert.notNull(activityVoteDto.getOtherFlag(), "otherFlag不能为空");
            return ResponseUtils.returnObjectSuccess(activityCandidateService.list(activityVoteDto));
        } catch (Exception e) {
            logger.error("参与投票活动 失败", e);
            return ResponseUtils.returnException(e);
        }
    }

    /**
     * 排行榜
     * @param   activityVoteDto
     * @return
     * */
    public Response<PageList<ActivityVoteDetailVo>> rank(ActivityVoteDto activityVoteDto) {
        try {
            Assert.notNull(activityVoteDto.getActivityInfoId(), "activityInfoId不能为空");
            Assert.notNull(activityVoteDto.getOtherFlag(), "otherFlag不能为空");
            return ResponseUtils.returnObjectSuccess(activityCandidateService.rank(activityVoteDto));
        } catch (Exception e) {
            logger.error("排行榜 失败", e);
            return ResponseUtils.returnException(e);
        }
    }

}
