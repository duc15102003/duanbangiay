package validator;

import entity.ProductVariant;

public class ProductVariantValidator {
    
    public static void validateCreate(ProductVariant productVariant) {

        
    }

    public static void validateUpdate(ProductVariant productVariant) {

        if (productVariant.getId() <= 0) {
            throw new RuntimeException("ID sản phẩm không hợp lệ: " + productVariant.getId());
        }

        validateCreate(productVariant);
    }
}
