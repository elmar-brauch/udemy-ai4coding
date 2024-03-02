package de.bsi.ai4coding.product.controller;

import de.bsi.ai4coding.product.model.Price;
import de.bsi.ai4coding.product.model.Product;
import de.bsi.ai4coding.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // TODO demo GET endpoint for all products (add filters later)

    /**
     * GET endpoint to find a Product by its ID.
     * @param id The ID of the Product to be found.
     * @return The Product with the given ID or null if not found.
     */
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productRepository.findById(id).orElse(null);
    }

    /**
     * POST endpoint to create a new Product.
     * @param product The Product to be created.
     * @return The created Product with its ID.
     * @throws ResponseStatusException If the Product is not valid for creation.
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        if (isProductValidForCreation(product)) {
            Product savedProduct = productRepository.save(product);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or null fields in product");
        }
    }

    private boolean isProductValidForCreation(Product product) {
        return product.getName() != null &&
               product.getCategory() != null &&
               product.getPrice() != null &&
               isPriceValid(product.getPrice()) &&
               product.getAttributes() != null &&
               !product.getAttributes().isEmpty();
    }

    private boolean isPriceValid(Price price) {
        return price.getCurrency() != null &&
               price.getDutyFreeAmount() != null &&
               price.getTaxIncludedAmount() != null &&
               price.getTaxOnlyAmount() != null &&
               Objects.nonNull(price.getTaxRate());
    }

    // TODO Bugfix demo
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable UUID id, @RequestBody Product update) {
        if (id != null || update != null || isProductValidForCreation(update)) {
            var existingProduct = productRepository.findById(id).get();
            existingProduct.setName(update.getName());
            existingProduct.setCategory(update.getCategory());
            existingProduct.setPrice(update.getPrice());
            existingProduct.setAttributes(update.getAttributes());
            return productRepository.save(existingProduct);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or null fields in product");
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        productRepository.deleteById(id);
    }
}
