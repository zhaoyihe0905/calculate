package com.CA;

import java.sql.Timestamp;

/**
 * 商业-CACMain_NCPPostpone-疫情期顺延保单信息表实体类
 * author:yy
 * DateTime:2020/3/24 10:46
 */
public class CACMain_NCPPostpone {
    //序号
    private int SerialNo;
    //顺延保单投保确认码
    private  String ConfirmSequenceNo;
    //顺延保单保单号
    private  String PolicyNo;
    //顺延保单公司代码
    private  String CompanyCode;
    //顺延保单保单归属地（地市）
    private String CityCode;
    //顺延保单保险起期
    private Timestamp EffectiveDate;
    //顺延保单保险止期
    private Timestamp ExpireDate;
    //顺延后保单保险止期
    private Timestamp AfterExpireDate;
    //疫情起期
    private Timestamp NCPStartDate;
    //疫情止期
    private Timestamp NCPEndDate;
    //疫情期间有效保期
    private int NCPValidDate;
    //顺延天数
    private int PostponeDay;
    //上张保单投保确认吗
    private String LastPolicyConfirmNo;
    //上张保单保单归属地（地市）
    private String LastCityCode;
    //车架号
    private String Vin;
    //车牌号
    private String LicenseNo;
    //发动机号
    private String EngineNo;
    //业务类型
    private String BusinessType;
    //提数时间
    private Timestamp InputDate;
    //更新时间
    private Timestamp UpdateTime;
    //是否已处理
    private String Flag;
    //效力状态
    private String ValidStatus;


    public CACMain_NCPPostpone() {
    }

    public String getLastCityCode() {
        return LastCityCode;
    }

    public void setLastCityCode(String lastCityCode) {
        LastCityCode = lastCityCode;
    }

    public int getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(int serialNo) {
        SerialNo = serialNo;
    }

    public String getConfirmSequenceNo() {
        return ConfirmSequenceNo;
    }

    public void setConfirmSequenceNo(String confirmSequenceNo) {
        ConfirmSequenceNo = confirmSequenceNo;
    }

    public String getPolicyNo() {
        return PolicyNo;
    }

    public void setPolicyNo(String policyNo) {
        PolicyNo = policyNo;
    }

    public String getCompanyCode() {
        return CompanyCode;
    }

    public void setCompanyCode(String companyCode) {
        CompanyCode = companyCode;
    }

    public String getCityCode() {
        return CityCode;
    }

    public void setCityCode(String cityCode) {
        CityCode = cityCode;
    }

    public Timestamp getEffectiveDate() {
        return EffectiveDate;
    }

    public void setEffectiveDate(Timestamp effectiveDate) {
        EffectiveDate = effectiveDate;
    }

    public Timestamp getExpireDate() {
        return ExpireDate;
    }

    public void setExpireDate(Timestamp expireDate) {
        ExpireDate = expireDate;
    }

    public Timestamp getAfterExpireDate() {
        return AfterExpireDate;
    }

    public void setAfterExpireDate(Timestamp afterExpireDate) {
        AfterExpireDate = afterExpireDate;
    }

    public Timestamp getNCPStartDate() {
        return NCPStartDate;
    }

    public void setNCPStartDate(Timestamp NCPStartDate) {
        this.NCPStartDate = NCPStartDate;
    }

    public Timestamp getNCPEndDate() {
        return NCPEndDate;
    }

    public void setNCPEndDate(Timestamp NCPEndDate) {
        this.NCPEndDate = NCPEndDate;
    }

    public int getNCPValidDate() {
        return NCPValidDate;
    }

    public void setNCPValidDate(int NCPValidDate) {
        this.NCPValidDate = NCPValidDate;
    }

    public int getPostponeDay() {
        return PostponeDay;
    }

    public void setPostponeDay(int postponeDay) {
        PostponeDay = postponeDay;
    }

    public String getLastPolicyConfirmNo() {
        return LastPolicyConfirmNo;
    }

    public void setLastPolicyConfirmNo(String lastPolicyConfirmNo) {
        LastPolicyConfirmNo = lastPolicyConfirmNo;
    }

    public String getVin() {
        return Vin;
    }

    public void setVin(String vin) {
        Vin = vin;
    }

    public String getLicenseNo() {
        return LicenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        LicenseNo = licenseNo;
    }

    public String getEngineNo() {
        return EngineNo;
    }

    public void setEngineNo(String engineNo) {
        EngineNo = engineNo;
    }

    public String getBusinessType() {
        return BusinessType;
    }

    public void setBusinessType(String businessType) {
        BusinessType = businessType;
    }

    public Timestamp getInputDate() {
        return InputDate;
    }

    public void setInputDate(Timestamp inputDate) {
        InputDate = inputDate;
    }

    public Timestamp getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        UpdateTime = updateTime;
    }

    public String getFlag() {
        return Flag;
    }

    public void setFlag(String flag) {
        Flag = flag;
    }

    public String getValidStatus() {
        return ValidStatus;
    }

    public void setValidStatus(String validStatus) {
        ValidStatus = validStatus;
    }
}
