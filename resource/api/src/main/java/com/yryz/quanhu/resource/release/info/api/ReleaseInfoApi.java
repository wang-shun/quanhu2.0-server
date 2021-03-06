package com.yryz.quanhu.resource.release.info.api;

import java.util.List;
import java.util.Set;

import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.quanhu.resource.release.info.dto.ReleaseInfoDto;
import com.yryz.quanhu.resource.release.info.entity.ReleaseInfo;
import com.yryz.quanhu.resource.release.info.vo.ReleaseInfoVo;

/**
* @Description: 平台文章Api
* @author wangheng
* @date 2018年1月22日 下午4:47:20
*/
public interface ReleaseInfoApi {

    /**  
    * @Description: 发布
    * @author wangheng
    * @param record
    * @return ReleaseInfo
    * @throws  
    */
    public Response<ReleaseInfo> release(ReleaseInfo record);

    /**  
    * @Description: 详情
    * @author wangheng
    * @param kid
    * @param headerUserId
    * @return ReleaseInfoVo
    * @throws  
    */
    public Response<ReleaseInfoVo> infoByKid(Long kid, Long headerUserId);

    /**  
    * @Description: 分页查询
    * @author wangheng
    * @param dto
    * @param headerUserId
    * @param isCount 是否count 总数
    * @param isGetCreateUser 是否获取创建者用户信息
    * @return PageList<ReleaseInfoVo>
    * @throws  
    */
    public Response<PageList<ReleaseInfoVo>> pageByCondition(ReleaseInfoDto dto, Long headerUserId, boolean isCount,
            boolean isGetCreateUser);

    /**  
    * @Description: 作者删除
    * @author wangheng
    * @param record
    * @return int
    * @throws  
    */
    public Response<Integer> deleteBykid(ReleaseInfo upInfo);
    
    public Response<List<Long>> getKidByCreatedate(String startDate,String endDate);
    
    public Response<List<ReleaseInfoVo>> selectByKids(Set<Long> kidList);

    /**  
    * @Description: 记录查询[无分页]
    * @author wangheng
    * @param @param dto
    * @param @return
    * @return Response<List<ReleaseInfoVo>>
    * @throws  
    */
    public Response<List<ReleaseInfoVo>> selectByCondition(ReleaseInfoDto dto);
}
