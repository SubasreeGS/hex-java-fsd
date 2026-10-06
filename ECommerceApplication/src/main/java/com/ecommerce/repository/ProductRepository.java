package com.ecommerce.repository;

import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.mapper.VendorProductCountMapper;
import com.ecommerce.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ProductMapper productMapper;
    private final VendorProductCountMapper vendorProductCountMapper;

    public ProductRepository(JdbcTemplate jdbcTemplate, ProductMapper productMapper, VendorProductCountMapper vendorProductCountMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.productMapper = productMapper;
        this.vendorProductCountMapper = vendorProductCountMapper;
    }

    public void save(Product product) {
        String sql = """
                insert into product(name,price,stock_quantity,
                category_id,vendor_id) values(?,?,?,?,?)
                """;
        jdbcTemplate.update(sql,
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory().getId(),
                product.getVendor().getId());

    }
    public Product getProductById(Long id) {
        String sql = """
            SELECT 
                p.id AS prod_id,
                p.name AS prod_name,
                p.price,
                p.stock_quantity,
                c.id AS cat_id,
                c.name AS cat_name,
                c.description,
                v.id AS ven_id,
                v.name AS ven_name,
                v.email
            FROM product p
            JOIN category c ON p.category_id = c.id
            JOIN vendor v ON p.vendor_id = v.id
            WHERE p.id = ?
        """;
        return jdbcTemplate.queryForObject(sql, productMapper, id);

    }

    public int updateStock(Long productId, int newQuantity) {
        String sql = "UPDATE product SET stock_quantity = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newQuantity, productId);
    }

    public Map<String, Integer> countProductsByVendor() {
        String sql = """
        SELECT v.name AS vendor_name, COUNT(p.id) AS product_count
        FROM vendor v
        JOIN product p ON v.id = p.vendor_id
        GROUP BY v.id, v.name
    """;

        List<Map.Entry<String, Integer>> entries = jdbcTemplate.query(sql, vendorProductCountMapper);

        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
