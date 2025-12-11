package pe.edu.vallegrande.arquitectura.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name="product")
public class Product {
    @Id
    @Column(name = "id_product")
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long idProduct;

    @Column(name = "\"name\"")
    private String name;

    @Lob
    @Column(name = "\"specification\"")
    private String specification;

    @Lob
    @Column(name = "\"description\"")
    private String description;

    @Column(name = "\"category\"")
    private String category;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "stock")
    private int stock;

    @Column(name = "\"status\"")
    private String status;

    @Column(name = "internal_code")
    private String internalCode;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "brand_id")
    private Long brandId; // o private int brandId; si es NOT NULL en DB
}
