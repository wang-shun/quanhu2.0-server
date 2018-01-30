package com.yryz.quanhu.behavior.comment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yryz.common.response.PageList;
import com.yryz.quanhu.behavior.comment.dao.CommentDao;
import com.yryz.quanhu.behavior.comment.dto.CommentDTO;
import com.yryz.quanhu.behavior.comment.dto.CommentFrontDTO;
import com.yryz.quanhu.behavior.comment.dto.CommentSubDTO;
import com.yryz.quanhu.behavior.comment.entity.Comment;
import com.yryz.quanhu.behavior.comment.service.CommentService;
import com.yryz.quanhu.behavior.comment.vo.CommentInfoVO;
import com.yryz.quanhu.behavior.comment.vo.CommentVO;
import com.yryz.quanhu.behavior.comment.vo.CommentVOForAdmin;
import com.yryz.quanhu.behavior.count.api.CountFlagApi;
import com.yryz.quanhu.user.service.UserApi;
import com.yryz.quanhu.user.vo.UserSimpleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:sun
 * @version:
 * @Description:
 * @Date:Created in 18:47 2018/1/23
 */
@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentDao commentDao;

    @Reference(check = false)
    private UserApi userApi;

    @Reference(check = false)
    private CountFlagApi countFlagApi;

    @Override
    public int accretion(Comment comment) {
        return commentDao.accretion(comment);
    }

    @Override
    public Comment querySingleComment(Comment comment) {
        return commentDao.querySingleComment(comment);
    }

    @Override
    public int delComment(Comment comment) {
        return commentDao.delComment(comment);
    }

    @Override
    public PageList<CommentVO> queryComments(CommentFrontDTO commentFrontDTO) {
        Page<Comment> page = PageHelper.startPage(commentFrontDTO.getCurrentPage().intValue(), commentFrontDTO.getPageSize().intValue());
        PageList pageList = new PageList();
        List<CommentVO> commentVOS = commentDao.queryComments(commentFrontDTO);
        pageList.setCurrentPage(commentFrontDTO.getCurrentPage());
        pageList.setPageSize(commentFrontDTO.getPageSize());
        List<CommentVO> commentVOS_ = null;
        List<Comment> commentsnew = null;
        for (CommentVO commentVO : commentVOS) {
            CommentFrontDTO commentFrontDTOnew = new CommentFrontDTO();
            commentFrontDTOnew.setTopId(commentVO.getKid());
            commentFrontDTOnew.setResourceId(commentVO.getResourceId());
            commentVOS_ = commentDao.queryComments(commentFrontDTOnew);
            commentsnew = new ArrayList<Comment>();
            int i = 0;
            Map<String,Object> map=new HashMap<String, Object>();
            map.put("resourceId",commentVO.getResourceId());
            map.put("userId",commentVO.getCreateUserId());
            map.put("moduleEnum",commentVO.getModuleEnum());
            for (CommentVO commentVOsnew : commentVOS_) {
                i++;
                Comment comment = new Comment();
                comment.setId(commentVOsnew.getId());
                comment.setKid(commentVOsnew.getKid());
                comment.setTopId(commentVOsnew.getTopId());
                comment.setParentId(commentVOsnew.getParentId());
                comment.setParentUserId(commentVOsnew.getParentUserId());
                comment.setModuleEnum(commentVOsnew.getModuleEnum());
                comment.setTargetUserId(commentVOsnew.getTargetUserId());
                UserSimpleVO userSimpleVO = this.getUserSimple(commentVOsnew.getTargetUserId());
                if (null != userSimpleVO) {
                    comment.setTargetUserNickName(userSimpleVO.getUserNickName());
                } else {
                    comment.setTargetUserNickName("");
                }
                comment.setDelFlag(commentVOsnew.getDelFlag());
                comment.setRecommend(commentVOsnew.getRecommend());
                comment.setCreateUserId(commentVOsnew.getCreateUserId());
                comment.setCreateDate(commentVOsnew.getCreateDate());
                comment.setTenantId(commentVOsnew.getTenantId());
                comment.setRevision(commentVOsnew.getRevision());
                comment.setNickName(commentVOsnew.getNickName());
                comment.setResourceId(commentVOsnew.getResourceId());
                comment.setCoterieId(commentVOsnew.getCoterieId());
                comment.setContentComment(commentVOsnew.getContentComment());
                comment.setUserImg(commentVOsnew.getUserImg());
                comment.setLastUpdateUserId(commentVOsnew.getLastUpdateUserId());
                comment.setLastUpdateDate(commentVOsnew.getLastUpdateDate());
                commentsnew.add(comment);
                if (i >= 3) {
                    break;
                }
            }
            //需要接统计
            Map<String,Long> maps=null;
            try{
                maps= countFlagApi.getAllCountFlag("11",commentVO.getResourceId(),"",map).getData();
            }catch (Exception e){
                logger.info("调用统计信息失败:" + e);
            }
            commentVO.setLikeCount(maps.get("likeCount").intValue());
            commentVO.setLikeFlag(maps.get("likeFlag").byteValue());
            commentVO.setCommentCount(commentVOS_.size());
            commentVO.setChildrenComments(commentsnew);
        }
        pageList.setCount(Long.valueOf(commentVOS.size()));
        pageList.setEntities(commentVOS);
        return pageList;
    }

    @Override
    public int updownBatch(List<Comment> comments) {
        return commentDao.updownBatch(comments);
    }

    @Override
    public PageList<CommentVOForAdmin> queryCommentForAdmin(CommentDTO commentDTO) {
        List<CommentVOForAdmin> commentVOForAdmins = commentDao.queryCommentForAdmin(commentDTO);
        Page<CommentVOForAdmin> page = PageHelper.startPage(commentDTO.getCurrentPage().intValue(), commentDTO.getPageSize().intValue());
        PageList pageList = new PageList();
        pageList.setCurrentPage(commentDTO.getCurrentPage());
        pageList.setPageSize(commentDTO.getPageSize());
        pageList.setEntities(commentVOForAdmins);
        pageList.setCount(Long.valueOf(commentVOForAdmins.size()));
        return pageList;
    }

    @Override
    public CommentInfoVO querySingleCommentInfo(CommentSubDTO commentSubDTO) {
        return commentDao.querySingleCommentInfo(commentSubDTO);
    }

    @Override
    public PageList<CommentVO> querySubCommentsInfo(CommentSubDTO commentSubDTO) {
        CommentSubDTO  commentSubDTO_=new CommentSubDTO();
        commentSubDTO_.setKid(commentSubDTO.getKid());
        CommentInfoVO commentInfoVO=commentDao.querySingleCommentInfo(commentSubDTO_);
        Page<Comment> page = PageHelper.startPage(commentSubDTO.getCurrentPage().intValue(), commentSubDTO.getPageSize().intValue());
        CommentFrontDTO commentFrontDTO = new CommentFrontDTO();
        commentFrontDTO.setCurrentPage(commentSubDTO.getCurrentPage());
        commentFrontDTO.setPageSize(commentSubDTO.getPageSize());
        commentFrontDTO.setTopId(commentSubDTO.getKid());
        if(null!=commentInfoVO){
            commentFrontDTO.setResourceId(commentInfoVO.getResourceId());
        }
        PageList pageList = new PageList();
        pageList.setCurrentPage(commentSubDTO.getCurrentPage());
        pageList.setPageSize(commentSubDTO.getPageSize());
        List<CommentVO> commentVOS = commentDao.queryComments(commentFrontDTO);
        for (CommentVO commentVO : commentVOS) {
            UserSimpleVO userSimpleVO = this.getUserSimple(commentVO.getTargetUserId());
            if(null!=userSimpleVO){
                commentVO.setTargetUserNickName(userSimpleVO.getUserNickName());
            }
        }
        pageList.setEntities(commentVOS);
        pageList.setCount(Long.valueOf(commentVOS.size()));
        return pageList;
    }

    public UserSimpleVO getUserSimple(Long userId) {
        UserSimpleVO userSimpleVO = null;
        try {
            userSimpleVO = userApi.getUserSimple(userId).getData();
        } catch (Exception e) {
            logger.info("调用用户信息失败:" + e);
        }
        return userSimpleVO;
    }
}
