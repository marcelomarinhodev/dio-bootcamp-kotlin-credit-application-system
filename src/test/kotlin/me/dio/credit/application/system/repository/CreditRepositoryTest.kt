package me.dio.credit.application.system.repository

import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.utils.TestsUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("tests")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CreditRepositoryTest {

    @Autowired
    lateinit var creditRepository: CreditRepository

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    private lateinit var mockedCustomer: Customer
    private lateinit var mockCreditOne: Credit
    private lateinit var mockCreditTwo: Credit

    @BeforeEach
    fun setUp() {
        mockedCustomer = testEntityManager.persist(TestsUtils.buildCustomer(id = null))
        mockCreditOne = testEntityManager.persist(TestsUtils.buildCredit(customer = mockedCustomer))
        mockCreditTwo = testEntityManager.persist(TestsUtils.buildCredit(customer = mockedCustomer))
    }

    @Test
    fun `findByCreditCode should return corresponding record from database`() {
        val uuidOne = UUID.fromString("1f6ea2c7-d839-4d94-8c81-ab359c50478b")
        mockCreditOne.creditCode = uuidOne

        val uuidTwo = UUID.fromString("2c2f4cfe-6d8d-45a5-a524-b0ac978d1d42")
        mockCreditTwo.creditCode = uuidTwo

        val creditDbOne = creditRepository.findByCreditCode(uuidOne)
        val creditDbTwo = creditRepository.findByCreditCode(uuidTwo)

        assertThat(creditDbOne).isNotNull
        assertThat(creditDbTwo).isNotNull
        assertThat(creditDbOne).isSameAs(mockCreditOne)
        assertThat(creditDbTwo).isSameAs(mockCreditTwo)
    }

    @Test
    fun `findAllByCustomerId should return all credits from customer by informed customerId`() {
        val customerId: Long = 1L
        val creditList = creditRepository.findAllByCustomerId(customerId)

        assertThat(creditList).isNotEmpty
        assertThat(creditList.size).isEqualTo(2)
        assertThat(creditList).contains(mockCreditOne, mockCreditTwo)
    }

    @Test
    fun `findAllByCustomerId should return empty list when providing an invalid customerId`() {
        val customerId: Long = 0L
        val creditList = creditRepository.findAllByCustomerId(customerId)

        assertThat(creditList).isEmpty()
    }


}