package com.yryz.quanhu.coterie.coterie.provider;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yryz.common.exception.QuanhuException;
import com.yryz.common.response.Response;
import com.yryz.common.response.ResponseUtils;
import com.yryz.common.utils.GsonUtils;
import com.yryz.quanhu.coterie.coterie.common.CoterieConstant;
import com.yryz.quanhu.coterie.coterie.common.FrontendConfig;
import com.yryz.quanhu.coterie.coterie.entity.CoterieAuditRecord;
import com.yryz.quanhu.coterie.coterie.exception.DatasOptException;
import com.yryz.quanhu.coterie.coterie.exception.ServiceException;
import com.yryz.quanhu.coterie.coterie.service.CoterieApi;
import com.yryz.quanhu.coterie.coterie.service.CoterieService;
import com.yryz.quanhu.coterie.coterie.until.ImageUtils;
import com.yryz.quanhu.coterie.coterie.until.ZxingHandler;
import com.yryz.quanhu.coterie.coterie.vo.Coterie;
import com.yryz.quanhu.coterie.coterie.vo.CoterieAuditInfo;
import com.yryz.quanhu.coterie.coterie.vo.CoterieBasicInfo;
import com.yryz.quanhu.coterie.coterie.vo.CoterieInfo;
import com.yryz.quanhu.user.service.AccountApi;
import com.yryz.quanhu.user.service.UserApi;
import com.yryz.quanhu.user.vo.UserBaseInfoVO;

/**
 * 私圈服务实现
 * @author wt
 *
 */
@Service(interfaceClass=CoterieApi.class)
public class CoterieProvider implements CoterieApi {
	private Logger logger = LoggerFactory.getLogger(getClass());
	//todo
	private static final FrontendConfig frontendUrl = new FrontendConfig();
	@Autowired
	private CoterieService coterieService;

