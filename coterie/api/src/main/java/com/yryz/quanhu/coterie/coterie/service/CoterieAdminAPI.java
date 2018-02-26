package com.yryz.quanhu.coterie.coterie.service;

import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.quanhu.coterie.coterie.vo.CoterieAdmin;
import com.yryz.quanhu.coterie.coterie.vo.CoterieInfo;
import com.yryz.quanhu.coterie.coterie.vo.CoterieSearchParam;
import com.yryz.quanhu.coterie.coterie.vo.CoterieUpdateAdmin;

/**
 * 私圈管理后台 接口
 * @author wt
 *
 */
public interface CoterieAdminAPI {

    public Response<PageList<CoterieInfo>> getCoterieByPage(CoterieSearchParam param);

    public Response<String> audit(CoterieUpdateAdmin info);

    public Response<CoterieInfo> queryCoterieInfo(Long coterieId);

    /**
     * 设置私圈信息
     * @param info  coterieId必填
     * @throws ServiceException
     */
    public Response<CoterieInfo> modifyCoterieInfo(CoterieInfo info);

}
