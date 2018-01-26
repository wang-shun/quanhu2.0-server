package com.yryz.quanhu.coterie.member.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yryz.common.constant.ExceptionEnum;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.utils.GsonUtils;
import com.yryz.common.utils.JsonUtils;
import com.yryz.quanhu.coterie.coterie.dao.CoterieMapper;
import com.yryz.quanhu.coterie.coterie.entity.Coterie;
import com.yryz.quanhu.coterie.member.constants.MemberConstant;
import com.yryz.quanhu.coterie.member.dao.CoterieApplyDao;
import com.yryz.quanhu.coterie.member.dao.CoterieMemberDao;
import com.yryz.quanhu.coterie.member.dto.CoterieMemberSearchDto;
import com.yryz.quanhu.coterie.member.entity.CoterieMember;
import com.yryz.quanhu.coterie.member.entity.CoterieMemberApply;
import com.yryz.quanhu.coterie.member.entity.CoterieMemberNotify;
import com.yryz.quanhu.coterie.member.service.CoterieMemberService;
import com.yryz.quanhu.coterie.member.vo.CoterieMemberApplyVo;
import com.yryz.quanhu.coterie.member.vo.CoterieMemberVo;
import com.yryz.quanhu.coterie.member.vo.CoterieMemberVoForJoin;
import com.yryz.quanhu.support.id.api.IdAPI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 私圈成员服务实现
 *
 * @author chengyunfei
 */
@Service
public class CoterieMemberServiceImpl implements CoterieMemberService {
    @Resource
    private CoterieMemberDao coterieMemberDao;
    @Resource
    private CoterieApplyDao coterieApplyDao;
    @Resource
    private CoterieMapper coterieMapper;

    @Reference
    private IdAPI idApi;

//	@Resource
//	private CoterieMemberMessageManager coterieMemberMessageManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoterieMemberVoForJoin join(Long userId, Long coterieId, String reason) {

        CoterieMemberVoForJoin result = new CoterieMemberVoForJoin();

        Coterie coterie = coterieMapper.selectByCoterieId(coterieId.toString());

        //私圈人数已满
        if (coterie.getMemberNum().intValue() >= 2000) {
            result.setStatus(40);
            return result;
        }

        //收费时,直接加入,返回orderId
        //todo  付费加入
        if (coterie.getJoinFee() > 0) {
            CoterieMemberNotify coterieMemberNotify = new CoterieMemberNotify();

            coterieMemberNotify.setUserId(userId);
            coterieMemberNotify.setCoterieId(coterieId);
            coterieMemberNotify.setReason(reason);
            coterieMemberNotify.setCoterieName(coterie.getName());
            coterieMemberNotify.setOwner(coterie.getOwnerId());
            coterieMemberNotify.setAmount(coterie.getJoinFee().longValue());
            coterieMemberNotify.setIcon(coterie.getIcon());

            String extJson = JsonUtils.toFastJson(coterieMemberNotify);

//            Order order = new Order(custId, coterieInfo.getJoinFee() * 1L, extJson, CommonConstants.JOIN_COTERIE_MODULE_ENUM, coterieId,
//                    CommonConstants.JOIN_COTERIE_RESOURCE_ID, custId, circleId);
//            Long orderId = orderService.createOrder(OrderConstant.JOIN_COTERIE_ORDER, custId, coterieInfo.getOwnerId(), order);
            Long orderId = 1111111111L;

            result.setStatus(10);
            result.setOrderId(orderId);
            return result;
        }

        //免费不审核,直接加入
        if (coterie.getJoinFee() == 0 && coterie.getJoinCheck() == 10) {

            //先入审请表
            CoterieMemberApply apply = makeApplyInfo(userId, coterieId, reason, MemberConstant.MemberStatus.PASS.getStatus());
            coterieApplyDao.insert(apply);

            //再入成员表
            CoterieMember record = new CoterieMember();
            record.setKid(idApi.getSnowflakeId().getData());
            record.setUserId(userId);
            record.setCoterieId(coterieId);
            record.setReason(reason);
            record.setBanSpeak(MemberConstant.BanSpeak.NORMAL.getStatus());
            record.setCreateDate(new Date());
            record.setLastUpdateDate(new Date());
            record.setMemberStatus(MemberConstant.MemberStatus.PASS.getStatus());
            record.setProcessTime(new Date());
            record.setJoinType(MemberConstant.JoinType.NOTFREE.getStatus());
            record.setKickStatus(MemberConstant.KickStatus.NORMAL.getStatus());
            record.setDelFlag(MemberConstant.DelFlag.NORMAL.getStatus());
            record.setAmount(Long.valueOf(coterie.getJoinFee()));
            record.setCreateUserId(userId);
            saveMember(record);

            result.setStatus(20);
            return result;
        }

