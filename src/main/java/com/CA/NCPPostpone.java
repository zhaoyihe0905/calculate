package com.CA;

import java.sql.Timestamp;

/**
 * author:yy
 * DateTime:2020/4/29 15:32
 */
public class NCPPostpone {

    //顺延保单投保确认码
    private  String confirmSequenceNo;

    //顺延保单保险止期
    private Timestamp expireDate;

    //顺延后保单保险止期
    private Timestamp afterExpireDate;


    public NCPPostpone() {
    }

    public String getConfirmSequenceNo() {
        return confirmSequenceNo;
    }

    public void setConfirmSequenceNo(String confirmSequenceNo) {
        this.confirmSequenceNo = confirmSequenceNo;
    }

    public Timestamp getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Timestamp expireDate) {
        this.expireDate = expireDate;
    }

    public Timestamp getAfterExpireDate() {
        return afterExpireDate;
    }

    public void setAfterExpireDate(Timestamp afterExpireDate) {
        this.afterExpireDate = afterExpireDate;
    }
}
