package com.CI;

import java.sql.Timestamp;

/**
 * 交强-IACMain_NCPX-疫情期续保保单信息表实体类
 * author:yy
 * DateTime:2020/3/24 11:11
 */
public class IACMain_NCPX {
    //序号
    private int SerialNo;
    //投保确认码
    private  String PolicyConfirmNo;
    //保单号
    private  String PolicyNo;
    //公司代码
    private  String CompanyCode;
    //保单归属地（地市）
    private String CityCode;
    //保险起期
    private Timestamp StartDate;
    //保险止期
    private Timestamp EndDate;
    //上张保单投保确认吗
    private String LastPoliConfirmNo;
    //车架号
    private String FrameNo;
    //车牌号
    private String LicenseNo;
    //发动机号
    private String EngineNo;
    //几级续保单
    private int level;
    //提数时间
    private Timestamp InputDate;


    public IACMain_NCPX() {
    }


    public int getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(int serialNo) {
        SerialNo = serialNo;
    }

    public String getPolicyConfirmNo() {
        return PolicyConfirmNo;
    }

    public void setPolicyConfirmNo(String policyConfirmNo) {
        PolicyConfirmNo = policyConfirmNo;
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

    public Timestamp getStartDate() {
        return StartDate;
    }

    public void setStartDate(Timestamp startDate) {
        StartDate = startDate;
    }

    public Timestamp getEndDate() {
        return EndDate;
    }

    public void setEndDate(Timestamp endDate) {
        EndDate = endDate;
    }

    public String getLastPoliConfirmNo() {
        return LastPoliConfirmNo;
    }

    public void setLastPoliConfirmNo(String lastPoliConfirmNo) {
        LastPoliConfirmNo = lastPoliConfirmNo;
    }

    public String getFrameNo() {
        return FrameNo;
    }

    public void setFrameNo(String frameNo) {
        FrameNo = frameNo;
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
