package com.ecommerce.mapper;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.Vendor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ProductMapper implements RowMapper<Product> {

    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Product(
                rs.getInt("prod_id"),
                rs.getString("prod_name"),
                rs.getLong("price"),
                rs.getInt("stock_quantity"),
                new Category(
                        rs.getInt("cat_id"),
                        rs.getString("cat_name"),
                        rs.getString("description")
                ),
                new Vendor(
                        rs.getInt("ven_id"),
                        rs.getString("ven_name"),
                        rs.getString("email")
                )
        );
    }
}