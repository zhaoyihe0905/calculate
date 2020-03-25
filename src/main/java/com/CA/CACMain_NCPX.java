package com.CA;

import java.sql.Timestamp;

/**
 * 商业-CACMain_NCPX-疫情期续保保单信息表实体类
 * author:yy
 * DateTime:2020/3/24 10:41
 */
public class CACMain_NCPX {

    //序号
    private int SerialNo;
    //投保确认码
    private  String ConfirmSequenceNo;
    //保单号
    private  String PolicyNo;
    //公司代码
    private  String CompanyCode;
    //保单归属地（地市）
    private String CityCode;
    //保险起期
    private Timestamp EffectiveDate;
    //保险止期
    private Timestamp ExpireDate;
    //上张保单投保确认吗
    private String LastPolicyConfirmNo;
    //车架号
    private String Vin;
    //车牌号
    private String LicenseNo;
    //发动机号
    private String EngineNo;
    //几级续保单
    private int level;
    //提数时间
    private Timestamp InputDate;


    public CACMain_NCPX() {
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Timestamp getInputDate() {
        return InputDate;
    }

    public void setInputDate(Timestamp inputDate) {
        InputDate = inputDate;
    }
}
