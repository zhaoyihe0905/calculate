package com.sinosoft;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
/**
 * Date 2020/3/24
 * @author zyh
 *
 */
public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_2;
	/**
	 * 疫情起期
	 */
	JTextPane textPane = new JTextPane();
	/**
	 * 疫情止期
	 */
	JTextPane textPane_1 = new JTextPane();
	
	//日志对象
	JTextArea textArea = new JTextArea();
	/**
	 * 下拉菜单栏
	 */
	JComboBox comboBox = new JComboBox();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setFont(new Font("华文行楷", Font.PLAIN, 18));
		setTitle("顺延保期计算");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 647, 515);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(255, 255, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setText("2020-03-01 00:00:00");
		textField.setBounds(137, 37, 148, 24);
		contentPane.add(textField);
		textField.setColumns(10);
		
		textField_2 = new JTextField();
		textField_2.setText("2020-03-01 00:00:00");
		textField_2.setColumns(10);
		textField_2.setBounds(137, 74, 148, 24);
		contentPane.add(textField_2);
		

		textPane.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane.setBackground(new Color(255, 255, 255));
		textPane.setText("疫情起期");
		textPane.setBounds(40, 37, 96, 24);
		contentPane.add(textPane);
		

		textPane_1.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_1.setBackground(new Color(255, 255, 255));
		textPane_1.setText("疫情止期");
		textPane_1.setBounds(40, 74, 96, 24);
		contentPane.add(textPane_1);
		

		comboBox.setBackground(new Color(255, 255, 255));
		comboBox.setBounds(437, 37, 113, 24);
		//初始化下拉菜单栏
		this.initComboBox(comboBox);
		contentPane.add(comboBox);
		
		JButton btnNewButton = new JButton("执行");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//交强险执行
				//获取输入值
				//疫情起期
				String start = textField.getText();
				//疫情止期
				String end = textField_2.getText();
				//地区
				String areaCode = ((String)comboBox.getSelectedItem()).substring(0, 6);
				//校验
				if(MainFrame.validFormat(start,end,areaCode)){
					//校验通过，开始执行业务
					//转换日期格式
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date startDate = null;
					Date endDate = null;
					try {
						startDate = format.parse(start);
						endDate = format.parse(end);
					} catch (Exception e2) {
						// TODO: handle exception
					}
					SinosoftIa ia = new SinosoftIa();
					ia.SituationOne(startDate, endDate, textArea, areaCode);
					ia.SituationTwo(startDate, endDate, textArea, areaCode);
					ia.SituationTree(startDate, endDate, textArea, areaCode);
					
				}
				
				
			}
		});
		btnNewButton.setFont(new Font("华文行楷", Font.PLAIN, 18));
		btnNewButton.setBounds(137, 131, 113, 27);
		contentPane.add(btnNewButton);
		
		JTextPane textPane_2 = new JTextPane();
		textPane_2.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_2.setBackground(new Color(255, 255, 255));
		textPane_2.setText("地市选择");
		textPane_2.setBounds(341, 37, 96, 24);
		contentPane.add(textPane_2);
		
		JTextPane textPane_3 = new JTextPane();
		textPane_3.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_3.setBackground(new Color(255, 255, 255));
		textPane_3.setText("交强险");
		textPane_3.setBounds(44, 134, 92, 24);
		contentPane.add(textPane_3);
		
		JTextPane textPane_4 = new JTextPane();
		textPane_4.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_4.setBackground(new Color(255, 255, 255));
		textPane_4.setText("商业险");
		textPane_4.setBounds(345, 134, 92, 24);
		contentPane.add(textPane_4);
		
		JButton button_4 = new JButton("执行");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//商业险执行
				//获取输入值
				//疫情起期
				String start = textField.getText();
				//疫情止期
				String end = textField_2.getText();
				//地区
				String areaCode = ((String)comboBox.getSelectedItem()).substring(0, 6);
				//校验
				if(MainFrame.validFormat(start,end,areaCode)){
					//校验通过，开始执行业务
					//转换日期格式
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date startDate = null;
					Date endDate = null;
					try {
						startDate = format.parse(start);
						endDate = format.parse(end);
					} catch (Exception e2) {
						// TODO: handle exception
					}
					SinosoftCa ca = new SinosoftCa();
					ca.SituationOne(startDate, endDate, textArea, areaCode);
					ca.SituationTwo(startDate, endDate, textArea, areaCode);
					ca.SituationTree(startDate, endDate, textArea, areaCode);
				}
			}
		});
		button_4.setFont(new Font("华文行楷", Font.PLAIN, 18));
		button_4.setBounds(437, 131, 113, 27);
		contentPane.add(button_4);
		textArea.setFont(new Font("黑体", Font.PLAIN, 16));
		
		textArea.setForeground(Color.BLACK);
		textArea.setBackground(Color.WHITE);
		textArea.setBounds(44, 190, 541, 89);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		textArea.setBorder(border);
		//滚动条
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(44, 190, 541, 265);
		scrollPane.setViewportView(textArea);
		contentPane.add(scrollPane);
	}
	/**
	 * 初始化下拉菜单
	 * @param comboBox
	 */
	public void initComboBox(JComboBox comboBox){
		//读取config配置文件
		Properties prop = new Properties();
		InputStream in = MainFrame.class.getClassLoader().getResourceAsStream("com/config/config.properties");
		try {
			prop.load(new InputStreamReader(in,"UTF-8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		comboBox.addItem("---请选择---");
		Set<Entry<Object, Object>> propSet= prop.entrySet();
		for(Entry<Object, Object> entrty:propSet ){
			comboBox.addItem(entrty.getKey()+"-"+entrty.getValue());		
		}	
		textArea.append("["+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"]:地区初始化完成"+"\n");
	}
	public static Boolean validFormat(String start,String end,String areaCode){
		Pattern pattern=Pattern.compile("^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-)) (20|21|22|23|[0-1]?\\d):[0-5]?\\d:[0-5]?\\d$"); 				Matcher st=pattern.matcher(start);
		if(!st.matches()){
			JOptionPane.showMessageDialog(null, "疫情起期格式不正确", "错误提示",JOptionPane.WARNING_MESSAGE);
			return false;
		}
		Matcher en=pattern.matcher(end);
		if(!en.matches()){
			JOptionPane.showMessageDialog(null, "疫情止期格式不正确", "错误提示",JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if("---请选择---".equals(areaCode)){
			JOptionPane.showMessageDialog(null, "请选择地区", "错误提示",JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}
}
