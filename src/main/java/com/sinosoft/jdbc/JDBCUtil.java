package com.sinosoft.jdbc;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * author:yy
 * DateTime:2020/3/26 10:27
 */
public class JDBCUtil {

    private static String driver;

    private static String ciUrl;
    private static String ciUsername;
    private static String ciPassword;


    private static String caUrl;
    private static String caUsername;
    private static String caPassword;

    //静态代码块，在程序编译的时候执行
    static {
        try {
           //创建Properties对象
            Properties p = new Properties();
            //获取文件输入流
            InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream("com/config/db.properties");
            /*InputStream in = new FileInputStream("com/db.properties");*/
            //加载输入流
            p.load(in);
            //获取数据库连接驱动名字
            driver = p.getProperty("riverClassName");
            //获取数据库连接地址
            ciUrl = p.getProperty("ciUrl");
            //获取数据库连接用户名
            ciUsername = p.getProperty("ciUsername");
            //获取数据库连接密码
            ciPassword = p.getProperty("ciPassword");


            //获取数据库连接地址
            caUrl = p.getProperty("caUrl");
            //获取数据库连接用户名
            caUsername = p.getProperty("caUsername");
            //获取数据库连接密码
            caPassword = p.getProperty("caPassword");

            if(driver != null
                    && ciUrl != null && ciUsername != null && ciPassword != null
                    && caUrl != null && caUsername != null && caPassword != null){
                //加载驱动
                Class.forName(driver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取连接对象
     * @return Connection连接对象
     */
    public static Connection getConn(String dataSource){
        Connection conn = null;


        if (dataSource.equals("ci")){
            try {
                conn = DriverManager.getConnection(ciUrl,ciUsername,ciPassword);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if (dataSource.equals("ca")){
            try {
                conn = DriverManager.getConnection(caUrl,caUsername,caPassword);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return conn;
    }

    /**
     * 关闭连接（Connection连接对象必须在最后关闭）
     * @param conn Connection连接对象
     * @param st 编译执行对象
     * @param rs 结果集
     */
    public static void close(Connection conn, Statement st, ResultSet rs){
        try {
            if(rs != null){
                rs.close();
            }
            if(st != null){
                st.close();
            }
            if(conn != null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
