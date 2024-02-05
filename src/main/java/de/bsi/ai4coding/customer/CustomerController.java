package de.bsi.ai4coding.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final Map<UUID, Customer> customerMap = new HashMap<>();

    // CRUD operations for CustomerController

    @GetMapping
    public List<Customer> getAllCustomers() {
        logger.info("GET /customers - Retrieving all customers");
        return new ArrayList<>(customerMap.values());
    }

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        UUID customerId = UUID.randomUUID();
        customer.setId(customerId);
        customerMap.put(customerId, customer);
        logger.info("POST /customers - Created new customer with ID: {}", customerId);
        return customer;
    }

    /**
     * DELETE endpoint to delete all Customers.
     */
    @DeleteMapping
    public void deleteAllCustomers() {
        customerMap.clear();
        logger.info("DELETE /customers - Deleted all customers");
    }

    /**
     * GET endpoint to find a Customer by its ID.
     * @param id The ID of the Customer to be found.
     * @return The Customer with the given ID or null if not found.
     * @throws ResponseStatusException If the Customer with the given ID is not found.
     */
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable String id) {
        UUID customerId = UUID.fromString(id);
        if (!customerMap.containsKey(customerId)) {
            logger.error("GET /customers/{} - Customer with ID: {} not found", id, customerId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        logger.info("GET /customers/{} - Retrieving customer with ID: {}", id, customerId);
        return customerMap.get(customerId);
    }

    @PutMapping("/{id}")
    /**
     * PUT endpoint to update a Customer by its ID.
     * @param id The ID of the Customer to be updated.
     * @param customer The Customer with updated data.
     * @return The updated Customer with its ID.
     * @throws ResponseStatusException If the Customer with the given ID is not found.
     */
    public Customer updateCustomerById(@PathVariable String id, @RequestBody Customer customer) {
        // If id is not found in customerMap, throw a ResponseStatusException with status code 404.
        UUID customerId = UUID.fromString(id);
        if (!customerMap.containsKey(customerId)) {
            logger.error("PUT /customers/{} - Customer with ID: {} not found", id, customerId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        customer.setId(customerId);
        customerMap.put(customerId, customer);
        logger.info("PUT /customers/{} - Updated customer with ID: {}", id, customerId);
        return customer;
    }

}
