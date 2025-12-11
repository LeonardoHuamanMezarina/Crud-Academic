package pe.edu.vallegrande.arquitectura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.vallegrande.arquitectura.model.SaleDetail;

public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {
}
