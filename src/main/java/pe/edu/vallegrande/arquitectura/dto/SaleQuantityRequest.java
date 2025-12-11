package pe.edu.vallegrande.arquitectura.dto;

import lombok.Data;

@Data
public class SaleQuantityRequest {
    private Long productId;
    private Integer quantity;
}
