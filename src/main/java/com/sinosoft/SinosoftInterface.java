package com.sinosoft;

import java.util.Date;

import javax.swing.JTextArea;

public interface SinosoftInterface {
	
	/**
	 * �������һ
	 * @param start ��������
	 * @param end ����ֹ��
	 * @param textArea ��־����
	 * @param areaCode ��������
	 */
	public void SituationOne(Date start,Date end,JTextArea textArea,String areaCode);
	/**
	 * ���������
	 * @param start ��������
	 * @param end ����ֹ��
	 * @param textArea ��־����
	 * @param areaCode ��������
	 */
	public void SituationTwo(Date start,Date end,JTextArea textArea,String areaCode);
	/**
	 * ���������
	 * @param start ��������
	 * @param end ����ֹ��
	 * @param textArea ��־����
	 * @param areaCode ��������
	 */
	public void SituationTree(Date start,Date end,JTextArea textArea,String areaCode);
}
