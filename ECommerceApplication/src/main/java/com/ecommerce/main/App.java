package com.ecommerce.main;

import java.util.Map;
import java.util.Scanner;
import com.ecommerce.config.AppConfig;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.Vendor;
import com.ecommerce.service.ProductService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class App {
    public static void main(String[] args) {


        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        ProductService productService = context.getBean(ProductService.class);

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n---------------E-Commerce App-----------");
            System.out.println("1. Add Product");
            System.out.println("2. Find Product by ID");
            System.out.println("3. Update Stock");
            System.out.println("4. Count Products By Vendor");
            System.out.println("5. Exit");
            System.out.println("----------------------------------------");
            System.out.println("Enter an option: ");

            int input = sc.nextInt();
            if (input == 5) {
                System.out.println("Exiting...");
                break;
            }
            switch (input)
            {
                case 1 -> {
                    System.out.println("---Adding a new Product---");
                    sc.nextLine();
                    Product product = new Product();
                    Category category = new Category();
                    Vendor vendor = new Vendor();
                    System.out.println("Enter Product Name: ");
                    product.setName(sc.nextLine());
                    System.out.println("Enter Product Price: ");
                    product.setPrice(sc.nextLong());
                    System.out.println("Enter stock quantity: ");
                    product.setStockQuantity(sc.nextInt());

                    System.out.println("Enter Category ID: ");
                    category.setId(sc.nextInt());
                    product.setCategory(category);
                    System.out.println("Enter the Vendor ID: ");
                    vendor.setId(sc.nextInt());
                    product.setVendor(vendor);

                    try {
                        productService.save(product);
                        System.out.println("Product Successfully added..!");
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    break;

                }
                case 2 -> {
                    System.out.println("--- Product Details ---");
                    System.out.print("Enter Product ID: ");
                    Long id = sc.nextLong();

                    Product product = productService.getProductById(id);
                    //System.out.println(product);

                    if (product != null) {
                        System.out.println("Product Found:");
                        System.out.println("ID: " + product.getId());
                        System.out.println("Name: " + product.getName());
                        System.out.println("Price: ₹" + product.getPrice());
                        System.out.println("Stock Quantity: " + product.getStockQuantity());
                        System.out.println("Category Name: " + product.getCategory().getName());
                        System.out.println("Category Description: " + product.getCategory().getDescription());
                        System.out.println("Vendor Name: " + product.getVendor().getName());
                        System.out.println("Vendor Email: " + product.getVendor().getEmail());
                    } else {
                        System.out.println("No product found with ID: " + id);
                    }
                    break;
                }

                case 3 -> {
                    System.out.println("--- Update Stock Quantity ---");
                    System.out.print("Enter Product ID: ");
                    Long productId = sc.nextLong();

                    System.out.print("Enter New Stock Quantity: ");
                    int newQuantity = sc.nextInt();

                    boolean isUpdated = productService.updateStock(productId, newQuantity);

                    if (isUpdated) {
                        System.out.println("Stock updated successfully for Product ID: " + productId);
                    } else {
                        System.out.println("Update failed: Product ID " + productId + " does not exist.");
                    }
                    break;
                }
                case 4 ->
                {
                    System.out.println("--- Product Count by Vendor ---");
                    Map<String, Integer> vendorProductCounts = productService.countProductsByVendor();

                    if (vendorProductCounts.isEmpty()) {
                        System.out.println("No vendor-product data available.");
                    } else {
                        vendorProductCounts.forEach((vendorName, count) ->
                                System.out.println("Vendor: " + vendorName + " -> " + count + " products")
                        );
                    }
                    break;
                }
                default -> {
                    System.out.println("Invalid option..");
                    return;
                }
            }
        }
        sc.close();
    }
}
