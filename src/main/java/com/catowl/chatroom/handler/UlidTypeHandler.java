package com.catowl.chatroom.handler;

import com.catowl.chatroom.utils.UlidUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @program: ChatRoom
 * @description: Mybatis自动转换BINARY为SRING配置类
 * @author: qqCatOwlbb
 * @create: 2025-11-15 13:10
 **/
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.BINARY)
public class UlidTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setBytes(i, UlidUtils.toBytes(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        return UlidUtils.fromBytes(bytes);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        return UlidUtils.fromBytes(bytes);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        return UlidUtils.fromBytes(bytes);
    }
}
