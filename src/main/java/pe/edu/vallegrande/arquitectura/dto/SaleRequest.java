package pe.edu.vallegrande.arquitectura.dto;

import lombok.Data;
import java.util.List;

@Data
public class SaleRequest {
    private Long idCustomer;
    private String paymentType;
    private String receiptType;
    private Long receiptNumber;
    private List<ProductRequest> product;

    @Data
    public static class ProductRequest {
        private Long productId;
        private int quantity;

    }
}
