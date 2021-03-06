package com.yryz.quanhu.dymaic;

import com.yryz.common.response.PageList;
import com.yryz.common.response.Response;
import com.yryz.common.utils.GsonUtils;
import com.yryz.quanhu.dymaic.dto.QueryDymaicDTO;
import com.yryz.quanhu.dymaic.service.DymaicService;
import com.yryz.quanhu.dymaic.vo.Dymaic;
import com.yryz.quanhu.dymaic.vo.DymaicVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @author xiepeng
 * @version 1.0
 * @data 2018/1/19 0019 32
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = {"classpath:test.xml"})
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DymaicTest {

    private final static Long USER_DEMO = 739855842419507200L;
    private final Long USER_DEBAR = 200L;


    @Autowired
    private DymaicService dymaicService;

    @Test
    public void add() {
        Dymaic tmp = new Dymaic();
        tmp.setKid(1L);
        tmp.setUserId(USER_DEMO);

        Dymaic dymaic = new Dymaic();
        dymaic.setUserId(USER_DEMO);
        dymaic.setTransmitNote("transmit");
        dymaic.setTransmitType("1001");
        dymaic.setExtJson(GsonUtils.parseJson(tmp));
        dymaic.setModuleEnum("1000");

        Response<Boolean> response = dymaicService.send(dymaic);
        print(response);
    }

    @Test
    public void delete() {
        Response<Boolean> response = dymaicService.delete(USER_DEMO, 107635L);
        print(response);
    }

    @Test
    public void shelve() {
        Response<Boolean> response = dymaicService.shelve(USER_DEMO, 107641L, false);
        print(response);
    }

    @Test
    public void get() {
        Response<Dymaic> response = dymaicService.get(107641L);
        print(response);
    }

    @Test
    public void getBatch() {
        List<Long> kids = Arrays.asList(new Long[]{107226L,107227L,107230L,107426L});
        Response<Map<Long, Dymaic>> response = dymaicService.get(kids);
        print(response);
    }

    @Test
    public void getLastSend() {
        Set<Long> userIds = new HashSet<>();
        userIds.add(724007310011252736L);
        Response<Map<Long, Dymaic>> response = dymaicService.getLastSend(userIds);
        print(response);
    }

    //============  SendList ==============

    @Test
    public void getSendList() {
        Response<List<DymaicVo>> response = dymaicService.getSendList(USER_DEMO, USER_DEMO, 0L, 5L);
        print(response);
    }

    @Test
    public void rebuildSendList() {
        Response<Boolean> response = dymaicService.rebuildSendList(USER_DEBAR);
        print(response);
    }


    //============  TimeLine ==============
    @Test
    public void getTimeLine() {
        Response<List<DymaicVo>> response = dymaicService.getTimeLine(USER_DEMO, 0L, 2L);
        print(response);
    }

    @Test
    public void pushTimeLine() {
        Dymaic dymaic = new Dymaic();
        dymaic.setUserId(USER_DEMO);
        dymaic.setKid(107233L);
        dymaic.setModuleEnum("1000");

        Response<Boolean> response = dymaicService.pushTimeLine(dymaic);
        print(response);
    }

    @Test
    public void rebuildTimeLine() {
        Response<Boolean> response = dymaicService.rebuildTimeLine(USER_DEBAR, USER_DEMO);
        print(response);
    }

    @Test
    public void shuffleTimeLine() {
        Response<Boolean> response = dymaicService.shuffleTimeLine(USER_DEBAR, USER_DEMO);
        print(response);
    }


    //============  置顶动态 ==============
    private static final Long userId = 724011759597371392L;
    private static final Long kid = 109844L;

    @Test
    public void addTop() {
        dymaicService.addTopDymaic(userId, kid);
    }

    @Test
    public void getTop() {
        Response<DymaicVo> response = dymaicService.getTopDymaic(userId);
        print(response);
    }

    @Test
    public void deleteTop() {
        dymaicService.deleteTopDymaic(userId, kid);
    }


    //============  管理动态 ==============
    @Test
    public void queryAll() {
        QueryDymaicDTO queryDymaicDTO = new QueryDymaicDTO();
        for (int i = 1; i < 5; i ++) {
            queryDymaicDTO.setCurrentPage(i);
            queryDymaicDTO.setPageSize(1);
            queryDymaicDTO.setDelFlag(10);
            queryDymaicDTO.setEndDate("2018-02-02");
            Response<PageList<DymaicVo>> response = dymaicService.queryAllDymaic(queryDymaicDTO);
            print(response);
        }
    }

    private void print(Object obj) {
        System.out.println("\n" + GsonUtils.parseJson(obj) + "\n");
    }
}
