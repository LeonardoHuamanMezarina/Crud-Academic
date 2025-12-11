package pe.edu.vallegrande.arquitectura.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "sale_detail")
public class SaleDetail {

    @Id
    @Column(name = "id_sale_detail")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "registration_date")
    private java.util.Date registrationDate;


}
