package com.yryz.quanhu.resource.topic.service;

import com.yryz.common.response.PageList;
import com.yryz.quanhu.resource.topic.dto.TopicPostDto;
import com.yryz.quanhu.resource.topic.entity.TopicPostWithBLOBs;
import com.yryz.quanhu.resource.topic.vo.TopicPostVo;

import java.util.List;

public interface TopicPost4AdminService {

    public Integer saveTopicPost(TopicPostDto topicPostDto);

    public TopicPostVo getDetail(Long kid, Long userId);

    public PageList<TopicPostVo> queryList(TopicPostDto dto);

    public Integer deleteTopicPost(Long kid, Long userId);

    public Long countPostByTopicId(Long kid);

    public List<Long> getKidByCreatedate(String startDate, String endDate);
    
    public List<TopicPostWithBLOBs> getByKids(List<Long> kidList);
}
