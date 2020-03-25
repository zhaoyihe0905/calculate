package com.sinosoft;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
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
import java.awt.event.ActionEvent;
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
		setTitle("\u987A\u5EF6\u4FDD\u671F\u8BA1\u7B97");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 647, 339);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(255, 255, 255));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		textField = new JTextField();
		textField.setText("\u75AB\u60C5\u8D77\u671F");
		textField.setBounds(137, 37, 148, 24);
		contentPane.add(textField);
		textField.setColumns(10);
		
		textField_2 = new JTextField();
		textField_2.setText("\u75AB\u60C5\u6B62\u671F");
		textField_2.setColumns(10);
		textField_2.setBounds(137, 74, 148, 24);
		contentPane.add(textField_2);
		
		JTextPane textPane = new JTextPane();
		textPane.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane.setBackground(new Color(255, 255, 255));
		textPane.setText("\u75AB\u60C5\u8D77\u671F\uFF1A");
		textPane.setBounds(40, 37, 96, 24);
		contentPane.add(textPane);
		
		JTextPane textPane_1 = new JTextPane();
		textPane_1.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_1.setBackground(new Color(255, 255, 255));
		textPane_1.setText("\u75AB\u60C5\u6B62\u671F\uFF1A");
		textPane_1.setBounds(40, 74, 96, 24);
		contentPane.add(textPane_1);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBackground(new Color(255, 255, 255));
		comboBox.setBounds(437, 37, 113, 24);
		contentPane.add(comboBox);
		
		JButton btnNewButton = new JButton("\u6267\u884C");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//交强险业务执行
			}
		});
		btnNewButton.setFont(new Font("华文行楷", Font.PLAIN, 18));
		btnNewButton.setBounds(137, 131, 113, 27);
		contentPane.add(btnNewButton);
		
		JTextPane textPane_2 = new JTextPane();
		textPane_2.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_2.setBackground(new Color(255, 255, 255));
		textPane_2.setText("\u5730\u5E02\u9009\u62E9\uFF1A");
		textPane_2.setBounds(341, 37, 96, 24);
		contentPane.add(textPane_2);
		
		JTextPane textPane_3 = new JTextPane();
		textPane_3.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_3.setBackground(new Color(255, 255, 255));
		textPane_3.setText("\u4EA4\u5F3A\u9669");
		textPane_3.setBounds(44, 134, 92, 24);
		contentPane.add(textPane_3);
		
		JTextPane textPane_4 = new JTextPane();
		textPane_4.setFont(new Font("华文行楷", Font.PLAIN, 18));
		textPane_4.setBackground(new Color(255, 255, 255));
		textPane_4.setText("\u5546\u4E1A\u9669");
		textPane_4.setBounds(345, 134, 92, 24);
		contentPane.add(textPane_4);
		
		JButton button_4 = new JButton("\u6267\u884C");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//商业险业务执行
			}
		});
		button_4.setFont(new Font("华文行楷", Font.PLAIN, 18));
		button_4.setBounds(437, 131, 113, 27);
		contentPane.add(button_4);
		
		JTextArea textArea = new JTextArea();
		textArea.setForeground(new Color(230, 230, 250));
		textArea.setBackground(new Color(255, 255, 255));
		textArea.setBounds(44, 190, 541, 89);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		textArea.setBorder(border);
		contentPane.add(textArea);
	}
}
