package com.sinosoft;

import java.util.Date;

import javax.swing.JTextArea;

public interface SinosoftInterface {
	
	/**
	 * 疫情情况一
	 * @param start 疫情起期
	 * @param end 疫情止期
	 * @param textArea 日志对象
	 * @param areaCode 地区代码
	 */
	public void SituationOne(Date start,Date end,JTextArea textArea,String areaCode);
	/**
	 * 疫情情况二
	 * @param start 疫情起期
	 * @param end 疫情止期
	 * @param textArea 日志对象
	 * @param areaCode 地区代码
	 */
	public void SituationTwo(Date start,Date end,JTextArea textArea,String areaCode);
	/**
	 * 疫情情况三
	 * @param start 疫情起期
	 * @param end 疫情止期
	 * @param textArea 日志对象
	 * @param areaCode 地区代码
	 */
	public void SituationTree(Date start,Date end,JTextArea textArea,String areaCode);
}
