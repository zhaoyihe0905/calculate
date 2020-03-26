package com.sinosoft;

import com.CI.IACMain_NCPB;
import com.CI.IACMain_NCPPostpone;
import com.CI.IACMain_NCPX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JTextArea;

public class SinosoftIa implements SinosoftInterface{

	public void SituationOne(Date start, Date end, JTextArea textArea, String areaCode) {
		//获取疫情起止日期
		long NCPStartDate = start.getTime();
        long NCPEndDate = end.getTime();
        //查询满足条件的疫情期本保单信息数据
        List<IACMain_NCPB> iacMain_ncpbs = new ArrayList<IACMain_NCPB>();
		
	}

	public void SituationTwo(Date start, Date end, JTextArea textArea, String areaCode) {
        long NCPStartDate = start.getTime();
        long NCPEndDate = end.getTime();
		int tag =0;
		//查询IACMain_NCPB-疫情期本保单信息表中的保单信息集合，“保单归属地（地市）-CityCode”“业务类型-BusinessType”、“非延期原因-Reason”、“是否顺延-Flag”三字段作为查询条件进行取值判断
		//模拟获取到的本保单集合
		List<IACMain_NCPB> iacMain_ncpbs = new ArrayList<IACMain_NCPB>();
		//遍历数据
		for (IACMain_NCPB iacMain_ncpb : iacMain_ncpbs) {
			//拿每个保单的投保确认码，查询IACMain_NCPX-疫情期续保保单信息表中有无续保单，根据此情况进行业务判断
			//模拟获取到的续保单集合，此保单集合根据保单止期，倒叙排序，
			List<IACMain_NCPX> iacMain_ncpxs = new ArrayList<IACMain_NCPX>();
			//判断:一、无续保单
			if (iacMain_ncpxs == null||iacMain_ncpxs.size()==0) {
				//疫情有效期
				List<Timestamp> list= new ArrayList();
				list.add(0,iacMain_ncpb.getStartDate());
				list.add(1,iacMain_ncpb.getEndDate());
				List< List<Timestamp>> bigList= new ArrayList();
				bigList.add(list);
				long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);
				//局部变量
				long l=0l;
				Timestamp AfterEndDate=null;

				//两种情况:1.保单保险止期小于疫情截止日
				if(iacMain_ncpb.getEndDate().getTime()<NCPEndDate){
					//顺延后保单止期:疫情止期+疫情有效期
					l=NCPEndDate+(NCPValidDate*86400);
					AfterEndDate = new Timestamp(l);

				}else{//2.保单保险止期>=疫情截止日
					//顺延后保单止期:原保单止期+疫情有效期
					l=iacMain_ncpb.getEndDate().getTime()+(NCPValidDate*86400);
					AfterEndDate = new Timestamp(l);
				}
				//顺延天数：顺延后保单止期-原保单止期
				long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400;

				//组织参数存库 疫情期顺延后保单信息表
				IACMain_NCPPostpone ncpPostpone = new IACMain_NCPPostpone();
				ncpPostpone.setPolicyConfirmNo(iacMain_ncpb.getPolicyConfirmNo());
				ncpPostpone.setPolicyNo(iacMain_ncpb.getPolicyNo());
				ncpPostpone.setCompanyCode(iacMain_ncpb.getCompanyCode());
				ncpPostpone.setStartDate(iacMain_ncpb.getStartDate());
				ncpPostpone.setEndDate(iacMain_ncpb.getEndDate());
				ncpPostpone.setAfterEndDate(AfterEndDate);
				Timestamp ncpStartDate = new Timestamp(NCPStartDate);
				ncpPostpone.setNCPStartDate(ncpStartDate);
				Timestamp ncpEndDate = new Timestamp(NCPEndDate);
				ncpPostpone.setNCPEndDate(ncpEndDate);
				ncpPostpone.setNCPValidDate(Integer.parseInt(String.valueOf(NCPValidDate)));
				ncpPostpone.setPostponeDay(Integer.parseInt(String.valueOf(PostponeDay)));
				ncpPostpone.setCityCode(iacMain_ncpb.getCityCode());
				ncpPostpone.setFrameNo(iacMain_ncpb.getFrameNo());
				ncpPostpone.setLicenseNo(iacMain_ncpb.getLicenseNo());
				ncpPostpone.setEngineNo(iacMain_ncpb.getEngineNo());
				ncpPostpone.setBusinessType(iacMain_ncpb.getBusinessType());
				ncpPostpone.setInputDate(new Timestamp(System.currentTimeMillis()));
				ncpPostpone.setValidStatus("1");


			}else{  //二、有续保单

				//疫情有效期
				List<Timestamp> list= new ArrayList();
				List< List<Timestamp>> bigList= new ArrayList();
				list.add(0,iacMain_ncpb.getStartDate());
				list.add(1,iacMain_ncpb.getEndDate());
				//本保单起止日期
				bigList.add(list);
				for (IACMain_NCPX iacMain_ncpx : iacMain_ncpxs) {
					List<Timestamp> list1= new ArrayList();
					list1.add(0,iacMain_ncpx.getStartDate());
					list1.add(1,iacMain_ncpx.getEndDate());
					//每个续保单起止日期
					bigList.add(list1);
				}
				long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);

				//局部变量
				long l=0;
				Timestamp AfterEndDate=null;
				//以保单起期，顺序排序，找到第一张续保单
				Util.ciStartTimeSort(iacMain_ncpxs);
				//特殊情况：本保单疫情期间起保，疫情期间到期；有一张起保日期＞疫情止期的续保单，续保单起保日期-疫情止期≥N；顺延本保单
				if((iacMain_ncpb.getEndDate().getTime()<NCPEndDate) && iacMain_ncpxs.get(0).getStartDate().getTime()>NCPEndDate && ((iacMain_ncpxs.get(0).getStartDate().getTime()-NCPEndDate)>NCPValidDate) ){
					//顺延后保单止期
					l=NCPEndDate+(NCPValidDate*86400);
					AfterEndDate = new Timestamp(l);
					//顺延天数：顺延后保单止期-原保单止期
					long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400;

					//组织参数存库 疫情期顺延后保单信息表
					IACMain_NCPPostpone ncpPostpone = new IACMain_NCPPostpone();
					ncpPostpone.setPolicyConfirmNo(iacMain_ncpb.getPolicyConfirmNo());
					ncpPostpone.setPolicyNo(iacMain_ncpb.getPolicyNo());
					ncpPostpone.setCompanyCode(iacMain_ncpb.getCompanyCode());
					ncpPostpone.setStartDate(iacMain_ncpb.getStartDate());
					ncpPostpone.setEndDate(iacMain_ncpb.getEndDate());
					ncpPostpone.setAfterEndDate(AfterEndDate);
					Timestamp ncpStartDate = new Timestamp(NCPStartDate);
					ncpPostpone.setNCPStartDate(ncpStartDate);
					Timestamp ncpEndDate = new Timestamp(NCPEndDate);
					ncpPostpone.setNCPEndDate(ncpEndDate);
					ncpPostpone.setNCPValidDate(Integer.parseInt(String.valueOf(NCPValidDate)));
					ncpPostpone.setPostponeDay(Integer.parseInt(String.valueOf(PostponeDay)));
					ncpPostpone.setCityCode(iacMain_ncpb.getCityCode());
					ncpPostpone.setFrameNo(iacMain_ncpb.getFrameNo());
					ncpPostpone.setLicenseNo(iacMain_ncpb.getLicenseNo());
					ncpPostpone.setEngineNo(iacMain_ncpb.getEngineNo());
					ncpPostpone.setBusinessType(iacMain_ncpb.getBusinessType());
					ncpPostpone.setInputDate(new Timestamp(System.currentTimeMillis()));
					ncpPostpone.setValidStatus("1");

				}else{
					//以保单止期，倒序排序，找到最靠后一张续保单
					Util.ciEndTimeReverse(iacMain_ncpxs);
					//2种情况：1、最后一张续保单止期>=疫情截止日
					if (iacMain_ncpxs.get(0).getEndDate().getTime()>=NCPEndDate){
						//顺延后保单止期:最靠后一张续保单止期+疫情有效期
						l=iacMain_ncpxs.get(0).getEndDate().getTime()+(NCPValidDate*86400);
						AfterEndDate = new Timestamp(l);
					}else{//2、最后一张续保单止期<疫情截止日
						//顺延后保单止期:疫情止期+疫情有效期
						l=NCPEndDate+(NCPValidDate*86400);
						AfterEndDate = new Timestamp(l);
					}
					//顺延天数：顺延后保单止期-原最靠后一张续保单止期
					long PostponeDay = (l - iacMain_ncpxs.get(0).getEndDate().getTime()) / 86400;

					//组织参数存库 疫情期顺延后保单信息表
					IACMain_NCPPostpone ncpPostpone = new IACMain_NCPPostpone();
					ncpPostpone.setPolicyConfirmNo(iacMain_ncpxs.get(0).getPolicyConfirmNo());
					ncpPostpone.setPolicyNo(iacMain_ncpxs.get(0).getPolicyNo());
					ncpPostpone.setCompanyCode(iacMain_ncpxs.get(0).getCompanyCode());
					ncpPostpone.setStartDate(iacMain_ncpxs.get(0).getStartDate());
					ncpPostpone.setEndDate(iacMain_ncpxs.get(0).getEndDate());
					ncpPostpone.setAfterEndDate(AfterEndDate);
					Timestamp ncpStartDate = new Timestamp(NCPStartDate);
					ncpPostpone.setNCPStartDate(ncpStartDate);
					Timestamp ncpEndDate = new Timestamp(NCPEndDate);
					ncpPostpone.setNCPEndDate(ncpEndDate);
					ncpPostpone.setNCPValidDate(Integer.parseInt(String.valueOf(NCPValidDate)));
					ncpPostpone.setPostponeDay(Integer.parseInt(String.valueOf(PostponeDay)));
					ncpPostpone.setCityCode(iacMain_ncpxs.get(0).getCityCode());
					ncpPostpone.setLastPoliConfirmNo(iacMain_ncpb.getPolicyConfirmNo());
					ncpPostpone.setFrameNo(iacMain_ncpxs.get(0).getFrameNo());
					ncpPostpone.setLicenseNo(iacMain_ncpxs.get(0).getLicenseNo());
					ncpPostpone.setEngineNo(iacMain_ncpxs.get(0).getEngineNo());
					ncpPostpone.setBusinessType(iacMain_ncpb.getBusinessType());
					ncpPostpone.setInputDate(new Timestamp(System.currentTimeMillis()));
					ncpPostpone.setValidStatus("1");


				}

			}

		}

		
	}

	public void SituationTree(Date start, Date end, JTextArea textArea, String areaCode) {
		// TODO Auto-generated method stub
		
	}

}
