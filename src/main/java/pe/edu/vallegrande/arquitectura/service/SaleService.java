package pe.edu.vallegrande.arquitectura.service;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.vallegrande.arquitectura.dto.SaleRequest;
import pe.edu.vallegrande.arquitectura.dto.SaleQuantityRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.vallegrande.arquitectura.dto.SaleResponse;
import pe.edu.vallegrande.arquitectura.model.Sale;
import pe.edu.vallegrande.arquitectura.model.SaleDetail;
import pe.edu.vallegrande.arquitectura.repository.SaleRepository;
import pe.edu.vallegrande.arquitectura.repository.UserRepository;
import pe.edu.vallegrande.arquitectura.repository.ProductRepository;
import pe.edu.vallegrande.arquitectura.service.SaleService;
import pe.edu.vallegrande.arquitectura.service.EncryptionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Service
public class SaleService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final EncryptionService encryptionService;

    public SaleService(UserRepository userRepository, ProductRepository productRepository,
            SaleRepository saleRepository, EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Genera el siguiente número de recibo (secuencial).
     * Método sincronizado para evitar colisiones simples en concurrencia.
     */
    private synchronized long generateNextReceiptNumber() {
        Sale last = saleRepository.findTopByOrderByReceiptNumberDesc();
        if (last == null || last.getReceiptNumber() == null) {
            return 1L;
        }
        return last.getReceiptNumber() + 1L;
    }

    @Transactional
    public SaleResponse save(SaleRequest request) {
        var customer = userRepository.findById(request.getIdCustomer())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Primera pasada: validar existencia y stock sin modificar nada (operación atómica)
        java.util.List<String> errors = new java.util.ArrayList<>();
        java.util.Map<Long, pe.edu.vallegrande.arquitectura.model.Product> productsCache = new java.util.HashMap<>();

        for (SaleRequest.ProductRequest pr : request.getProduct()) {
            if (pr.getProductId() == null) {
                errors.add("Falta productId en uno de los items");
                continue;
            }

            var optProduct = productRepository.findById(pr.getProductId());
            if (optProduct.isEmpty()) {
                errors.add("Producto no encontrado: id=" + pr.getProductId());
                continue;
            }

            var product = optProduct.get();
            productsCache.put(product.getIdProduct(), product);

            if (product.getStock() < pr.getQuantity()) {
                errors.add("Stock insuficiente para: " + product.getName() + 
                        ". Stock disponible: " + product.getStock() + 
                        ", cantidad solicitada: " + pr.getQuantity());
            }
        }

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }

        // Segunda pasada: aplicar cambios sabiendo que todo pasó la validación
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setRegistrationDate(new java.util.Date());
        sale.setStatus("A");
        sale.setPaymentType(request.getPaymentType());
        sale.setReceiptType(request.getReceiptType());
        sale.setReceiptNumber(request.getReceiptNumber());

        List<SaleDetail> details = new ArrayList<>();
        double total = 0;

        for (SaleRequest.ProductRequest pr : request.getProduct()) {
            var product = productsCache.get(pr.getProductId());

            // actualizar stock
            product.setStock(product.getStock() - pr.getQuantity());
            productRepository.save(product);
            log.info("Producto actualizado: {} - Nuevo stock: {}", product.getName(), product.getStock());

            // calcular subtotal
            double subtotal = product.getUnitPrice() * pr.getQuantity();

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setQuantity(pr.getQuantity());
            detail.setUnitPrice(BigDecimal.valueOf(product.getUnitPrice()));
            detail.setSubtotal(subtotal);
            detail.setRegistrationDate(new java.util.Date());
            detail.setSale(sale);

            details.add(detail);
            total += subtotal;
        }

        sale.setTotalPayment(BigDecimal.valueOf(total));
        // Generar receiptNumber si no viene en el request
        if (request.getReceiptNumber() == null) {
            sale.setReceiptNumber(generateNextReceiptNumber());
        } else {
            sale.setReceiptNumber(request.getReceiptNumber());
        }
        sale.setDetails(details);

        Sale saved = saleRepository.save(sale); // cascada guarda sale + detalles
        return toDto(saved);
    }

    @Transactional
    public SaleResponse save(Long customerId, SaleRequest request) {
        var customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        // Validar primero todos los items
        java.util.List<String> errors = new java.util.ArrayList<>();
        java.util.Map<Long, pe.edu.vallegrande.arquitectura.model.Product> productsCache = new java.util.HashMap<>();

        for (SaleRequest.ProductRequest pr : request.getProduct()) {
            if (pr.getProductId() == null) {
                errors.add("Falta productId en uno de los items");
                continue;
            }

            var optProduct = productRepository.findById(pr.getProductId());
            if (optProduct.isEmpty()) {
                errors.add("Producto no encontrado: id=" + pr.getProductId());
                continue;
            }

            var product = optProduct.get();
            productsCache.put(product.getIdProduct(), product);

            if (product.getStock() < pr.getQuantity()) {
                errors.add("Stock insuficiente para: " + product.getName() + 
                        ". Stock disponible: " + product.getStock() + 
                        ", cantidad solicitada: " + pr.getQuantity());
            }
        }

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }

        // Aplicar cambios
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setRegistrationDate(new java.util.Date());
        sale.setStatus("A");
        sale.setPaymentType(request.getPaymentType());
        sale.setReceiptType(request.getReceiptType());
        sale.setReceiptNumber(request.getReceiptNumber());

        List<SaleDetail> details = new ArrayList<>();
        double total = 0;

        for (SaleRequest.ProductRequest pr : request.getProduct()) {
            var product = productsCache.get(pr.getProductId());

            // actualizar stock
            product.setStock(product.getStock() - pr.getQuantity());
            productRepository.save(product);
            log.info("Producto actualizado: {} - Nuevo stock: {}", product.getName(), product.getStock());

            // calcular subtotal
            double subtotal = product.getUnitPrice() * pr.getQuantity();

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setQuantity(pr.getQuantity());
            detail.setUnitPrice(BigDecimal.valueOf(product.getUnitPrice()));
            detail.setSubtotal(subtotal);
            detail.setRegistrationDate(new java.util.Date());
            detail.setSale(sale);

            details.add(detail);
            total += subtotal;
        }

        sale.setTotalPayment(BigDecimal.valueOf(total));
        // Generar receiptNumber si no viene en el request
        if (request.getReceiptNumber() == null) {
            sale.setReceiptNumber(generateNextReceiptNumber());
        } else {
            sale.setReceiptNumber(request.getReceiptNumber());
        }
        sale.setDetails(details);

        Sale saved = saleRepository.save(sale); // cascada guarda sale + detalles
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public java.util.List<SaleResponse> listByCustomer(Long customerId) {
        var sales = saleRepository.findByCustomer_IdUsuario(customerId);
        return sales.stream().map(SaleService::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public pe.edu.vallegrande.arquitectura.dto.SaleSummaryResponse summary(String from, String to) {
        java.util.List<Sale> sales;
        try {
            if (from != null && to != null) {
                java.time.LocalDate startLocal = java.time.LocalDate.parse(from);
                java.time.LocalDate endLocal = java.time.LocalDate.parse(to);
                java.util.Date start = java.util.Date.from(startLocal.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                java.util.Date end = java.util.Date.from(endLocal.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                sales = saleRepository.findByRegistrationDateBetween(start, end);
            } else {
                sales = saleRepository.findAll();
            }
        } catch (Exception ex) {
            // en caso de parseo inválido, devolver resumen vacío
            sales = saleRepository.findAll();
        }

        long totalSales = sales.size();
        double totalRevenue = sales.stream().mapToDouble(s -> s.getTotalPayment() != null ? s.getTotalPayment().doubleValue() : 0.0).sum();
        long totalProductsSold = sales.stream().flatMap(s -> s.getDetails().stream()).mapToLong(d -> d.getQuantity()).sum();
        double averageSale = totalSales > 0 ? totalRevenue / totalSales : 0.0;

        pe.edu.vallegrande.arquitectura.dto.SaleSummaryResponse resp = new pe.edu.vallegrande.arquitectura.dto.SaleSummaryResponse();
        resp.setTotalSales(totalSales);
        resp.setTotalRevenue(totalRevenue);
        resp.setTotalProductsSold(totalProductsSold);
        resp.setAverageSale(averageSale);
        return resp;
    }

    @Transactional(readOnly = true)
    public java.util.List<SaleResponse> listAll() {
        var sales = saleRepository.findAll();
        return sales.stream().map(SaleService::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleResponse getById(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        return toDto(sale);
    }

    @Transactional
    public void deleteLogic(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        sale.setStatus("I");
        saleRepository.save(sale);
    }

    @Transactional
    public void restore(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        sale.setStatus("A");
        saleRepository.save(sale);
    }

    @Transactional
    public SaleResponse updateQuantity(Long saleId, Long productId, Integer newQuantity) {
        // Autorizar: solo ADMIN o propietario de la venta pueden modificar
        authorizeOwnerOrAdmin(saleId);

        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-null and >= 0");
        }

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        // Buscar el detalle correspondiente al productId
        SaleDetail target = null;
        for (SaleDetail d : sale.getDetails()) {
            if (d.getProduct() != null && d.getProduct().getIdProduct().equals(productId)) {
                target = d;
                break;
            }
        }

        if (target == null) {
            throw new RuntimeException("Detalle de venta no encontrado para producto " + productId);
        }

        int oldQty = target.getQuantity() != null ? target.getQuantity() : 0;
        int delta = newQuantity - oldQty;

        // Ajustar stock del producto
        var product = target.getProduct();
        if (product == null) {
            throw new RuntimeException("Producto asociado al detalle no encontrado");
        }

        if (delta > 0) {
            // Necesitamos sacar stock adicional
            if (product.getStock() < delta) {
                throw new RuntimeException("Stock insuficiente para aumentar cantidad. Disponible: " + product.getStock());
            }
            product.setStock(product.getStock() - delta);
        } else if (delta < 0) {
            // Devolver stock al inventario
            product.setStock(product.getStock() + (-delta));
        }

        productRepository.save(product);

        // Actualizar cantidad y subtotal en detalle
        target.setQuantity(newQuantity);
        double unit = target.getUnitPrice() != null ? target.getUnitPrice().doubleValue() : 0.0;
        double newSubtotal = unit * newQuantity;
        target.setSubtotal(newSubtotal);

        // Recalcular total de la venta sumando subtotales
        double total = sale.getDetails().stream().mapToDouble(d -> d.getSubtotal() != null ? d.getSubtotal() : 0.0).sum();
        sale.setTotalPayment(BigDecimal.valueOf(total));

        Sale saved = saleRepository.save(sale);
        return toDto(saved);
    }

    @Transactional
    public SaleResponse updateQuantitiesBatch(Long saleId, java.util.List<SaleQuantityRequest> items) {
        // Autorizar: solo ADMIN o propietario de la venta pueden modificar
        authorizeOwnerOrAdmin(saleId);

        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La lista 'product' está vacía");
        }

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        // Mapear detalles existentes por productId
        java.util.Map<Long, SaleDetail> detailMap = new java.util.HashMap<>();
        for (SaleDetail d : sale.getDetails()) {
            if (d.getProduct() != null && d.getProduct().getIdProduct() != null) {
                detailMap.put(d.getProduct().getIdProduct(), d);
            }
        }

        // Preparar cambios y validar stock
        java.util.List<pe.edu.vallegrande.arquitectura.model.Product> productsToSave = new java.util.ArrayList<>();

        java.util.List<String> errors = new java.util.ArrayList<>();

        for (SaleQuantityRequest item : items) {
            if (item.getProductId() == null) {
                errors.add("Falta productId en uno de los items");
                continue;
            }

            SaleDetail target = detailMap.get(item.getProductId());
            if (target == null) {
                errors.add("Detalle no encontrado para productId=" + item.getProductId());
                continue;
            }

            int oldQty = target.getQuantity() != null ? target.getQuantity() : 0;
            int newQty = item.getQuantity() != null ? item.getQuantity() : 0;
            int delta = newQty - oldQty;

            var product = target.getProduct();
            if (product == null) {
                errors.add("Producto no encontrado para productId=" + item.getProductId());
                continue;
            }

            if (delta > 0) {
                int available = product.getStock();
                if (available < delta) {
                    errors.add("Stock insuficiente para productId=" + item.getProductId() + ". Disponible: " + available + ". Necesita: " + delta);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }

        // Aplicar cambios (ya validados)
        for (SaleQuantityRequest item : items) {
            SaleDetail target = detailMap.get(item.getProductId());
            int oldQty = target.getQuantity() != null ? target.getQuantity() : 0;
            int newQty = item.getQuantity() != null ? item.getQuantity() : 0;
            int delta = newQty - oldQty;

            var product = target.getProduct();
            if (delta > 0) {
                product.setStock(product.getStock() - delta);
            } else if (delta < 0) {
                product.setStock(product.getStock() + (-delta));
            }
            productsToSave.add(product);

            target.setQuantity(newQty);
            double unit = target.getUnitPrice() != null ? target.getUnitPrice().doubleValue() : 0.0;
            target.setSubtotal(unit * newQty);
        }

        // Guardar productos modificados
        productRepository.saveAll(productsToSave);

        // Recalcular total
        double total = sale.getDetails().stream().mapToDouble(d -> d.getSubtotal() != null ? d.getSubtotal() : 0.0).sum();
        sale.setTotalPayment(BigDecimal.valueOf(total));

        Sale saved = saleRepository.save(sale);
        return toDto(saved);
    }

    /**
     * Autoriza que el usuario actual sea ADMIN o el propietario (cliente) de la venta.
     * Lanza 401 si no está autenticado, 403 si no tiene permiso.
     */
    private void authorizeOwnerOrAdmin(Long saleId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT requerido para acceder a este recurso");
        }

        // Si tiene ROLE_ADMIN, permitir
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        // Obtener el username del principal (subject del token)
        String username = auth.getName();
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token JWT inválido");
        }

        // Buscar el usuario desencriptando los usernames en BD (igual que JwtFilter)
        java.util.List<pe.edu.vallegrande.arquitectura.model.User> allUsers = userRepository.findAll();
        pe.edu.vallegrande.arquitectura.model.User foundUser = null;
        for (pe.edu.vallegrande.arquitectura.model.User u : allUsers) {
            try {
                String dec = encryptionService.decrypt(u.getUsername());
                if (username.equals(dec)) {
                    foundUser = u;
                    break;
                }
            } catch (Exception ex) {
                // ignorar y continuar
            }
        }

        if (foundUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado");
        }

        // Comparar id de cliente de la venta con el id del usuario autenticado
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        if (sale.getCustomer() == null || !sale.getCustomer().getIdUsuario().equals(foundUser.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para modificar esta venta");
        }
    }

    public static SaleResponse toDto(Sale sale) {
        SaleResponse dto = new SaleResponse();
        dto.setSaleId(sale.getId());

        // Convierte la fecha si es necesario
        if (sale.getRegistrationDate() != null) {
            dto.setSaleDate(sale.getRegistrationDate()
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        }

        dto.setTotal(sale.getTotalPayment() != null ? sale.getTotalPayment().doubleValue() : 0.0);
        dto.setState(sale.getStatus());
        dto.setPaymentType(sale.getPaymentType());
        dto.setReceiptType(sale.getReceiptType());
        Long rn = sale.getReceiptNumber();
        dto.setReceiptNumber(rn != null ? String.format("%06d", rn) : null);

        // cliente
        SaleResponse.CustomerDto c = new SaleResponse().new CustomerDto();
        c.setCustomerId(sale.getCustomer().getIdUsuario());
        c.setDni(String.valueOf(sale.getCustomer().getDocumentNumber())); // Si quieres mostrar el número de documento como DNI
        c.setFirstName(sale.getCustomer().getFirstName());
        c.setLastName(sale.getCustomer().getLastName());
        dto.setCustomer(c);

        // productos
        dto.setProducts(
                sale.getDetails().stream().map(d -> {
                    SaleResponse.ProductDetailDto pd = new SaleResponse().new ProductDetailDto();
                    pd.setProductId(d.getProduct().getIdProduct());
                    pd.setName(d.getProduct().getName());
                    pd.setStatus(d.getProduct().getStatus());
                    pd.setDescription(d.getProduct().getDescription());
                    pd.setSalePrice(d.getUnitPrice() != null ? d.getUnitPrice().doubleValue() : 0.0);
                    pd.setQuantity(d.getQuantity());
                    pd.setSubtotal(d.getSubtotal());
                    return pd;
                }).collect(Collectors.toList()));

        return dto;
    }

}
