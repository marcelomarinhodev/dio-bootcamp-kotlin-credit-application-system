package me.dio.credit.application.system.utils

import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import java.math.BigDecimal
import java.time.LocalDate

object TestsUtils {
    fun buildCustomer(
        firstName: String = "Customer",
        lastName: String = "Mock",
        cpf: String = "64030983065",
        email: String = "e@mail.com",
        password: String = "password",
        zipCode: String = "123456",
        street: String = "Rua Tal",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        id: Long? = 1L
    ) = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        income = income,
        password = password,
        address = Address(
            zipCode,
            street
        ),
        id = id
    )

    fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(500.0),
        dayFirstInstallment: LocalDate = LocalDate.now().plusDays(3),
        numberOfInstallments: Int = 5,
        customer: Customer
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer
    )

    fun buildCustomerDto(
        firstName: String = "Customer",
        lastName: String = "Dto",
        cpf: String = "64030983065",
        email: String = "e@mail.com",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        password: String = "password",
        zipCode: String = "123456",
        street: String = "Rua Tal",
    ) = CustomerDto(
        firstName, lastName,
        cpf, income, email, password, zipCode, street
    )

    fun buildCustomerUpdateDto(
        firstName: String = "Customer",
        lastName: String = "Update",
        income: BigDecimal = BigDecimal.valueOf(1000.0),
        zipCode: String = "123456",
        street: String = "Rua Tal",
    ) = CustomerUpdateDto(
        firstName, lastName, income, zipCode, street
    )

    fun buildCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(500),
        dayOfFirstInstallment: LocalDate = LocalDate.now().plusDays(1),
        numberOfInstallments: Int = 3,
        customer: Customer
    ) = CreditDto(
        creditValue,
        dayOfFirstInstallment,
        numberOfInstallments,
        customerId = customer.id!!
    )
}