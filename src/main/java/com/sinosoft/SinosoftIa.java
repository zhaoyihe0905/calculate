package com.sinosoft;

import com.CI.IACMain_NCPB;
import com.CI.IACMain_NCPPostpone;
import com.CI.IACMain_NCPX;
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

public class SinosoftIa implements SinosoftInterface{
    /**
     * 公共变量定义
     */
    private static long NCPStartDate= 0;
    private static long NCPEndDate= 0;
    private static Date start;
    private static Date end;
    private static JTextArea textArea;
    private static String areaCode;

    private static Integer ThreadCount = 0;

    /**
     * 线程安全队列 正确处理数据数量统计
     */
    private  static Queue<Integer> queueTag = new ConcurrentLinkedQueue();
    /**
     * 线程安全队列 异常数据数量统计
     */
    private  static Queue<Integer> queueError = new ConcurrentLinkedQueue();

    public SinosoftIa(Date start, Date end, JTextArea textArea, String areaCode){
        this.start = start;
        this.end = end;
        this.textArea = textArea;
        this.areaCode = areaCode;
        //读取config配置文件
        Properties prop = new Properties();
        InputStream in = MainFrame.class.getClassLoader().getResourceAsStream("com/config/config.properties");
        try {
            prop.load(new InputStreamReader(in,"UTF-8"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ThreadCount = Integer.parseInt(prop.getProperty("ThreadMaxCount"));
    }

    public void SituationOne() {
        /**
         * 线程池 100
         */
        ExecutorService service = Executors.newFixedThreadPool(100);

        textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:交强业务类型1，业务计算方法处理开始-----------\n");
//        textArea.paintImmediately(textArea.getBounds());
        //获取疫情起止日期
		NCPStartDate = start.getTime();
        NCPEndDate = end.getTime();
        int tag =0;
        int error = 0;
        //查询满足条件的疫情期本保单信息数据
        String IACMain_NCPBsql = "select * from IACMain_NCPB where CityCode = ? and BusinessType= ? and Reason = ? and Flag = ?";
        List<IACMain_NCPB> iacMain_ncpbs = (List<IACMain_NCPB>) CRUDTemplate.executeQuery("ci", IACMain_NCPBsql, new BeanListHandler(IACMain_NCPB.class), areaCode, "1", "", "");
        if(iacMain_ncpbs.size()<=0){
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:未查询到满足交强业务类型-1-的数据\n");
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:交强业务类型1，业务计算方法执行结束-----------\n");
            return;
        }else{
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:查询到满足交强业务类型-1-的数据是：【"+iacMain_ncpbs.size()+"】条\n");
        }
        //多线程处理数据
        //数组开始下标
        int n =ThreadCount;
        int count = 0;
        for(int i =0;i<=iacMain_ncpbs.size()/n;i++){
             List<IACMain_NCPB> list =null;
            if(count+n>iacMain_ncpbs.size()){
                list = iacMain_ncpbs.subList(count,iacMain_ncpbs.size());
            }else{
                list = iacMain_ncpbs.subList(count,count+n);
            }
            count+=10;
            final List<IACMain_NCPB> ThreadList = list;
            service.execute(new Runnable() {
                public void run() {
                    int flag = 0;
                    int err = 0;
                    //遍历数据
                    for (IACMain_NCPB iacMain_ncpb : ThreadList) {
                        //拿每个保单的投保确认码，查询IACMain_NCPX-疫情期续保保单信息表中有无续保单，止期倒叙排序，根据此情况进行业务判断
                        String selectSql1 = "select * from IACMain_NCPX where LastPoliConfirmNo = ? order by enddate desc";
                        List<IACMain_NCPX> iacMain_ncpxs = (List<IACMain_NCPX>) CRUDTemplate.executeQuery("ci", selectSql1, new BeanListHandler(IACMain_NCPX.class), iacMain_ncpb.getPolicyConfirmNo());

                        //无续保单存在
                        if (iacMain_ncpxs == null || iacMain_ncpxs.size() == 0) {
                            try{
                                List<Timestamp> list = new ArrayList();
                                list.add(0, iacMain_ncpb.getStartDate());
                                list.add(1, iacMain_ncpb.getEndDate());
                                List<List<Timestamp>> bigList = new ArrayList();
                                bigList.add(list);
                                long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);
                                //局部变量
                                long l = 0;
                                Timestamp AfterEndDate = null;

                                //本保单顺延后止期=疫情截止日+（本保单止期-疫情起期）
                                l = NCPEndDate + (iacMain_ncpb.getEndDate().getTime()-NCPStartDate);
                                AfterEndDate = new Timestamp(l);
                                //顺延天数：顺延后保单止期-原保单止期
                                long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400000;
                                Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                        " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                        "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpb.getPolicyConfirmNo(),
                                        iacMain_ncpb.getPolicyNo(),
                                        iacMain_ncpb.getCompanyCode(),
                                        iacMain_ncpb.getStartDate(),
                                        iacMain_ncpb.getEndDate(),
                                        AfterEndDate,
                                        ncpStartDate,
                                        ncpEndDate,
                                        Integer.parseInt(String.valueOf(NCPValidDate)),
                                        Integer.parseInt(String.valueOf(PostponeDay)),
                                        iacMain_ncpb.getCityCode(),
                                        "",
                                        iacMain_ncpb.getFrameNo(),
                                        iacMain_ncpb.getLicenseNo(),
                                        iacMain_ncpb.getEngineNo(),
                                        iacMain_ncpb.getBusinessType(),
                                        new Timestamp(System.currentTimeMillis()),"","1");
                                flag += 1;
                            }catch (Exception e){
                                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常\n");
                                err+=1;
                                e.getMessage();
                            }
                            //有续保单存在
                        }else{
                            try {
                                //疫情有效期
                                List<Timestamp> list = new ArrayList();
                                List<List<Timestamp>> bigList = new ArrayList();
                                list.add(0, iacMain_ncpb.getStartDate());
                                list.add(1, iacMain_ncpb.getEndDate());
                                //本保单起止日期
                                bigList.add(list);
                                for (IACMain_NCPX iacMain_ncpx : iacMain_ncpxs) {
                                    List<Timestamp> list1 = new ArrayList();
                                    list1.add(0, iacMain_ncpx.getStartDate());
                                    list1.add(1, iacMain_ncpx.getEndDate());
                                    //每个续保单起止日期
                                    bigList.add(list1);
                                }
                                long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);

                                //局部变量
                                long l = 0;
                                Timestamp AfterEndDate = null;
                                //以保单起期，顺序排序，找到第一张续保单
                                Util.ciStartTimeSort(iacMain_ncpxs);
                                //若第一张续保保单起期小于等于疫情截止日，则获取续保保单中终保日期最靠后一张续保保单，并顺延该保单保险止期。
                                if(iacMain_ncpxs.get(0).getStartDate().getTime()<=NCPEndDate){
                                    Util.ciEndTimeReverse(iacMain_ncpxs);
                                    //若最靠后一张续保保单的止期>=疫情截止日
                                    if(iacMain_ncpxs.get(0).getEndDate().getTime()>=NCPEndDate){
                                        //靠后一张续保保单顺延后止期=原保险止期+疫情期间有效保期
                                        l = iacMain_ncpxs.get(0).getEndDate().getTime()+(NCPValidDate * 86400000);
                                        AfterEndDate = new Timestamp(l);
                                        //顺延天数：顺延后保单止期-原保单止期
                                        long PostponeDay = (l - iacMain_ncpxs.get(0).getEndDate().getTime()) / 86400000;
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpxs.get(0).getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getPolicyNo(),
                                                iacMain_ncpxs.get(0).getCompanyCode(),
                                                iacMain_ncpxs.get(0).getStartDate(),
                                                iacMain_ncpxs.get(0).getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpxs.get(0).getCityCode(),
                                                iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getFrameNo(),
                                                iacMain_ncpxs.get(0).getLicenseNo(),
                                                iacMain_ncpxs.get(0).getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
                                                new Timestamp(System.currentTimeMillis()),"","1");

                                        flag += 1;
                                        //若最靠后一张续保保单的止期<疫情截止日
                                    }else  if(iacMain_ncpxs.get(0).getEndDate().getTime()<NCPEndDate){
                                        //最靠后一张续保保单顺延后止期=疫情截止日+疫情期间有效保期
                                        l = NCPEndDate +(NCPValidDate * 86400000);
                                        AfterEndDate = new Timestamp(l);
                                        //顺延天数：顺延后保单止期-原保单止期
                                        long PostponeDay = (l - iacMain_ncpxs.get(0).getEndDate().getTime()) / 86400000;
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpxs.get(0).getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getPolicyNo(),
                                                iacMain_ncpxs.get(0).getCompanyCode(),
                                                iacMain_ncpxs.get(0).getStartDate(),
                                                iacMain_ncpxs.get(0).getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpxs.get(0).getCityCode(),
                                                iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getFrameNo(),
                                                iacMain_ncpxs.get(0).getLicenseNo(),
                                                iacMain_ncpxs.get(0).getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
                                                new Timestamp(System.currentTimeMillis()),"","1");
                                        flag += 1;
                                    }
                                    //若第一张续保保单起期大于疫情截止日，获取续保保单中最靠前一张保单起期，判断（最靠前续保保单起期-疫情截止日天数）与疫情期间有效保期大小
                                }else if(iacMain_ncpxs.get(0).getStartDate().getTime()>NCPEndDate){
                                    //最靠前续保保单起期-疫情截止日天数
                                    long days = (iacMain_ncpxs.get(0).getStartDate().getTime()-NCPEndDate)/86400000;
                                    if(days>=NCPValidDate){
                                        //顺延本保单保险止期
                                        //本保单顺延后止期=疫情截止日+疫情期间有效保期。
                                        l = NCPEndDate + (NCPValidDate*86400000);
                                        AfterEndDate = new Timestamp(l);
                                        //顺延天数：顺延后保单止期-原保单止期
                                        long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400000;
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpb.getPolicyNo(),
                                                iacMain_ncpb.getCompanyCode(),
                                                iacMain_ncpb.getStartDate(),
                                                iacMain_ncpb.getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpb.getCityCode(),
                                                "",
                                                iacMain_ncpb.getFrameNo(),
                                                iacMain_ncpb.getLicenseNo(),
                                                iacMain_ncpb.getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
                                                new Timestamp(System.currentTimeMillis()),"","1");
                                        flag += 1;
                                        //（续保保单起期-疫情截止日天数）<=疫情期间有效保期 顺延续保保单中最靠后一张续保保单的止期
                                    }else if(days<NCPValidDate){
                                        Util.ciEndTimeReverse(iacMain_ncpxs);
                                        //最靠后一张续保保单顺延后止期=原保险止期+疫情期间有效保期
                                        l = iacMain_ncpxs.get(0).getEndDate().getTime() +(NCPValidDate*86400000);
                                        AfterEndDate = new Timestamp(l);
                                        //顺延天数：顺延后保单止期-原保单止期
                                        long PostponeDay = (l - iacMain_ncpxs.get(0).getEndDate().getTime()) / 86400000;
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpxs.get(0).getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getPolicyNo(),
                                                iacMain_ncpxs.get(0).getCompanyCode(),
                                                iacMain_ncpxs.get(0).getStartDate(),
                                                iacMain_ncpxs.get(0).getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpxs.get(0).getCityCode(),
                                                iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getFrameNo(),
                                                iacMain_ncpxs.get(0).getLicenseNo(),
                                                iacMain_ncpxs.get(0).getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
                                                new Timestamp(System.currentTimeMillis()),"","1");
                                        flag += 1;
                                    }
                                }
                            }catch (Exception e){
                                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常\n");
                                err+=1;
                                e.getMessage();
                            }
                        }
                    }
                    queueTag.add(flag);
                    queueError.add(err);
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
        textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:交强业务类型1，业务计算方法处理结束-----------处理数据量：" + tag + "异常数据量："+error+"\n");
        textArea.paintImmediately(textArea.getBounds());
       }

