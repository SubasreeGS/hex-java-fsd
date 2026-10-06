package com.ecommerce.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Map;

@Component
public class VendorProductCountMapper implements RowMapper<Map.Entry<String, Integer>> {

    @Override
    public Map.Entry<String, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AbstractMap.SimpleEntry<>(
                rs.getString("vendor_name"),
                rs.getInt("product_count")
        );
    }
}