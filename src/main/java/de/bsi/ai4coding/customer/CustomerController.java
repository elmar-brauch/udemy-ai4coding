package de.bsi.ai4coding.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final Map<UUID, Customer> customerMap = new HashMap<>();

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
}