	public void SituationTwo() {
        /**
         * 线程池 100
         */
        ExecutorService service = Executors.newFixedThreadPool(100);
	    if((start!=null||start.getTime()!=0)&&(end!=null||end.getTime()!=0)&&(areaCode!=null||!areaCode.equals(""))) {
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:交强业务类型2，业务计算方法处理开始-----------"+"\n");
            textArea.paintImmediately(textArea.getBounds());
            NCPStartDate = start.getTime();
            NCPEndDate = end.getTime();
            int tag = 0;
            int error = 0;
            //查询IACMain_NCPB-疫情期本保单信息表中的保单信息集合，“保单归属地（地市）-CityCode”“业务类型-BusinessType”、“非延期原因-Reason”、“是否顺延-Flag”三字段作为查询条件进行取值判断
            String IACMain_NCPBsql = "select * from IACMain_NCPB where CityCode = ? and BusinessType= ? and Reason = ? and Flag = ?";
            List<IACMain_NCPB> iacMain_ncpbs = (List<IACMain_NCPB>) CRUDTemplate.executeQuery("ci", IACMain_NCPBsql, new BeanListHandler(IACMain_NCPB.class), areaCode, "2", "", "");
            if(iacMain_ncpbs == null || iacMain_ncpbs.size() == 0){
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:未查询到满足交强业务类型-2-的数据\n");
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:交强业务类型2，业务计算方法执行结束-----------\n");
                return;
            }else{
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:查询到满足交强业务类型-2-的数据是：【"+iacMain_ncpbs.size()+"】条\n");
            }
            //多线程处理数据
            //数组开始下标
            int n =ThreadCount;
            int count = 0;
            for(int i =0;i<=iacMain_ncpbs.size()/n;i++){
                List<IACMain_NCPB> list =null;
                if(count+n>iacMain_ncpbs.size()){
                    list = iacMain_ncpbs.subList(count,iacMain_ncpbs.size());
                }else{
                    list = iacMain_ncpbs.subList(count,count+n);
                }
                count+=10;
                final List<IACMain_NCPB> ThreadList = list;

                service.execute(new Runnable() {
                    public void run() {
                        int tag = 0;
                        int error = 0;
                        //遍历数据
                        for (IACMain_NCPB iacMain_ncpb : ThreadList) {
                            //拿每个保单的投保确认码，查询IACMain_NCPX-疫情期续保保单信息表中有无续保单，止期倒叙排序，根据此情况进行业务判断
                            String selectSql1 = "select * from IACMain_NCPX where LastPoliConfirmNo = ? order by enddate desc";
                            List<IACMain_NCPX> iacMain_ncpxs = (List<IACMain_NCPX>) CRUDTemplate.executeQuery("ci", selectSql1, new BeanListHandler(IACMain_NCPX.class), iacMain_ncpb.getPolicyConfirmNo());
                            //判断:一、无续保单
                            if (iacMain_ncpxs == null || iacMain_ncpxs.size() == 0) {
                                try {
                                    //疫情有效期
                                    List<Timestamp> list = new ArrayList();
                                    list.add(0, iacMain_ncpb.getStartDate());
                                    list.add(1, iacMain_ncpb.getEndDate());
                                    List<List<Timestamp>> bigList = new ArrayList();
                                    bigList.add(list);
                                    long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);
                                    //局部变量
                                    long l = 0;
                                    Timestamp AfterEndDate = null;

                                    //两种情况:1.保单保险止期小于疫情截止日
                                    if (iacMain_ncpb.getEndDate().getTime() < NCPEndDate) {
                                        //顺延后保单止期:疫情止期+疫情有效期
                                        l = NCPEndDate + (NCPValidDate * 86400000);
                                        AfterEndDate = new Timestamp(l);

                                    } else {//2.保单保险止期>=疫情截止日
                                        //顺延后保单止期:原保单止期+疫情有效期
                                        l = iacMain_ncpb.getEndDate().getTime() + (NCPValidDate * 86400000);
                                        AfterEndDate = new Timestamp(l);
                                    }
                                    //顺延天数：顺延后保单止期-原保单止期
                                    long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400000;

                                    //疫情起期
                                    Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                    //疫情止期
                                    Timestamp ncpEndDate = new Timestamp(NCPEndDate);

                                    String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                            " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                    int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpb.getPolicyConfirmNo(),
                                            iacMain_ncpb.getPolicyNo(),
                                            iacMain_ncpb.getCompanyCode(),
                                            iacMain_ncpb.getStartDate(),
                                            iacMain_ncpb.getEndDate(),
                                            AfterEndDate,
                                            ncpStartDate,
                                            ncpEndDate,
                                            Integer.parseInt(String.valueOf(NCPValidDate)),
                                            Integer.parseInt(String.valueOf(PostponeDay)),
                                            iacMain_ncpb.getCityCode(),
                                            "",
                                            iacMain_ncpb.getFrameNo(),
                                            iacMain_ncpb.getLicenseNo(),
                                            iacMain_ncpb.getEngineNo(),
                                            iacMain_ncpb.getBusinessType(),
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
                                list.add(0, iacMain_ncpb.getStartDate());
                                list.add(1, iacMain_ncpb.getEndDate());
                                //本保单起止日期
                                bigList.add(list);
                                for (IACMain_NCPX iacMain_ncpx : iacMain_ncpxs) {
                                    List<Timestamp> list1 = new ArrayList();
                                    list1.add(0, iacMain_ncpx.getStartDate());
                                    list1.add(1, iacMain_ncpx.getEndDate());
                                    //每个续保单起止日期
                                    bigList.add(list1);
                                }
                                long NCPValidDate = Util.Calculate(bigList, NCPStartDate, NCPEndDate);

                                //局部变量
                                long l = 0;
                                Timestamp AfterEndDate = null;
                                //以保单起期，顺序排序，找到第一张续保单
                                Util.ciStartTimeSort(iacMain_ncpxs);
                                //特殊情况：1、本保单疫情期间起保，疫情期间到期；有一张起保日期＞疫情止期的续保单，续保单起保日期-疫情止期≥N；顺延本保单
                                if ((iacMain_ncpb.getEndDate().getTime() <= NCPEndDate && iacMain_ncpxs.get(0).getStartDate().getTime() > NCPEndDate && (((iacMain_ncpxs.get(0).getStartDate().getTime() - NCPEndDate)/ 86400000) >= NCPValidDate)) ||
                                        //2、本保单疫情期间起保，本保单止期>疫情截止日；有一张起保日期＞本保单止期的续保单，续保单起保日期-本保单止期≥N；顺延本保单
                                        (iacMain_ncpb.getEndDate().getTime() > NCPEndDate && iacMain_ncpxs.get(0).getStartDate().getTime() > iacMain_ncpb.getEndDate().getTime() && (((iacMain_ncpxs.get(0).getStartDate().getTime() - iacMain_ncpb.getEndDate().getTime())/ 86400000) >= NCPValidDate))){
                                    try {

                                        //顺延后保单止期
                                        l = NCPEndDate + (NCPValidDate * 86400000);
                                        AfterEndDate = new Timestamp(l);
                                        //顺延天数：顺延后保单止期-原保单止期
                                        long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400000;
                                        //疫情起期
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        //疫情止期
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);

                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpb.getPolicyNo(),
                                                iacMain_ncpb.getCompanyCode(),
                                                iacMain_ncpb.getStartDate(),
                                                iacMain_ncpb.getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpb.getCityCode(),
                                                "",
                                                iacMain_ncpb.getFrameNo(),
                                                iacMain_ncpb.getLicenseNo(),
                                                iacMain_ncpb.getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
                                                new Timestamp(System.currentTimeMillis()),"" ,"1");
                                        tag += 1;

                                    } catch(Exception e) {
                                        textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
                                        error+=1;
                                        e.getMessage();
                                    }

                                } else {
                                    try {
                                        //以保单止期，倒序排序，找到最靠后一张续保单
                                        Util.ciEndTimeReverse(iacMain_ncpxs);
                                        //2种情况：1、最后一张续保单止期>=疫情截止日
                                        if (iacMain_ncpxs.get(0).getEndDate().getTime() >= NCPEndDate) {
                                            //顺延后保单止期:最靠后一张续保单止期+疫情有效期
                                            l = iacMain_ncpxs.get(0).getEndDate().getTime() + (NCPValidDate * 86400000);
                                            AfterEndDate = new Timestamp(l);
                                        } else {//2、最后一张续保单止期<疫情截止日
                                            //顺延后保单止期:疫情止期+疫情有效期
                                            l = NCPEndDate + (NCPValidDate * 86400000);
                                            AfterEndDate = new Timestamp(l);
                                        }
                                        //顺延天数：顺延后保单止期-原最靠后一张续保单止期
                                        long PostponeDay = (l - iacMain_ncpxs.get(0).getEndDate().getTime()) / 86400000;
                                        //疫情起期
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        //疫情止期
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);

                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpxs.get(0).getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getPolicyNo(),
                                                iacMain_ncpxs.get(0).getCompanyCode(),
                                                iacMain_ncpxs.get(0).getStartDate(),
                                                iacMain_ncpxs.get(0).getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpxs.get(0).getCityCode(),
                                                iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getFrameNo(),
                                                iacMain_ncpxs.get(0).getLicenseNo(),
                                                iacMain_ncpxs.get(0).getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
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
                    System.out.println("线程池停止");
                    break;
                }

            }
            while(queueTag.peek()!=null){
                tag +=queueTag.poll();
            }
            while(queueError.peek()!=null){
                error +=queueError.poll();
            }
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:交强业务类型2，业务计算方法处理结束-----------处理数据量：" + tag + "异常数据量："+error+"\n");
        }else {
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:业务计算参数为空，请选择必要参数"+"\n");
        }
        textArea.paintImmediately(textArea.getBounds());
	}

	public void SituationTree() {
        /**
         * 线程池 100
         */
        ExecutorService service = Executors.newFixedThreadPool(100);
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
            //查询IACMain_NCPB-疫情期本保单信息表中的保单信息集合，“保单归属地（地市）-CityCode”“业务类型-BusinessType”、“非延期原因-Reason”、“是否顺延-Flag”三字段作为查询条件进行取值判断
            //模拟获取到的本保单集合
            String IACMain_NCPBsql ="select * from IACMain_NCPB where CityCode = ? and BusinessType= ? and Reason = ? and Flag = ?";
            List<IACMain_NCPB> iacMain_ncpbs = (List<IACMain_NCPB>)CRUDTemplate.executeQuery("ci",IACMain_NCPBsql, new BeanListHandler(IACMain_NCPB.class), areaCode, "3", "","");
            if(iacMain_ncpbs == null || iacMain_ncpbs.size() == 0){
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:未查询到满足交强业务类型-3-的数据\n");
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:交强业务类型3，业务计算方法执行结束-----------\n");
                return;
            }else{
                textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:查询到满足交强业务类型-3-的数据是：【"+iacMain_ncpbs.size()+"】条\n");
                textArea.paintImmediately(textArea.getBounds());
            }
            //多线程处理数据
            //数组开始下标
            int n =ThreadCount;
            int count = 0;
            for(int i =0;i<=iacMain_ncpbs.size()/n;i++){
                List<IACMain_NCPB> list =null;
                if(count+n>iacMain_ncpbs.size()){
                    list = iacMain_ncpbs.subList(count,iacMain_ncpbs.size());
                }else{
                    list = iacMain_ncpbs.subList(count,count+n);
                }
                count+=10;
                final List<IACMain_NCPB> ThreadList = list;
                service.execute(new Runnable() {
                    public void run() {
                        int tag = 0;
                        int error = 0;
                        //遍历数据
                        for (IACMain_NCPB iacMain_ncpb : ThreadList) {
                            //拿每个保单的投保确认码，查询IACMain_NCPX-疫情期续保保单信息表中有无续保单，根据此情况进行业务判断
                            //模拟获取到的续保单集合，此保单集合根据保单止期，倒叙排序，
                            String IACMain_NCPXsql ="select * from IACMain_NCPX where LastPoliConfirmNo = ? order by enddate desc";
                            List<IACMain_NCPX> iacMain_ncpxs = (List<IACMain_NCPX>)CRUDTemplate.executeQuery("ci",IACMain_NCPXsql, new BeanListHandler(IACMain_NCPX.class), iacMain_ncpb.getPolicyConfirmNo());
                            //判断:一、无续保单
                            if (iacMain_ncpxs == null||iacMain_ncpxs.size()==0) {
                                try {
                                    //局部变量
                                    long l=0;
                                    //顺延后保单保险止期
                                    Timestamp AfterEndDate=null;
                                    l=iacMain_ncpb.getEndDate().getTime()+(NCPValidDate*86400000);
                                    AfterEndDate = new Timestamp(l);

                                    //顺延天数：顺延后保单止期-原保单止期
                                    long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400000;

                                    //组织参数存库 疫情期顺延后保单信息表
                                    Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                    Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                    String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                            " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                    int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpb.getPolicyConfirmNo(),
                                            iacMain_ncpb.getPolicyNo(),
                                            iacMain_ncpb.getCompanyCode(),
                                            iacMain_ncpb.getStartDate(),
                                            iacMain_ncpb.getEndDate(),
                                            AfterEndDate,
                                            ncpStartDate,
                                            ncpEndDate,
                                            Integer.parseInt(String.valueOf(NCPValidDate)),
                                            Integer.parseInt(String.valueOf(PostponeDay)),
                                            iacMain_ncpb.getCityCode(),
                                            "",
                                            iacMain_ncpb.getFrameNo(),
                                            iacMain_ncpb.getLicenseNo(),
                                            iacMain_ncpb.getEngineNo(),
                                            iacMain_ncpb.getBusinessType(),
                                            new Timestamp(System.currentTimeMillis()),"", "1");
                                    tag += 1;
                                }catch(Exception e) {
                                    textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:数据处理异常"+"\n");
                                    error+=1;
                                    e.getMessage();
                                }
                            }else{  //二、有续保单
                                //局部变量
                                long l=0;
                                Timestamp AfterEndDate=null;
                                //以保单起期，顺序排序，找到第一张续保单
                                Util.ciStartTimeSort(iacMain_ncpxs);
                                //特殊情况：续保单起保日期-本保单止期>=N；顺延本保单
                                if((iacMain_ncpxs.get(0).getStartDate().getTime()-iacMain_ncpb.getEndDate().getTime())>=NCPValidDate ){
                                    try {
                                        //顺延后保单止期
                                        l=iacMain_ncpb.getEndDate().getTime()+(NCPValidDate*86400000);
                                        AfterEndDate = new Timestamp(l);
                                        //顺延天数：顺延后保单止期-原保单止期
                                        long PostponeDay = (l - iacMain_ncpb.getEndDate().getTime()) / 86400000;

                                        //组织参数存库 疫情期顺延后保单信息表
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpb.getPolicyNo(),
                                                iacMain_ncpb.getCompanyCode(),
                                                iacMain_ncpb.getStartDate(),
                                                iacMain_ncpb.getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpb.getCityCode(),
                                                "",
                                                iacMain_ncpb.getFrameNo(),
                                                iacMain_ncpb.getLicenseNo(),
                                                iacMain_ncpb.getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
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
                                        Util.ciEndTimeReverse(iacMain_ncpxs);
                                        //顺延后保单止期:最靠后一张续保单止期+疫情有效期
                                        l=iacMain_ncpxs.get(0).getEndDate().getTime()+(NCPValidDate*86400000);
                                        AfterEndDate = new Timestamp(l);

                                        //顺延天数：顺延后保单止期-原最靠后一张续保单止期
                                        long PostponeDay = (l - iacMain_ncpxs.get(0).getEndDate().getTime()) / 86400000;

                                        //组织参数存库 疫情期顺延后保单信息表
                                        Timestamp ncpStartDate = new Timestamp(NCPStartDate);
                                        Timestamp ncpEndDate = new Timestamp(NCPEndDate);
                                        String insertSql = "insert into IACMain_NCPPostpone(PolicyConfirmNo,PolicyNo,CompanyCode,StartDate,EndDate,AfterEndDate,NCPStartDate,\n" +
                                                " NCPEndDate,NCPValidDate,PostponeDay,CityCode,LastPoliConfirmNo,FrameNo,LicenseNo,EngineNo,BusinessType,InputDate,Flag,ValidStatus) \n" +
                                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                                        int i = CRUDTemplate.executeUpdate("ci", insertSql, iacMain_ncpxs.get(0).getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getPolicyNo(),
                                                iacMain_ncpxs.get(0).getCompanyCode(),
                                                iacMain_ncpxs.get(0).getStartDate(),
                                                iacMain_ncpxs.get(0).getEndDate(),
                                                AfterEndDate,
                                                ncpStartDate,
                                                ncpEndDate,
                                                Integer.parseInt(String.valueOf(NCPValidDate)),
                                                Integer.parseInt(String.valueOf(PostponeDay)),
                                                iacMain_ncpxs.get(0).getCityCode(),
                                                iacMain_ncpb.getPolicyConfirmNo(),
                                                iacMain_ncpxs.get(0).getFrameNo(),
                                                iacMain_ncpxs.get(0).getLicenseNo(),
                                                iacMain_ncpxs.get(0).getEngineNo(),
                                                iacMain_ncpb.getBusinessType(),
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
                    System.out.println("线程池停止");
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
            textArea.append("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]:业务类型3，业务计算方法处理结束-----------处理数据量："+tag+ "异常数据量："+error+"\n");

        }else {
            textArea.append("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]:业务计算参数为空，请选择必要参数"+"\n");
        }
        textArea.paintImmediately(textArea.getBounds());
    }

}
