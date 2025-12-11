package pe.edu.vallegrande.arquitectura.dto;

import lombok.Data;

@Data
public class SaleSummaryResponse {
    private long totalSales;
    private double totalRevenue;
    private long totalProductsSold;
    private double averageSale;
}
