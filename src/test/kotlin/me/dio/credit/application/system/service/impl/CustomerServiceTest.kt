package me.dio.credit.application.system.service.impl

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.utils.TestsUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {
    @MockK
    lateinit var customerRepository: CustomerRepository

    @InjectMockKs
    lateinit var customerService: CustomerService

    @Test
    fun `should create customer`() {
        val mockedCustomer = TestsUtils.buildCustomer()
        every {
            customerRepository.save(any())
        } returns mockedCustomer

        val saved: Customer = customerService.save(mockedCustomer)

        assertThat(saved).isNotNull
        assertThat(saved).isSameAs(mockedCustomer)
        verify(exactly = 1) {
            customerRepository.save(mockedCustomer)
        }
    }

    @Test
    fun `findById should return customer with provided id`() {
        val randomId: Long = Random().nextLong()

        val mockedCustomer = TestsUtils.buildCustomer(id = randomId)

        every {
            customerRepository.findById(randomId)
        } returns Optional.of(mockedCustomer)

        val customer = customerService.findById(randomId)

        assertThat(customer).isNotNull
        assertThat(customer).isSameAs(mockedCustomer)
        assertThat(customer).isExactlyInstanceOf(Customer::class.java)
        verify(exactly = 1) {
            customerRepository.findById(randomId)
        }
    }

    @Test
    fun `findById should throw BusinessException when providing invalid id`() {
        val randomId: Long = Random().nextLong()

        every {
            customerRepository.findById(randomId)
        } returns Optional.empty()

        assertThatExceptionOfType(BusinessException::class.java)
            .isThrownBy {
                customerService.findById(randomId)
            }
            .withMessage("Id $randomId not found")
        verify(exactly = 1) {
            customerRepository.findById(randomId)
        }
    }

    @Test
    fun `delete should delete customer by id`() {
        val randomId: Long = Random().nextLong()

        val mockedCustomer = TestsUtils.buildCustomer(id = randomId)

        every {
            customerRepository.findById(randomId)
        } returns Optional.of(mockedCustomer)
        every {
            customerRepository.delete(mockedCustomer)
        } just runs

        customerService.delete(randomId)
        verify(exactly = 1) {
            customerRepository.findById(randomId)
            customerRepository.delete(mockedCustomer)
        }
    }
}