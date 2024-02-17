package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.repository.CreditRepository
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
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ActiveProfiles("tests")
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setUp() {
        customerRepository.deleteAll()
        creditRepository.deleteAll()
    }

    @Test
    fun `saveCredit should return status code 201 when successfully create new credit`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer())

        val creditToSave = TestsUtils.buildCreditDto(customer = savedCustomer)
        val creditToSaveStr = objectMapper.writeValueAsString(creditToSave)

        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(creditToSaveStr)
        ).andExpect(MockMvcResultMatchers.status().isCreated)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `saveCredit should return status code 400 when invalid information is provided`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer())

        val creditToSave = TestsUtils.buildCreditDto(customer = savedCustomer, dayOfFirstInstallment = LocalDate.now())
        val creditToSaveStr = objectMapper.writeValueAsString(creditToSave)

        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(creditToSaveStr)
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
    fun `findAllByCustomerId should return status code 200 when successfully return credit list`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer())
        creditRepository.save(TestsUtils.buildCredit(customer = savedCustomer,
            creditValue = BigDecimal.valueOf(1000)))
        creditRepository.save(TestsUtils.buildCredit(customer = savedCustomer,
            creditValue = BigDecimal.valueOf(2000)))

        mockMvc.perform(MockMvcRequestBuilders.get("${URL}?customerId=${savedCustomer.id}")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `findByCreditCode should return status code 200 when successfully return credit by code`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer())
        val savedCredit = creditRepository.save(TestsUtils.buildCredit(customer = savedCustomer,
            creditValue = BigDecimal.valueOf(1000.0)))

        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${savedCredit.creditCode}?customerId=${savedCustomer.id}")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").value(savedCredit.creditCode.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value(savedCredit.creditValue))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(savedCredit.status.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallment").value(savedCredit.numberOfInstallments))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `findByCreditCode should return status code 400 when providing invalid credit code`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer())
        creditRepository.save(TestsUtils.buildCredit(customer = savedCustomer,
            creditValue = BigDecimal.valueOf(1000.0)))

        val searchedCreditCode = UUID.randomUUID()

        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${searchedCreditCode}?customerId=${savedCustomer.id}")
            .accept(MediaType.APPLICATION_JSON)
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

    @Test
    fun `findByCreditCode should return status code 400 when informing another customer's credit`() {
        val savedCustomer = customerRepository.save(TestsUtils.buildCustomer(firstName = "Another",
            lastName = "Customer", email = "another@email.com", cpf = "123456789"))
        val savedCredit = creditRepository.save(TestsUtils.buildCredit(customer = savedCustomer,
            creditValue = BigDecimal.valueOf(2000.0)))

        val invalidCustomerId = Random().nextLong()

        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${savedCredit.creditCode}?customerId=${invalidCustomerId}")
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class java.lang.IllegalArgumentException")
            ).andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }


}