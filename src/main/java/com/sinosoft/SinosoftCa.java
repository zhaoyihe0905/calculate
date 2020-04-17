package com.sinosoft;

import com.CA.CACCoverage;
import com.CA.CACMain_NCPB;
import com.CA.CACMain_NCPPostpone;
import com.CA.CACMain_NCPX;
import com.CI.IACMain_NCPB;
import com.sinosoft.jdbc.BeanListHandler;
import com.sinosoft.jdbc.CRUDTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JTextArea;

public class SinosoftCa implements SinosoftInterface{
    /**
     * 公共变量定义
     */
    private static long NCPStartDate= 0;
    private static long NCPEndDate= 0;
    private static Date start;
    private static Date end;
    private static JTextArea textArea;
    private static String areaCode;
	/**
	 * 数组拆分单位量
	 */
	private static Integer ThreadCount = 0;
	/**
	 * 线程数量
	 */
	private static Integer ServiceThreadSize = 0;
    /**
     * 线程安全队列 正确处理数据数量统计
     */
    private  static Queue<Integer> queueTag = new ConcurrentLinkedQueue();
    /**
     * 线程安全队列 异常数据数量统计
     */
    private  static Queue<Integer> queueError = new ConcurrentLinkedQueue();
    /**
     * 线程安全队列 险种不处理数据数量统计
     */
    private  static Queue<Integer> queueTag2 = new ConcurrentLinkedQueue();
    public SinosoftCa(Date start, Date end, JTextArea textArea, String areaCode){
        this.start = start;
        this.end = end;
        this.textArea = textArea;
        this.areaCode = areaCode;
        //读取config配置文件
        Properties prop = new Properties();
        InputStream in = MainFrame.class.getClassLoader().getResourceAsStream("com/config/db.properties");
        try {
            prop.load(new InputStreamReader(in,"UTF-8"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ThreadCount = Integer.parseInt(prop.getProperty("ThreadMaxCount"));
		ServiceThreadSize = Integer.parseInt(prop.getProperty("ServiceThreadSize"));
    }

	public void SituationOne() {
		/**
		 * 线程池 100
		 */
		ExecutorService service = Executors.newFixedThreadPool(ServiceThreadSize);

		textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:商业业务类型1，业务计算方法处理开始-----------\n");
		textArea.paintImmediately(textArea.getBounds());
		//获取疫情起止日期
		NCPStartDate = start.getTime();
		NCPEndDate = end.getTime();
		int tag = 0;
		int error = 0;
		//查询IACMain_NCPB-疫情期本保单信息表中的保单信息集合，“保单归属地（地市）-CityCode”“业务类型-BusinessType”、“非延期原因-Reason”、“是否顺延-Flag”三字段作为查询条件进行取值判断
		String selectSql = "select * from CACMain_NCPB where CityCode = ? and BusinessType= ? and Reason = ? and Flag = ?";
		List<CACMain_NCPB> cacMain_ncpbs = (List<CACMain_NCPB>) CRUDTemplate.executeQuery("ca", selectSql, new BeanListHandler(CACMain_NCPB.class), areaCode, "1", "", "");
		if(cacMain_ncpbs==null||cacMain_ncpbs.size()<=0){
			textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:未查询到满足商业业务类型-1-的数据\n");
			textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:商业业务类型1，业务计算方法执行结束-----------\n");
			return;
		}else{
			textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:查询到满足商业业务类型-1-的数据是：【"+cacMain_ncpbs.size()+"】条\n");
		}
		//多线程处理数据
		//数组开始下标
		int n =ThreadCount;
		int count = 0;
		for(int h =0;h<=cacMain_ncpbs.size()/n;h++) {
			List<CACMain_NCPB> list1 = null;
			if (count + n > cacMain_ncpbs.size()) {
				list1 = cacMain_ncpbs.subList(count, cacMain_ncpbs.size());
			} else {
				list1 = cacMain_ncpbs.subList(count, count + n);
			}
			count += n;
			final List<CACMain_NCPB> ThreadList = list1;
			//遍历数据
			service.execute(new Runnable() {
				public void run() {
					int tag = 0;
					int error = 0;
					for (CACMain_NCPB cacMain_ncpb : ThreadList) {
						//拿每个保单的投保确认码，查询IACMain_NCPX-疫情期续保保单信息表中有无续保单，保单止期倒叙排序,根据此情况进行业务判断
						String selectSql1 = "select * from CACMain_NCPX where LastPolicyConfirmNo = ? order by ExpireDate desc";
						List<CACMain_NCPX> cacMain_ncpxs = (List<CACMain_NCPX>) CRUDTemplate.executeQuery("ca", selectSql1, new BeanListHandler(CACMain_NCPX.class), cacMain_ncpb.getConfirmSequenceNo());

						//无续保单存在
						if (cacMain_ncpxs == null || cacMain_ncpxs.size() == 0) {
							try{
								//保单起期<疫情起期，疫情起期<=保单止期<=疫情止期
								List<Timestamp> list = new ArrayList();
								list.add(0, cacMain_ncpb.getEffectiveDate());
								list.add(1, cacMain_ncpb.getExpireDate());
								List<List<Timestamp>> bigList = new ArrayList();
								bigList.add(list);
								long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);
								//局部变量
								long l = 0;
								Timestamp AfterExpireDate = null;

								//本保单顺延后止期=疫情截止日+（本保单止期-疫情起期）
								l = NCPEndDate + 86400000 + (NCPValidDate*86400000);
								AfterExpireDate = new Timestamp(l);
								//顺延天数：顺延后保单止期-原保单止期
								long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400000;
								Timestamp ncpStartDate = new Timestamp(NCPStartDate);
								Timestamp ncpEndDate = new Timestamp(NCPEndDate);
								String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
										" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
										"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
								int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpb.getConfirmSequenceNo(),
										cacMain_ncpb.getPolicyNo(),
										cacMain_ncpb.getCompanyCode(),
										cacMain_ncpb.getEffectiveDate(),
										cacMain_ncpb.getExpireDate(),
										AfterExpireDate,
										ncpStartDate,
										ncpEndDate,
										Integer.parseInt(String.valueOf(NCPValidDate)),
										Integer.parseInt(String.valueOf(PostponeDay)),
										cacMain_ncpb.getCityCode(),
										"",
										"",
										cacMain_ncpb.getVin(),
										cacMain_ncpb.getLicenseNo(),
										cacMain_ncpb.getEngineNo(),
										cacMain_ncpb.getBusinessType(),
										new Timestamp(System.currentTimeMillis()), "","1");
								tag += 1;
							}catch (Exception e){
								textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常\n");
								error+=1;
								e.getMessage();
							}
							//有续保单存在
						}else{
							try {
								//疫情有效期
								List<Timestamp> list = new ArrayList();
								List<List<Timestamp>> bigList = new ArrayList();
								list.add(0, cacMain_ncpb.getEffectiveDate());
								list.add(1, cacMain_ncpb.getExpireDate());
								//本保单起止日期
								bigList.add(list);
								for (CACMain_NCPX cacMain_ncpx : cacMain_ncpxs) {
									List<Timestamp> list2 = new ArrayList();
									list2.add(0, cacMain_ncpx.getEffectiveDate());
									list2.add(1, cacMain_ncpx.getExpireDate());
									//每个续保单起止日期
									bigList.add(list2);
								}
								long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);

								//局部变量
								long l = 0;
								Timestamp AfterExpireDate = null;
								//以保单起期，顺序排序，找到第一张续保单
								Util.caStartTimeSort(cacMain_ncpxs);
								//若第一张续保保单起期小于等于疫情截止日，则获取续保保单中终保日期最靠后一张续保保单，并顺延该保单保险止期。
								if(cacMain_ncpxs.get(0).getEffectiveDate().getTime()<=NCPEndDate){
									Util.caEndTimeReverse(cacMain_ncpxs);
									//若最靠后一张续保保单的止期>=疫情截止日
									if(cacMain_ncpxs.get(0).getExpireDate().getTime()>=NCPEndDate){
										//靠后一张续保保单顺延后止期=原保险止期+疫情期间有效保期
										l = cacMain_ncpxs.get(0).getExpireDate().getTime()+(NCPValidDate * 86400000);
										AfterExpireDate = new Timestamp(l);
										//顺延天数：顺延后保单止期-原保单止期
										long PostponeDay = (l - cacMain_ncpxs.get(0).getExpireDate().getTime()) / 86400000;
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);
										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpxs.get(0).getConfirmSequenceNo(),
												cacMain_ncpxs.get(0).getPolicyNo(),
												cacMain_ncpxs.get(0).getCompanyCode(),
												cacMain_ncpxs.get(0).getEffectiveDate(),
												cacMain_ncpxs.get(0).getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpxs.get(0).getCityCode(),
												cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getCityCode(),
												cacMain_ncpxs.get(0).getVin(),
												cacMain_ncpxs.get(0).getLicenseNo(),
												cacMain_ncpxs.get(0).getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()), "","1");
										tag += 1;
										//若最靠后一张续保保单的止期<疫情截止日
									}else  if(cacMain_ncpxs.get(0).getExpireDate().getTime()<NCPEndDate){
										//最靠后一张续保保单顺延后止期=疫情截止日+疫情期间有效保期
										l = NCPEndDate + 86400000 +(NCPValidDate * 86400000);
										AfterExpireDate = new Timestamp(l);
										//顺延天数：顺延后保单止期-原保单止期
										long PostponeDay = (l - cacMain_ncpxs.get(0).getExpireDate().getTime()) / 86400000;
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);
										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpxs.get(0).getConfirmSequenceNo(),
												cacMain_ncpxs.get(0).getPolicyNo(),
												cacMain_ncpxs.get(0).getCompanyCode(),
												cacMain_ncpxs.get(0).getEffectiveDate(),
												cacMain_ncpxs.get(0).getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpxs.get(0).getCityCode(),
												cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getCityCode(),
												cacMain_ncpxs.get(0).getVin(),
												cacMain_ncpxs.get(0).getLicenseNo(),
												cacMain_ncpxs.get(0).getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()), "", "1");
										tag += 1;
									}
									//若第一张续保保单起期大于疫情截止日，获取续保保单中最靠前一张保单起期，判断（最靠前续保保单起期-疫情截止日天数）与疫情期间有效保期大小
								}else if(cacMain_ncpxs.get(0).getEffectiveDate().getTime()>NCPEndDate){
									//最靠前续保保单起期-疫情截止日天数
									long days = (cacMain_ncpxs.get(0).getEffectiveDate().getTime()-NCPEndDate)/86400000;
									if(days>=NCPValidDate){
										//顺延本保单保险止期
										//本保单顺延后止期=疫情截止日+疫情期间有效保期。
										l = NCPEndDate + 86400000 + (NCPValidDate*86400000);
										AfterExpireDate = new Timestamp(l);
										//顺延天数：顺延后保单止期-原保单止期
										long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400000;
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);
										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getPolicyNo(),
												cacMain_ncpb.getCompanyCode(),
												cacMain_ncpb.getEffectiveDate(),
												cacMain_ncpb.getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpb.getCityCode(),
                                                "",
												"",
												cacMain_ncpb.getVin(),
												cacMain_ncpb.getLicenseNo(),
												cacMain_ncpb.getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()), "","1");
										tag += 1;
										//（续保保单起期-疫情截止日天数）<=疫情期间有效保期 顺延续保保单中最靠后一张续保保单的止期
									}else if(days<NCPValidDate){
										Util.caEndTimeReverse(cacMain_ncpxs);
										//最靠后一张续保保单顺延后止期=原保险止期+疫情期间有效保期
										l = cacMain_ncpxs.get(0).getExpireDate().getTime() +(NCPValidDate*86400000);
										AfterExpireDate = new Timestamp(l);
										//顺延天数：顺延后保单止期-原保单止期
										long PostponeDay = (l - cacMain_ncpxs.get(0).getExpireDate().getTime()) / 86400000;
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);
										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpxs.get(0).getConfirmSequenceNo(),
												cacMain_ncpxs.get(0).getPolicyNo(),
												cacMain_ncpxs.get(0).getCompanyCode(),
												cacMain_ncpxs.get(0).getEffectiveDate(),
												cacMain_ncpxs.get(0).getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpxs.get(0).getCityCode(),
												cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getCityCode(),
												cacMain_ncpxs.get(0).getVin(),
												cacMain_ncpxs.get(0).getLicenseNo(),
												cacMain_ncpxs.get(0).getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()),"","1");
										tag += 1;
									}
								}
							}catch (Exception e){
								textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常\n");
								error+=1;
								e.getMessage();
							}
						}
					}
					queueTag.add(tag);
					queueError.add(error);
				}
			});
		}
		service.shutdown();
		while(true){
			if(service.isTerminated()){
				break;
			}

		}
		//统计数量
		while(queueTag.peek()!=null){
			tag +=queueTag.poll();
		}
		while(queueError.peek()!=null){
			error +=queueError.poll();
		}
		textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:商业业务类型1，业务计算方法处理结束-----------处理数据量：" + tag + "异常数据量："+error+"\n");
		textArea.paintImmediately(textArea.getBounds());
	}

	public void SituationTwo() {
		/**
		 * 线程池 100
		 */
		ExecutorService service = Executors.newFixedThreadPool(ServiceThreadSize);
		if((start!=null||start.getTime()!=0)&&(end!=null||end.getTime()!=0)&&(areaCode!=null||!areaCode.equals(""))) {
			textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:商业业务类型2，业务计算方法处理开始-----------"+"\n");
			textArea.paintImmediately(textArea.getBounds());
			NCPStartDate = start.getTime();
			NCPEndDate = end.getTime();
			int tag = 0;
			int error = 0;
			//查询IACMain_NCPB-疫情期本保单信息表中的保单信息集合，“保单归属地（地市）-CityCode”“业务类型-BusinessType”、“非延期原因-Reason”、“是否顺延-Flag”三字段作为查询条件进行取值判断
			String selectSql = "select * from CACMain_NCPB where CityCode = ? and BusinessType= ? and Reason = ? and Flag = ?";
			List<CACMain_NCPB> cacMain_ncpbs = (List<CACMain_NCPB>) CRUDTemplate.executeQuery("ca", selectSql, new BeanListHandler(CACMain_NCPB.class), areaCode, "2", "", "");
			if(cacMain_ncpbs == null || cacMain_ncpbs.size() == 0){
				textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:未查询到满足商业业务类型-2-的数据\n");
				textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:商业业务类型2，业务计算方法执行结束-----------\n");
				return;
			}else{
				textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:查询到满足商业业务类型-2-的数据是：【"+cacMain_ncpbs.size()+"】条\n");
			}
			//多线程处理数据
			//数组开始下标
			int n =ThreadCount;
			int count = 0;
			for(int h =0;h<=cacMain_ncpbs.size()/n;h++) {
				List<CACMain_NCPB> list1 = null;
				if (count + n > cacMain_ncpbs.size()) {
					list1 = cacMain_ncpbs.subList(count, cacMain_ncpbs.size());
				} else {
					list1 = cacMain_ncpbs.subList(count, count + n);
				}
				count += n;
				final List<CACMain_NCPB> ThreadList = list1;


				service.execute(new Runnable() {
					public void run() {
						int tag = 0;
						int error = 0;
						//遍历数据
						for (CACMain_NCPB cacMain_ncpb : ThreadList) {
							//拿每个保单的投保确认码，查询IACMain_NCPX-疫情期续保保单信息表中有无续保单，保单止期倒叙排序,根据此情况进行业务判断
							String selectSql1 = "select * from CACMain_NCPX where LastPolicyConfirmNo = ? order by ExpireDate desc";
							List<CACMain_NCPX> cacMain_ncpxs = (List<CACMain_NCPX>) CRUDTemplate.executeQuery("ca", selectSql1, new BeanListHandler(CACMain_NCPX.class), cacMain_ncpb.getConfirmSequenceNo());

							//判断:一、无续保单
							if (cacMain_ncpxs == null || cacMain_ncpxs.size() == 0) {
								try {
									//疫情有效期
									List<Timestamp> list = new ArrayList();
									list.add(0, cacMain_ncpb.getEffectiveDate());
									list.add(1, cacMain_ncpb.getExpireDate());
									List<List<Timestamp>> bigList = new ArrayList();
									bigList.add(list);
									long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);
									//局部变量
									long l = 0;
									Timestamp AfterExpireDate = null;

									//两种情况:1.保单保险止期小于疫情截止日
									if (cacMain_ncpb.getExpireDate().getTime() < NCPEndDate) {
										//顺延后保单止期:疫情止期+疫情有效期
										l = NCPEndDate + 86400000 + (NCPValidDate * 86400000);
										AfterExpireDate = new Timestamp(l);

									} else {//2.保单保险止期>=疫情截止日
										//顺延后保单止期:原保单止期+疫情有效期
										l = cacMain_ncpb.getExpireDate().getTime() + (NCPValidDate * 86400000);
										AfterExpireDate = new Timestamp(l);
									}
									//顺延天数：顺延后保单止期-原保单止期
									long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400000;
									//疫情起期
									Timestamp ncpStartDate = new Timestamp(NCPStartDate);
									//疫情止期
									Timestamp ncpEndDate = new Timestamp(NCPEndDate);

									String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
											" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
											"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
									int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpb.getConfirmSequenceNo(),
											cacMain_ncpb.getPolicyNo(),
											cacMain_ncpb.getCompanyCode(),
											cacMain_ncpb.getEffectiveDate(),
											cacMain_ncpb.getExpireDate(),
											AfterExpireDate,
											ncpStartDate,
											ncpEndDate,
											Integer.parseInt(String.valueOf(NCPValidDate)),
											Integer.parseInt(String.valueOf(PostponeDay)),
											cacMain_ncpb.getCityCode(),
											"",
											"",
											cacMain_ncpb.getVin(),
											cacMain_ncpb.getLicenseNo(),
											cacMain_ncpb.getEngineNo(),
											cacMain_ncpb.getBusinessType(),
											new Timestamp(System.currentTimeMillis()), "","1");
									tag += 1;

								} catch(Exception e) {
									textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
									error+=1;
									e.getMessage();
								}

							} else {  //二、有续保单

								//疫情有效期
								List<Timestamp> list = new ArrayList();
								List<List<Timestamp>> bigList = new ArrayList();
								list.add(0, cacMain_ncpb.getEffectiveDate());
								list.add(1, cacMain_ncpb.getExpireDate());
								//本保单起止日期
								bigList.add(list);
								for (CACMain_NCPX cacMain_ncpx : cacMain_ncpxs) {
									List<Timestamp> list2 = new ArrayList();
									list2.add(0, cacMain_ncpx.getEffectiveDate());
									list2.add(1, cacMain_ncpx.getExpireDate());
									//每个续保单起止日期
									bigList.add(list2);
								}
								long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);

								//局部变量
								long l = 0;
								Timestamp AfterExpireDate = null;
								//以保单起期，顺序排序，找到第一张续保单
								Util.caStartTimeSort(cacMain_ncpxs);
								//特殊情况：1、本保单疫情期间起保，疫情期间到期；有一张起保日期＞疫情止期的续保单，续保单起保日期-疫情止期≥N；顺延本保单
								if (cacMain_ncpb.getExpireDate().getTime() <= NCPEndDate && cacMain_ncpxs.get(0).getEffectiveDate().getTime() > NCPEndDate && (((cacMain_ncpxs.get(0).getEffectiveDate().getTime() - NCPEndDate)/ 86400000) >= NCPValidDate)) {
									try {
										//顺延后保单止期
										l = NCPEndDate + 86400000 + (NCPValidDate * 86400000);
										AfterExpireDate = new Timestamp(l);
										//顺延天数：顺延后保单止期-原保单止期
										long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400000;
										//疫情起期
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										//疫情止期
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);

										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getPolicyNo(),
												cacMain_ncpb.getCompanyCode(),
												cacMain_ncpb.getEffectiveDate(),
												cacMain_ncpb.getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpb.getCityCode(),
												"",
												"",
												cacMain_ncpb.getVin(),
												cacMain_ncpb.getLicenseNo(),
												cacMain_ncpb.getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()), "","1");
										tag += 1;

									} catch(Exception e) {
										textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
										error+=1;
										e.getMessage();
									}
                                   //2、本保单疫情期间起保，本保单止期>疫情截止日；有一张起保日期＞本保单止期的续保单，续保单起保日期-本保单止期≥N；顺延本保单
								} else if((cacMain_ncpb.getExpireDate().getTime() > NCPEndDate && cacMain_ncpxs.get(0).getEffectiveDate().getTime() > cacMain_ncpb.getExpireDate().getTime() && (((cacMain_ncpxs.get(0).getEffectiveDate().getTime() - cacMain_ncpb.getExpireDate().getTime())/ 86400000) >= NCPValidDate))){
								    try {
                                        //顺延后保单止期
                                        l = cacMain_ncpb.getExpireDate().getTime() + (NCPValidDate * 86400000);
                                        AfterExpireDate = new Timestamp(l);
                                        //顺延天数：顺延后保单止期-原保单止期
                                        long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400000;
                                        //疫情起期
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        //疫情止期
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);

                                        String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpb.getConfirmSequenceNo(),
                                                cacMain_ncpb.getPolicyNo(),
                                                cacMain_ncpb.getCompanyCode(),
                                                cacMain_ncpb.getEffectiveDate(),
                                                cacMain_ncpb.getExpireDate(),
                                                AfterExpireDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                cacMain_ncpb.getCityCode(),
                                                "",
                                                "",
                                                cacMain_ncpb.getVin(),
                                                cacMain_ncpb.getLicenseNo(),
                                                cacMain_ncpb.getEngineNo(),
                                                cacMain_ncpb.getBusinessType(),
                                                new Timestamp(System.currentTimeMillis()), "","1");
                                        tag += 1;
								    } catch(Exception e) {
                                        textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
                                        error+=1;
                                        e.getMessage();
								    }
                                }else{
									try {
										//以保单止期，倒序排序，找到最靠后一张续保单
										Util.caEndTimeReverse(cacMain_ncpxs);
										//2种情况：1、最后一张续保单止期>=疫情截止日
										if (cacMain_ncpxs.get(0).getExpireDate().getTime() >= NCPEndDate) {
											//顺延后保单止期:最靠后一张续保单止期+疫情有效期
											l = cacMain_ncpxs.get(0).getExpireDate().getTime() + (NCPValidDate * 86400000);
											AfterExpireDate = new Timestamp(l);
										} else {//2、最后一张续保单止期<疫情截止日
											//顺延后保单止期:疫情止期+疫情有效期
											l = NCPEndDate + 86400000 + (NCPValidDate * 86400000);
											AfterExpireDate = new Timestamp(l);
										}
										//顺延天数：顺延后保单止期-原最靠后一张续保单止期
										long PostponeDay = (l - cacMain_ncpxs.get(0).getExpireDate().getTime()) / 86400000;
										//疫情起期
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										//疫情止期
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);

										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpxs.get(0).getConfirmSequenceNo(),
												cacMain_ncpxs.get(0).getPolicyNo(),
												cacMain_ncpxs.get(0).getCompanyCode(),
												cacMain_ncpxs.get(0).getEffectiveDate(),
												cacMain_ncpxs.get(0).getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpxs.get(0).getCityCode(),
												cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getCityCode(),
												cacMain_ncpxs.get(0).getVin(),
												cacMain_ncpxs.get(0).getLicenseNo(),
												cacMain_ncpxs.get(0).getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()), "","1");
										tag += 1;

									} catch(Exception e) {
										textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
										error+=1;
										e.getMessage();
									}

								}

							}

						}
						queueTag.add(tag);
						queueError.add(error);
					}
				});

			}
			service.shutdown();
			while(true){
				if(service.isTerminated()){
					break;
				}
			}
			//统计数量
			while(queueTag.peek()!=null){
				tag +=queueTag.poll();
			}
			while(queueError.peek()!=null){
				error +=queueError.poll();
			}
			textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:商业业务类型2，业务计算方法处理结束-----------处理数据量：" + tag + "异常数据量："+error+"\n");
		}else {
			textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:业务计算参数为空，请选择必要参数"+"\n");
		}
		textArea.paintImmediately(textArea.getBounds());
	}

	public void SituationTree() {
		/**
		 * 线程池 100
		 */
		ExecutorService service = Executors.newFixedThreadPool(ServiceThreadSize);
        if((start!=null||start.getTime()!=0)&&(end!=null||end.getTime()!=0)&&(areaCode!=null||!areaCode.equals(""))) {
            textArea.append("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]:业务类型3，业务计算方法处理开始-----------"+"\n");
			textArea.paintImmediately(textArea.getBounds());
            //疫情起期
            NCPStartDate = start.getTime();
            //疫情止期
            NCPEndDate = end.getTime();
            int tag =0;
            int error = 0;
            //疫情有效保单天数
            long NCPValidDate1 = 0;
            try {
                NCPValidDate1 = Util.Calculate(start, end);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final long NCPValidDate = NCPValidDate1;
            //查询CACMain_NCPB-疫情期本保单信息表中的保单信息集合，“保单归属地（地市）-CityCode”“业务类型-BusinessType”、“非延期原因-Reason”、“是否顺延-Flag”三字段作为查询条件进行取值判断
            //模拟获取到的本保单集合
            String CACMain_NCPBsql ="select * from CACMain_NCPB where CityCode = ? and BusinessType= ? and Reason = ? and Flag = ?";
            List<CACMain_NCPB> cacMain_ncpbs = (List<CACMain_NCPB>) CRUDTemplate.executeQuery("ca",CACMain_NCPBsql, new BeanListHandler(CACMain_NCPB.class), areaCode, "3", "","");
            if(cacMain_ncpbs==null||cacMain_ncpbs.size()<=0){
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:未查询到满足商业业务类型-3-的数据\n");
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:商业业务类型3，业务计算方法执行结束-----------\n");
                return;
            }else{
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:查询到满足商业业务类型-3-的数据是：【"+cacMain_ncpbs.size()+"】条\n");
            }
			//多线程处理数据
			//数组开始下标
			int n =ThreadCount;
			int count = 0;
			for(int h =0;h<=cacMain_ncpbs.size()/n;h++) {
				List<CACMain_NCPB> list1 = null;
				if (count + n > cacMain_ncpbs.size()) {
					list1 = cacMain_ncpbs.subList(count, cacMain_ncpbs.size());
				} else {
					list1 = cacMain_ncpbs.subList(count, count + n);
				}
				count += n;
				final List<CACMain_NCPB> ThreadList = list1;
				service.execute(new Runnable() {
					public void run() {
						int tag = 0;
						int error = 0;
						//遍历数据
						for (CACMain_NCPB cacMain_ncpb : ThreadList) {
							//拿每个保单的投保确认码，查询CACMain_NCPX-疫情期续保保单信息表中有无续保单，根据此情况进行业务判断
							//模拟获取到的续保单集合，此保单集合根据保单止期，倒叙排序，
							String CACMain_NCPXsql ="select * from CACMain_NCPX where LastPolicyConfirmNo = ? order by ExpireDate desc";
							List<CACMain_NCPX> cacMain_ncpxs = (List<CACMain_NCPX>)CRUDTemplate.executeQuery("ca",CACMain_NCPXsql, new BeanListHandler(CACMain_NCPX.class), cacMain_ncpb.getConfirmSequenceNo());
							//判断:一、无续保单
							if (cacMain_ncpxs == null||cacMain_ncpxs.size()==0) {
								try {
									//局部变量
									long l=0l;
									//顺延后保单保险止期
									Timestamp AfterExpireDate=null;
									l=cacMain_ncpb.getExpireDate().getTime()+(NCPValidDate*86400000);
									AfterExpireDate = new Timestamp(l);

									//顺延天数：顺延后保单止期-原保单止期
									long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400000;

									//组织参数存库 疫情期顺延后保单信息表
									Timestamp ncpStartDate = new Timestamp(NCPStartDate);
									Timestamp ncpEndDate = new Timestamp(NCPEndDate);
									String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
											" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
											"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
									int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpb.getConfirmSequenceNo(),
											cacMain_ncpb.getPolicyNo(),
											cacMain_ncpb.getCompanyCode(),
											cacMain_ncpb.getEffectiveDate(),
											cacMain_ncpb.getExpireDate(),
											AfterExpireDate,
											ncpStartDate,
											ncpEndDate,
											Integer.parseInt(String.valueOf(NCPValidDate)),
											Integer.parseInt(String.valueOf(PostponeDay)),
											cacMain_ncpb.getCityCode(),
											"",
											"",
											cacMain_ncpb.getVin(),
											cacMain_ncpb.getLicenseNo(),
											cacMain_ncpb.getEngineNo(),
											cacMain_ncpb.getBusinessType(),
											new Timestamp(System.currentTimeMillis()), "","1");
									tag += 1;
								}catch(Exception e) {
									textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
									error+=1;
									e.getMessage();
								}

							}else{  //二、有续保单
								//局部变量
								long l=0;
								Timestamp AfterExpireDate=null;
								//以保单起期，顺序排序，找到第一张续保单
								Util.caStartTimeSort(cacMain_ncpxs);
								//特殊情况：续保单起保日期-本保单止期>=N；顺延本保单
								if(((cacMain_ncpxs.get(0).getEffectiveDate().getTime()-cacMain_ncpb.getExpireDate().getTime())/86400000)>=NCPValidDate ){
									try {
										//顺延后保单止期
										l=cacMain_ncpb.getExpireDate().getTime()+(NCPValidDate*86400000);
										AfterExpireDate = new Timestamp(l);
										//顺延天数：顺延后保单止期-原保单止期
										long PostponeDay = (l - cacMain_ncpb.getExpireDate().getTime()) / 86400000;

										//组织参数存库 疫情期顺延后保单信息表
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);
										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getPolicyNo(),
												cacMain_ncpb.getCompanyCode(),
												cacMain_ncpb.getEffectiveDate(),
												cacMain_ncpb.getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpb.getCityCode(),
												"",
												"",
												cacMain_ncpb.getVin(),
												cacMain_ncpb.getLicenseNo(),
												cacMain_ncpb.getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()), "","1");
										tag += 1;
									}catch(Exception e) {
										textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
										error+=1;
										e.getMessage();
									}

								}else{
									try {
										//以保单止期，倒序排序，找到最靠后一张续保单
										Util.caEndTimeReverse(cacMain_ncpxs);
										//顺延后保单止期:最靠后一张续保单止期+疫情有效期
										l=cacMain_ncpxs.get(0).getExpireDate().getTime()+(NCPValidDate*86400000);
										AfterExpireDate = new Timestamp(l);

										//顺延天数：顺延后保单止期-原最靠后一张续保单止期
										long PostponeDay = (l - cacMain_ncpxs.get(0).getExpireDate().getTime()) / 86400000;

										//组织参数存库 疫情期顺延后保单信息表
										Timestamp ncpStartDate = new Timestamp(NCPStartDate);
										Timestamp ncpEndDate = new Timestamp(NCPEndDate);
										String insertSql = "insert into CACMain_NCPPostpone(ConfirmSequenceNo,PolicyNo,CompanyCode,EffectiveDate,ExpireDate,AfterExpireDate,NCPStartDate,\n" +
												" NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPolicyConfirmNo,LastCityCode,Vin,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
												"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
										int i = CRUDTemplate.executeUpdate("ca", insertSql, cacMain_ncpxs.get(0).getConfirmSequenceNo(),
												cacMain_ncpxs.get(0).getPolicyNo(),
												cacMain_ncpxs.get(0).getCompanyCode(),
												cacMain_ncpxs.get(0).getEffectiveDate(),
												cacMain_ncpxs.get(0).getExpireDate(),
												AfterExpireDate,
												ncpStartDate,
												ncpEndDate,
												Integer.parseInt(String.valueOf(NCPValidDate)),
												Integer.parseInt(String.valueOf(PostponeDay)),
												cacMain_ncpxs.get(0).getCityCode(),
												cacMain_ncpb.getConfirmSequenceNo(),
												cacMain_ncpb.getCityCode(),
												cacMain_ncpxs.get(0).getVin(),
												cacMain_ncpxs.get(0).getLicenseNo(),
												cacMain_ncpxs.get(0).getEngineNo(),
												cacMain_ncpb.getBusinessType(),
												new Timestamp(System.currentTimeMillis()), "","1");
										tag += 1;
									}catch(Exception e) {
										textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
										error+=1;
										e.getMessage();
									}
								}

							}

						}
						queueTag.add(tag);
						queueError.add(error);
					}
				});
			}
			service.shutdown();
			while(true){
				if(service.isTerminated()){
					break;
				}

			}
			//统计数量
			while(queueTag.peek()!=null){
				tag +=queueTag.poll();
			}
			while(queueError.peek()!=null){
				error +=queueError.poll();
			}
            textArea.append("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]:业务类型3，业务计算方法处理结束-----------处理数据量："+tag + "异常数据量："+error+"\n");

        }else {
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:业务计算参数为空，请选择必要参数"+"\n");
        }
		textArea.paintImmediately(textArea.getBounds());
	}

	public void SituationFour(){
		/**
		 * 线程池 100
		 */
		ExecutorService service = Executors.newFixedThreadPool(ServiceThreadSize);
        textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:险种顺延业务，计算方法处理开始-----------\n");
        textArea.paintImmediately(textArea.getBounds());
        //查询顺延保单数据开始
		String sq ="select * from CACMain_NCPPostpone where Flag != ?";
		List<CACMain_NCPPostpone> NCPPostpone = (List<CACMain_NCPPostpone>)CRUDTemplate.executeQuery("ca", sq, new BeanListHandler(CACMain_NCPPostpone.class), "1");
        //查询保单数据结束
		if (NCPPostpone == null || NCPPostpone.size()==0) {
			textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:险种顺延业务，未查询到顺延保单表信息-----------\n");
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:险种顺延业务，计算方法处理结束-----------\n");
			return;
		}else{
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:查询到符合条件的顺延保单表的数据是：【"+NCPPostpone.size()+"】条\n");
        }
        int tag =0;
        int error = 0;
        int tag2 = 0;
        //多线程处理数据
        //数组开始下标
        int n =ThreadCount;
        int count =0;
		for(int h =0;h<=NCPPostpone.size()/n;h++) {
			List<CACMain_NCPPostpone> list = null;
			if (count + n > NCPPostpone.size()) {
				list = NCPPostpone.subList(count, NCPPostpone.size());
			} else {
				list = NCPPostpone.subList(count, count + n);
			}
			count += n;
            final List<CACMain_NCPPostpone> threadList = list;
            service.execute(new Runnable() {
                public void run() {
                    int tag = 0;
                    int error = 0;
                    int tag2 = 0;
						for (CACMain_NCPPostpone cacMain_ncpPostpone : threadList) {
                            boolean flag =true;
							String sql="select ConfirmSequenceNo,CompanyCode,CoverageCode,EffectiveDate,ExpireDate from CACCoverage where ConfirmSequenceNo = ? ";
							List<CACCoverage> cacCoverages = (List<CACCoverage>)CRUDTemplate.executeQuery("ca", sql, new BeanListHandler(CACCoverage.class), cacMain_ncpPostpone.getConfirmSequenceNo());
							if (cacCoverages != null && cacCoverages.size()!=0) {
								for (CACCoverage cacCoverage : cacCoverages) {
									if (cacCoverage.getExpireDate().compareTo(cacMain_ncpPostpone.getExpireDate())==0){
										String insertSql = "insert into CACCoverage_NCPPostpone(ConfirmSequenceNo,CompanyCode,CoverageCode,EffectiveDate,ExpireDate,AfterExpireDate,InputDate,ValidStatus) values(?,?,?,?,?,?,?,?)";
										try {
                                            CRUDTemplate.executeUpdate("ca",insertSql,
                                                    cacCoverage.getConfirmSequenceNo(),
                                                    cacCoverage.getCompanyCode(),
                                                    cacCoverage.getCoverageCode(),
                                                    cacCoverage.getEffectiveDate(),
                                                    cacCoverage.getExpireDate(),
                                                    cacMain_ncpPostpone.getAfterExpireDate(),
                                                    new Timestamp(System.currentTimeMillis()), "1");

                                            flag = false;

										} catch(Exception e) {
										    error+=1;
										    e.getMessage();
										}

									}
								}
							}
							if(flag){
                                tag2 +=1;
                            }
                            if (!flag){
                                tag+=1;
                            }
						}

                    queueTag.add(tag);
                    queueError.add(error);
                    queueTag2.add(tag2);
                }
            });

        }
		service.shutdown();
		while(true){
			if(service.isTerminated()){
				break;
			}

		}
        //统计数量
        while(queueTag.peek()!=null){
            tag +=queueTag.poll();
        }
        while(queueError.peek()!=null){
            error +=queueError.poll();
        }
        while(queueTag2.peek()!=null){
		    tag2+=queueTag2.poll();
        }
		textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:险种顺延业务，计算方法处理结束----处理数据量：" + tag + "不处理数据量："+tag2+"异常数据量：" + error + "\n");
        textArea.paintImmediately(textArea.getBounds());
    }
}
