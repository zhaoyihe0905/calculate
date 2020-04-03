package com.CA;

import java.sql.Timestamp;

/**
 * 险种表
 * author:yy
 * DateTime:2020/4/2 13:14
 */
public class CACCoverage {

    private String confirmSequenceNo;
    private String companyCode;
    private String coverageCode;
    private Timestamp effectiveDate;
    private Timestamp expireDate;


    public String getConfirmSequenceNo() {
        return confirmSequenceNo;
    }

    public void setConfirmSequenceNo(String confirmSequenceNo) {
        this.confirmSequenceNo = confirmSequenceNo;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCoverageCode() {
        return coverageCode;
    }

    public void setCoverageCode(String coverageCode) {
        this.coverageCode = coverageCode;
    }

    public Timestamp getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Timestamp effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Timestamp getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Timestamp expireDate) {
        this.expireDate = expireDate;
    }


}
