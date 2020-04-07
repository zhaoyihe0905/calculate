package com.sinosoft;

import com.CA.CACMain_NCPX;
import com.CI.IACMain_NCPX;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author zyh
 *
 */
public class Util {

    /**
     * 交强-以保单起期，顺序排序
     * @param list
     */
    public static void ciStartTimeSort(List<IACMain_NCPX> list) {
        Collections.sort(list, new Comparator<IACMain_NCPX>() {

            public int compare(IACMain_NCPX o1, IACMain_NCPX o2) {
                try {
                    if (o1.getStartDate().getTime() > o2.getStartDate().getTime()) {
                        return 1;
                    } else if (o1.getStartDate().getTime() < o2.getStartDate().getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    /**
     * 交强-以保单止期，倒序排序
     * @param list
     */
    public static void ciEndTimeReverse(List<IACMain_NCPX> list) {
        Collections.sort(list, new Comparator<IACMain_NCPX>() {

            public int compare(IACMain_NCPX o1, IACMain_NCPX o2) {
                try {
                    if (o1.getEndDate().getTime() < o2.getEndDate().getTime()) {
                        return 1;
                    } else if (o1.getEndDate().getTime() > o2.getEndDate().getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }




    /**
     * 商业-以保单起期，顺序排序
     * @param list
     */
    public static void caStartTimeSort(List<CACMain_NCPX> list) {
        Collections.sort(list, new Comparator<CACMain_NCPX>() {

            public int compare(CACMain_NCPX o1, CACMain_NCPX o2) {
                try {
                    if (o1.getEffectiveDate().getTime() > o2.getEffectiveDate().getTime()) {
                        return 1;
                    } else if (o1.getEffectiveDate().getTime() < o2.getEffectiveDate().getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    /**
     * 商业-以保单止期，倒序排序
     * @param list
     */
    public static void caEndTimeReverse(List<CACMain_NCPX> list) {
        Collections.sort(list, new Comparator<CACMain_NCPX>() {

            public int compare(CACMain_NCPX o1, CACMain_NCPX o2) {
                try {
                    if (o1.getExpireDate().getTime() < o2.getExpireDate().getTime()) {
                        return 1;
                    } else if (o1.getExpireDate().getTime() > o2.getExpireDate().getTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }

    /**
     * 计算疫情有效保单天数
     * @param bigList 本张保单、续保单
     * @param NCPStartDate 疫情起期
     * @param NCPEndDate 疫情止期
     * @return
     */
    public static long Calculate(List< List<Timestamp>> bigList, long NCPStartDate, long NCPEndDate){
        //set去重集合
        Set<Timestamp> timestampSet =new HashSet<Timestamp>();
        //天数计算
        long count = 0;

        //遍历保单起止日期数组
        for (List<Timestamp> timestamps : bigList) {
            //保单起期
            long startDate = timestamps.get(0).getTime()/1000;
            //保单止期
            long endDate = timestamps.get(1).getTime()/1000;

            long time = startDate;

            if (startDate> NCPEndDate/1000+86400 || endDate <=NCPStartDate/1000 ){
                continue;
            }else if(startDate<=NCPStartDate/1000 && endDate < NCPEndDate/1000+86400){
                time = NCPStartDate/1000;

                //保单起期在疫情起期之后，保单止期在疫情止期之后
            }else if(startDate>=NCPStartDate/1000 && endDate >= NCPEndDate/1000){

                endDate = NCPEndDate/1000+86400;
            }

         //疫情止期当天 算作疫情有效期，
            while (time<endDate){
                if(NCPStartDate/1000 <= time && time < NCPEndDate/1000+86400){
                    Timestamp timestamp = new Timestamp(time*1000);
                    timestampSet.add(timestamp);
                }
                time += 86400;
            }
            //判断保单止期是否是0点，若是零点，排除止期当天不算保期天数，反之算作保期天数
//            if (endDate%86400==57600){
//                while (time<endDate){
//                    if(NCPStartDate/1000 <= time && time <= NCPEndDate/1000){
//                        Timestamp timestamp = new Timestamp(time*1000);
//                        timestampSet.add(timestamp);
//                    }
//                    time += 86400;
//                }
//            }else{
//
//            }
        }
        count  = timestampSet.size();
        //获取疫情起止日期内的有效天数
//        for(Timestamp t:timestampSet){
//            long l = t.getTime()/1000;
//            if(NCPStartDate/1000<=l&&l<NCPEndDate/1000){
//                count=count+1;
//            }
//        }
        return count;
    }

    /**
     * 疫情有效期计算
     * @param NCPStartDate
     * @param NCPEndDate
     * @return
     * @throws ParseException
     */
    public static long Calculate(Date NCPStartDate,Date NCPEndDate) throws ParseException {

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        NCPStartDate=sdf.parse(sdf.format(NCPStartDate));

        NCPEndDate=sdf.parse(sdf.format(NCPEndDate));

        Calendar cal = Calendar.getInstance();

        cal.setTime(NCPStartDate);

        long time1 = cal.getTimeInMillis();

        cal.setTime(NCPEndDate);

        long time2 = cal.getTimeInMillis();

        long between_days=(time2-time1)/(1000*3600*24);

        return between_days;

    }
}