        //免费要审核
        if (coterie.getJoinFee() == 0 && coterie.getJoinCheck() == 11) {
            try {
                //不能有通过或是不通过的审请
                CoterieMemberApply memberApply = coterieApplyDao.selectByCoterieIdAndUserId(coterieId, userId);
                if (null == memberApply) {
                    CoterieMemberApply apply = makeApplyInfo(userId, coterieId, reason, null);
                    coterieApplyDao.insert(apply);
                    //coterieMemberMessageManager.joinMessage(custId, coterieId, reason);
                }
            } catch (Exception e) {
                throw new QuanhuException(ExceptionEnum.SysException);
            }
            result.setStatus(30);
        }

        return result;
    }

    @Override
    @Transactional
    public void kick(Long userId, Long coterieId, String reason) {
        try {
//            Coterie coterie = coterieMapper.selectByCoterieId(coterieId.toString());
//            if (coterie != null) {
            //update kickStatus
            CoterieMember coterieMember = new CoterieMember();
            coterieMember.setUserId(userId);
            coterieMember.setCoterieId(coterieId);
            coterieMember.setReason(reason);
            coterieMember.setDelFlag(MemberConstant.DelFlag.DELETED.getStatus());
            int resultMember = coterieMemberDao.updateByCoterieMember(coterieMember);

            //update delFlag
            CoterieMemberApply apply = new CoterieMemberApply();
            apply.setUserId(userId);
            apply.setCoterieId(coterieId);
            apply.setDelFlag(MemberConstant.DelFlag.DELETED.getStatus());
            int resultApply = coterieApplyDao.updateByCoterieApply(apply);

            if (resultMember > 0 && resultApply > 0) {
                //更新私圈成员数
//                    coterieMapper.updateMemberNum(coterie.getCoterieId().toString(), coterie.getMemberNum() - 1, coterie.getMemberNum());
            }
//            }
//            coterieMemberMessageManager.kickMessage(custId, coterieId, reason);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new MysqlOptException("param custId:" + userId + "coterieId:" + coterieId, e);
        }
    }

    @Override
    @Transactional
    public void quit(Long userId, Long coterieId) {
        try {
//            Coterie coterie = coterieMapper.selectByCoterieId(coterieId.toString());
//            if (coterie != null) {
            //update delFlag
            CoterieMember coterieMember = new CoterieMember();
            coterieMember.setUserId(userId);
            coterieMember.setCoterieId(coterieId);
            coterieMember.setDelFlag(MemberConstant.DelFlag.DELETED.getStatus());
            int resultMember = coterieMemberDao.updateByCoterieMember(coterieMember);
            //update delFlag
            CoterieMemberApply apply = new CoterieMemberApply();
            apply.setUserId(userId);
            apply.setCoterieId(coterieId);
            apply.setDelFlag(MemberConstant.DelFlag.DELETED.getStatus());
            int resultApply = coterieApplyDao.updateByCoterieApply(apply);
            if (resultMember > 0 && resultApply > 0) {
                //更新私圈成员数
//                    coterieMapper.updateMemberNum(coterie.getCoterieId().toString(), coterie.getMemberNum() - 1, coterie.getMemberNum());
            }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuanhuException(ExceptionEnum.SysException);
        }
    }

    @Override
    public void banSpeak(Long userId, Long coterieId, Integer type) {
        CoterieMember record = new CoterieMember();
        record.setUserId(userId);
        record.setCoterieId(coterieId);
        record.setLastUpdateDate(new Date());
        if (type == 1) {
            record.setBanSpeak(MemberConstant.BanSpeak.BANSPEAK.getStatus());
        } else {
            record.setBanSpeak(MemberConstant.BanSpeak.NORMAL.getStatus());
        }

        try {
            Integer result = coterieMemberDao.updateByCoterieMember(record);

            if (result > 0) {

            } else {
                System.out.println("#######################################"+ result);
            }
//            coterieMemberMessageManager.banSpeakMessage(custId, coterieId);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new QuanhuException(ExceptionEnum.SysException);
        }
    }


    /************** 0124 ***********************************************************************/

    @Override
    public Integer permission(Long userId, Long coterieId) {

        //是否为圈主
        Coterie coterie = coterieMapper.selectByCustIdAndCircleId(userId.toString(), coterieId.toString());

        if (null != coterie && userId.longValue() == Long.getLong(coterie.getOwnerId()).longValue()) {
            return MemberConstant.Permission.OWNER.getStatus();
        }

        //是否为成员

        CoterieMember member = coterieMemberDao.selectByCoterieIdAndUserId(coterieId, userId);

        if (null != member) {
            return MemberConstant.Permission.MEMBER.getStatus();
        }

        //路人

        CoterieMemberApply apply = coterieApplyDao.selectWaitingByCoterieIdAndUserId(coterieId, userId);

        if (null != apply) {
            return MemberConstant.Permission.STRANGER_WAITING_CHECK.getStatus();
        } else {
            return MemberConstant.Permission.STRANGER_NON_CHECK.getStatus();
        }
    }

    @Override
    public Boolean isBanSpeak(Long userId, Long coterieId) {

        CoterieMember member = coterieMemberDao.selectByCoterieIdAndUserId(coterieId, userId);

        if (member.getBanSpeak() == MemberConstant.BanSpeak.BANSPEAK.getStatus()) {
            return true;
        }
        return false;
    }

    @Override
    public Integer queryNewMemberNum(Long coterieId) {
        try {
            return coterieApplyDao.selectNewMemberNum(coterieId);
        } catch (Exception e) {
//            throw new MysqlOptException("param coterieId:" + coterieId, e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long userId, Long coterieId, Integer type) {
        CoterieMemberApply apply = new CoterieMemberApply();
        apply.setCoterieId(coterieId);
        apply.setUserId(userId);
        apply.setLastUpdateDate(new Date());
        apply.setProcessTime(new Date());
        if (null == type) {
            apply.setMemberStatus(MemberConstant.MemberStatus.PASS.getStatus());
        } else {
            apply.setMemberStatus(type);
        }

        try {
            Integer result = coterieApplyDao.updateByCoterieApply(apply);

            CoterieMemberApply memberApply = (CoterieMemberApply) coterieApplyDao.selectByCoterieIdAndUserId(coterieId, userId);

            if (result > 0 && apply.getMemberStatus() == MemberConstant.MemberStatus.PASS.getStatus()) {
                CoterieMember member = coterieMemberDao.selectByCoterieIdAndUserId(coterieId, userId);

                CoterieMember record = new CoterieMember();
                record.setKid(idApi.getSnowflakeId().getData());
                record.setUserId(userId);
                record.setCoterieId(coterieId);
                record.setLastUpdateDate(new Date());
                record.setMemberStatus(MemberConstant.MemberStatus.PASS.getStatus());
                record.setBanSpeak(MemberConstant.BanSpeak.NORMAL.getStatus());
                record.setDelFlag(MemberConstant.DelFlag.NORMAL.getStatus());
                record.setKickStatus(MemberConstant.KickStatus.NORMAL.getStatus());
                record.setProcessTime(apply.getProcessTime());
                //需要审批的一定是免费的
                record.setJoinType(MemberConstant.JoinType.FREE.getStatus());
                record.setAmount(0L);
                record.setReason(memberApply.getReason());
                record.setCreateUserId(userId);


                if (member == null) {
                    record.setCreateDate(new Date());
                    saveMember(record);
                } else {
                    coterieMemberDao.updateByCoterieMember(record);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw new MysqlOptException("param coterieApply:" + apply, e);

        }
    }

    @Override
    public List<CoterieMemberVo> queryMemberList(Long coterieId, Integer pageNum, Integer pageSize) {
        List<CoterieMember> list = Lists.newArrayList();
        try {
            int start = (pageNum.intValue() - 1) * pageSize.intValue();
            list = coterieMemberDao.selectPageByCoterieId(coterieId, start, pageSize);
        } catch (Exception e) {
//            throw new MysqlOptException("param coterieId:" + coterieId, e);
        }
        return (List<CoterieMemberVo>) GsonUtils.parseList(list, CoterieMemberVo.class);

    }

    @Override
    public List<CoterieMemberApplyVo> queryMemberApplyList(Long coterieId, Integer pageNum, Integer pageSize) {
        List<CoterieMemberApply> list = Lists.newArrayList();
        try {
            int start = (pageNum - 1) * pageSize;
            list = coterieApplyDao.selectPageByCoterieId(coterieId, start, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new MysqlOptException("param coterieId:" + coterieId, e);
        }
        return (List<CoterieMemberApplyVo>) GsonUtils.parseList(list, CoterieMemberApplyVo.class);
    }

    private CoterieMemberApply makeApplyInfo(Long userId, Long coterieId, String reason, Integer status){
        CoterieMemberApply apply = new CoterieMemberApply();
        apply.setCoterieId(coterieId);
        apply.setCreateDate(new Date());
        apply.setUserId(userId);
        apply.setLastUpdateDate(new Date());
        apply.setReason(reason);
        apply.setCreateUserId(userId);
        if (null != status) {
            apply.setMemberStatus(status);
        } else {
            apply.setMemberStatus(MemberConstant.MemberStatus.WAIT.getStatus());
        }

        Long kid = idApi.getSnowflakeId().getData();
        apply.setKid(kid);

        return apply;
    }

    private void saveMember(CoterieMember record) {
        try {
            CoterieMember member = coterieMemberDao.selectByCoterieIdAndUserId(record.getCoterieId(), record.getUserId());
            if (member == null) {
                Coterie coterie = coterieMapper.selectByCoterieId(record.getCoterieId().toString());
                if (coterie != null) {
//                    int count = coterieMapper.updateMemberNum(coterie.getCoterieId().toString(), coterie.getMemberNum() + 1, coterie.getMemberNum());
//                    if (count > 0) {
                    coterieMemberDao.insert(record);
//                    } else {
//                        throw new QuanhuException(ExceptionEnum.SysException);//"更新成员人数失败"
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw new MysqlOptException("param coterieMember:" + record, e);
        }
    }
}