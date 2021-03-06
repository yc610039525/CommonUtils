package com.boco.flow.order.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.boco.attemp.pojo.IName;
import com.boco.attemp.pojo.IService;
import com.boco.core.bean.SpringContextUtil;
import com.boco.core.ibatis.dao.IbatisDAO;
import com.boco.core.ibatis.dao.IbatisDAOHelper;
import com.boco.core.ibatis.vo.Record;
import com.boco.core.ibatis.vo.ServiceActionContext;
import com.boco.core.spring.SysProperty;
import com.boco.core.utils.id.CUIDHexGenerator;
import com.boco.core.utils.lang.TimeFormatHelper;
import com.boco.flow.common.SheetConstants;
import com.boco.flow.common.bo.IKeyMapping;
import com.boco.flow.common.pojo.IOrder;
import com.boco.flow.common.pojo.IOrderDetail;
import com.boco.flow.order.OrderConstants;
import com.boco.flow.order.bo.mapping.OrderDetailServiceMapping;
import com.boco.flow.order.bo.mapping.PortType2RateMapping;
import com.boco.flow.order.pojo.InfoAttempType;
import com.boco.flow.order.pojo.InfoDesignType;
import com.boco.flow.order.pojo.OrderDetailInfo;
import com.boco.flow.order.pojo.OrderInfo;
import com.boco.flow.traph.bo.TraphMaintainBO;
import com.boco.maintain.device.bo.Property;
import com.boco.maintain.traph.bo.ResTraphBO;

@SuppressWarnings("unchecked")
public class BaseOrderMaintainBO  {
	
	private static final Logger logger = Logger.getLogger(BaseOrderMaintainBO.class);
	
	private static final String sqlMap = "BaseOrderMaintain";
	
	private IKeyMapping detailMapping = new OrderDetailServiceMapping();
	
	protected IbatisDAO IbatisResDAO;
	
	public void setIbatisResDAO(IbatisDAO ibatisResDAO) {
		IbatisResDAO = ibatisResDAO;
	}
	protected ResTraphBO ResTraphBO;

	public void setResTraphBO(ResTraphBO resTraphBO) {
		ResTraphBO = resTraphBO;
	}
	private PortType2RateMapping PortType2RateMapping = new PortType2RateMapping();
	
	
	/**
	 * 根据订单ID获取订单信息
	 */
	public IOrder getOrderById(String orderId) {
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("cuid", orderId);
		Map<String, Object> map = (Map<String, Object>)IbatisResDAO.getSqlMapClientTemplate().queryForObject(sqlMap + ".getOrder", pm);
		OrderInfo orderInfo = null;
		if(map != null) {
			String orderCode = IbatisDAOHelper.getStringValue(map, "ORDER_CODE");
			String title = IbatisDAOHelper.getStringValue(map, "TITLE");
			String relatedDistrictCuid = IbatisDAOHelper.getStringValue(map, "RELATED_DISTRICT_CUID");
			int netType = IbatisDAOHelper.getIntValue(map, "NET_TYPE");
			int sheetType = IbatisDAOHelper.getIntValue(map, "SHEET_TYPE");
			orderInfo = new OrderInfo(orderCode, orderId, title, relatedDistrictCuid, netType, sheetType);
			String formCode = IbatisDAOHelper.getStringValue(map, "FROMCODE");
			Date applyDate = (Date)map.get("APPLY_DATE");
			Date finishDate = (Date)map.get("FINISH_DATE");
			orderInfo.setFormCode(formCode);
			orderInfo.setApplyDate(applyDate);
			orderInfo.setFinishDate(finishDate);
			orderInfo.setData(map);
		}
		return orderInfo;
	}
	/**
	 * @author xueyh 20130227
	 * 根据大客户系统单据ID获取申请信息
	 * @param irmstitle  大客户系统单据ID
	 */
	public IOrder getOrderByOssNum(String irmstitle) {
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("heading_num", irmstitle);
		Map<String, Object> map = (Map<String, Object>)IbatisResDAO.getSqlMapClientTemplate().queryForObject(sqlMap + ".getOrder", pm);
		OrderInfo orderInfo = null;
		if(map != null) {
			String orderCode = IbatisDAOHelper.getStringValue(map, "ORDER_CODE");
			String title = IbatisDAOHelper.getStringValue(map, "TITLE");
			String relatedDistrictCuid = IbatisDAOHelper.getStringValue(map, "RELATED_DISTRICT_CUID");
			int netType = IbatisDAOHelper.getIntValue(map, "NET_TYPE");
			int sheetType = IbatisDAOHelper.getIntValue(map, "SHEET_TYPE");
			orderInfo = new OrderInfo(orderCode, irmstitle, title, relatedDistrictCuid, netType, sheetType);
			String formCode = IbatisDAOHelper.getStringValue(map, "FROMCODE");
			Date applyDate = (Date)map.get("APPLY_DATE");
			Date finishDate = (Date)map.get("FINISH_DATE");
			orderInfo.setFormCode(formCode);
			orderInfo.setApplyDate(applyDate);
			orderInfo.setFinishDate(finishDate);
			orderInfo.setData(map);
		}
		return orderInfo;
	}
	/**
	 * 创建订单信息
	 * @param order 订单信息
	 * @return
	 */
	public void createOrder(ServiceActionContext ac, IOrder order) {
		if(!(order instanceof OrderInfo)) {
			throw new RuntimeException("订单信息创建失败，订单实例无法转换！");
		}
		OrderInfo orderInfo = (OrderInfo) order;
		Map<String, Object> orderMap = orderInfo.getData();
		Date now = new Date();
		Record record = new Record("T_ACT_ORDER");
		record.addColValue("CUID", orderInfo.getOrderId());
		record.addColValue("ORDER_CODE", orderInfo.getOrderCode());
		record.addColValue("NET_TYPE", orderInfo.getNetType()); //网络类型
		record.addColValue("FROMCODE", orderInfo.getFormCode());// 订单来源
		record.addColValue("RELATED_DISTRICT_CUID", orderInfo.getRelatedDistrictCuid());// 电路所属地市
		record.addColValue("SHEET_TYPE", orderInfo.getSheetType());// 申请单类型
		record.addColValue("TITLE", orderInfo.getTitle());// 工单主题
		record.addColValue("FINISH_DATE", orderInfo.getFinishDate());// 要求开通时间
		record.addColValue("APPLY_DATE", orderInfo.getApplyDate());// 申请时间
		
		record.addColValue("IRMSTITLE", IbatisDAOHelper.getStringValue(orderMap, "IRMSTITLE"));// 上层工单号
		record.addColValue("IRMS_SHEET_NO", IbatisDAOHelper.getStringValue(orderMap, "IRMS_SHEET_NO"));// 工单流水号
		record.addColValue("CHANGE_SOURCE", IbatisDAOHelper.getStringValue(orderMap, "CHANGE_SOURCE"));// 变更来源
		record.addColValue("CHANGE_BACKUP", IbatisDAOHelper.getStringValue(orderMap, "CHANGE_BACKUP"));// 是否变更实施备案
		record.addColValue("WITH_SECURITY", IbatisDAOHelper.getStringValue(orderMap, "WITH_SECURITY"));// 是否涉及安全
		record.addColValue("WITH_CONNECTION", IbatisDAOHelper.getStringValue(orderMap, "WITH_CONNECTION"));// 是否互联互通
		record.addColValue("RELATED_SHEET_NUM", IbatisDAOHelper.getStringValue(orderMap, "RELATED_SHEET_NUM"));// 设置相关工单号
		record.addColValue("RELATED_CREATER_CUID", IbatisDAOHelper.getStringValue(orderMap, "RELATED_CREATER_NAME"));// 申请人
		record.addColValue("PROJECT_INFO", IbatisDAOHelper.getStringValue(orderMap, "PROJECT_INFO"));// 申请依据
		record.addColValue("PROJECT_DETAIL_INFO", IbatisDAOHelper.getStringValue(orderMap, "PROJECT_DETAIL_INFO"));// 详细描述
		record.addColValue("IS_JIKE", IbatisDAOHelper.getStringValue(orderMap, "IS_JIKE"));// 是否集客电路
		record.addColValue("JIKE_TYPE", IbatisDAOHelper.getIntValue(orderMap, "JIKE_TYPE"));// 集客电路类型
		record.addColValue("APPLYER_TEL", IbatisDAOHelper.getStringValue(orderMap, "APPLYER_TEL"));// 申请人电话
		record.addColValue("SHEET_MEMO", IbatisDAOHelper.getStringValue(orderMap, "SHEET_MEMO"));// 路由信息
		record.addColValue("INTERFACE_TYPE", IbatisDAOHelper.getStringValue(orderMap, "SYS_CALLER"));// 路由信息
		String applyReason=IbatisDAOHelper.getStringValue(orderMap, "APPLY_REASON");
        if(applyReason!=null&&!applyReason.equals("")){
        	record.addColValue("APPLY_REASON", applyReason);
        }else{
        	record.addColValue("APPLY_REASON", "");
        }
		//联通扩展信息
		record.addColValue("HEADING_NUM", IbatisDAOHelper.getStringValue(orderMap, "HEADING_NUM"));// 申请单编号
		record.addColValue("HEADING_NUM_CODE", IbatisDAOHelper.getStringValue(orderMap, "HEADING_NUM_CODE"));// 传真
		record.addColValue("STATE", IbatisDAOHelper.getStringValue(orderMap, "STATE"));//是否为草稿
		record.addColValue("TRAPH_DESIGN_USER_CUID", IbatisDAOHelper.getStringValue(orderMap, "TRAPH_DESIGN_USER_CUID"));//创建人
		
		// 系统信息
		record.addColValue("APPLY_ORG_CUID", IbatisDAOHelper.getStringValue(orderMap, "APPLY_ORG_CUID"));// 申请部门
		record.addColValue("LAST_MODIFY_TIME", now);// 最后修改时间
		record.addColValue("CREATE_TIME", now);// 创建时间
		record.addColValue("TRAPH_COUNT", "0");// 电路数量
		record.addColValue("IS_ARCHIVE", "0");// 是否归档
		record.addColValue("EMERGENCY_FLAG", "0");
		record.addColValue("IS_VIEW", "1");
		record.addColValue("ISDELETE", "0");
		record.addColValue("IRMS_SHEET_TITLE",IbatisDAOHelper.getStringValue(orderMap, "IRMS_SHEET_TITLE"));

		IbatisResDAO.insertDynamicTable(record);
		logger.info(ac.getUserName()+"["+ac.getUserId()+"]创建订单信息完成");
		orderMap.putAll(record.getValueMap());
		
	}
	/**
	 * 更新订单信息
	 * @param ac
	 * @param order
	 * @param nullValueIsUpdate  true 更新空值，false 不更新空值
	 */
	public void updateOrder(ServiceActionContext ac, IOrder order,boolean nullValueIsUpdate) {
		if(!(order instanceof OrderInfo)) {
			throw new RuntimeException("订单信息创建失败，订单实例无法转换！");
		}
		String [] modifyValues = {"CHANGE_BACKUP","WITH_SECURITY","WITH_CONNECTION",
									"RELATED_SHEET_NUM","PROJECT_INFO","PROJECT_DETAIL_INFO",
									"SHEET_MEMO","HEADING_NUM","HEADING_NUM_CODE",
									"STATE","TRAPH_DESIGN_USER_CUID"/*,"DESIGN_TYPE"*/};
		
		Date now = new Date();
		Map<String, Object> orderMap = order.getData();
		Record pk = new Record("T_ACT_ORDER");
		pk.addColValue("CUID", order.getOrderId());
		
		Record r = new Record("T_ACT_ORDER");
		r.addColValue("LAST_MODIFY_TIME", now);// 最后修改时间
		String title = order.getTitle();
		Date finishDate = order.getFinishDate();
		if(nullValueIsUpdate){
			 r.addColValue("TITLE", title);
			 r.addColValue("FINISH_DATE", order.getFinishDate());
		}else{
			if(StringUtils.isNotEmpty(title)){
				r.addColValue("TITLE", title);
			}
			if(finishDate!=null){
				r.addColValue("FINISH_DATE", order.getFinishDate());// 要求开通时间FINISH_DATE
			}
		}
		
		for(String field:modifyValues){
			String value =IbatisDAOHelper.getStringValue(orderMap, field);
			if(nullValueIsUpdate){
				 r.addColValue(field, value);
			}else{
				if(StringUtils.isNotEmpty(value)){
					r.addColValue(field, value);
				}
			}
		}
//		r.addColValue("WITH_SECURITY", IbatisDAOHelper.getStringValue(orderMap, "WITH_SECURITY"));// 是否涉及安全
//		r.addColValue("WITH_CONNECTION", IbatisDAOHelper.getStringValue(orderMap, "WITH_CONNECTION"));// 是否互联互通
//		r.addColValue("RELATED_SHEET_NUM", IbatisDAOHelper.getStringValue(orderMap, "RELATED_SHEET_NUM"));// 相关工单号
//		r.addColValue("PROJECT_INFO", IbatisDAOHelper.getStringValue(orderMap, "PROJECT_INFO"));// 申请依据
//		r.addColValue("PROJECT_DETAIL_INFO", IbatisDAOHelper.getStringValue(orderMap, "PROJECT_DETAIL_INFO"));// 详细描述
//		r.addColValue("SHEET_MEMO", IbatisDAOHelper.getStringValue(orderMap, "SHEET_MEMO"));// 路由信息
//		
//		//联通扩展信息
//		r.addColValue("HEADING_NUM", IbatisDAOHelper.getStringValue(orderMap, "HEADING_NUM"));// 申请单编号
//		r.addColValue("HEADING_NUM_CODE", IbatisDAOHelper.getStringValue(orderMap, "HEADING_NUM_CODE"));// 传真
//		r.addColValue("STATE", IbatisDAOHelper.getStringValue(orderMap, "STATE"));//是否为草稿
//		r.addColValue("TRAPH_DESIGN_USER_CUID", IbatisDAOHelper.getStringValue(orderMap, "TRAPH_DESIGN_USER_CUID"));//创建人
		
		IbatisResDAO.updateDynamicTable(r, pk);
		logger.info(ac.getUserName()+"["+ac.getUserId()+"]更新订单信息完成");
	}
	
	
	/**
	 * 创建订单明细信息
	 * @param ac
	 * @param detailList
	 */
	public void createOrderDetail(ServiceActionContext ac, List<IOrderDetail> detailList) {
		Date now = new Date();
		List<String> traphCuidList = new ArrayList<String>();
		if(detailList != null && !detailList.isEmpty()) {
			IOrder order = detailList.get(0).getOrder();
			if(OrderConstants.ORDER_TYPE_ADD == order.getSheetType()) {
				List<Record> rList = new ArrayList<Record>();
				for(IOrderDetail detail : detailList) {
					if(!order.getOrderId().equals(detail.getOrder().getOrderId())) {
						throw new RuntimeException("订单明细必须归属同一张订单！");
					}
					Record r = this.parseDetailInfo(detail);
					r.addColValue("CREATE_TIME", now);
					r.addColValue("LAST_MODIFY_TIME", now);
					String detailCuid = CUIDHexGenerator.getInstance().generate("T_ACT_ORDER_DETAIL");
					r.addColValue("CUID", detailCuid);
					r.addColValue("ATTEMP_TYPE", detail.getAttempType());
					r.addColValue("DESIGN_TYPE", detail.getDesignType());
					r.addColValue("RELATED_SHEET_CUID", detail.getRelatedSheetCuid());
					r.addColValue("RELATED_APPLYSHEET_CUID", order.getOrderId());
					r.addColValue("SORT_NO", detail.getSortNo());
					
					r.addColValue("IS_WRITE_RESULT", 0);
					r.addColValue("ISDELETE", 0);
					r.addColValue("IS_TO_STOP_SHEET", 0);
					r.addColValue("OBJECT_TYPE_CODE", 1001);
					r.addColValue("PORT_REJECT_DIRECTION", 0);
					r.addColValue("REJECT_TYPE", 0);
//					r.addColValue("END_SWITCHDEV_PORT_Z", detail.getData().get("END_SWITCHDEV_PORT_Z"));
//					r.addColValue("END_SWITCHDEV_PORT_A", detail.getData().get("END_SWITCHDEV_PORT_A"));
//					r.addColValue("END_SWITCH_DEV_A", detail.getData().get("END_SWITCH_DEV_A"));
//					r.addColValue("END_SWITCH_DEV_Z", detail.getData().get("END_SWITCH_DEV_Z"));
//					r.addColValue("EXT_IDS", ","+detail.getData().get("EXT_IDS")+",");
					rList.add(r);
					
					detail.setCuid(detailCuid);
				}
				this.IbatisResDAO.insertDynamicTableBatch(rList);
			}else {
				for (IOrderDetail detail : detailList) {
					if(!order.getOrderId().equals(detail.getOrder().getOrderId())) {
						throw new RuntimeException("订单明细必须归属同一张订单！");
					}
					String detailCuid = CUIDHexGenerator.getInstance().generate("T_ACT_ORDER_DETAIL");
					Map<String, Object> pm = new HashMap<String, Object>();
					pm.put("cuid", detailCuid);
					pm.put("relatedSheetCuid", detail.getRelatedSheetCuid());
					pm.put("relatedOrderCuid", order.getOrderId());
					pm.put("sortNo", detail.getSortNo());
					pm.put("relatedTraphCuid", detail.getRelatedServiceCuid());
					pm.put("attempType", detail.getAttempType());
					if(SheetConstants.DESIGN_TYPE_TRAPH.equals(detail.getDesignType())) {
						traphCuidList.add(detail.getRelatedServiceCuid());
						this.IbatisResDAO.getSqlMapClientTemplate().insert(sqlMap+".insertOrderDetailByTraph", pm);
					}else if(SheetConstants.DESIGN_TYPE_OPTIC.equals(detail.getDesignType())) {
						this.IbatisResDAO.getSqlMapClientTemplate().insert(sqlMap+".insertOrderDetailByOpticalWay", pm);
					}else if(SheetConstants.DESIGN_TYPE_PON.equals(detail.getDesignType())) {
						this.IbatisResDAO.getSqlMapClientTemplate().insert(sqlMap+".insertOrderDetailByPonWay", pm);
					}else {
						
					}
					
					detail.setCuid(detailCuid);
				}
			}
		}
		if(!traphCuidList.isEmpty()){
			ResTraphBO.updateServiceSchduleState(traphCuidList,SheetConstants.SCHEDULE_STATE_RUN);
		}
	}
	/**
	 * 创建订单明细(外部接口）
	 * @param ac
	 * @param detailList
	 */
	public void createOrderDetailIface(ServiceActionContext ac, List<IOrderDetail> detailList){
		if(detailList == null || detailList.isEmpty())throw new RuntimeException("订单明细为空！");
//		IOrderDetail orderDetail = detailList.get(0);
//		Map<String, Object> detailMap = orderDetail.getData();
//		//设计类型为空时，默认电路类型
//		String desType = orderDetail.getDesignType();
//		if(StringUtils.isBlank(desType)) {
//			desType = SheetConstants.DESIGN_TYPE_TRAPH;
//		}
//		String fromCode = IbatisDAOHelper.getStringValue(detailMap,"FROMCODE");
//		List<String> list=new ArrayList<String>();
//		String BusiSiteA = IbatisDAOHelper.getStringValue(detailMap, "A_SWITCH_SITE_NAME");
//		String BusiSiteZ = IbatisDAOHelper.getStringValue(detailMap, "Z_SWITCH_SITE_NAME");
//		String SiteA = IbatisDAOHelper.getStringValue(detailMap, "A_SITE_NAME");
//		String SiteZ = IbatisDAOHelper.getStringValue(detailMap, "Z_SITE_NAME");
//		String aRelatedSite = SiteA;
//		if(StringUtils.isBlank(aRelatedSite))aRelatedSite = BusiSiteA;
//		String zRelatedSite = SiteZ;
//		if(StringUtils.isBlank(zRelatedSite))zRelatedSite = BusiSiteZ;
//		list.add(aRelatedSite);
//		list.add(zRelatedSite);
//		Map<String,Object> params = new HashMap<String,Object>();
//		params.put("nameList", list);
//		List<Map<String, Object>> districtCuidsList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".getDistrictCuid", params);
//		Map<String,Map<String, Object>> districtCuidsMap = IbatisDAOHelper.parseList2Map(districtCuidsList, "LABEL_CN");
//		String subADistrictCuid="";
//		String subZDistrictCuid="";
//		Map<String, Object> aDistrictCuidMap = districtCuidsMap.get(aRelatedSite);
//		Map<String, Object> zDistrictCuidMap = districtCuidsMap.get(zRelatedSite);
//		String aDistrictCuid = IbatisDAOHelper.getStringValue(aDistrictCuidMap, "DISTRICT_CUID");
//		String zDistrictCuid = IbatisDAOHelper.getStringValue(zDistrictCuidMap, "DISTRICT_CUID");
//		if(StringUtils.isNotEmpty(aDistrictCuid)&&StringUtils.isNotEmpty(zDistrictCuid)){
//			if(aDistrictCuid.length()>26){
//				subADistrictCuid = aDistrictCuid.substring(0,26);
//			}else{
//				subADistrictCuid = aDistrictCuid;
//			}
//			if (zDistrictCuid.length()>26){
//				subZDistrictCuid = zDistrictCuid.substring(0,26);
//			}else{
//				subZDistrictCuid = zDistrictCuid;
//			}
//		}
//		String origNeA = IbatisDAOHelper.getStringValue(detailMap, "ORIG_NE_A");   //存放客户侧网元
//		String origNeZ = IbatisDAOHelper.getStringValue(detailMap, "DEST_NE_Z");
//		if(Property.IRMS.equalsIgnoreCase(fromCode) && desType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH) 
//				&& SysProperty.getInstance().getValue("districtName").trim().equals("广西") && !subADistrictCuid.equals(subZDistrictCuid)
//				&&StringUtils.isNotEmpty(origNeA) && StringUtils.isNotEmpty(origNeZ)){
//			createExtOrderDetailIface(ac, detailList);
//		}else{
			IOrder order = detailList.get(0).getOrder();
			Date now = new Date();
			List<String> outsideKeyIdList = new ArrayList<String>();
			Set<String> traphNameSet = new HashSet<String>();
			Set<String> rateValueSet = new HashSet<String>();
			Set<String> rateNumSet = new HashSet<String>();
			List<Record> recordList = new ArrayList<Record>();
			String islte = "0";
			String orderId = order.getOrderId();
			String districtCuid = order.getRelatedDistrictCuid();
			Set<String> siteSet = new HashSet<String>();
			String designType = "";
			String[] exts = null;
			Date finishDate = order.getFinishDate();
			for(IOrderDetail detail : detailList){
				Map<String, Object> map = detail.getData();
				String aBusiSite = IbatisDAOHelper.getStringValue(map, "A_SWITCH_SITE_NAME");
				String zBusiSite = IbatisDAOHelper.getStringValue(map, "Z_SWITCH_SITE_NAME");
				String aSite = IbatisDAOHelper.getStringValue(map, "A_SITE_NAME");
				String zSite = IbatisDAOHelper.getStringValue(map, "Z_SITE_NAME");
				String traphRate = IbatisDAOHelper.getStringValue(map, "TRAPH_RATE");
				String relatedSiteA = aSite;
				if(StringUtils.isBlank(relatedSiteA))relatedSiteA = aBusiSite;
				String relatedSiteZ = zSite;
				if(StringUtils.isBlank(relatedSiteZ))relatedSiteZ = zBusiSite;
				if(StringUtils.isNotBlank(relatedSiteA))siteSet.add(relatedSiteA);
				if(StringUtils.isNotBlank(relatedSiteZ))siteSet.add(relatedSiteZ);
				//设计类型为空时，默认电路类型
				designType = detail.getDesignType();
				if(StringUtils.isBlank(designType)) {
					designType = SheetConstants.DESIGN_TYPE_TRAPH;
				}
				map.put("DESIGN_TYPE", designType);
				String relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_NAME");
				if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
					relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_OPTICAL_NAME");
				}
				if(StringUtils.isNotBlank(relatedTraphName)){
					traphNameSet.add(relatedTraphName);
				}
				if(StringUtils.isNotBlank(traphRate)){
					//判断是否是数字
					if(StringUtils.isNumeric(traphRate)){
						if(!rateNumSet.contains(traphRate)){
							rateNumSet.add(traphRate);
						}
					}else{
						if(!rateValueSet.contains(traphRate)){
							rateValueSet.add(traphRate);
						}
					}
				}
			}
			
