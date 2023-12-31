package de.bsi.ai4coding.customer;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CustomerController.class)
class CustomerControllerBuggyTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper jsonMapper;
    
    @BeforeEach
    void setup(@Autowired CustomerController controller) throws Exception {
    	controller.deleteAllCustomers();
    	createCustomer(null, 35);
    	createCustomer("John Doe", 30);
    }

    @RepeatedTest(1)
    void testGetAllCustomers() throws Exception {
        var responseBody = mockMvc.perform(MockMvcRequestBuilders.get("/customers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        var customers = jsonMapper.readValue(responseBody, new TypeReference<List<Customer>>() {});
        
        Assertions.assertTrue(customers.stream()
        		.map(Customer::getName)
        		.anyMatch(name -> name.equals("John Doe")));
        
        //Assertions.assertEquals(35, customers.getFirst().getAge());
    }
    
    private void createCustomer(String name, int age) throws Exception {
    	var customer = new Customer(null, name, age);
    	mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(customer)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
