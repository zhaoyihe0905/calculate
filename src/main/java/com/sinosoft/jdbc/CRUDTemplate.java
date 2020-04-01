package com.sinosoft.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * author:yy
 * DateTime:2020/3/26 10:35
 */
public class CRUDTemplate {
    /**
     * 增删改操作
     * @param dataSource 区分交强：ci 商业：ca
     * @param sql 传入的SQL语句
     * @param params 可变参数
     * @return 操作结果
     */
    public static int executeUpdate(String dataSource,String sql,Object... params)throws Exception {
        Connection conn = null;
        PreparedStatement psmt = null;
        int result = 0;
        try {
            //获取数据库连接对象
            conn = JDBCUtil.getConn(dataSource);
            //获取预编译语句对象
            psmt = conn.prepareStatement(sql);
            //给预编译语句赋值
            for (int i = 0; i < params.length; i++) {
                psmt.setObject(i+1,params[i]);
            }
            //执行SQL语句获取执行结果
            result = psmt.executeUpdate();
        } catch (Exception e) {
           throw  e;
        } finally {
            //关闭数据库连接
            JDBCUtil.close(conn,psmt,null);
        }
        return result;
    }

    /**
     * 查询操作
     * @param dataSource 区分交强：ci 商业：ca
     * @param sql SQL语句
     * @param handler 判断查询一个还是多个
     * @param params 可变参数
     * @param <T> 具体操作的实体类
     * @return 返回IResultSetHandler接口中的泛型
     */
    public static <T> T executeQuery(String dataSource,String sql, IResultSetHandler<T> handler,Object... params){
        Connection conn = null;
        PreparedStatement psmt = null;
        ResultSet rs = null;
        try {
            //获取数据库连接对象
            conn = JDBCUtil.getConn(dataSource);
            //获取预编译语句对象
            psmt = conn.prepareStatement(sql);
            //给预编译语句赋值
            for (int i = 0; i < params.length; i++) {
                psmt.setObject(i+1,params[i]);
            }
            //执行SQL语句获取结果集
            rs = psmt.executeQuery();
            //处理结果集
            return handler.handle(rs);
        } catch (Exception e) {
             e.printStackTrace();
        } finally {
            //关闭数据库连接
            JDBCUtil.close(conn,psmt,rs);
        }
        return null;
    }

}
