package pe.edu.vallegrande.arquitectura.rest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.vallegrande.arquitectura.dto.SaleRequest;
import pe.edu.vallegrande.arquitectura.dto.SaleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.method.HandlerMethod;
import pe.edu.vallegrande.arquitectura.dto.SaleQuantityRequest;
import pe.edu.vallegrande.arquitectura.service.SaleService;

@RestController
@RequestMapping("/api/v1/sale")
@CrossOrigin("*")
public class SaleRest {
    private final SaleService saleService;

    public SaleRest(SaleService saleService) {
        this.saleService = saleService;
    }

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    @PostMapping("/save/{customerId}")
    public SaleResponse save(@PathVariable Long customerId, @RequestBody SaleRequest saleRequest) {
        System.out.println("=== ENDPOINT SALE ALCANZADO ===");
        System.out.println("SaleRequest recibido (customerId in URL): " + saleRequest + " customerId=" + customerId);
        return saleService.save(customerId, saleRequest);
    }

    @GetMapping("/customer/{customerId}")
    public java.util.List<SaleResponse> listByCustomer(@PathVariable Long customerId) {
        System.out.println("=== Listando ventas para cliente: " + customerId);
        return saleService.listByCustomer(customerId);
    }

    @GetMapping("/summary")
    public pe.edu.vallegrande.arquitectura.dto.SaleSummaryResponse summary(@RequestParam(required = false) String from,
                                                                             @RequestParam(required = false) String to) {
        System.out.println("=== Solicitado resumen de ventas from=" + from + " to=" + to);
        return saleService.summary(from, to);
    }

    @GetMapping("/all")
    public java.util.List<SaleResponse> listAll() {
        System.out.println("=== Listando todas las ventas (ALL)");
        return saleService.listAll();
    }

