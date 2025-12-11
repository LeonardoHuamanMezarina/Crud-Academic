package pe.edu.vallegrande.arquitectura.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleResponse {

    private Long saleId;
    private CustomerDto customer;
    private LocalDateTime saleDate;
    private Double total;
    private String state;
    private String paymentType;
    private String receiptType;
    // Formato con ceros a la izquierda: "000001"
    private String receiptNumber;
    private List<ProductDetailDto> products;

    @Data
    public class ProductDetailDto {
        private Long productId;
        private String name;
        private String description;
        private String status;
        private Double salePrice;
        private Integer quantity;
        private Double subtotal;
    }

    @Data
    public class CustomerDto {
        private Long customerId;
        private String dni;
        private String firstName;
        private String lastName;
    }
}
