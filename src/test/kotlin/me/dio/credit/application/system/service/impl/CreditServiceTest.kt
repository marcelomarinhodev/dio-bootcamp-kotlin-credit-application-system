package me.dio.credit.application.system.service.impl

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.utils.TestsUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.*

@ActiveProfiles("tests")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(MockKExtension::class)
class CreditServiceTest {

    @MockK
    lateinit var creditRepository: CreditRepository

    @MockK
    lateinit var customerService: CustomerService

    @InjectMockKs
    lateinit var creditService: CreditService

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Test
    fun `save should create new credit`() {
        val customerMock = TestsUtils.buildCustomer()
        val creditMock = TestsUtils.buildCredit(customer = customerMock)

        every {
            creditRepository.save(any())
        } returns creditMock

        every {
            customerService.findById(any())
        } returns TestsUtils.buildCustomer(id = 1L)

        val savedCredit: Credit = creditService.save(creditMock)

        Assertions.assertThat(savedCredit).isNotNull
        Assertions.assertThat(savedCredit).isSameAs(creditMock)
        verify(exactly = 1) {
            creditRepository.save(creditMock)
            customerService.findById(1L)
        }
    }

    @Test
    fun `save should throw BusinessException when providing invalid dayFirstInstallment`() {
        val customerMock = TestsUtils.buildCustomer()
        val creditMock =
            TestsUtils.buildCredit(customer = customerMock, dayFirstInstallment = LocalDate.now().plusMonths(4))

        every {
            creditRepository.save(any())
        } returns creditMock

        every {
            customerService.findById(any())
        } returns TestsUtils.buildCustomer(id = 1L)

        Assertions.assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy {
                creditService.save(creditMock)
            }
            .withMessage("Invalid Date")
        verify(exactly = 0) {
            creditRepository.save(creditMock)
        }
    }

    @Test
    fun `findAllByCustomer should call creditRepository findAllByCustomerId`() {
        every {
            creditRepository.findAllByCustomerId(any())
        } returns mutableListOf()

        creditService.findAllByCustomer(1L)

        verify(exactly = 1) {
            creditRepository.findAllByCustomerId(1L)
        }
    }

    @Test
    fun `findByCreditCode should return corresponding credit`() {
        val customerMock = TestsUtils.buildCustomer(id = null)
        val savedCustomer = testEntityManager.persist(customerMock)

        val creditMock = TestsUtils.buildCredit(customer = savedCustomer)
        val savedCredit = testEntityManager.persist(creditMock)

        every {
            creditRepository.findByCreditCode(any())
        } returns savedCredit

        every {
            customerService.findById(any())
        } returns savedCustomer

        val credit = creditService.findByCreditCode(savedCustomer.id!!, savedCredit.creditCode)

        Assertions.assertThat(savedCredit).isNotNull
        verify(exactly = 1) {
            creditRepository.findByCreditCode(credit.creditCode)
        }
    }

    @Test
    fun `findByCreditCode should throw BusinessException when providing invalid code`() {
        val customerMock = TestsUtils.buildCustomer(id = null)
        val savedCustomer = testEntityManager.persist(customerMock)

        every {
            creditRepository.findByCreditCode(any())
        } returns null

        val randomUUID = UUID.randomUUID()
        val expectedMessage = "Creditcode $randomUUID not found"

        Assertions.assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy {
                creditService.findByCreditCode(savedCustomer.id!!, randomUUID)
            }
            .withMessage(expectedMessage)
        verify(exactly = 1) {
            creditRepository.findByCreditCode(randomUUID)
        }
    }

    @Test
    fun `findByCreditCode should throw IllegalArgumentException when providing code from another customer`() {
        val customerMock = TestsUtils.buildCustomer(id = null)
        val savedCustomer = testEntityManager.persist(customerMock)

        val creditMock = TestsUtils.buildCredit(customer = savedCustomer)
        val savedCredit = testEntityManager.persist(creditMock)

        every {
            creditRepository.findByCreditCode(any())
        } returns savedCredit

        Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy {
                creditService.findByCreditCode(Random().nextLong(), savedCredit.creditCode)
            }
            .withMessage("Contact admin")
        verify(exactly = 1) {
            creditRepository.findByCreditCode(savedCredit.creditCode)
        }
    }
}