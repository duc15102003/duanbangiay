package validator;

import entity.Product;

public class ProductValidator {

    public static void validateCreate(Product product) {

        if (product.getCode() == null || product.getCode().isBlank()) {
            throw new RuntimeException("Mã không được để trống");
        }

        if (product.getName() == null || product.getName().isBlank()) {
            throw new RuntimeException("Tên không được để trống");
        }

        if (product.getCategoryId() <= 0) {
            throw new RuntimeException("Danh mục không hợp lệ");
        }

        if (product.getBrandId() <= 0) {
            throw new RuntimeException("Thương hiệu không hợp lệ");
        }

        if (product.getStatus() == null) {
            throw new RuntimeException("Trạng thái không được để trống");
        }
    }

    public static void validateUpdate(Product product) {

        if (product.getId() <= 0) {
            throw new RuntimeException("ID sản phẩm không hợp lệ: " + product.getId());
        }

        validateCreate(product);
    }
}