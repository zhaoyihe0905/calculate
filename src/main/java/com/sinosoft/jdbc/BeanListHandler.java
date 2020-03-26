package com.sinosoft.jdbc;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * author:yy
 * DateTime:2020/3/26 10:39
 */
public class BeanListHandler<T> implements IResultSetHandler<List<T>>  {

    private Class<T> clazz;

    public BeanListHandler(Class<T> clazz){
        this.clazz = clazz;
    }

    public List<T> handle(ResultSet rs) throws Exception {
        //获取指定字节码信息
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz,Object.class);
        //获取所有属性描述器
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        List<T> list = new ArrayList<T>();
        while (rs.next()){
            T obj = clazz.newInstance();
            for (PropertyDescriptor pd:pds){
                //获取结果集中对应字段名的值
                Object o = rs.getObject(pd.getName());
                //执行当前方法并传入参数
                pd.getWriteMethod().invoke(obj,o);
            }
            list.add(obj);
        }
        return list;
    }

}
