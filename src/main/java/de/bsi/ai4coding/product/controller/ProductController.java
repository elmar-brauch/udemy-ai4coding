package de.bsi.ai4coding.product.controller;

import de.bsi.ai4coding.product.model.Price;
import de.bsi.ai4coding.product.model.Product;
import de.bsi.ai4coding.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // GET endpoint to find all Products matching filter criteria name and category.
    // Filters are optional. Use annotation for request parameters.
    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String name,
                                     @RequestParam(required = false) Product.Category category) {
        // Validate name to be a German word and category to be a valid enum value.
        // If validation fails, throw a ResponseStatusException with status code 400.
        if (name != null && !name.matches("[A-Za-zÄäÖöÜüß]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        }
        if (category != null && !List.of(Product.Category.values()).contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category");
        }
        if (name != null && category != null) {
            return productRepository.findByNameAndCategory(name, category);
        } else if (name != null) {
            return productRepository.findByName(name);
        } else if (category != null) {
            return productRepository.findByCategory(category);
        } else {
            return productRepository.findAll();
        }
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productRepository.findById(id).orElse(null);
    }

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

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable UUID id, @RequestBody Product productDetails) {
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setCategory(productDetails.getCategory());
            product.setPrice(productDetails.getPrice());
            product.setAttributes(productDetails.getAttributes());
            return productRepository.save(product);
        }).orElseGet(() -> {
            productDetails.setId(id);
            return productRepository.save(productDetails);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        productRepository.deleteById(id);
    }
}
