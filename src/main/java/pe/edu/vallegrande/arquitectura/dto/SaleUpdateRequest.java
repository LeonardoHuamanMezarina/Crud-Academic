package pe.edu.vallegrande.arquitectura.dto;

import lombok.Data;
import java.util.List;

@Data
public class SaleUpdateRequest {
    private List<SaleQuantityRequest> product;
}
