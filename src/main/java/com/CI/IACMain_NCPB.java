package com.CI;

import java.sql.Timestamp;

/**
 * 交强-IACMain_NCPB-疫情期本保单信息表实体类
 * author:yy
 * DateTime:2020/3/24 11:01
 */
public class IACMain_NCPB {
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
    //车架号
    private String FrameNo;
    //车牌号
    private String LicenseNo;
    //发动机号
    private String EngineNo;
    //业务类型
    private String BusinessType;
    //非延期原因
    private String Reason;
    //非顺延原因描述
    private String Desc;
    //是否顺延
    private String Flag;
    //提数时间
    private Timestamp InputDate;


    public IACMain_NCPB() {
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

    public String getBusinessType() {
        return BusinessType;
    }

    public void setBusinessType(String businessType) {
        BusinessType = businessType;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String reason) {
        Reason = reason;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getFlag() {
        return Flag;
    }

    public void setFlag(String flag) {
        Flag = flag;
    }

    public Timestamp getInputDate() {
        return InputDate;
    }

    public void setInputDate(Timestamp inputDate) {
        InputDate = inputDate;
    }
}