			Map<String,Object> mp = new HashMap<String,Object>();
			mp.put("traphNameList", traphNameSet.toArray());
			List<Map<String,Object>> existsTraphNameList = null;
			//翻译所有的名称
			if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH)){
				existsTraphNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryTraphName", mp);
			}else if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
				existsTraphNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryOpticalWayName", mp);
			}
			for(Map<String,Object> map : existsTraphNameList){
				if(StringUtils.isEmpty(IbatisDAOHelper.getStringValue(map, "A_POINT_NAME"))||StringUtils.isEmpty(IbatisDAOHelper.getStringValue(map, "Z_POINT_NAME"))){
					throw new RuntimeException("AZ端站点数据有误！");
				}
				siteSet.add(IbatisDAOHelper.getStringValue(map, "A_POINT_NAME"));
				siteSet.add(IbatisDAOHelper.getStringValue(map, "Z_POINT_NAME"));
			}
			Map<String,Map<String,Object>> labelCnMap = IbatisDAOHelper.parseList2Map(existsTraphNameList, "LABEL_CN");
			mp.put("siteSet", siteSet.toArray());
			//翻译站点名称
			List<Map<String,Object>> relatedNameList = IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryLabelCnByName", mp);
			Map<String,Map<String,Object>> relatedNameMap = IbatisDAOHelper.parseList2Map(relatedNameList, "LABEL_CN");
			
			Map<String,Map<String,Object>> relatedNameByAliasMap = IbatisDAOHelper.parseList2Map(relatedNameList, "ALIAS");
			Map<String,Map<String,Object>> ratesValueMap = new HashMap<String,Map<String,Object>>();
			if(!rateValueSet.isEmpty()){
				mp.clear();
				mp.put("rateValueList", rateValueSet.toArray());
				List<Map<String,Object>> rateList = IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryRateByName",mp);
				ratesValueMap = IbatisDAOHelper.parseList2Map(rateList, "KEY_VALUE");
			}
			Map<String,Map<String,Object>> ratesNumMap = new HashMap<String,Map<String,Object>>();
			if(!rateNumSet.isEmpty()){
				mp.clear();
				mp.put("rateNumList", rateNumSet.toArray());
				List<Map<String,Object>> rateList = IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryRateByName",mp);
				ratesNumMap = IbatisDAOHelper.parseList2Map(rateList, "KEY_NUM");
			}
			for(IOrderDetail detail : detailList){
				Map<String, Object> map = detail.getData();
				int attempType = IbatisDAOHelper.getIntValue(map, "ATTEMP_TYPE"); 
				islte = IbatisDAOHelper.getStringValue(map, "LTE");
				if(IbatisDAOHelper.getStringValue(map, "BUSINESSTYPE")!=null){
					exts = StringUtils.split(IbatisDAOHelper.getStringValue(map, "BUSINESSTYPE"), ",");
				}
				String addASiteCuid=(String) map.get("RELATED_SITE_A_CUID");
				String addZSiteCuid=(String) map.get("RELATED_SITE_Z_CUID");
				String addSiteAname=(String) map.get("RELATED_SITE_A");
				String addSiteZname=(String) map.get("RELATED_SITE_Z");
				String endSwitchDevA = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_A");
				String endSwitchDevZ = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_Z");
				String endSwitchDevPortA = IbatisDAOHelper.getStringValue(map, "END_SWITCHDEV_PORT_A");
				String endSwitchDevPortZ = IbatisDAOHelper.getStringValue(map, "END_SWITCHDEV_PORT_Z");
				String relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_NAME");
				if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
					relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_OPTICAL_NAME");
				}
				String aBusiSite = IbatisDAOHelper.getStringValue(map, "A_SWITCH_SITE_NAME");
				String zBusiSite = IbatisDAOHelper.getStringValue(map, "Z_SWITCH_SITE_NAME");
				String aSite = IbatisDAOHelper.getStringValue(map, "A_SITE_NAME");
				String zSite = IbatisDAOHelper.getStringValue(map, "Z_SITE_NAME");
				
				//组网方式，细化方式
				String aApMode = IbatisDAOHelper.getStringValue(map, "A_AP_MODE");
				String zApMode = IbatisDAOHelper.getStringValue(map, "Z_AP_MODE");
				String aSubApMode = IbatisDAOHelper.getStringValue(map, "A_SUB_AP_MODE");
				String zSubApMode = IbatisDAOHelper.getStringValue(map, "Z_SUB_AP_MODE");
				
				if(StringUtils.isNotEmpty(aApMode)){
					aApMode = this.getApMode(aApMode);
				}
				if(StringUtils.isNotEmpty(zApMode)){
					zApMode = this.getApMode(zApMode);
				}
				if(StringUtils.isNotEmpty(aSubApMode)){
					aSubApMode = this.getSubApMode(aSubApMode);
				}
				if(StringUtils.isNotEmpty(zSubApMode)){
					zSubApMode = this.getSubApMode(zSubApMode);
				}
				
				
				//订单明细站点默认取传输站点
				String relatedSiteA = aSite;
				if(StringUtils.isBlank(relatedSiteA))relatedSiteA = aBusiSite;
				String relatedSiteZ = zSite;
				if(StringUtils.isBlank(relatedSiteZ))relatedSiteZ = zBusiSite;
				String cuid = CUIDHexGenerator.getInstance().generate("T_IFACE_TRAPH");
				String outsideKeyId = IbatisDAOHelper.getStringValue(map, "OUTSIDE_KEY_ID");
				String portTypeA = IbatisDAOHelper.getStringValue(map, "PORT_TYPE_A");
				String portTypeZ = IbatisDAOHelper.getStringValue(map, "PORT_TYPE_Z");
				String traphRate = IbatisDAOHelper.getStringValue(map, "TRAPH_RATE");
				//PTN调度特有
				String cir = IbatisDAOHelper.getStringValue(map, "CIR");
				String pir = IbatisDAOHelper.getStringValue(map, "PIR");
				String cvlan = IbatisDAOHelper.getStringValue(map, "CVLAN");
				String qos = IbatisDAOHelper.getStringValue(map, "QOS");
				String aDfPort=IbatisDAOHelper.getStringValue(map, "ASWITCHDEVPORTDDF");
				String zDfPort=IbatisDAOHelper.getStringValue(map, "ZSWITCHDEVPORTDDF");
				
				String endSwitchDevACuid = null;
				String endSwitchDevZCuid = null;
				String endSwitchDevPortACuid = null;
				String endSwitchDevPortZCuid = null;
				if(SysProperty.getInstance().getValue("districtName").trim().equals("广西")){
					List<String> nameList = new ArrayList<String>();
					nameList.add(endSwitchDevA);
					nameList.add(endSwitchDevZ);
					nameList.add(endSwitchDevPortA);
					nameList.add(endSwitchDevPortZ);
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("nameList", nameList);
					List<Map<String, Object>> endSwitchList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".getEndSwitchCuid", param);
					for(Map<String, Object> endSwitchMap : endSwitchList){
						String labelCn = IbatisDAOHelper.getStringValue(endSwitchMap, "LABEL_CN");
						if(StringUtils.isNotEmpty(labelCn) && labelCn.equals(endSwitchDevA)){
							endSwitchDevACuid = IbatisDAOHelper.getStringValue(endSwitchMap, "CUID");
						}
						if(StringUtils.isNotEmpty(labelCn) && labelCn.equals(endSwitchDevZ)){
							endSwitchDevZCuid = IbatisDAOHelper.getStringValue(endSwitchMap, "CUID");
						}
						if(StringUtils.isNotEmpty(labelCn) && labelCn.equals(endSwitchDevPortA)){
							endSwitchDevPortACuid = IbatisDAOHelper.getStringValue(endSwitchMap, "CUID");
						}
						if(StringUtils.isNotEmpty(labelCn) && labelCn.equals(endSwitchDevPortZ)){
							endSwitchDevPortZCuid = IbatisDAOHelper.getStringValue(endSwitchMap, "CUID");
						}
					}
				}
				
				if(StringUtils.isEmpty(traphRate)||SysProperty.getInstance().getValue("districtName").trim().equals("内蒙")){
					traphRate = "1";
				}else{
					if(StringUtils.isNumeric(traphRate)){
						Map<String,Object> rateNumMap = ratesNumMap.get(traphRate);
						if(rateNumMap==null){
							rateNumMap = new HashMap<String,Object>();
						}
						traphRate = IbatisDAOHelper.getStringValue(rateNumMap, "KEY_NUM");
						if(StringUtils.isEmpty(traphRate)){
							traphRate = "1";
						}
					}else{
						Map<String,Object> rateValueMap = ratesValueMap.get(traphRate);
						if(rateValueMap==null){
							rateValueMap = new HashMap<String,Object>();
						}
						traphRate = IbatisDAOHelper.getStringValue(rateValueMap, "KEY_NUM");
						String rateValue = IbatisDAOHelper.getStringValue(rateValueMap, "KEY_VALUE");
						if(StringUtils.isEmpty(traphRate)){
							traphRate = "1";
						}
						//如果是10M或者100M转换成FE
						if("10M".equalsIgnoreCase(rateValue)||"100M".equalsIgnoreCase(rateValue)){
							traphRate="34";
						}
					}
				}
				//默认状态为RUN
				String state = "RUN";
				String bandWidth="";
				String extIds="";
				if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH)){
					//带宽为空时，默认2
					bandWidth = IbatisDAOHelper.getStringValue(map, "BAND_WIDTH");
					if(StringUtils.isBlank(bandWidth)) {
						if(StringUtils.isNotBlank(traphRate)&&!StringUtils.isNumeric(traphRate)){
							bandWidth = traphRate;
						}else{
							bandWidth = "2";
						}
					}
					//电路业务类型为空时，默认语音电路
					extIds = IbatisDAOHelper.getStringValue(map, "EXT_IDS");
//					if(StringUtils.isNotEmpty(extIds)){
//						if(("0".equals(extIds) && !"0".equals(islte))||(!"0".equals(extIds) && "0".equals(islte))){
//							 throw new RuntimeException("业务类型冲突");
//						}
//					}
					if(StringUtils.isBlank(extIds)&&attempType==1) {//新增单是默认
						String isJike = IbatisDAOHelper.getStringValue(map, "IS_JIKE");
						if("1".equals(isJike)){
							extIds = ",5,";
						}else{
							extIds = ",1,";
						}
					}
				}
				if(exts != null &&exts.length>0){
					extIds=",";
					for(int i = 0;i<exts.length;i++){
						
						if(exts[i].equals("LTE")){
							extIds=extIds+"101,";
						}
						if(exts[i].equals("BS_LTE")){
							extIds=extIds+"102,";
						}
						if(exts[i].equals("NM_LTE")){
							extIds=extIds+"103,";
						}
					}

				}
					
				Record record = new Record("T_IFACE_TRAPH");
				record.addColValue("CUID", cuid);
				record.addColValue("OUTSIDE_KEY_ID", outsideKeyId);
				record.addColValue("STATE", state);
				record.addColValue("DESIGN_TYPE", designType);
				record.addColValue("RELATED_APPLYSHEET_CUID", orderId);
				record.addColValue("SORT_NO", map.get("SORT_NO"));
				record.addColValue("BUSINESS_NAME", IbatisDAOHelper.getStringValue(map, "BUSINESS_NAME"));
				record.addColValue("BAND_WIDTH", bandWidth);
				record.addColValue("PORT_TYPE_A", portTypeA);
				record.addColValue("PORT_TYPE_Z", portTypeZ);
				record.addColValue("TRAPH_RATE", traphRate);
				record.addColValue("ATTEMP_TYPE", detail.getAttempType());
				//添加业务站点
				record.addColValue("A_SWITCH_SITE_NAME", aBusiSite);
				record.addColValue("Z_SWITCH_SITE_NAME", zBusiSite);
				record.addColValue("END_SWITCH_ROOM_A", map.get("END_SWITCH_ROOM_A"));
				record.addColValue("END_SWITCH_ROOM_Z", map.get("END_SWITCH_ROOM_Z"));
				record.addColValue("END_SWITCH_DEV_A", endSwitchDevA);
				record.addColValue("END_SWITCH_DEV_Z", endSwitchDevZ);
				record.addColValue("END_SWITCHDEV_PORT_A", endSwitchDevPortA);
				record.addColValue("END_SWITCHDEV_PORT_Z", endSwitchDevPortZ);
				record.addColValue("END_SWITCH_DF_PORT_A", aDfPort);
				record.addColValue("END_SWITCH_DF_PORT_Z", zDfPort);
				record.addColValue("END_SWITCH_DEV_ACUID", endSwitchDevACuid);
				record.addColValue("END_SWITCH_DEV_ZCUID", endSwitchDevZCuid);
				record.addColValue("END_SWITCHDEV_PORT_ACUID", endSwitchDevPortACuid);
				record.addColValue("END_SWITCHDEV_PORT_ZCUID", endSwitchDevPortZCuid);
				record.addColValue("CREATE_TIME", now);
				record.addColValue("LAST_MODIFY_TIME", now);
				record.addColValue("EXT_IDS", extIds);
				record.addColValue("LTE", islte);
				record.addColValue("VLANID", map.get("AVLAN"));
				record.addColValue("ZJDF_A", map.get("ZJDF_A"));
				record.addColValue("ZJDF_Z", map.get("ZJDF_Z"));
				record.addColValue("REQUEST_DATE", finishDate);
				record.addColValue("REMARK", IbatisDAOHelper.getStringValue(map, "REMARK"));
				
				//组网方式，细化方式
				record.addColValue("A_AP_MODE", aApMode);
				record.addColValue("Z_AP_MODE", zApMode);
				record.addColValue("A_SUB_AP_MODE", aSubApMode);
				record.addColValue("Z_SUB_AP_MODE", zSubApMode);

				//光路纤芯数
				if(map.get("OPTICALCOUNT")==null){
					record.addColValue("OPTICAL_NUM",2);
				}else{
					record.addColValue("OPTICAL_NUM", map.get("OPTICALCOUNT"));
				}
				record.addColValue("ORIG_PRENE_NAME", map.get("ORIGPRENENAME"));
				record.addColValue("ORIG_NEXTNE_NAME", map.get("ORIGNEXTNENAME"));
				record.addColValue("DEST_PRENE_NAME", map.get("DESTPRENENAME"));
				record.addColValue("DEST_NEXTNE_NAME", map.get("DESTNEXTNENAME"));
				record.addColValue("RELATED_TRAPH_NAME", detail.getAttempType() != InfoAttempType.ATTEMP_TYPE_ADD?relatedTraphName:null);
				Map<String,Object> nameMap = labelCnMap.get(relatedTraphName);
				if(nameMap==null){
					nameMap = new HashMap<String,Object>();
				}
				record.addColValue("RELATED_TRAPH_CUID", detail.getAttempType() != InfoAttempType.ATTEMP_TYPE_ADD?IbatisDAOHelper.getStringValue(nameMap, "CUID"):null);
				Map<String,Object> aSiteNameMap = relatedNameMap.get(relatedSiteA);
				if(aSiteNameMap==null){
					aSiteNameMap = relatedNameByAliasMap.get(relatedSiteA);
					if(aSiteNameMap==null){
						aSiteNameMap = new HashMap<String,Object>();
					}else {
						relatedSiteA = IbatisDAOHelper.getStringValue(aSiteNameMap, "LABEL_CN");
					}
				}
				String relateASiteName = IbatisDAOHelper.getStringValue(nameMap, "A_POINT_NAME");
				String relateZSiteName = IbatisDAOHelper.getStringValue(nameMap, "Z_POINT_NAME");
				//综资过来的业务站点查询不到将用户的区域赋给RELATED_DISTRICT_A_CUID,RELATED_DISTRICT_Z_CUID
				String dCuid ="";
				if(ac.getRelatedDistrictCuid().length()>26){
					dCuid = ac.getRelatedDistrictCuid().substring(0, 26);
				}else{
					dCuid = ac.getRelatedDistrictCuid();
				}
				String aPointCuid = IbatisDAOHelper.getStringValue(aSiteNameMap, "CUID");
				String aPointType = IbatisDAOHelper.getStringValue(aSiteNameMap, "POINT_TYPE");
				if(SysProperty.getInstance().getValue("districtName").equals("河南")){
					record.addColValue("RELATED_SITE_A_CUID", addASiteCuid);
					record.addColValue("A_POINT_CUID", addASiteCuid);
					if(addASiteCuid!=null){
						record.addColValue("RELATED_SITE_A", addSiteAname);
						record.addColValue("A_POINT_NAME", addSiteAname);
					}else{
						record.addColValue("A_POINT_NAME", "");
					}
				}else{
					record.addColValue("RELATED_SITE_A", relatedSiteA);
					record.addColValue("A_POINT_NAME", relatedSiteA);
					record.addColValue("A_POINT_NAME", StringUtils.isNotBlank(relatedSiteA)?relatedSiteA:relateASiteName);
					record.addColValue("A_POINT_CUID", StringUtils.isNotBlank(relatedSiteA)?aPointCuid:IbatisDAOHelper.getStringValue(nameMap, "SITE_CUID_A"));
					record.addColValue("RELATED_SITE_A_CUID", StringUtils.isNotBlank(relatedSiteA)?aPointCuid:IbatisDAOHelper.getStringValue(nameMap, "SITE_CUID_A"));
				}
			
				Map<String,Object> aPointTypeMap = relatedNameMap.get(relateASiteName);
				if(aPointTypeMap==null){
					aPointTypeMap = relatedNameByAliasMap.get(relateASiteName);
					if(aPointTypeMap==null){
						aPointTypeMap = new HashMap<String,Object>();
					}
				}
				record.addColValue("A_POINT_TYPE", StringUtils.isNotBlank(aPointType)?aPointType:IbatisDAOHelper.getStringValue(aPointTypeMap, "POINT_TYPE"));
				String adistrictCuid = IbatisDAOHelper.getStringValue(aSiteNameMap, "DISTRICT_CUID");
				record.addColValue("RELATED_DISTRICT_A_CUID", StringUtils.isNotBlank(adistrictCuid)?adistrictCuid:dCuid);
				Map<String,Object> zStieNameMap = relatedNameMap.get(relatedSiteZ);
				if(zStieNameMap==null){
					zStieNameMap = relatedNameByAliasMap.get(relatedSiteZ);
					if(zStieNameMap==null){
						zStieNameMap = new HashMap<String,Object>();
					}else {
						relatedSiteZ = IbatisDAOHelper.getStringValue(zStieNameMap, "LABEL_CN");
					}
				}
				String zPointCuid = IbatisDAOHelper.getStringValue(zStieNameMap, "CUID");
				String zPointType = IbatisDAOHelper.getStringValue(zStieNameMap, "POINT_TYPE");
				if(SysProperty.getInstance().getValue("districtName").equals("河南")){
					record.addColValue("RELATED_SITE_Z_CUID", addZSiteCuid);
					record.addColValue("Z_POINT_CUID", addZSiteCuid);
					if(addZSiteCuid!=null){
						record.addColValue("RELATED_SITE_Z", relatedSiteZ);
						record.addColValue("Z_POINT_NAME", addSiteZname);
					}else{
						record.addColValue("Z_POINT_NAME", "");
					}
				}else{
					record.addColValue("RELATED_SITE_Z", relatedSiteZ);
					record.addColValue("Z_POINT_NAME", relatedSiteZ);
					record.addColValue("Z_POINT_NAME", StringUtils.isNotBlank(relatedSiteZ)?relatedSiteZ:relateZSiteName);
					record.addColValue("Z_POINT_CUID", StringUtils.isNotBlank(relatedSiteZ)?zPointCuid:IbatisDAOHelper.getStringValue(nameMap, "SITE_CUID_Z"));
					record.addColValue("RELATED_SITE_Z_CUID", StringUtils.isNotBlank(relatedSiteZ)?zPointCuid:IbatisDAOHelper.getStringValue(nameMap, "SITE_CUID_Z"));
				}
			
				Map<String,Object> zPointTypeMap = relatedNameMap.get(relateZSiteName);
				if(zPointTypeMap==null){
					zPointTypeMap = relatedNameByAliasMap.get(relateZSiteName);
					if(zPointTypeMap==null){
						zPointTypeMap = new HashMap<String,Object>();
					}
				}
				record.addColValue("Z_POINT_TYPE", StringUtils.isNotBlank(zPointType)?zPointType:IbatisDAOHelper.getStringValue(zPointTypeMap, "POINT_TYPE"));
				String zdistrictCuid = IbatisDAOHelper.getStringValue(zStieNameMap, "DISTRICT_CUID");
				record.addColValue("RELATED_DISTRICT_Z_CUID", StringUtils.isNotBlank(zdistrictCuid)?zdistrictCuid:dCuid);
				record.addColValue("CIR",cir);
				record.addColValue("PIR",pir);
				record.addColValue("CVLAN",cvlan);
				record.addColValue("QOS",qos);
				//接口一干信息
				record.addColValue("MAIN_SITE_NAME",IbatisDAOHelper.getStringValue(map, "MAIN_SITE_NAME"));
				record.addColValue("MAIN_TRANS_DEV_PORT",IbatisDAOHelper.getStringValue(map, "MAIN_TRANS_DEV_PORT"));
				record.addColValue("MAIN_TRAPH_ROUTE",IbatisDAOHelper.getStringValue(map, "MAIN_TRAPH_ROUTE"));
				record.addColValue("MAIN_TRAPH_LEVEL",IbatisDAOHelper.getStringValue(map, "MAIN_TRAPH_LEVEL"));
				record.addColValue("MAIN_TRAPH_NAME", IbatisDAOHelper.getStringValue(map, "MAIN_TRAPH_NAME"));
				
				//集客电路，添加服务保障等级、客户等级、用户等信息
				record.addColValue("BIZ_SECURITY_LV",IbatisDAOHelper.getStringValue(map, "BIZ_SECURITY_LV"));
				record.addColValue("CUSTOM_NAME",IbatisDAOHelper.getStringValue(map, "CUSTOM_NAME"));
				String serviceLevel = IbatisDAOHelper.getStringValue(map, "SERVICE_LEVEL");
				if("金".equals(serviceLevel)){
					serviceLevel = "金牌";
				}else if("银".equals(serviceLevel)){
					serviceLevel = "银牌";
				}else if("铜".equals(serviceLevel)){
					serviceLevel = "铜牌";
				}
				record.addColValue("SERVICE_LEVEL",serviceLevel);
				recordList.add(record);
			}
			//插入综资接口明细表
			this.IbatisResDAO.insertDynamicTableBatch(recordList);
			//通过接口明细，插入申请单明细表，主要是为了防止外部系统并发、重复申请。通过这个表的外部电路ID做唯一索引控制入口
			this.IbatisResDAO.getSqlMapClientTemplate().insert(sqlMap+".insertDetailFromIfaceByOrder", orderId);
		}
	/**
	 * 校验订单明细方法
	 * @param ac
	 * @param detailList
	 */
	public void existsOrderDetailIface(ServiceActionContext ac, List<IOrderDetail> detailList){
		if(detailList == null || detailList.isEmpty())throw new RuntimeException("订单明细为空！");
		String errorInfo = "";
		boolean hasError = false;
		List<String> outsideKeyIdList = new ArrayList<String>();
		Set<String> siteNameSet = new HashSet<String>();
		Set<String> traphNameSet = new HashSet<String>();
		for(IOrderDetail detail : detailList){
			Map<String, Object> map = detail.getData();
			String designType = detail.getDesignType();
			if(StringUtils.isBlank(designType)) {
				designType = SheetConstants.DESIGN_TYPE_TRAPH;
			}
			String relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_NAME");
			if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
				relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_OPTICAL_NAME");
			}
			String aBusiSite = IbatisDAOHelper.getStringValue(map, "A_SWITCH_SITE_NAME");
			String zBusiSite = IbatisDAOHelper.getStringValue(map, "Z_SWITCH_SITE_NAME");
			String aSite = IbatisDAOHelper.getStringValue(map, "A_SITE_NAME");
			String zSite = IbatisDAOHelper.getStringValue(map, "Z_SITE_NAME");
			String relatedSiteA = aSite;
			if(StringUtils.isBlank(relatedSiteA))relatedSiteA = aBusiSite;
			siteNameSet.add(relatedSiteA);
			
			String relatedSiteZ = zSite;
			if(StringUtils.isBlank(relatedSiteZ))relatedSiteZ = zBusiSite;
			siteNameSet.add(relatedSiteZ);
			
			String outsideKeyId = IbatisDAOHelper.getStringValue(map, "OUTSIDE_KEY_ID");
			outsideKeyIdList.add(outsideKeyId);
			String endSwitchDevA = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_A");
			String endSwitchDevZ = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_Z");
			//工单类型非新增时，校验调整前电路是否为空opticalCount
			if(detail.getAttempType() != InfoAttempType.ATTEMP_TYPE_ADD) {
				if(StringUtils.isBlank(relatedTraphName)) {
					hasError = true;
					errorInfo = endSwitchDevA + "-" + endSwitchDevZ + "的调整前电路不允许为空！";
					break;
				} else {
					traphNameSet.add(relatedTraphName);
				}
			}else{
				//校验业务站点和传输站点是否同时为空
				if (StringUtils.isBlank(aSite) && StringUtils.isBlank(aBusiSite)) {
					hasError = true;
					errorInfo = endSwitchDevA + "-" + endSwitchDevZ + "A端的业务站点和传输站点不允许同时为空！";
					break;
				}
				if (StringUtils.isBlank(zSite) && StringUtils.isBlank(zBusiSite)) {
					hasError = true;
					errorInfo = endSwitchDevA + "-" + endSwitchDevZ + "Z端的业务站点和传输站点不允许同时为空！";
					break;
				}
			}
			if(hasError) {
				logger.error(errorInfo);
				throw new RuntimeException(errorInfo);
			}
		}
		IOrder order = detailList.get(0).getOrder();
		String districtCuid = order.getRelatedDistrictCuid();
		Map<String, Object> pm = new HashMap<String, Object>();
