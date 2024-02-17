package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.utils.TestsUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ActiveProfiles("tests")
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class CustomerResourceTest {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/customers"
    }

    @BeforeEach
    fun setUp() = customerRepository.deleteAll()

    @Test
    fun `saveCustomer should return status code 201 when successfully created new customer`() {
        val customerDto: CustomerDto = TestsUtils.buildCustomerDto()
        val customerDtoStr = objectMapper.writeValueAsString(customerDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON).content(customerDtoStr)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(TestsUtils.buildCustomerDto().firstName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(TestsUtils.buildCustomerDto().lastName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value(TestsUtils.buildCustomerDto().cpf))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(TestsUtils.buildCustomerDto().email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value(TestsUtils.buildCustomerDto().zipCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value(TestsUtils.buildCustomerDto().street))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `saveCustomer should return status code 409 when trying to save a new customer with existing cpf`() {
        val firstCustomer: Customer = TestsUtils.buildCustomer(email = "another@email.com")
        customerRepository.save(firstCustomer)

        val customerDto: CustomerDto = TestsUtils.buildCustomerDto()
        val customerDtoStr = objectMapper.writeValueAsString(customerDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON).content(customerDtoStr)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Conflict! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.dao.DataIntegrityViolationException")
            ).andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `saveCustomer should return status code 409 when trying to save a new customer with existing email`() {
        val firstCustomer: Customer = TestsUtils.buildCustomer(cpf = "64577405024")
        customerRepository.save(firstCustomer)

        val customerDto: CustomerDto = TestsUtils.buildCustomerDto()
        val customerDtoStr = objectMapper.writeValueAsString(customerDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON).content(customerDtoStr)
        ).andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Conflict! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.dao.DataIntegrityViolationException")
            ).andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `saveCustomer should return status code 400 when trying to save a new customer with empty fields`() {
        val customerDto: CustomerDto = TestsUtils.buildCustomerDto(firstName = "")
        val customerDtoStr = objectMapper.writeValueAsString(customerDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON).content(customerDtoStr)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            ).andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `findById should return status code 200 and corresponding customer`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer())

        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${savedCustomer.id}")
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(TestsUtils.buildCustomer().firstName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(TestsUtils.buildCustomer().lastName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value(TestsUtils.buildCustomer().cpf))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(TestsUtils.buildCustomer().email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value(TestsUtils.buildCustomer().address.zipCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value(TestsUtils.buildCustomer().address.street))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `findById should return status code 400 when invalid customerId is provided`() {
        val customerId = 0L

        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${customerId}")
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            ).andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `delete should return status code 200 and corresponding customer`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer())

        mockMvc.perform(MockMvcRequestBuilders.delete("${URL}/${savedCustomer.id}")
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `delete should return status code 400 when invalid customerId is provided`() {
        val customerId = 0L

        mockMvc.perform(MockMvcRequestBuilders.delete("${URL}/${customerId}")
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            ).andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `updateCustomer should return status code 200 when successfully return customer`() {
        val savedCustomer: Customer = customerRepository.save(TestsUtils.buildCustomer())

        val updatedCustomerDto: CustomerUpdateDto = TestsUtils.buildCustomerUpdateDto(lastName = "Edited")
        val updatedCustomerDtoStr = objectMapper.writeValueAsString(updatedCustomerDto)

        mockMvc.perform(MockMvcRequestBuilders.patch("${URL}?customerId=${savedCustomer.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedCustomerDtoStr)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value(updatedCustomerDto.firstName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value(updatedCustomerDto.lastName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value(TestsUtils.buildCustomerDto().cpf))
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(TestsUtils.buildCustomerDto().email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value(updatedCustomerDto.zipCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.street").value(updatedCustomerDto.street))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `update should return status code 400 when invalid customerId is provided`() {
        customerRepository.save(TestsUtils.buildCustomer())
        val updatedCustomerDto: CustomerUpdateDto = TestsUtils.buildCustomerUpdateDto(lastName = "Edited")
        val updatedCustomerDtoStr = objectMapper.writeValueAsString(updatedCustomerDto)

        val customerId = 0L

        mockMvc.perform(MockMvcRequestBuilders.patch("${URL}?customerId=${customerId}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedCustomerDtoStr)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class me.dio.credit.application.system.exception.BusinessException")
            ).andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

}