package pe.edu.vallegrande.arquitectura.rest;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.arquitectura.model.Product;
import pe.edu.vallegrande.arquitectura.service.ProductService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("api/v1/product")

public class ProductRest {

    private ProductService productService;

    @GetMapping("/listar")
    public List<Product> getAll() {
        return productService.getAll();
    }

    /**
     * Endpoint específico para CLIENTES - Solo productos activos
     */
    @GetMapping("/disponibles")
    public List<Product> getAvailableProducts() {
        return productService.getActiveProducts();
    }

    /*Listado por ID*/
    @GetMapping("/listar/id/{id}")
    public Optional<Product> findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/listar/estado/{status}")
    public List<Product> findByEstado(@PathVariable String status) {
        return productService.findByStatus(status);
    }

    @PostMapping("/crear")
    public Product create(@RequestBody Product product) {
        return productService.create(product);
    }

    @PutMapping("/editar")
    public Product update(@RequestBody Product product) {
        return productService.update(product);
    }

    @DeleteMapping("/eliminar/{id}")
    public Product delete(@PathVariable Long id) {
        return productService.delete(id);
    }

    @PutMapping("/restaurar/{id}")
    public Product restaurar(@PathVariable Long id) {
        return productService.restore(id);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generateJasperPdfReport() {
        try {
            byte[] pdf = productService.generateJasperPdfReport();
            return ResponseEntity.ok()
                    //Renombrar el archivo PDF al descargar
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_productos.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


}
//esto es de ProductRest.java