	 @Reference
	 private UserApi userApi;
	@Reference
	private AccountApi accountApi;
	@Reference
	private CoterieApi coterieApi;
	 /**
	  * 查询私圈信息列表
	 * @param coterieIdList 私圈ID集合
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> queryListByCoterieIdList(List<Long> coterieIdList) {
		logger.info("queryListByCoterieIdList params:" + coterieIdList);
		if (CollectionUtils.isEmpty(coterieIdList)) {
			return ResponseUtils.returnListSuccess(Lists.newArrayList());
		}
		try {
			List<CoterieInfo> list=coterieService.findList(coterieIdList);
			fillCircleInfo(list);
			fillCustInfo(list);
			return ResponseUtils.returnListSuccess(list);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 查询已上架的私圈列表
	 * @param circleId
	 * @param pageNum 当前页，最小为1
	 * @param pageSize 每页显示多少条
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> queryPage(String circleId, Integer pageNum, Integer pageSize) {
		logger.info("CoterieApi.queryPage params: " + pageNum + "," + pageSize);
		if (StringUtils.isEmpty(circleId) || pageNum == null || pageNum < 1 || pageSize == null || pageSize < 1) {
			return ResponseUtils.returnListSuccess(Lists.newArrayList());
		}
		try {
			List<CoterieInfo> list=coterieService.findPage( pageNum, pageSize, CoterieConstant.Status.PUTON.getStatus());
			fillCircleInfo(list);
			fillCustInfo(list);
			return ResponseUtils.returnListSuccess(list);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 查询私圈信息
	 * @param
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<CoterieInfo> queryCoterieInfo(Long coterieId) {

		logger.info("CoterieApi.queryCoterieInfo params: " + coterieId);
		if (coterieId == null) {
			return null;
		}
		try {
			CoterieInfo info = coterieService.find(coterieId);
			if (info != null) {
				//fillCustInfo(Arrays.asList(info));
				Integer newMemberNum =0;
				//todo
				//Integer newMemberNum = coterieMemberService.queryNewMemberNum(coterieId);
				info.setNewMemberNum(newMemberNum);
				fillCircleInfo(Arrays.asList(info));
			}
			return ResponseUtils.returnObjectSuccess(info);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			throw ServiceException.parse(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 设置私圈信息
	 * @param info  coterieId必填
	 * @throws ServiceException
	 */
	@Override
	public Response<CoterieInfo> modifyCoterieInfo(CoterieInfo info) {
		logger.info("CoterieApi.modifyCoterieInfo params:" + info);

		try {
			if (info == null ||  info.getCoterieId()==null) {
				throw new QuanhuException( "2007","参数错误","coterieId",null);

			}
			String name = StringUtils.trim(info.getName());
			if (StringUtils.isNotEmpty(name)) {
				List<CoterieInfo> clist = coterieService.findByName(name);
				if (!clist.isEmpty()&&!clist.get(0).getCoterieId().equals(info.getCoterieId())) {
					throw new QuanhuException( "2007","参数错误","私圈名称已存在",null);
				}
			}
			//费用单位错误防范
			if (!(info.getJoinFee()!=null && info.getJoinFee()<=100 && info.getJoinFee()>=0)) {

				throw new QuanhuException( "2007","参数错误","加入私圈金额设置不正确。",null);
			}
			if (!(info.getConsultingFee()!=null && info.getConsultingFee()<=100 && info.getConsultingFee()>=0)) {
				throw new QuanhuException( "2007","参数错误","私圈咨询费金额设置不正确。",null);
			}
			coterieService.modify(info);

		} catch (QuanhuException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		}catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			throw ServiceException.sysError();
		} catch (ServiceException e) {
			throw ServiceException.parse(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			throw ServiceException.sysError();
		}
		return ResponseUtils.returnObjectSuccess(info);
	}
	/**
	 * 申请创建私圈
	 * @param info
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<CoterieInfo> applyCreate(CoterieBasicInfo info) {
				logger.info("CoterieApi.applyCreate params:" + info);
				try {
					checkApplyCreateParam(info);
					return ResponseUtils.returnObjectSuccess(coterieService.save(info));
				} catch (DatasOptException e) {
					logger.error(e.getMessage(), e);
					return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
		return ResponseUtils.returnException(e);
		}
		catch (QuanhuException e) {
		return ResponseUtils.returnException(e);
		} catch (Exception e) {
		logger.error("unKown Exception", e);
		return ResponseUtils.returnException(e);
		}
		}

	private void checkApplyCreateParam(CoterieBasicInfo info) {
		if (info == null) {
			throw new QuanhuException( "2007","对象为空","对象为空",null);
		}
		if (StringUtils.isEmpty(info.getIcon())) {
			throw new QuanhuException( "2007","参数错误","icon不能为空",null);

		}
		if (StringUtils.isEmpty(info.getIntro())) {

			throw new QuanhuException( "2007","参数错误","intro不能为空",null);
		}
		if (StringUtils.isEmpty(info.getName())) {

			throw new QuanhuException( "2007","参数错误","name不能为空",null);
		}
		if (StringUtils.isEmpty(info.getOwnerId())) {

			throw new QuanhuException( "2007","参数错误","ownerId不能为空",null);
		}
		if (!(info.getJoinFee()!=null && info.getJoinFee()<=500 && info.getJoinFee()>=0)) {//私圈单位为分，0表示免费，小于100的  单位必定错误

			throw new QuanhuException( "2007","参数错误","加入私圈金额设置不正确。",null);
		}

		//todo
		List<CoterieInfo> coterieList = coterieService.findByName(StringUtils.trim(info.getName()));
		if (!coterieList.isEmpty()) {
			//return ResponseUtils.returnObjectSuccess(coterieService.save(info));

			throw new QuanhuException( "2007","参数错误","私圈名称已存在",null);
		}
		Response<List<CoterieInfo>> responseCoterieInfo=coterieApi.getMyCreateCoterie(info.getOwnerId());
		if(responseCoterieInfo.getData().size()>10)
		{

			throw new QuanhuException( "2007","参数错误","最多只能申请10个私圈",null);
		}
		//todo
//		Response<UserSimpleVO> cust=userApi.getUserSimple(info.getOwnerId());
//		if(cust==null){
//			throw new ServiceException(ServiceException.CODE_SYS_ERROR, "用户("+info.getOwnerId()+")不存在");
//		}
		//todo
		/*
		if(StringUtils.isEmpty(cust.getCustLevel())){
			throw new ServiceException(ServiceException.CODE_SYS_ERROR, "用户等级为v5或以上才能创建私圈");
		}
		int level=Integer.valueOf(cust.getCustLevel()).intValue();
		if(level<5){
			throw new ServiceException(ServiceException.CODE_SYS_ERROR, "用户等级为v5或以上才能创建私圈");
		}
		*/
	}
	/**
	 * 我创建的私圈
	 * @param custId
	 * @param
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> getMyCreateCoterie(String custId ) throws ServiceException {
		logger.info("CoterieApi.getMyCreateCoterie params: " + custId    );
		if (StringUtils.isEmpty(custId)  ) {
			throw ServiceException.paramsError();
		}
		try {
			List<CoterieInfo> list = coterieService.findMyCreateCoterie(custId);
			fillCustInfo(list);
			fillCircleInfo(list);
			return ResponseUtils.returnListSuccess(list);
		}
		catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		}
		catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
	} catch (Exception e) {
		logger.error("unKown Exception", e);
		return ResponseUtils.returnException(e);
	}
	}
	/**
	 * 我加入的私圈
	 * @param custId
	 * @param
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> getMyJoinCoterie(String custId) throws ServiceException {
		logger.info("CoterieApi.getMyJoinCoterie params: " + custId   );
		if (StringUtils.isEmpty(custId)  ) {
			throw ServiceException.paramsError();
		}
		try {
			List<CoterieInfo> list = coterieService.findMyJoinCoterie(custId );
			fillCircleInfo(list);
			fillCustInfo(list);
			return ResponseUtils.returnListSuccess(list);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		}catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			throw ServiceException.parse(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 私圈数量
	 * @param circleId null表示不需要此条件
	 * @param status null表示不需要此条件
	 * @return 私圈数量
	 * @throws ServiceException
	 */
	@Override
	public Response<Integer> getCoterieCount(String circleId, Byte status) throws ServiceException {
		logger.info("CoterieApi.getCoterieCount circleId: " + circleId);
		try {
			return  ResponseUtils.returnObjectSuccess(coterieService.getCoterieCount(circleId, status));
		}catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 查询已上架的私圈列表
	 * @param pageNum 当前页，最小为1
	 * @param pageSize 每页显示多少条
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> queryPage(Integer pageNum, Integer pageSize) throws ServiceException {
		logger.info("CoterieApi.queryPage pageNum: " + pageNum + ",pageSize:" + pageSize);
		try {
			List<CoterieInfo> list=coterieService.findPage(pageNum, pageSize);
			fillCustInfo(list);
			fillCircleInfo(list);
			return ResponseUtils.returnListSuccess(list);
		}catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 我创建的私圈
	 * @param custId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> getMyCreateCoterie(String custId, Integer pageNum, Integer pageSize, Integer status)
			throws ServiceException {
		logger.info("CoterieApi.getMyCreateCoterie pageNum: " + pageNum + ",pageSize:" + pageSize+",custId:"+custId);
		try {
			List<CoterieInfo> list=coterieService.findMyCreateCoterie(custId, pageNum, pageSize,status);
			fillCircleInfo(list);
			fillCustInfo(list);
			return ResponseUtils.returnListSuccess(list);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		}catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 我创建的私圈  数量
	 * @param custId
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<Integer> getMyCreateCoterieCount(String custId, Integer status) throws ServiceException {
		logger.info("CoterieApi.getMyCreateCoterieCount custId: " + custId);
		try {
			return  ResponseUtils.returnObjectSuccess(coterieService.findMyCreateCoterieCount(custId,status));
		}catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 我加入的私圈
	 * @param custId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> getMyJoinCoterie(String custId, Integer pageNum, Integer pageSize)
			throws ServiceException {
		logger.info("CoterieApi.getMyJoinCoterie pageNum: " + pageNum + ",pageSize:" + pageSize+",custId:"+custId);
		try {
			List<CoterieInfo> list=coterieService.findMyJoinCoterie(custId, pageNum, pageSize);
			fillCircleInfo(list);
			fillCustInfo(list);
			return ResponseUtils.returnListSuccess(list);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		}catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 我加入的私圈 数量
	 * @param custId
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<Integer> getMyJoinCoterieCount(String custId) throws ServiceException {
		logger.info("CoterieApi.getMyJoinCoterieCount custId: " + custId);
		try {
			return  ResponseUtils.returnObjectSuccess(coterieService.findMyJoinCoterieCount(custId));
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		}catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 平台首页 推荐3个私圈
	 * @param custId
	 * @return
	 */
	@Override
	public Response<List<CoterieInfo>> getRecommendCoterie(String custId) {

		List<CoterieInfo> resultList=Lists.newArrayList();

		return ResponseUtils.returnListSuccess(resultList);
	}
	/**
	 * 圈子首页 推荐
	 * @param
	 * @return
	 */
	@Override
	public Response<List<CoterieInfo>> getRecommendCoterieForCircle() {
		logger.info("CoterieApi.getRecommendCoterieForCircle circleId: " );
		//获取运营推荐的私圈
		List<CoterieInfo> recommendList=coterieService.getRecommendList("", 0, 1);
		//获取达人的私圈
		List<CoterieInfo> expertList=coterieService.getHeatList("", CoterieConstant.Expert.YES.getStatus(), 0, 20);
		//获取非达人的私圈
		List<CoterieInfo> list=coterieService.getHeatList("", CoterieConstant.Expert.NO.getStatus(), 0, 20);
		
		List<CoterieInfo> resultList=Lists.newArrayList();
		resultList.addAll(recommendList);
		
		for (int i = 0; i < expertList.size(); i++) {
			CoterieInfo info=expertList.get(i);
			if(resultList.size()<5 && !resultList.contains(info)){
				resultList.add(info);
			}
		}
		
		for (int i = 0; i < list.size(); i++) {
			CoterieInfo info=list.get(i);
			if(resultList.size()<10 && !resultList.contains(info)){
				resultList.add(info);
			}
		}
		
		//如果不够10个则取上线的任意圈子补
		List<CoterieInfo> allList=coterieService.findPage(  1, 20, CoterieConstant.Status.PUTON.getStatus());
		for (int i = 0; i < allList.size(); i++) {
			CoterieInfo info=allList.get(i);
			if(resultList.size()<10 && !resultList.contains(info)){
				resultList.add(info);
			}
		}
		
		fillCustInfo(resultList);
		fillCircleInfo(resultList);
		return ResponseUtils.returnListSuccess(resultList);
	}
	/**
	 * 查询已上架的私圈列表
	 * @param pageNum 当前页，最小为1
	 * @param pageSize 每页显示多少条
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> queryPageForApp(Integer pageNum, Integer pageSize) throws ServiceException {
		logger.info("CoterieApi.queryPageForApp params: " + pageNum + "," + pageSize);
		if (pageNum == null) {
			pageNum=1;
		}
		if(pageSize == null){
			pageSize=10;
		}
		
		try {
			List<CoterieInfo> list=coterieService.queryPageForApp(pageNum, pageSize);
			fillCircleInfo(list);
			fillCustInfo(list);
			return ResponseUtils.returnListSuccess(list);
		}catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 根据名称模糊所属私圈
	 * @param circleId  可以为null
	 * @param name
	 * @param start
	 * @param pageSize
	 * @return
	 */
	@Override
	public Response<List<CoterieInfo>> getCoterieLikeName(String circleId, String name, Integer start, Integer pageSize) {
		logger.info("CoterieApi.getCoterieLikeName circleId:" + circleId + ",name: " + name + ",start:" + start
				+ ",pageSize" + pageSize);
		if (name == null) {
			ServiceException.paramsError("name");
		}
		if(start==null){
			start=0;
		}
		if(pageSize==null){
			pageSize=10;
		}
		
		try {
			List<CoterieInfo> infoList = coterieService.getCoterieLikeName(circleId,name, start, pageSize);
			fillCircleInfo(infoList);
			fillCustInfo(infoList);
			return ResponseUtils.returnListSuccess(infoList);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		}catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 更新私圈主达人状态
	 * @param custId  圈主用户ID
	 * @param isExpert 是否达人，0否，1是
	 * @throws ServiceException
	 */
	@Override
	public void modifyCoterieExpert(String custId, Byte isExpert) throws ServiceException {
		logger.info("CoterieApi.modifyCoterieExpert custId: " + custId + ",isExpert" + isExpert);
		if(StringUtils.isEmpty(custId) || isExpert==null){
			throw ServiceException.paramsError();
		}
		try {
			coterieService.modifyCoterieExpert(custId, isExpert);
		}catch (QuanhuException e) {
			logger.error(e.getMessage(), e);
			throw new QuanhuException( "2007","参数错误"," ",null);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			throw ServiceException.sysError();
		} catch (ServiceException e) {
			throw ServiceException.parse(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			throw ServiceException.sysError();
		}
	}
	/**
	 * 查询我创建了私圈的圈子ID集合
	 * @param ownerId
	 * @return
	 */
	@Override
	public Response<List<String>> getCircleIdList(String ownerId) {
		logger.info("CoterieApi.getCircleIdList ownerId:" + ownerId);
		if (StringUtils.isEmpty(ownerId)) {
			ServiceException.paramsError("ownerId");
		}
		
		try {
			List<String> circleIdList = coterieService.getCircleIdListByOwnerId(ownerId);
			return ResponseUtils.returnListSuccess(circleIdList);
		} catch (QuanhuException e) {
			return ResponseUtils.returnException(e);
		}
		catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	
	private void fillCircleInfo(List<CoterieInfo> infoList){
		if(infoList==null || infoList.isEmpty()){
			return;
		}
		Set<String> circleIdSet=Sets.newHashSet();
		for (int i = 0; i < infoList.size(); i++) {
			CoterieInfo coterie=infoList.get(i);
			if(coterie.getMemberNum()==null){
				coterie.setMemberNum(0);
			}
			coterie.setMemberNum(coterie.getMemberNum()+1);//私圈成员数量需要加上圈主
		}

	}
	
	private void fillCustInfo(List<CoterieInfo> infoList){
		if(infoList==null || infoList.isEmpty()){
			return;
		}
		Set<String> custIdSet=Sets.newHashSet();
		for (int i = 0; i < infoList.size(); i++) {
			if(infoList.get(i)==null){
				continue;
			}
			String custId=infoList.get(i).getOwnerId();
			if(StringUtils.isNotEmpty(custId)){
				custIdSet.add(custId);
			}
		}
		if(custIdSet.isEmpty()){
			return;
		}
		Response<Map<String,UserBaseInfoVO>> UserBaseInfoVOs= userApi.getUser(custIdSet);
		Map<String, UserBaseInfoVO> custMaps=UserBaseInfoVOs.getData();
		for (int i = 0; i < infoList.size(); i++) {
			CoterieInfo o=infoList.get(i);
			UserBaseInfoVO cust=custMaps.get(o.getOwnerId());
			if(cust!=null){
				o.setCustIcon(cust.getUserImg());
				o.setOwnerName(cust.getUserNickName());
				o.getUser().setHeadImg(cust.getUserImg());
				o.getUser().setAuthStatus(cust.getAuthStatus().toString());
				o.getUser().setNickName(cust.getUserNickName());


			}
		}
	}
	/**
	 * 我的圈子数：创建的和参加的私圈数总和
	 * @param custId
	 * @return
	 */
	@Override
	public Response<Integer> getMyCoterieCount(String custId) {
		logger.info("CoterieApi.getMyCreateCoterieCount custId: " + custId);
		try {
			Integer count=coterieService.findMyJoinCoterieCount(custId);
			count+=coterieService.findMyCreateCoterieCount(custId,null);
			return  ResponseUtils.returnObjectSuccess(count);
		} catch (QuanhuException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		}catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			return ResponseUtils.returnException(e);
		} catch (ServiceException e) {
			return ResponseUtils.returnException(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	/**
	 * 保存私圈审核记录
	 * @param info
	 */
	@Override
	public void saveAuditRecord(CoterieAuditInfo info) {
		logger.info("CoterieApi.saveAuditRecord params:" + info);
		if (info == null || StringUtils.isEmpty(info.getCoterieId()) || StringUtils.isEmpty(info.getCustId())
				|| StringUtils.isEmpty(info.getCustName()) || info.getStatus() == null) {
			throw ServiceException.paramsError();
		}
		
		try {
			CoterieAuditRecord record= GsonUtils.parseObj(info, CoterieAuditRecord.class);
			record.setCreateDate(new Date());
			record.setLastUpdateTime(new Date());
			coterieService.saveAuditRecord(record);
		} catch (QuanhuException e) {
			logger.error(e.getMessage(), e);
			throw new QuanhuException( "2007","参数错误"," ",null);
		} catch (DatasOptException e) {
			logger.error(e.getMessage(), e);
			throw ServiceException.sysError();
		} catch (ServiceException e) {
			throw ServiceException.parse(e);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			throw ServiceException.sysError();
		}
	}
	/**
	 * 我创建的私圈
	 * @param custId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<List<CoterieInfo>> getMyCreateCoterie(String custId, Integer pageNum, Integer pageSize)
			throws ServiceException {
		return getMyCreateCoterie(custId,pageNum,pageSize,null);
	}
	/**
	 * 我创建的私圈  数量
	 * @param custId
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public Response<Integer> getMyCreateCoterieCount(String custId) throws ServiceException {
		return getMyCreateCoterieCount(custId,null);
	}
	/**
	 * @Override
	 * @Title: regroupQr
	 * @Description:  组装二维码
	 * @author
	 * @param @param info
	 * @throws
	 */
	public Response<String> regroupQr(CoterieInfo info) {
		String result = null;

		if (null == info || StringUtils.isEmpty(info.getQrUrl())) {
			return ResponseUtils.returnObjectSuccess(result);
		}
		Set<String> set = new HashSet<String>();
		set.add(info.getOwnerId());
		Response<Map<String,UserBaseInfoVO>> cust=userApi.getUser(set);
		UserBaseInfoVO user = cust.getData().get(0);
		try {
			BufferedImage base = ImageUtils.createBaseImage(300, 400, Color.WHITE);

			// 生成二维码跳转链接:http://m-dev.quanhu365.com/sjmwq/coterie/3qrfpp64crnp
			//todo
			String frontend ="";
//			String frontend = frontendUrl.getFrontendUrl()
//					+ String.valueOf(((Map<String, Object>) AppRuntimeContext.getTenantConfig().get(info.getCircleId())).get(AppConst.APPNAME))
//					+ "/coterie/" + info.getCoterieId();
			ByteArrayOutputStream frontendQrOS = new ByteArrayOutputStream();
			ZxingHandler.encode2(frontend, 200, 200, frontendQrOS);
			BufferedImage i1 = ImageIO.read(new ByteArrayInputStream(frontendQrOS.toByteArray()));
			ImageUtils.overlapMiddleImage(base, i1);
			if (StringUtils.isNotBlank(user.getUserImg())) {
				try {
					BufferedImage i2 = ImageUtils.loadImageUrl(user.getUserImg());
					ImageUtils.overlapImage(base, ImageUtils.resize(i2, 45, 45), (base.getWidth() - 45) / 2, 280);
				} catch (Exception e) {
					logger.error("组装私圈二维码，添加用户头像异常", e);
				}
			}
			ImageUtils.middleAddText(base, info.getName(), 100, 20);
			ImageUtils.middleAddText(base, info.getOwnerName(), 350, 16);
			//ImageUtils.writeImageLocal("F:/合成123.png", base);

			// 转流
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(base, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			// 文件上传至 oss服务器
			/**UploadResultDetail uploadResult = UploadUtil.uploadImg(is, new File(info.getQrUrl()).getName(),
			 AppRuntimeContext.getAppEnv().getAppName() + "/coterie", false);
			 if (null != uploadResult && "success".equals(uploadResult.getMsg())) {
			 result = uploadResult.getResouceUrl();
			 log.debug("重组私圈二维码图片成功，地址：" + uploadResult.getMsg() + result);
			 }*/

			// 生成的文件输出为 base64
			// in.available()返回文件的字节长度
			byte[] bytes = new byte[is.available()];
			// 将文件中的内容读入到数组中
			try {
				is.read(bytes);
			} finally {
				is.close();
			}

			// 将图片字节流数组转换为base64
			result = "data:image/png;base64," + new String(Base64.encodeBase64(bytes));
			//Base64Util.encodeToString(bytes);

		} catch (Exception e) {
			logger.error("组装私圈二维码图片异常！", e);
		}
		return ResponseUtils.returnObjectSuccess(result);
	}
	
	@Override
	public Response<List<Long>> getKidByCreateDate(String startDate, String endDate) {
		logger.info("CoterieApi.getKidByCreateDate startDate:" + startDate+",endDate:"+endDate);
		try {
			List<Long> kidList=coterieService.getKidByCreateDate(startDate, endDate);
			return  ResponseUtils.returnObjectSuccess(kidList);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
	
	@Override
	public Response<List<Coterie>> getByKids(List<Long> kidList) {
		logger.info("CoterieApi.getByKids kidList:" + kidList);
		try {
			List<com.yryz.quanhu.coterie.coterie.entity.Coterie> list=coterieService.getByKids(kidList);
			List<Coterie> rstList=GsonUtils.parseList(list, Coterie.class);
			return  ResponseUtils.returnObjectSuccess(rstList);
		} catch (Exception e) {
			logger.error("unKown Exception", e);
			return ResponseUtils.returnException(e);
		}
	}
}

