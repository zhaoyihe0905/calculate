package com.sinosoft.jdbc;

import java.sql.ResultSet;

/**
 * author:yy
 * DateTime:2020/3/26 10:38
 */
public interface IResultSetHandler<T> {

    T handle(ResultSet rs) throws Exception;
}
