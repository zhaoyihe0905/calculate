package com.sinosoft;

import com.CA.CACMain_NCPB;
import com.CA.CACMain_NCPPostpone;
import com.CA.CACMain_NCPX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JTextArea;

public class SinosoftCa implements SinosoftInterface{
	
	public void SituationOne(Date start, Date end, JTextArea textArea, String areaCode) {
		// TODO Auto-generated method stub
		
	}

	public void SituationTwo(Date start, Date end, JTextArea textArea, String areaCode) {
		long NCPStartDate = start.getTime();
		long NCPEndDate = end.getTime();
		int tag =0;
		//查询IACMain_NCPB-疫情期本保单信息表中的保单信息集合，“保单归属地（地市）-CityCode”“业务类型-BusinessType”、“非延期原因-Reason”、“是否顺延-Flag”三字段作为查询条件进行取值判断
		//模拟获取到的本保单集合
		List<CACMain_NCPB> cacMain_ncpbs = new ArrayList<CACMain_NCPB>();
		//遍历数据
		for (CACMain_NCPB cacMain_ncpb : cacMain_ncpbs) {
			//拿每个保单的投保确认码，查询IACMain_NCPX-疫情期续保保单信息表中有无续保单，根据此情况进行业务判断
			//模拟获取到的续保单集合，此保单集合根据保单止期，倒叙排序，
			List<CACMain_NCPX> cacMain_ncpxs = new ArrayList<CACMain_NCPX>();
			//判断:一、无续保单
			if (cacMain_ncpxs == null||cacMain_ncpxs.size()==0) {
				//疫情有效期
				List<Timestamp> list= new ArrayList();
				list.add(0,cacMain_ncpb.getEffectiveDate());
				list.add(1,cacMain_ncpb.getExpireDate());
				List< List<Timestamp>> bigList= new ArrayList();
				bigList.add(list);
				long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);
				//局部变量
				long l=0l;
				Timestamp AfterExpireDate=null;

				//两种情况:1.保单保险止期小于疫情截止日
				if(cacMain_ncpb.getExpireDate().getTime()<NCPEndDate){
					//顺延后保单止期:疫情止期+疫情有效期
					l=NCPEndDate+(NCPValidDate*86400);
					AfterExpireDate = new Timestamp(l);

				}else{//2.保单保险止期>=疫情截止日
					//顺延后保单止期:原保单止期+疫情有效期
					l=cacMain_ncpb.getExpireDate().getTime()+(NCPValidDate*86400);
					AfterExpireDate = new Timestamp(l);
				}
				//顺延天数：顺延后保单止期-原保单止期
				long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400;

				//组织参数存库 疫情期顺延后保单信息表
				CACMain_NCPPostpone ncpPostpone = new CACMain_NCPPostpone();
				ncpPostpone.setConfirmSequenceNo(cacMain_ncpb.getConfirmSequenceNo());
				ncpPostpone.setPolicyNo(cacMain_ncpb.getPolicyNo());
				ncpPostpone.setCompanyCode(cacMain_ncpb.getCompanyCode());
				ncpPostpone.setEffectiveDate(cacMain_ncpb.getEffectiveDate());
				ncpPostpone.setExpireDate(cacMain_ncpb.getExpireDate());
				ncpPostpone.setAfterExpireDate(AfterExpireDate);
				Timestamp ncpStartDate = new Timestamp(NCPStartDate);
				ncpPostpone.setNCPStartDate(ncpStartDate);
				Timestamp ncpEndDate = new Timestamp(NCPEndDate);
				ncpPostpone.setNCPEndDate(ncpEndDate);
				ncpPostpone.setNCPValidDate(Integer.parseInt(String.valueOf(NCPValidDate)));
				ncpPostpone.setPostponeDay(Integer.parseInt(String.valueOf(PostponeDay)));
				ncpPostpone.setCityCode(cacMain_ncpb.getCityCode());
				ncpPostpone.setVin(cacMain_ncpb.getVin());
				ncpPostpone.setLicenseNo(cacMain_ncpb.getLicenseNo());
				ncpPostpone.setEngineNo(cacMain_ncpb.getEngineNo());
				ncpPostpone.setBusinessType(cacMain_ncpb.getBusinessType());
				ncpPostpone.setInputDate(new Timestamp(System.currentTimeMillis()));
				ncpPostpone.setValidStatus("1");


			}else{  //二、有续保单

				//疫情有效期
				List<Timestamp> list= new ArrayList();
				List< List<Timestamp>> bigList= new ArrayList();
				list.add(0,cacMain_ncpb.getEffectiveDate());
				list.add(1,cacMain_ncpb.getExpireDate());
				//本保单起止日期
				bigList.add(list);
				for (CACMain_NCPX cacMain_ncpx : cacMain_ncpxs) {
					List<Timestamp> list1= new ArrayList();
					list1.add(0,cacMain_ncpx.getEffectiveDate());
					list1.add(1,cacMain_ncpx.getExpireDate());
					//每个续保单起止日期
					bigList.add(list1);
				}
				long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);

				//局部变量
				long l=0;
				Timestamp AfterExpireDate=null;
				//以保单起期，顺序排序，找到第一张续保单
				Util.caStartTimeSort(cacMain_ncpxs);
				//特殊情况：本保单疫情期间起保，疫情期间到期；有一张起保日期＞疫情止期的续保单，续保单起保日期-疫情止期≥N；顺延本保单
				if((cacMain_ncpb.getExpireDate().getTime()<NCPEndDate) && cacMain_ncpxs.get(0).getEffectiveDate().getTime()>NCPEndDate && ((cacMain_ncpxs.get(0).getEffectiveDate().getTime()-NCPEndDate)>NCPValidDate) ){
					//顺延后保单止期
					l=NCPEndDate+(NCPValidDate*86400);
					AfterExpireDate = new Timestamp(l);
					//顺延天数：顺延后保单止期-原保单止期
					long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400;

					//组织参数存库 疫情期顺延后保单信息表
					CACMain_NCPPostpone ncpPostpone = new CACMain_NCPPostpone();
					ncpPostpone.setConfirmSequenceNo(cacMain_ncpb.getConfirmSequenceNo());
					ncpPostpone.setPolicyNo(cacMain_ncpb.getPolicyNo());
					ncpPostpone.setCompanyCode(cacMain_ncpb.getCompanyCode());
					ncpPostpone.setEffectiveDate(cacMain_ncpb.getEffectiveDate());
					ncpPostpone.setExpireDate(cacMain_ncpb.getExpireDate());
					ncpPostpone.setAfterExpireDate(AfterExpireDate);
					Timestamp ncpStartDate = new Timestamp(NCPStartDate);
					ncpPostpone.setNCPStartDate(ncpStartDate);
					Timestamp ncpEndDate = new Timestamp(NCPEndDate);
					ncpPostpone.setNCPEndDate(ncpEndDate);
					ncpPostpone.setNCPValidDate(Integer.parseInt(String.valueOf(NCPValidDate)));
					ncpPostpone.setPostponeDay(Integer.parseInt(String.valueOf(PostponeDay)));
					ncpPostpone.setCityCode(cacMain_ncpb.getCityCode());
					ncpPostpone.setVin(cacMain_ncpb.getVin());
					ncpPostpone.setLicenseNo(cacMain_ncpb.getLicenseNo());
					ncpPostpone.setEngineNo(cacMain_ncpb.getEngineNo());
					ncpPostpone.setBusinessType(cacMain_ncpb.getBusinessType());
					ncpPostpone.setInputDate(new Timestamp(System.currentTimeMillis()));
					ncpPostpone.setValidStatus("1");

				}else{
					//以保单止期，倒序排序，找到最靠后一张续保单
					Util.caEndTimeReverse(cacMain_ncpxs);
					//2种情况：1、最后一张续保单止期>=疫情截止日
					if (cacMain_ncpxs.get(0).getExpireDate().getTime()>=NCPEndDate){
						//顺延后保单止期:最靠后一张续保单止期+疫情有效期
						l=cacMain_ncpxs.get(0).getExpireDate().getTime()+(NCPValidDate*86400);
						AfterExpireDate = new Timestamp(l);
					}else{//2、最后一张续保单止期<疫情截止日
						//顺延后保单止期:疫情止期+疫情有效期
						l=NCPEndDate+(NCPValidDate*86400);
						AfterExpireDate = new Timestamp(l);
					}
					//顺延天数：顺延后保单止期-原最靠后一张续保单止期
					long PostponeDay = (l - cacMain_ncpxs.get(0).getExpireDate().getTime()) / 86400;

					//组织参数存库 疫情期顺延后保单信息表
					CACMain_NCPPostpone ncpPostpone = new CACMain_NCPPostpone();
					ncpPostpone.setConfirmSequenceNo(cacMain_ncpxs.get(0).getConfirmSequenceNo());
					ncpPostpone.setPolicyNo(cacMain_ncpxs.get(0).getPolicyNo());
					ncpPostpone.setCompanyCode(cacMain_ncpxs.get(0).getCompanyCode());
					ncpPostpone.setEffectiveDate(cacMain_ncpxs.get(0).getEffectiveDate());
					ncpPostpone.setExpireDate(cacMain_ncpxs.get(0).getExpireDate());
					ncpPostpone.setAfterExpireDate(AfterExpireDate);
					Timestamp ncpStartDate = new Timestamp(NCPStartDate);
					ncpPostpone.setNCPStartDate(ncpStartDate);
					Timestamp ncpEndDate = new Timestamp(NCPEndDate);
					ncpPostpone.setNCPEndDate(ncpEndDate);
					ncpPostpone.setNCPValidDate(Integer.parseInt(String.valueOf(NCPValidDate)));
					ncpPostpone.setPostponeDay(Integer.parseInt(String.valueOf(PostponeDay)));
					ncpPostpone.setCityCode(cacMain_ncpxs.get(0).getCityCode());
					ncpPostpone.setLastPolicyConfirmNo(cacMain_ncpb.getConfirmSequenceNo());
					ncpPostpone.setVin(cacMain_ncpxs.get(0).getVin());
					ncpPostpone.setLicenseNo(cacMain_ncpxs.get(0).getLicenseNo());
					ncpPostpone.setEngineNo(cacMain_ncpxs.get(0).getEngineNo());
					ncpPostpone.setBusinessType(cacMain_ncpb.getBusinessType());
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
