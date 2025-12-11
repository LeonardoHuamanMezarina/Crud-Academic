package pe.edu.vallegrande.arquitectura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.vallegrande.arquitectura.model.Sale;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
	// Buscar ventas por id del cliente (User.idUsuario)
	List<Sale> findByCustomer_IdUsuario(Long customerId);

	// Buscar ventas por rango de fecha (inclusive)
	List<Sale> findByRegistrationDateBetween(java.util.Date start, java.util.Date end);

	// Obtener la venta con mayor número de comprobante (para calcular el siguiente)
	Sale findTopByOrderByReceiptNumberDesc();
}
