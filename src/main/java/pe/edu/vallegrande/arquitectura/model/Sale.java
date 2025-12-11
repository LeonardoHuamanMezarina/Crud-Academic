package pe.edu.vallegrande.arquitectura.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
@Entity
@Table(name = "sale")
public class Sale {

    @Id
    @Column(name = "id_sale")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    private String status;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "total_payment")
    private BigDecimal totalPayment;

    @Column(name = "receipt_type")
    private String receiptType;

    @Column(name = "receipt_number")
    private Long receiptNumber;

    @Column(name = "registration_date")
    private Date registrationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User customer;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    private List<SaleDetail> details;



}