//		if(districtCuid.substring(0, 20).equalsIgnoreCase("DISTRICT-00001-00009")){
//			if(siteNameSet != null && siteNameSet.size() > 0) {
//				pm.clear();
//				pm.put("siteNameList", siteNameSet.toArray());
//				logger.debug("校验站点是否存在");
//				List<String> existsSiteNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".querySiteName", pm);
//				if(existsSiteNameList != null && existsSiteNameList.size() > 0) {
//					List<String> notExistsSiteNameList = new ArrayList<String>();
//					notExistsSiteNameList.addAll(siteNameSet);
//					notExistsSiteNameList.removeAll(existsSiteNameList);
//					if(notExistsSiteNameList != null && notExistsSiteNameList.size() > 0) {
//						errorInfo = StringUtils.join(notExistsSiteNameList, ",\n") + "\n站点在传输不存在！";
//						logger.error(errorInfo);
//						throw new RuntimeException(errorInfo);
//					}
//				} else {
//					errorInfo = StringUtils.join(siteNameSet, ",\n") + "\n站点在传输不存在！";
//					logger.error(errorInfo);
//					throw new RuntimeException(errorInfo);
//				}
//			}
//		}
//		//校验调整前电路是否存在
		if(traphNameSet != null && traphNameSet.size() > 0) {
			pm.clear();
			pm.put("traphNameList", traphNameSet.toArray());
			logger.debug("校验调整前电路是否存在");
			List<Map<String,Object>> existsTraphNameList = null;
			String designType = detailList.get(0).getDesignType();
			String str = "电路";
			if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH)){
				existsTraphNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryTraphName", pm);
			}else if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
				existsTraphNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryOpticalWayName", pm);
				str = "光路";
			}
			List<String> traphNameList = new ArrayList<String>();
			for(Map<String,Object> map : existsTraphNameList){
				String labelCn = IbatisDAOHelper.getStringValue(map, "LABEL_CN");
				traphNameList.add(labelCn);
			}
			if(existsTraphNameList != null && existsTraphNameList.size() > 0) {
				List<String> notExistsTraphNameList = new ArrayList<String>();
				notExistsTraphNameList.addAll(traphNameSet);
				notExistsTraphNameList.removeAll(traphNameList);
				if(notExistsTraphNameList != null && notExistsTraphNameList.size() > 0) {
					errorInfo = StringUtils.join(notExistsTraphNameList, ",\n") + "\n"+str+"在传输不存在！";
					logger.error(errorInfo);
					throw new RuntimeException(errorInfo);
				}
			} else {
				errorInfo = StringUtils.join(traphNameSet, ",\n") + "\n"+str+"在传输不存在！";
				logger.error(errorInfo);
				throw new RuntimeException(errorInfo);
			}
		}
	}
	/**
	 * 新的校验调单明细的方法
	 * 		--返回所有明细校验不通过原因
	 * @param ac
	 * @param detailList
	 * @return  Map<String,List<String>>
	 *      -- key ：outSideKeyId
	 *      -- value: 错误原因 明细
	 */ 
	public Map<String,List<String>> validateOrderDetailIface(ServiceActionContext ac, List<IOrderDetail> detailList){
		Map<String,List<String>> errorDetail = new HashMap<String, List<String>>();
		
		Map<String,List<String>> siteDetailMap = new HashMap<String, List<String>>(); 
		Map<String,String> befDetailMap = new HashMap<String, String>(); 
		for(IOrderDetail detail : detailList){
			List<String> errorList = new ArrayList<String>();
			Map<String, Object> map = detail.getData();
			String outsideKeyId = IbatisDAOHelper.getStringValue(map, "OUTSIDE_KEY_ID");
			
			String designType = detail.getDesignType();
			String relatedServiceName = IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_NAME");
			if(SheetConstants.DESIGN_TYPE_OPTIC.equalsIgnoreCase(designType)){
				relatedServiceName = IbatisDAOHelper.getStringValue(map, "RELATED_OPTICAL_NAME");
			}
			String aBusiSite = IbatisDAOHelper.getStringValue(map, "A_SWITCH_SITE_NAME");
			String zBusiSite = IbatisDAOHelper.getStringValue(map, "Z_SWITCH_SITE_NAME");
			String aSite = IbatisDAOHelper.getStringValue(map, "A_SITE_NAME");
			String zSite = IbatisDAOHelper.getStringValue(map, "Z_SITE_NAME");
			String relatedSiteA = aSite;
			if(StringUtils.isBlank(relatedSiteA))relatedSiteA = aBusiSite;
			
			String relatedSiteZ = zSite;
			if(StringUtils.isBlank(relatedSiteZ))relatedSiteZ = zBusiSite;
			
			String endSwitchDevA = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_A");
			String endSwitchDevZ = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_Z");
			//工单类型非新增时，校验调整前电路是否为空opticalCount
			if(detail.getAttempType() != InfoAttempType.ATTEMP_TYPE_ADD) {
				if(StringUtils.isBlank(relatedServiceName)) {
					errorList.add(endSwitchDevA + "-" + endSwitchDevZ + "的调整前电路不允许为空！");
				} else {
					befDetailMap.put(relatedServiceName,outsideKeyId);
				}
			}else{
				//校验业务站点和传输站点是否同时为空
				if (StringUtils.isBlank(aSite) && StringUtils.isBlank(aBusiSite)) {
					errorList.add(endSwitchDevA + "-" + endSwitchDevZ + "A端的业务站点和传输站点不允许同时为空！");
				}
				if (StringUtils.isBlank(zSite) && StringUtils.isBlank(zBusiSite)) {
					errorList.add(endSwitchDevA + "-" + endSwitchDevZ + "Z端的业务站点和传输站点不允许同时为空！");
				}
			}
			if(!errorList.isEmpty()){
				errorDetail.put(outsideKeyId, errorList);
			}else{
				if(StringUtils.isNotBlank(relatedSiteA)){
					if(!siteDetailMap.containsKey(relatedSiteA)){
						siteDetailMap.put(relatedSiteA, new ArrayList<String>());
					}
					siteDetailMap.get(relatedSiteA).add(outsideKeyId);
				}
				if(StringUtils.isNotBlank(relatedSiteZ)){
					if(!relatedSiteZ.equals(relatedSiteA)){
						if(!siteDetailMap.containsKey(relatedSiteZ)){
							siteDetailMap.put(relatedSiteZ, new ArrayList<String>());
						}
						siteDetailMap.get(relatedSiteZ).add(outsideKeyId);
					}
				}
			}
		}
		IOrder order = detailList.get(0).getOrder();
		String districtCuid = order.getRelatedDistrictCuid();
		Map<String, Object> pm = new HashMap<String, Object>();
		if("DISTRICT-00001-00009".equalsIgnoreCase(districtCuid.substring(0, 20))){
			if(!siteDetailMap.keySet().isEmpty()) {
				pm.clear();
				pm.put("siteNameList", siteDetailMap.keySet().toArray());
				logger.debug("校验站点是否存在");
				List<String> existsSiteNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".querySiteName", pm);
				
				List<String> notExistsSiteNameList = new ArrayList<String>();
				notExistsSiteNameList.addAll(siteDetailMap.keySet());
				notExistsSiteNameList.removeAll(existsSiteNameList);
				for(String siteName: notExistsSiteNameList){
					for(String osid:siteDetailMap.get(siteName)){
						if(!errorDetail.containsKey(osid)){
							errorDetail.put(osid, new ArrayList<String>());
						}
						errorDetail.get(osid).add(siteName+",站点在传输不存在！");
					}
				}
			}
		}
		if(!befDetailMap.keySet().isEmpty()){
			pm.clear();
			pm.put("traphNameList", befDetailMap.keySet().toArray());
			logger.debug("校验调整前电路是否存在");
			List<Map<String,Object>> existsServiceNameList = null;
			String designType = detailList.get(0).getDesignType();
			if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH)){
				existsServiceNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryTraphName", pm);
			}else if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
				existsServiceNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryOpticalWayName", pm);
			}
			List<String> serviceNameList = new ArrayList<String>();
			for(Map<String,Object> map : existsServiceNameList){
				String labelCn = IbatisDAOHelper.getStringValue(map, "LABEL_CN");
				serviceNameList.add(labelCn);
			}
			List<String> notExistsServiceNameList = new ArrayList<String>();
			notExistsServiceNameList.addAll(befDetailMap.keySet());
			notExistsServiceNameList.removeAll(serviceNameList);
			for(String serviceName: notExistsServiceNameList){
				String osid = befDetailMap.get(serviceName);
				if(!errorDetail.containsKey(osid)){
					errorDetail.put(osid, new ArrayList<String>());
				}
				errorDetail.get(osid).add("调整前电路:"+serviceName+",不存在！");
			}
		}
		return errorDetail;
	}  
	
	/**
	 * 创建订单明细(外部接口）
	 * @param ac
	 * @param detailList
	 */
