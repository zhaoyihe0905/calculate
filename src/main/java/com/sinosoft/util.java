package com.sinosoft;

import com.CA.CACMain_NCPX;
import com.CI.IACMain_NCPX;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ������������
 * @author zyh
 *
 */
public class util {

    /**
     * ��ǿ-�Ա������ڣ�˳������
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
     * ��ǿ-�Ա���ֹ�ڣ���������
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
     * ��ҵ-�Ա������ڣ�˳������
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
     * ��ҵ-�Ա���ֹ�ڣ���������
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



}