    @GetMapping("/{saleId}")
    public SaleResponse getSaleById(@PathVariable Long saleId) {
        System.out.println("=== GET sale by id: " + saleId);
        return saleService.getById(saleId);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{saleId}/product/{productId}")
    public SaleResponse updateProductQuantity(@PathVariable Long saleId,
                                              @PathVariable Long productId,
                                              @org.springframework.web.bind.annotation.RequestBody SaleQuantityRequest body) {
        System.out.println("=== PATCH: actualizar cantidad saleId=" + saleId + " productId=" + productId + " qty=" + body.getQuantity());
        return saleService.updateQuantity(saleId, productId, body.getQuantity());
    }

    @org.springframework.web.bind.annotation.PatchMapping("/update/{saleId}")
    public SaleResponse updateProductQuantityByBody(@PathVariable Long saleId,
                                                    @org.springframework.web.bind.annotation.RequestBody(required = true) pe.edu.vallegrande.arquitectura.dto.SaleUpdateRequest body) {
        // Soportar dos formas de petición:
        // 1) { "productId": 2, "quantity": 5 }  --> SaleUpdateRequest.product == null pero client puede send single object
        // 2) { "product": [ { "productId": 2, "quantity": 5 }, ... ] }

        if (body == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        // Caso: body.product presente (batch) -> procesar en una sola operación atómica
        if (body.getProduct() != null && !body.getProduct().isEmpty()) {
            System.out.println("=== PATCH BATCH (atomic): saleId=" + saleId + " items=" + body.getProduct().size());
            return saleService.updateQuantitiesBatch(saleId, body.getProduct());
        }

        // Caso: cliente envía un único objeto con productId y quantity en el root
        // Intentamos mapear a SaleQuantityRequest usando Jackson automatic mapping
        try {
            // Usamos el cuerpo como JSON simple — si el cliente ha enviado un objeto distinto, fallará
            // Para mantener compatibilidad, mostramos mensaje de error claro cuando falten campos
            throw new IllegalArgumentException("Request debe contener 'product' array o un único objeto con productId y quantity. Ej: { \"productId\":2, \"quantity\":1 }");
        } catch (Exception ex) {
            throw ex;
        }
    }

    // Alias RESTful: permitir PATCH /api/v1/sale/{saleId} como forma más RESTful de actualizar cantidades
    @org.springframework.web.bind.annotation.PatchMapping("/{saleId}")
    public SaleResponse updateProductQuantityByBodyAlias(@PathVariable Long saleId,
                                                          @org.springframework.web.bind.annotation.RequestBody(required = true) pe.edu.vallegrande.arquitectura.dto.SaleUpdateRequest body) {
        return updateProductQuantityByBody(saleId, body);
    }

    @DeleteMapping("/{saleId}/delete")
    public org.springframework.http.ResponseEntity<?> deleteSaleLogic(@PathVariable Long saleId) {
        System.out.println("=== DELETE logical sale id (new path): " + saleId);
        saleService.deleteLogic(saleId);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "Venta marcada como inactiva (I) id=" + saleId);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    // Alias directo para evitar cualquier ambigüedad de rutas: /api/v1/sale/delete/{saleId}
    @DeleteMapping("/delete/{saleId}")
    public org.springframework.http.ResponseEntity<?> deleteSaleLogicAlt(@PathVariable Long saleId) {
        System.out.println("=== DELETE logical sale id (alt path): " + saleId);
        saleService.deleteLogic(saleId);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "Venta marcada como inactiva (I) id=" + saleId);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    // Endpoint de depuración para DELETE — INVOCARÁ deleteLogic temporalmente (sin autenticación)
    @RequestMapping("/debug-delete/{saleId}")
    public org.springframework.http.ResponseEntity<?> deleteDebugAny(@PathVariable Long saleId) {
        System.out.println("=== DEBUG DELETE (any method) reached for saleId=" + saleId);
        // Llamada directa al servicio de borrado lógico (temporal para pruebas)
        saleService.deleteLogic(saleId);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "delete performed (debug) id=" + saleId);
        resp.put("id", saleId);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    // Endpoint de depuración para RESTORE — acepta cualquier método y llama a restore(saleId)
    @RequestMapping("/debug-restore/{saleId}")
    public org.springframework.http.ResponseEntity<?> restoreDebugAny(@PathVariable Long saleId) {
        System.out.println("=== DEBUG RESTORE (any method) reached for saleId=" + saleId);
        // Invocar restore en el servicio (temporal para pruebas sin auth)
        saleService.restore(saleId);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "restore performed (debug) id=" + saleId);
        resp.put("id", saleId);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    // Endpoint de prueba para comprobar que rutas con 'delete' llegan al controlador
    @GetMapping("/delete-test/{saleId}")
    public org.springframework.http.ResponseEntity<?> deleteTest(@PathVariable Long saleId) {
        System.out.println("=== GET delete-test reached for saleId=" + saleId);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "delete-test ok");
        resp.put("id", saleId);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    @GetMapping("/_debug/mappings")
    public org.springframework.http.ResponseEntity<?> listMappings() {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        handlerMapping.getHandlerMethods().forEach((info, method) -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            try {
                java.util.Set<String> patterns = info.getPatternsCondition() != null ? info.getPatternsCondition().getPatterns() : java.util.Collections.emptySet();
                java.util.Set<RequestMethod> methods = java.util.Collections.emptySet();
                if (info.getMethodsCondition() != null) {
                    methods = info.getMethodsCondition().getMethods();
                }
                m.put("patterns", patterns);
                m.put("methods", methods);
                m.put("bean", method.getBeanType().getName());
                m.put("handlerMethod", method.getMethod().getName());
            } catch (Exception ex) {
                m.put("error", ex.getMessage());
            }
            list.add(m);
        });
        return org.springframework.http.ResponseEntity.ok(list);
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{saleId}/restore")
    public org.springframework.http.ResponseEntity<?> restoreSale(@PathVariable Long saleId) {
        System.out.println("=== PATCH restore sale id (new path): " + saleId);
        saleService.restore(saleId);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "Venta restaurada a activa (A) id=" + saleId);
        return org.springframework.http.ResponseEntity.ok(resp);
    }
}
