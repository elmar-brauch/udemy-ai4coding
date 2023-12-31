package de.bsi.ai4coding.customer;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper jsonMapper;
    
    private Customer testCustomer;

    @Test
    void testCreateCustomer() throws Exception {
        // Create a new customer
        Customer newCustomer = new Customer(null, "John Doe", 30);

        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(newCustomer)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(30))
                .andDo(print());
    }

    @Test
    void testGetAllCustomers() throws Exception {
        // Create some sample customers
        Customer customer1 = new Customer(null, "John Doe", 30);
//        Customer customer2 = new Customer(null, "Bob", 35);

        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(customer1)))
                .andExpect(MockMvcResultMatchers.status().isOk());

//        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(jsonMapper.writeValueAsString(customer2)))
//                .andExpect(MockMvcResultMatchers.status().isOk());

        // Get all customers
        mockMvc.perform(MockMvcRequestBuilders.get("/customers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("John Doe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].age").value(30))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").exists())
//                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Bob"))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[1].age").value(35))
                .andDo(print());
    }
}