//	public void createOrderDetailIface(ServiceActionContext ac, List<IOrderDetail> detailList) {
//		if(detailList == null || detailList.isEmpty())throw new RuntimeException("订单明细为空！");
//		IOrder order = detailList.get(0).getOrder();
//		Date now = new Date();
//		String errorInfo = "";
//		boolean hasError = false;
//		List<String> outsideKeyIdList = new ArrayList<String>();
//		Set<String> traphNameSet = new HashSet<String>();
//		Set<String> siteNameSet = new HashSet<String>();
//		List<Record> recordList = new ArrayList<Record>();
//		
//		String orderId = order.getOrderId();
//		String districtCuid = order.getRelatedDistrictCuid();
//		Date finishDate = order.getFinishDate();
//		Set<String> siteSet = new HashSet<String>();
//		for(IOrderDetail detail : detailList){
//			Map<String, Object> map = detail.getData();
//			String aBusiSite = IbatisDAOHelper.getStringValue(map, "A_SWITCH_SITE_NAME");
//			String zBusiSite = IbatisDAOHelper.getStringValue(map, "Z_SWITCH_SITE_NAME");
//			String aSite = IbatisDAOHelper.getStringValue(map, "A_SITE_NAME");
//			String zSite = IbatisDAOHelper.getStringValue(map, "Z_SITE_NAME");
//			String relatedSiteA = aSite;
//			if(StringUtils.isBlank(relatedSiteA))relatedSiteA = aBusiSite;
//			String relatedSiteZ = zSite;
//			if(StringUtils.isBlank(relatedSiteZ))relatedSiteZ = zBusiSite;
//			siteSet.add(relatedSiteA);
//			siteSet.add(relatedSiteZ);
//			//设计类型为空时，默认电路类型
//			String designType = detail.getDesignType();
//			if(StringUtils.isBlank(designType)) {
//				designType = SheetConstants.DESIGN_TYPE_TRAPH;
//				map.put("DESIGN_TYPE", designType);
//			}
//			String relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_NAME");
//			if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
//				relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_OPTICAL_NAME");
//			}
//		}
//		Map<String,Object> mp = new HashMap<String,Object>();
//		mp.put("siteSet", siteSet.toArray());
//		List<Map<String,Object>> relatedNameList = IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryLabelCnByName", mp);
//		Map<String,Map<String,Object>> relatedNameMap = IbatisDAOHelper.parseList2Map(relatedNameList, "LABEL_CN");
//		for(IOrderDetail detail : detailList) {
//			Map<String, Object> map = detail.getData();
//			String endSwitchDevA = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_A");
//			String endSwitchDevZ = IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_Z");
//			//设计类型为空时，默认电路类型
//			String designType = detail.getDesignType();
//			if(StringUtils.isBlank(designType)) {
//				designType = SheetConstants.DESIGN_TYPE_TRAPH;
//				map.put("DESIGN_TYPE", designType);
//			}
//			String relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_NAME");
//			if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
//				relatedTraphName = IbatisDAOHelper.getStringValue(map, "RELATED_OPTICAL_NAME");
//			}
//			
//			//工单类型非新增时，校验调整前电路是否为空opticalCount
//			if(detail.getAttempType() != InfoAttempType.ATTEMP_TYPE_ADD) {
//				if(StringUtils.isBlank(relatedTraphName)) {
//					hasError = true;
//					errorInfo = endSwitchDevA + "-" + endSwitchDevZ + "的调整前电路不允许为空！";
//					break;
//				} else {
//					traphNameSet.add(relatedTraphName);
//				}
//			}
//			
//			
//			String aBusiSite = IbatisDAOHelper.getStringValue(map, "A_SWITCH_SITE_NAME");
//			String zBusiSite = IbatisDAOHelper.getStringValue(map, "Z_SWITCH_SITE_NAME");
//			String aSite = IbatisDAOHelper.getStringValue(map, "A_SITE_NAME");
//			String zSite = IbatisDAOHelper.getStringValue(map, "Z_SITE_NAME");
////			if(detail.getAttempType() != InfoAttempType.ATTEMP_TYPE_DEL){
////				//校验业务站点和传输站点是否同时为空
////				if (StringUtils.isBlank(aSite) && StringUtils.isBlank(aBusiSite)) {
////					hasError = true;
////					errorInfo = endSwitchDevA + "-" + endSwitchDevZ + "A端的业务站点和传输站点不允许同时为空！";
////					break;
////				}
////				if (StringUtils.isBlank(zSite) && StringUtils.isBlank(zBusiSite)) {
////					hasError = true;
////					errorInfo = endSwitchDevA + "-" + endSwitchDevZ + "Z端的业务站点和传输站点不允许同时为空！";
////					break;
////				}
////			}
//			//订单明细站点默认取传输站点
//			String relatedSiteA = aSite;
//			if(StringUtils.isBlank(relatedSiteA))relatedSiteA = aBusiSite;
//			siteNameSet.add(relatedSiteA);
//			
//			String relatedSiteZ = zSite;
//			if(StringUtils.isBlank(relatedSiteZ))relatedSiteZ = zBusiSite;
//			siteNameSet.add(relatedSiteZ);
//			
//			String cuid = CUIDHexGenerator.getInstance().generate("T_IFACE_TRAPH");
//			String outsideKeyId = IbatisDAOHelper.getStringValue(map, "OUTSIDE_KEY_ID");
//			outsideKeyIdList.add(outsideKeyId);
//			
//			String portTypeA = IbatisDAOHelper.getStringValue(map, "PORT_TYPE_A");
//			String portTypeZ = IbatisDAOHelper.getStringValue(map, "PORT_TYPE_Z");
//			int rate = Integer.parseInt(PortType2RateMapping.getMappingByKey(portTypeA+","+portTypeZ));
//			//默认状态为RUN
//			String state = "RUN";
//			String bandWidth="";
//			String extIds="";
//			if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH)){
//				//带宽为空时，默认2
//				bandWidth = IbatisDAOHelper.getStringValue(map, "BAND_WIDTH");
//				if(StringUtils.isBlank(bandWidth)) {
//					bandWidth = "2";
//				}
//				//电路业务类型为空时，默认语音电路
//				extIds = IbatisDAOHelper.getStringValue(map, "EXT_IDS");
//				if(StringUtils.isBlank(extIds)) {
//					extIds = ",1,";
//				}
//			}
//			Record record = new Record("T_IFACE_TRAPH");
//			record.addColValue("CUID", cuid);
//			record.addColValue("OUTSIDE_KEY_ID", outsideKeyId);
//			record.addColValue("STATE", state);
//			record.addColValue("DESIGN_TYPE", designType);
//			record.addColValue("RELATED_APPLYSHEET_CUID", orderId);
//			record.addColValue("SORT_NO", map.get("SORT_NO"));
//			record.addColValue("BUSINESS_NAME", IbatisDAOHelper.getStringValue(map, "BUSINESS_NAME"));
//			record.addColValue("BAND_WIDTH", bandWidth);
//			record.addColValue("PORT_TYPE_A", portTypeA);
//			record.addColValue("PORT_TYPE_Z", portTypeZ);
//			record.addColValue("TRAPH_RATE", rate);
//			record.addColValue("ATTEMP_TYPE", detail.getAttempType());
//			record.addColValue("RELATED_SITE_A", relatedSiteA);
//			record.addColValue("RELATED_SITE_Z", relatedSiteZ);
//			record.addColValue("END_SWITCH_ROOM_A", map.get("END_SWITCH_ROOM_A"));
//			record.addColValue("END_SWITCH_ROOM_Z", map.get("END_SWITCH_ROOM_Z"));
//			record.addColValue("END_SWITCH_DEV_A", endSwitchDevA);
//			record.addColValue("END_SWITCH_DEV_Z", endSwitchDevZ);
//			record.addColValue("END_SWITCHDEV_PORT_A", map.get("END_SWITCHDEV_PORT_A"));
//			record.addColValue("END_SWITCHDEV_PORT_Z", map.get("END_SWITCHDEV_PORT_Z"));
//			record.addColValue("END_SWITCH_DF_PORT_A", map.get("END_SWITCH_DF_PORT_A"));
//			record.addColValue("END_SWITCH_DF_PORT_Z", map.get("END_SWITCH_DF_PORT_Z"));
//			record.addColValue("CREATE_TIME", now);
//			record.addColValue("LAST_MODIFY_TIME", now);
//			record.addColValue("EXT_IDS", extIds);
//			record.addColValue("VLANID", map.get("AVLAN"));
//			record.addColValue("ZJDF_A", map.get("ZJDF_A"));
//			record.addColValue("ZJDF_Z", map.get("ZJDF_Z"));
//			record.addColValue("REQUEST_DATE", finishDate);
//			record.addColValue("REMARK", IbatisDAOHelper.getStringValue(map, "REMARK"));
//			//光路纤芯数
//			record.addColValue("OPTICAL_NUM", map.get("OPTICALCOUNT"));
//			record.addColValue("ORIG_PRENE_NAME", map.get("ORIGPRENENAME"));
//			record.addColValue("ORIG_NEXTNE_NAME", map.get("ORIGNEXTNENAME"));
//			record.addColValue("DEST_PRENE_NAME", map.get("DESTPRENENAME"));
//			record.addColValue("DEST_NEXTNE_NAME", map.get("DESTNEXTNENAME"));
//			record.addColValue("A_POINT_NAME", relatedSiteA);
//			record.addColValue("Z_POINT_NAME", relatedSiteZ);
//			Map<String,Object> aNameMap = relatedNameMap.get(relatedSiteA);
//			//综资过来的业务站点查询不到将用户的区域赋给RELATED_DISTRICT_A_CUID,RELATED_DISTRICT_Z_CUID
//			String dCuid ="";
//			if(ac.getRelatedDistrictCuid().length()>26){
//				dCuid = ac.getRelatedDistrictCuid().substring(0, 26);
//			}else{
//				dCuid = ac.getRelatedDistrictCuid();
//			}
//			String adistrictCuid ="";
//			String zdistrictCuid = "";
//			if(aNameMap!=null){
//				record.addColValue("A_POINT_CUID", IbatisDAOHelper.getStringValue(aNameMap, "CUID"));
//				record.addColValue("RELATED_SITE_A_CUID", IbatisDAOHelper.getStringValue(aNameMap, "CUID"));
//				record.addColValue("A_POINT_TYPE", IbatisDAOHelper.getStringValue(aNameMap, "POINT_TYPE"));
//				adistrictCuid = IbatisDAOHelper.getStringValue(aNameMap, "DISTRICT_CUID");
//			}
//			record.addColValue("RELATED_DISTRICT_A_CUID", StringUtils.isNotBlank(adistrictCuid)?adistrictCuid:dCuid);
//			Map<String,Object> zNameMap = relatedNameMap.get(relatedSiteZ);
//			if(zNameMap!=null){
//				record.addColValue("Z_POINT_CUID", IbatisDAOHelper.getStringValue(zNameMap, "CUID"));
//				record.addColValue("RELATED_SITE_Z_CUID", IbatisDAOHelper.getStringValue(zNameMap, "CUID"));
//				record.addColValue("Z_POINT_TYPE", IbatisDAOHelper.getStringValue(zNameMap, "POINT_TYPE"));
//				zdistrictCuid = IbatisDAOHelper.getStringValue(zNameMap, "DISTRICT_CUID");
//			}
//			record.addColValue("RELATED_DISTRICT_Z_CUID", StringUtils.isNotBlank(zdistrictCuid)?zdistrictCuid:dCuid);
//			if (detail.getAttempType() != InfoAttempType.ATTEMP_TYPE_ADD) {
//				record.addColValue("RELATED_TRAPH_NAME", relatedTraphName);
//				if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH)){
//					record.addColSqlValue("RELATED_TRAPH_CUID", "SELECT T.CUID FROM TRAPH T WHERE TRIM(T.LABEL_CN)='"+relatedTraphName+"'");
//				}else if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
//					record.addColSqlValue("RELATED_TRAPH_CUID", "SELECT T.CUID FROM OPTICAL_WAY T WHERE TRIM(T.LABEL_CN)='"+relatedTraphName+"'");
//				}
//			} 
//			
//			recordList.add(record);
//		}
//		
//		if(hasError) {
//			logger.error(errorInfo);
//			throw new RuntimeException(errorInfo);
//		}
//		if(districtCuid.substring(0, 20).equalsIgnoreCase("DISTRICT-00001-00009")){
//			Map<String, Object> pm = new HashMap<String, Object>();
//			logger.debug("校验订单明细是否已存在");
//			List<Map<String, Object>> orderDetailList = this.findIfaceDetailByOutKeyId(outsideKeyIdList);
//			if(orderDetailList != null && !orderDetailList.isEmpty()) {
//				errorInfo = "此工单已在传输生成，请勿重复提交！";
//				logger.error(errorInfo);
//				throw new RuntimeException(errorInfo);
//			}
//			if(siteNameSet != null && siteNameSet.size() > 0) {
//				pm.clear();
//				pm.put("siteNameList", siteNameSet.toArray());
//				logger.debug("校验站点是否存在");
//				List<String> existsSiteNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".querySiteName", pm);
//				if(existsSiteNameList != null && existsSiteNameList.size() > 0) {
//					List<String> notExistsSiteNameList = new ArrayList<String>();
//					notExistsSiteNameList.addAll(siteNameSet);
//					notExistsSiteNameList.removeAll(existsSiteNameList);
//					if(notExistsSiteNameList != null && notExistsSiteNameList.size() > 0) {
//						errorInfo = StringUtils.join(notExistsSiteNameList, ",\n") + "\n站点在传输不存在！";
//						logger.error(errorInfo);
//						throw new RuntimeException(errorInfo);
//					}
//				} else {
//					errorInfo = StringUtils.join(siteNameSet, ",\n") + "\n站点在传输不存在！";
//					logger.error(errorInfo);
//					throw new RuntimeException(errorInfo);
//				}
//			}
//			//校验调整前电路是否存在
//			if(traphNameSet != null && traphNameSet.size() > 0) {
//				pm.clear();
//				pm.put("traphNameList", traphNameSet.toArray());
//				logger.debug("校验调整前电路是否存在");
//				List<String> existsTraphNameList = null;
//				String designType = detailList.get(0).getDesignType();
//				String str = "电路";
//				if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_TRAPH)){
//					existsTraphNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryTraphName", pm);
//				}else if(designType.equalsIgnoreCase(SheetConstants.DESIGN_TYPE_OPTIC)){
//					existsTraphNameList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryOpticalWayName", pm);
//					str = "光路";
//				}
//				if(existsTraphNameList != null && existsTraphNameList.size() > 0) {
//					List<String> notExistsTraphNameList = new ArrayList<String>();
//					notExistsTraphNameList.addAll(traphNameSet);
//					notExistsTraphNameList.removeAll(existsTraphNameList);
//					if(notExistsTraphNameList != null && notExistsTraphNameList.size() > 0) {
//						errorInfo = StringUtils.join(notExistsTraphNameList, ",\n") + "\n"+str+"在传输不存在！";
//						logger.error(errorInfo);
//						throw new RuntimeException(errorInfo);
//					}
//				} else {
//					errorInfo = StringUtils.join(traphNameSet, ",\n") + "\n"+str+"在传输不存在！";
//					logger.error(errorInfo);
//					throw new RuntimeException(errorInfo);
//				}
//			}
//		}
//		
//		//插入综资接口明细表
//		this.IbatisResDAO.insertDynamicTableBatch(recordList);
//		//通过接口明细，插入申请单明细表，主要是为了防止外部系统并发、重复申请。通过这个表的外部电路ID做唯一索引控制入口
//		this.IbatisResDAO.getSqlMapClientTemplate().insert(sqlMap+".insertDetailFromIfaceByOrder", orderId);
//	}
	
	/**
	 * 根据综资电路ID查询接口明细
	 * @param outsideKeyIdList
	 * @return
	 */
	public List<Map<String, Object>> findIfaceDetailByOutKeyId(List<String> outsideKeyIdList) {
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("outsideKeyIdList", outsideKeyIdList);
		List<Map<String, Object>> devInfoList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".findIfaceDetail", pm);
		
		return devInfoList;
	}
	
	protected Record parseDetailInfo(IOrderDetail detail) {
		IOrder order = detail.getOrder();
		Map<String, Object> map = detail.getData();
		
		String aPointCuid = IbatisDAOHelper.getStringValue(map, "RELATED_SITE_A_CUID");
		if(StringUtils.isBlank(aPointCuid)) {
			aPointCuid = IbatisDAOHelper.getStringValue(map, "A_POINT_CUID");
		}
		String zPointCuid = IbatisDAOHelper.getStringValue(map, "RELATED_SITE_Z_CUID");
		if(StringUtils.isBlank(zPointCuid)) {
			zPointCuid = IbatisDAOHelper.getStringValue(map, "Z_POINT_CUID");
		}
		
		String aPointType = IbatisDAOHelper.getStringValue(map, "A_POINT_TYPE");
		if(StringUtils.isBlank(aPointType)) {
			if(StringUtils.isNotBlank(aPointCuid)) {
				if (aPointCuid.startsWith("ROOM")){
					aPointType = "ROOM";
				}else if (aPointCuid.startsWith("TRANS_ELEMENT")){
					aPointType = "TRANS_ELEMENT";
				}else if (aPointCuid.startsWith("ACCESSPOINT")){
					aPointType = "ACCESSPOINT";
				}else if (aPointCuid.startsWith("SITE_RESOURCE")){
					aPointType = "ACCESSPOINT";
				}else {
					aPointType = "SITE";
				}
			}
			
		}
		String zPointType = IbatisDAOHelper.getStringValue(map, "Z_POINT_TYPE");
		if(StringUtils.isBlank(zPointType)) {
			if(StringUtils.isNotBlank(zPointCuid)) {
				if (zPointCuid.startsWith("ROOM")){
					zPointType = "ROOM";
				}else if (zPointCuid.startsWith("TRANS_ELEMENT")){
					zPointType = "TRANS_ELEMENT";
				}else if (zPointCuid.startsWith("ACCESSPOINT")){
					zPointType = "ACCESSPOINT";
				}else if (zPointCuid.startsWith("SITE_RESOURCE")){
					zPointType = "ACCESSPOINT";
				}else {
					zPointType = "SITE";
				}
			}
			
		}
		
		String aPointName = IbatisDAOHelper.getStringValue(map, "RELATED_SITE_A");
		String zPointName = IbatisDAOHelper.getStringValue(map, "RELATED_SITE_Z");
		
		Record r = new Record("T_ACT_ORDER_DETAIL");
		r.addColValue("RELATED_SITE_A_CUID", aPointCuid);
		r.addColValue("RELATED_SITE_Z_CUID", zPointCuid);
//		r.addColSqlValue("RELATED_SITE_A", "SELECT MAX(LABEL_CN) FROM SITE R WHERE R.CUID='" + aPointCuid + "'");
//		r.addColSqlValue("RELATED_SITE_Z", "SELECT MAX(LABEL_CN) FROM SITE R WHERE R.CUID='" + zPointCuid + "'");
		r.addColValue("RELATED_SITE_A", aPointName);
		r.addColValue("RELATED_SITE_Z", zPointName);
		if(StringUtils.isBlank(aPointCuid)||StringUtils.isBlank(zPointCuid)){
			r.addColValue("RELATED_DISTRICT_A_CUID",  IbatisDAOHelper.getStringValue(map, "RELATED_DISTRICT_A_CUID"));
			r.addColValue("RELATED_DISTRICT_Z_CUID", IbatisDAOHelper.getStringValue(map, "RELATED_DISTRICT_Z_CUID"));
		}else{
			Map mp = new HashMap();
			mp.put("pointCuid", aPointCuid);
			List<Map<String,Object>> districtMap = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryDistrictByPointCuid", mp);
//			r.addColSqlValue("RELATED_DISTRICT_A_CUID", "SELECT SUBSTR(RELATED_SPACE_CUID, 0, 26) FROM SITE WHERE CUID ='" + aPointCuid + "'");
//			r.addColSqlValue("RELATED_DISTRICT_Z_CUID", "SELECT SUBSTR(RELATED_SPACE_CUID, 0, 26) FROM SITE WHERE CUID ='" + zPointCuid + "'");	
			//modify by yangcao 2016年10月11日9:27:23
			r.addColValue("RELATED_DISTRICT_A_CUID", districtMap.isEmpty()?"":(String)districtMap.get(0).get("RELATED_DISTRICT_CUID"));
			mp.clear();
			mp.put("pointCuid", zPointCuid);
			districtMap = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryDistrictByPointCuid", mp);
			r.addColValue("RELATED_DISTRICT_Z_CUID", districtMap.isEmpty()?"":(String)districtMap.get(0).get("RELATED_DISTRICT_CUID"));	
		}
		r.addColValue("A_POINT_CUID", aPointCuid);
		if(StringUtils.isBlank(aPointName)) {
			if(StringUtils.isNotBlank(aPointCuid)&&aPointCuid.startsWith("ACCESSPOINT")){
				 r.addColSqlValue("A_POINT_NAME", "SELECT MAX(LABEL_CN) FROM ACCESSPOINT R WHERE R.CUID='" + aPointCuid + "'");
		      }
		      else if ((StringUtils.isNotBlank(aPointCuid)) && (aPointCuid.startsWith("FIBER_DP")))
		        {
		    	  r.addColSqlValue("A_POINT_NAME", "SELECT MAX(LABEL_CN) FROM FIBER_DP R WHERE R.CUID='" + aPointCuid + "'");
		        }
		      else if ((StringUtils.isNotBlank(aPointCuid)) && (aPointCuid.startsWith("FIBER_CAB")))
		        {
		    	  r.addColSqlValue("A_POINT_NAME", "SELECT MAX(LABEL_CN) FROM FIBER_CAB R WHERE R.CUID='" + aPointCuid + "'");
		        }
		      else if ((StringUtils.isNotBlank(aPointCuid)) && (aPointCuid.startsWith("FIBER_JOINT_BOX")))
		        {
		    	  r.addColSqlValue("A_POINT_NAME", "SELECT MAX(LABEL_CN) FROM FIBER_JOINT_BOX R WHERE R.CUID='" + aPointCuid + "'");
		        }
		      else
		      {
		    	  r.addColSqlValue("A_POINT_NAME", "SELECT MAX(LABEL_CN) FROM SITE R WHERE R.CUID='" + aPointCuid + "'");
		      }
		} else {
			r.addColValue("A_POINT_NAME", aPointName);
		}
		r.addColValue("A_POINT_TYPE", aPointType);
		r.addColValue("Z_POINT_CUID", zPointCuid);
		if(StringUtils.isBlank(zPointName)) {
			if(StringUtils.isNotBlank(zPointCuid)&&zPointCuid.startsWith("ACCESSPOINT")){
				r.addColSqlValue("Z_POINT_NAME", "SELECT MAX(LABEL_CN) FROM ACCESSPOINT R WHERE R.CUID='" + zPointCuid + "'");
		      }
		      else if ((StringUtils.isNotBlank(zPointCuid)) && (zPointCuid.startsWith("FIBER_DP"))){
		    	  r.addColSqlValue("Z_POINT_NAME", "SELECT MAX(LABEL_CN) FROM FIBER_DP R WHERE R.CUID='" + zPointCuid + "'");
		        }
		      else if ((StringUtils.isNotBlank(zPointCuid)) && (zPointCuid.startsWith("FIBER_JOINT_BOX"))){
		    	  r.addColSqlValue("Z_POINT_NAME", "SELECT MAX(LABEL_CN) FROM FIBER_JOINT_BOX R WHERE R.CUID='" + zPointCuid + "'");
		      }
		      else if ((StringUtils.isNotBlank(zPointCuid)) && (zPointCuid.startsWith("FIBER_CAB"))){
		    	  r.addColSqlValue("Z_POINT_NAME", "SELECT MAX(LABEL_CN) FROM FIBER_CAB R WHERE R.CUID='" + zPointCuid + "'");
		        }
		      else{
		    	  r.addColSqlValue("Z_POINT_NAME", "SELECT MAX(LABEL_CN) FROM SITE R WHERE R.CUID='" + zPointCuid + "'");
		      }
		} else {
			r.addColValue("Z_POINT_NAME", zPointName);
		}
		r.addColValue("Z_POINT_TYPE", zPointType);
		
		r.addColValue("PORT_TYPE_A", IbatisDAOHelper.getStringValue(map, "PORT_TYPE_A_ID"));
		r.addColValue("PORT_TYPE_Z", IbatisDAOHelper.getStringValue(map, "PORT_TYPE_Z_ID"));
		r.addColValue("END_SWITCHDEV_PORT_A", IbatisDAOHelper.getStringValue(map, "END_SWITCHDEV_PORT_A"));
		r.addColValue("END_SWITCHDEV_PORT_Z", IbatisDAOHelper.getStringValue(map, "END_SWITCHDEV_PORT_Z"));
		r.addColValue("END_SWITCH_DEV_A", IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_A"));
		r.addColValue("END_SWITCH_DEV_Z", IbatisDAOHelper.getStringValue(map, "END_SWITCH_DEV_Z"));
		r.addColValue("IS_HIGH_RESISTANCE", IbatisDAOHelper.getStringValue(map, "IS_HIGH_RESISTANCE"));
		r.addColValue("HIGH_RESISTANCE_SPACE", IbatisDAOHelper.getStringValue(map, "HIGH_RESISTANCE_SPACE"));
		r.addColValue("OPTICAL_NUM", IbatisDAOHelper.getStringValue(map, "OPTICAL_NUM"));
		
		Object reqDate = map.get("REQUEST_DATE");
		// 电路要求开通时间
		Date requestDate = order.getFinishDate();
		if (reqDate instanceof String) {
			requestDate = TimeFormatHelper.convertDate((String) reqDate, TimeFormatHelper.TIME_FORMAT_A);
		} else if (reqDate instanceof Date) {
			requestDate = (Date) reqDate;
		}
		r.addColValue("REQUEST_DATE", requestDate);
		r.addColValue("HIGH_RESISTANCE_TRAPH_NO", IbatisDAOHelper.getStringValue(map, "HIGH_RESISTANCE_TRAPH_NO"));
		r.addColValue("END_SWITCH_DF_PORT_A", IbatisDAOHelper.getStringValue(map, "END_SWITCH_DF_PORT_A"));
		r.addColValue("END_SWITCH_DF_PORT_Z", IbatisDAOHelper.getStringValue(map, "END_SWITCH_DF_PORT_Z"));
		// 综资电路ID
		r.addColValue("OUTSIDE_KEY_ID", IbatisDAOHelper.getStringValue(map, "OUTSIDE_KEY_ID"));
		r.addColValue("VLANID", map.get("VLANID"));
		r.addColValue("RELATED_TRAPH_NAME", map.get("RELATED_TRAPH_NAME"));
		r.addColSqlValue("END_SWITCH_ROOM_ACUID", "SELECT MAX(R.CUID) FROM ROOM R WHERE R.LABEL_CN='" + IbatisDAOHelper.getStringValue(map, "END_SWITCH_ROOM_A") + "'");
		r.addColSqlValue("END_SWITCH_ROOM_ZCUID", "SELECT MAX(R.CUID) FROM ROOM R WHERE R.LABEL_CN='" + IbatisDAOHelper.getStringValue(map, "END_SWITCH_ROOM_Z") + "'");
		r.addColValue("END_SWITCH_ROOM_A", IbatisDAOHelper.getStringValue(map, "END_SWITCH_ROOM_A"));
		r.addColValue("END_SWITCH_ROOM_Z", IbatisDAOHelper.getStringValue(map, "END_SWITCH_ROOM_Z"));
		r.addColValue("ADDR_A", IbatisDAOHelper.getStringValue(map, "ADDR_A"));
		r.addColValue("CLIENT_LINKMAN_A", IbatisDAOHelper.getStringValue(map, "CLIENT_LINKMAN_A"));
		r.addColValue("JIKE_LINKMAN_A", IbatisDAOHelper.getStringValue(map, "JIKE_LINKMAN_A"));
		r.addColValue("ADDR_Z", IbatisDAOHelper.getStringValue(map, "ADDR_Z"));
		r.addColValue("CLIENT_LINKMAN_Z", IbatisDAOHelper.getStringValue(map, "CLIENT_LINKMAN_Z"));
		r.addColValue("JIKE_LINKMAN_Z", IbatisDAOHelper.getStringValue(map, "JIKE_LINKMAN_Z"));
		r.addColValue("SETTLE_CYCLE", IbatisDAOHelper.getStringValue(map, "SETTLE_CYCLE"));
		r.addColValue("INSTALL_TEST_FEE", IbatisDAOHelper.getStringValue(map, "INSTALL_TEST_FEE"));
		r.addColValue("MONTHLY_FEE", IbatisDAOHelper.getStringValue(map, "MONTHLY_FEE"));
		r.addColValue("SETTLE_SCALE", IbatisDAOHelper.getStringValue(map, "SETTLE_SCALE"));
		r.addColValue("SETTLE_DATE", TimeFormatHelper.convertDate(IbatisDAOHelper.getStringValue(map, "SETTLE_DATE")));
		//联通新加的字段值
		r.addColValue("PRODUCT_ID", IbatisDAOHelper.getStringValue(map, "PRODUCT_ID"));
		r.addColValue("PAY_ID", IbatisDAOHelper.getStringValue(map, "PAY_ID"));
		r.addColValue("TRAPH_BUSINESS_TYPE", IbatisDAOHelper.getStringValue(map, "TRAPH_BUSINESS_TYPE"));
		r.addColValue("OPERATE_TYPE", IbatisDAOHelper.getStringValue(map, "OPERATE_TYPE"));
		String traphRate = IbatisDAOHelper.getStringValue(map, "TRAPH_RATE");
		if (!StringUtils.isEmpty(traphRate)) {
			r.addColValue("TRAPH_RATE", IbatisDAOHelper.getStringValue(map, "TRAPH_RATE"));
		} else {
			// 界面提交的单
			String key = IbatisDAOHelper.getStringValue(map,"PORT_TYPE_A_ID")+","+IbatisDAOHelper.getStringValue(map, "PORT_TYPE_Z_ID");
			r.addColValue("TRAPH_RATE", PortType2RateMapping.getMappingByKey(key));
		}
		String extIds = IbatisDAOHelper.getStringValue(map, "EXT_IDS");
		if (StringUtils.isBlank(extIds)) {
			extIds = ",1,";
		}
		// 默认是语音电路
		r.addColValue("EXT_IDS", extIds);
		r.addColValue("BAND_WIDTH", IbatisDAOHelper.getStringValue(map, "BAND_WIDTH"));
		return r;
	}
	
	public List<IOrderDetail> findOrderDetail(List<String> detailCuidList) {
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("cuidList", detailCuidList);
		List<Map<String, Object>> list = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap + ".findOrderDetail", pm);
		return this.parseOrderDetail(list);
	}
	
	public List<IOrderDetail> findOrderDetail(String orderId) {
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("orderId", orderId);
		List<Map<String, Object>> list = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap + ".findOrderDetail", pm);
		return this.parseOrderDetail(list);
	}
	/**
	 * 界面根据订单明细获取详细的订单明细对象数据  更新AZ站点中文名
	 * @param orderId
	 */
	public void updateOrderAZInfo(String orderId) {
		Map<String, Object> pm = new HashMap<String, Object>();
		List<String> siteList = new ArrayList<String>();
		pm.put("orderId", orderId);
		List<Map<String, Object>> list = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap + ".findNewAZInfo", pm);
		List<List<String>> updateList=new ArrayList<List<String>>();
	    //取站点不存在的数据
		for(Map<String, Object> map:list){
			String aPointCuid=IbatisDAOHelper.getStringValue(map, "A_POINT_CUID");
			String zPointCuid=IbatisDAOHelper.getStringValue(map, "Z_POINT_CUID");
			if(aPointCuid==null||zPointCuid==null||StringUtils.isEmpty(aPointCuid)||StringUtils.isEmpty(zPointCuid)){
				List<String> infoList=new ArrayList<String>();
				if(IbatisDAOHelper.getStringValue(map, "A_POINT_NAME") != null){
					siteList.add(IbatisDAOHelper.getStringValue(map, "A_POINT_NAME"));
				} 
				if(IbatisDAOHelper.getStringValue(map, "Z_POINT_NAME") != null){
					siteList.add(IbatisDAOHelper.getStringValue(map, "Z_POINT_NAME"));
				}
				infoList.add(IbatisDAOHelper.getStringValue(map, "CUID"));
				infoList.add(IbatisDAOHelper.getStringValue(map, "A_POINT_NAME"));
				infoList.add(IbatisDAOHelper.getStringValue(map, "Z_POINT_NAME"));
				updateList.add(infoList);
			}
			
		}
		Map<String,Map<String,Object>> relatedNameMap=new HashMap<String, Map<String,Object>>();
		if(siteList!=null&&siteList.size()>0){
			pm.clear();
			pm.put("siteSet", siteList);
			//翻译站点名称
			List<Map<String,Object>> relatedNameList = IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryLabelCnByName", pm);
			relatedNameMap = IbatisDAOHelper.parseList2Map(relatedNameList, "LABEL_CN");	
		}
		List<Record> uList = new ArrayList<Record>();
		List<Record> uPkList = new ArrayList<Record>();
		for(List<String> infoList : updateList){
			String detailCuid=infoList.get(0);
			String siteAName=infoList.get(1);
			String siteZName=infoList.get(2);
			Map<String,Object> aSiteNameMap = relatedNameMap.get(siteAName);
			if(aSiteNameMap==null){
				aSiteNameMap = new HashMap<String,Object>();
			}
			String aPointCuid = IbatisDAOHelper.getStringValue(aSiteNameMap, "CUID");
			String aPointType = IbatisDAOHelper.getStringValue(aSiteNameMap, "POINT_TYPE");
			Map<String,Object> zSiteNameMap = relatedNameMap.get(siteZName);
			if(zSiteNameMap==null){
				zSiteNameMap = new HashMap<String,Object>();
			}
			String zPointCuid = IbatisDAOHelper.getStringValue(zSiteNameMap, "CUID");
			String zPointType = IbatisDAOHelper.getStringValue(zSiteNameMap, "POINT_TYPE");
			Record u = new Record("T_ACT_ORDER_DETAIL");
			u.addColValue("A_POINT_CUID", aPointCuid);
			u.addColValue("RELATED_SITE_A_CUID", aPointCuid);
			u.addColValue("A_POINT_TYPE", aPointType);
			u.addColValue("Z_POINT_CUID",zPointCuid );
			u.addColValue("RELATED_SITE_Z_CUID",zPointCuid );
			u.addColValue("Z_POINT_TYPE", zPointType);
			uList.add(u);
			Record uPk = new Record(u.getTableName());
			uPk.addColValue("CUID", detailCuid);
			uPkList.add(uPk);
		}
		this.IbatisResDAO.updateDynamicTableBatch(uList, uPkList);
	}
	/**
	 * 解析订单明细
	 * @param detailCuidList
	 * @return
	 */
	private List<IOrderDetail> parseOrderDetail(List<Map<String, Object>> list) {
		List<IOrderDetail> detailList = new ArrayList<IOrderDetail>();
		Map<String, IOrderDetail> detailMap = new HashMap<String, IOrderDetail>();
		List<String> detailCuidList = new ArrayList<String>();
		IOrder order = null;
		for(Map<String, Object> map : list) {
			if(order == null) {
				String orderId = IbatisDAOHelper.getStringValue(map, "RELATED_APPLYSHEET_CUID");
				order = this.getOrderById(orderId);
			}
			detailCuidList.add(IbatisDAOHelper.getStringValue(map, "CUID"));
			int attempType = IbatisDAOHelper.getIntValue(map, "ATTEMP_TYPE");
			String designType = IbatisDAOHelper.getStringValue(map, "DESIGN_TYPE");
			OrderDetailInfo orderDetailInfo = new OrderDetailInfo(order, attempType, designType);
			orderDetailInfo.setCuid(IbatisDAOHelper.getStringValue(map, "CUID"));
			orderDetailInfo.setRelatedSheetCuid(IbatisDAOHelper.getStringValue(map, "RELATED_SHEET_CUID"));
			orderDetailInfo.setRelatedServiceCuid(IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_CUID"));
			orderDetailInfo.setSortNo(IbatisDAOHelper.getIntValue(map, "SORT_NO"));
			orderDetailInfo.setaPointCuid(IbatisDAOHelper.getStringValue(map, "A_POINT_CUID"));
			orderDetailInfo.setaPointName(IbatisDAOHelper.getStringValue(map, "A_POINT_NAME"));
			orderDetailInfo.setaPointType(IbatisDAOHelper.getStringValue(map, "A_POINT_TYPE"));
			orderDetailInfo.setzPointCuid(IbatisDAOHelper.getStringValue(map, "Z_POINT_CUID"));
			orderDetailInfo.setzPointName(IbatisDAOHelper.getStringValue(map, "Z_POINT_NAME"));
			orderDetailInfo.setzPointType(IbatisDAOHelper.getStringValue(map, "Z_POINT_TYPE"));
			orderDetailInfo.setData(map);
			detailList.add(orderDetailInfo);
			detailMap.put(orderDetailInfo.getCuid(), orderDetailInfo);
		}
		
		detailList = this.findOrderDetailExtend(detailList);
		return detailList;
	}
	
	public List<IOrderDetail> findOrderDetailExtend(List<IOrderDetail> detailList) {
		return detailList;
	}
	
	/**
	 * 复制订单明细
	 * @param orderId
	 * @param detailCuidList
	 * @param changeMap
	 */
	public void copyOrderDetail(String orderId,List<String> serviceIdList, Map<String, Object> changeMap) {
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("orderId", orderId);
		pm.put("serviceCuidList", serviceIdList);
		pm.put("relatedSheetCuid", IbatisDAOHelper.getStringValue(changeMap, "relatedSheetCuid"));
		pm.put("relatedOrderCuid", IbatisDAOHelper.getStringValue(changeMap, "relatedOrderCuid"));
		this.IbatisResDAO.getSqlMapClientTemplate().insert(sqlMap+".copyOrderDetail", pm);
	}
	
	/**
	 * 根据订单更新订单明细的工单归属
	 * @param orderId
	 * @param sheetId
	 */
	public void updateOrderDetailSheetByOrder(String orderId, String sheetId) {
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("orderId", orderId);
		pm.put("sheetId", sheetId);
		this.IbatisResDAO.getSqlMapClientTemplate().update(sqlMap+".updateOrderDetailSheetByOrder", pm);
	}
	
	/**
	 * 获取订单明细最大SORT_NO
	 * 
	 * @param orderId
	 * @return
	 */
	public int getMaxDetailSortNo(String orderId) {
		int maxNo = 0;

		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("orderId", orderId);
		Integer no = (Integer) this.IbatisResDAO.getSqlMapClientTemplate().queryForObject(sqlMap + ".queryDetailMaxSortNo", pm);
		if (no != null) {
			maxNo = no + 1;
		}
		return maxNo;
	}
	public Map<Integer, InfoAttempType> getAttempTypeMap(IOrder order,List<IOrderDetail> detailList) {
		Map<Integer, InfoAttempType> infoAttempType = new HashMap<Integer, InfoAttempType>();
		for (IOrderDetail detail : detailList) {
			Integer attempType = detail.getAttempType();
			String designType = detail.getDesignType();
			InfoAttempType devInfo = infoAttempType.get(attempType);
			if (devInfo == null) {
				devInfo = new InfoAttempType(attempType);
				infoAttempType.put(attempType, devInfo);
			}
			Map<String, InfoDesignType> designTypeMap = devInfo.getDesignTypeMap();
			InfoDesignType designInfo = designTypeMap.get(designType);
			if (designInfo == null) {
				designInfo = new InfoDesignType(designType);
				designTypeMap.put(designType, designInfo);
			}
		}
		return infoAttempType;
	}
	public Map<Integer, InfoAttempType> buildOrderDetailMap(IOrder order,List<IOrderDetail> detailList) {
		Map<Integer, InfoAttempType> infoAttempType = new HashMap<Integer, InfoAttempType>();
		for (IOrderDetail detail : detailList) {
			Map<String, Object> map = detail.getData();
			String cuid = detail.getCuid();
			Integer attempType = detail.getAttempType();
			String designType = detail.getDesignType();
			String designUser = IbatisDAOHelper.getStringValue(map, "TRAPH_DESIGN_USER");
			String relatedDistrictCuid = IbatisDAOHelper.getStringValue(map, "RELATED_DISTRICT_CUID");
			InfoAttempType devInfo = infoAttempType.get(attempType);
			if (devInfo == null) {
				devInfo = new InfoAttempType(attempType);
				infoAttempType.put(attempType, devInfo);
			}
			Map<String, InfoDesignType> designTypeMap = devInfo.getDesignTypeMap();
			InfoDesignType designInfo = designTypeMap.get(designType);
			if (designInfo == null) {
				designInfo = new InfoDesignType(designType);
				designTypeMap.put(designType, designInfo);
			}
			List<String> infoCuidList = designInfo.getDetailIdList();
			infoCuidList.add(cuid);
			String oldTraphId ="";
			if(designType.equalsIgnoreCase("P")){
				logger.info("取得关联pon："+IbatisDAOHelper.getStringValue(detail.getData(),"RELATED_PON_WAY_CUID"));
				oldTraphId=IbatisDAOHelper.getStringValue(detail.getData(),"RELATED_PON_WAY_CUID");
			}else{
				oldTraphId = detail.getRelatedServiceCuid();
			}
			// 调整获取历史电路信息
			if (devInfo.getAttempType() != InfoAttempType.ATTEMP_TYPE_ADD) {
				if (StringUtils.isEmpty(oldTraphId)){
					if(designType.equalsIgnoreCase("O")){
						throw new RuntimeException("调整或停闭光路的存量光路不允许为空！");
					}else if(designType.equalsIgnoreCase("P")){
						throw new RuntimeException("调整或停闭PON的存量PON不允许为空！");
					}else{
						throw new RuntimeException("调整或停闭电路的存量电路不允许为空！");
					}
				}
				List<String> oldTraphCuidList = designInfo.getRelatedResInfo();
				String oldServiceName = IbatisDAOHelper.getStringValue(map, "RELATED_TRAPH_NAME");
				
				// 这里如果发现oldTraphId有重复的需要抛异常
				if (oldTraphCuidList.contains(oldTraphId))
					throw new RuntimeException("同一资源【" + oldServiceName + "】不能重复调整！");
				oldTraphCuidList.add(oldTraphId);
			} else {
				if (!StringUtils.isEmpty(oldTraphId))
					throw new RuntimeException("新增工单的关联资源ID必须为空！");
			}

			// 按设计用户分组存放信息
			Map<String, List<Map<String,Object>>> userPlanMap = designInfo.getUserPlanMap();
			List<Map<String,Object>> userPlanList = userPlanMap.get(designUser);
			if (userPlanList == null) {
				userPlanList = new ArrayList<Map<String,Object>>();
				userPlanMap.put(designUser, userPlanList);
			}
			userPlanList.add(map);
			
			// 按地市分组存放信息
			Map<String, List<Map<String,Object>>> distrcitPlanMap = designInfo.getDistrictPlanMap();
			List<Map<String,Object>> distrcitPlanList = distrcitPlanMap.get(relatedDistrictCuid);
			if (distrcitPlanList == null) {
				distrcitPlanList = new ArrayList<Map<String,Object>>();
				distrcitPlanMap.put(relatedDistrictCuid, distrcitPlanList);
			}
			distrcitPlanList.add(map);
			
			//根据业务明细的类型以及定单的类型来决定相关的adaptor
			String boName = detailMapping.getMappingByKey(designType+"-"+order.getOrderCode());
			IOrderExtendMaintainBO adaptor = (IOrderExtendMaintainBO)SpringContextUtil.getBean(boName);
			IName key = adaptor.coverOrderDetailToServiceName(order,detail);
			IService service = adaptor.coverOrderDetailToService(order,detail);
			Map<IName, List<IService>> serviceMap = designInfo.getResNameMap();
			//全国接口serviceMap有可能为空
			if(!serviceMap.isEmpty()){
				List<IService> infoList = serviceMap.get(key);
				if (infoList == null) {
					infoList = new ArrayList<IService>();
					serviceMap.put(key, infoList);
				}
				infoList.add(service);
			}
			List<IService> resList = designInfo.getResList();
			resList.add(service);
		}
		return infoAttempType;
	}

	public void deleteOrderDetail(ServiceActionContext ac, List<String> detailCuidList) {
		List<IOrderDetail> detailList = this.findOrderDetail(detailCuidList);
		if(!detailList.isEmpty()) {
			IOrder order = detailList.get(0).getOrder();
			List<String> traphCuidList = new ArrayList<String>();
			Map<String, List<IOrderDetail>> detailMap = new HashMap<String, List<IOrderDetail>>();
			for(IOrderDetail detail : detailList) {
				String traphCuid = IbatisDAOHelper.getStringValue(detail.getData(), "RELATED_TRAPH_CUID");
				if(StringUtils.isNotEmpty(traphCuid)){
					traphCuidList.add(traphCuid);
				}
				List<IOrderDetail> list = detailMap.get(detail.getDesignType());
				if(list == null) {
					list = new ArrayList<IOrderDetail>();
					detailMap.put(detail.getDesignType(), list);
				}
				list.add(detail);
			}
			for(String designType : detailMap.keySet()) {
				String boName = detailMapping.getMappingByKey(designType+"-"+order.getOrderCode());
				IOrderExtendMaintainBO bo = (IOrderExtendMaintainBO)SpringContextUtil.getBean(boName);
				bo.deleteOrderDetailExtend(ac, detailMap.get(designType));
			}
			Map<String, Object> pm = new HashMap<String, Object>();
			pm.put("cuidList", detailCuidList);
			IbatisResDAO.getSqlMapClientTemplate().delete(sqlMap + ".deleteOrderDetail", pm);
			
			if(traphCuidList != null && traphCuidList.size()>0){
				ResTraphBO.updateServiceSchduleState(traphCuidList,SheetConstants.SCHEDULE_STATE_END);
			}
		}
	}
	
	/**
	 * 将任务创建的服务关联至订单明细
	 * @param serviceIdList
	 * @param serviceTableName
	 * @param state
	 */
	public void updateOrderDetailStateByService(List<String> serviceIdList,String serviceTableName, String state) {
		if(StringUtils.isEmpty(serviceTableName))throw new RuntimeException("资源服务表名不允许为空");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("serviceTableName", serviceTableName);
		param.put("state", state);
		param.put("serviceIdList", serviceIdList);
		this.IbatisResDAO.getSqlMapClientTemplate().update(sqlMap + ".updateOrderDetailByService", param);
		logger.debug("更新业务明细状态");
	}
	/**
	 * 将任务创建的服务关联至订单明细
	 * @param sheetId
	 * @param state
	 */
	public void updateOrderDetailStateBySheet(String sheetId, String state) {
		if(StringUtils.isEmpty(sheetId))throw new RuntimeException("所属工单不允许为空");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("state", state);
		param.put("sheetId", sheetId);
		this.IbatisResDAO.getSqlMapClientTemplate().update(sqlMap + ".updateOrderDetailByService", param);
		logger.debug("更新业务明细状态");
	}
	
	/**
	 * 删除外部调度接口表数据
	 * @param outIdList
	 */
	public void deleteIfaceDetail(ServiceActionContext ac, List<String> outIdList){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("outIdList", outIdList);
		this.IbatisResDAO.getSqlMapClientTemplate().delete(sqlMap + ".deleteIfaceByOutKeyId", param);
		logger.debug("删除接口明细");
	}
	/**
	 * 删除外部调度接口表数据
	 * @param outIdList
	 */
	public void deleteIfaceDetail(ServiceActionContext ac, String orderId){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("orderId", orderId);
		this.IbatisResDAO.getSqlMapClientTemplate().delete(sqlMap + ".deleteIfaceByOrderId", param);
		logger.debug("删除接口明细");
	}
	/**
	 * 清空订单明细关联业务
	 * @param ac
	 * @param serviceIdList
	 * @param serviceTableName
	 */
	public void clearOrderDetailByService(ServiceActionContext ac, List<String> serviceIdList,String serviceTableName) {
		if(StringUtils.isEmpty(serviceTableName))throw new RuntimeException("资源服务表名不允许为空");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("serviceTableName", serviceTableName);
		param.put("serviceIdList", serviceIdList);
		param.put("clearService", "Y");
		this.IbatisResDAO.getSqlMapClientTemplate().update(sqlMap + ".updateOrderDetailByService", param);
		logger.debug("将创建的资源服务关联到对应的订单明细");
	}
	/**
	 * 根据综资id找到工单
	 * @param ac
	 * @param serviceIdList
	 * @param serviceTableName
	 */
	public List<Map<String,Object>> getSheetByIrmsId(ServiceActionContext ac, String irmsSheetId) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("irmsSheetId", irmsSheetId);
		return this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".querySheetByIrmsId", param);
	}
	/**
	 * 验证网元是否归属站点
	 * @param cuidList
	 */
	public List<String> queryNotHasSiteNe(List<String> cuidList) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("cuidList", cuidList);
		return this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".queryNotHasSiteNe", param);
	}
	
	public void copyTask2ServicefLinks (String oldTaskId,String newTaskId){
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("newTaskCuid", newTaskId);
		pm.put("oldTaskId", oldTaskId);
		IbatisResDAO.getSqlMapClientTemplate().insert(sqlMap + ".copyTask2ServiceLink", pm);
		
	}
	
	public void updateTraphSchduleState(ServiceActionContext ac, String orderId){
		Map<String, Object> pm = new HashMap<String,Object>();
		pm.put("orderId", orderId);
		List<String> serviceCuidList = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap+".getServiceCuidList", pm);
		//若是调整或者停闭电路，则作废时更改TRAPH表SCHEDULE_STATE状态；新增时作废则不更改
		if(serviceCuidList!=null && serviceCuidList.size()>0 && StringUtils.isNotEmpty(serviceCuidList.get(0))){
			ResTraphBO.updateServiceSchduleState(serviceCuidList,SheetConstants.SCHEDULE_STATE_END);
		}
	}
	
	public String getApMode(String apMode){
		if(apMode.equals("PTN")){
			apMode = "1";
		}else if (apMode.equals("SDH")){
			apMode = "2";
		} else{
			apMode = "0";
		}
		return apMode;
	}
	
	public String getSubApMode(String subApMode){
		if(subApMode.equals("SDH放客户侧直连")){
			subApMode = "1";
		}else if (subApMode.equals("接入型MSAP接入")){
			subApMode = "2";
		}else if (subApMode.equals("汇聚型MSAP接入")){
			subApMode = "3";
		}else if (subApMode.equals("以太网直连")){
			subApMode = "4";
		}else if (subApMode.equals("光纤收发器/协转接入")){
			subApMode = "5";
		}else if (subApMode.equals("微波接入")){
			subApMode = "6";
		}else if (subApMode.equals("交换机接入")){
			subApMode = "7";
		}else if (subApMode.equals("PTN放客户侧直连")){
			subApMode = "8";
		}else{
			subApMode = "0";
		}
		return subApMode;
	}
	
	/*public List<Map<String, Object>> findTaskCuid(String orderId,String flag){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> pm = new HashMap<String, Object>();
		pm.put("orderId", orderId);
		if(flag.equals("C")){
			list = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap + ".findTaskCuid", pm);
		}else if(flag.equals("T")){
			list = this.IbatisResDAO.getSqlMapClientTemplate().queryForList(sqlMap + ".findReApplyTaskCuid", pm);
		}
		return list;
	}*/
	/*Strting转化成number*/
	/*Strting转化成number*/
	/*public String getRouteMode(String routeMode){
		if(StringUtils.equals(routeMode,"单节点单路由")){
			routeMode = "1";
		}else if(StringUtils.equals(routeMode,"单节点双路由")){
			routeMode = "2";
		}else if(StringUtils.equals(routeMode,"双节点双路由")){
			routeMode = "3";
		}else{
			routeMode = "0"; 未知
		}
		return routeMode;
	}*/
}
