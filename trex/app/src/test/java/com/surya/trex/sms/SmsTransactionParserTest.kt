package com.surya.trex.sms

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class SmsTransactionParserTest {

    @Test
    fun testParseExpenseWithRs() {
        val sms = "Your a/c no. XXX123 has been debited by Rs. 1,250.00 on 2026-09-22 at Amazon India."
        val parsed = SmsTransactionParser.parse(sms, 1774567890000L)
        assertNotNull(parsed)
        assertEquals(BigDecimal("1250.00"), parsed!!.amount)
        assertEquals("Expense", parsed.transactionType)
        assertEquals("Amazon India", parsed.merchant)
        assertEquals(1774567890000L, parsed.dateTimeMillis)
    }

    @Test
    fun testParseIncomeWithReceived() {
        val sms = "Rs 5,000.00 received in your account from John Doe ref 123456"
        val parsed = SmsTransactionParser.parse(sms, 1774567890000L)
        assertNotNull(parsed)
        assertEquals(BigDecimal("5000.00"), parsed!!.amount)
        assertEquals("Income", parsed.transactionType)
        assertEquals("John Doe", parsed.merchant)
    }

    @Test
    fun testParseInvalidSms() {
        val sms = "Hello, your OTP for login is 4829."
        val parsed = SmsTransactionParser.parse(sms, 1774567890000L)
        assertNull(parsed)
    }

    @Test
    fun testParseUpiDebit() {
        val sms = "Money Transfer: Rs 500.00 debited from AC XXXXXX1234 to UPI ID user@okaxis on 22-09-26"
        val parsed = SmsTransactionParser.parse(sms, 1774567890000L)
        assertNotNull(parsed)
        println("Parsed merchant: ${parsed?.merchant}")
        assertEquals(BigDecimal("500.00"), parsed!!.amount)
        assertEquals("Expense", parsed.transactionType)
        assertEquals("UPI ID user@okaxis", parsed.merchant)
    }
}
