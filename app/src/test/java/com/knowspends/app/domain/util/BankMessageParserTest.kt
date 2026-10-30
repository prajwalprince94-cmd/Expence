package com.knowspends.app.domain.util

import com.knowspends.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankMessageParserTest {

    @Test
    fun `parse upi expense from google pay or phonepe`() {
        val message = "Paid ₹450 to Starbucks on 29 Oct 2026. UPI Ref 3290123910."
        val parsed = BankMessageParser.parse(message)

        assertNotNull(parsed)
        parsed?.let {
            assertEquals(45000L, it.amountMinor)
            assertEquals(TransactionType.EXPENSE, it.type)
            assertEquals("Starbucks", it.merchantOrPayee)
        }
    }

    @Test
    fun `parse upi income or received money`() {
        val message = "Received ₹2,500.50 from Rohit Sharma via UPI."
        val parsed = BankMessageParser.parse(message)

        assertNotNull(parsed)
        parsed?.let {
            assertEquals(250050L, it.amountMinor)
            assertEquals(TransactionType.INCOME, it.type)
            assertTrue(it.merchantOrPayee.contains("Rohit Sharma"))
        }
    }

    @Test
    fun `parse bank debit sms with account number`() {
        val message = "Rs.1,500.00 debited from HDFC Bank A/c ending with **1234 to SWIGGY on 28-OCT-26"
        val parsed = BankMessageParser.parse(message)

        assertNotNull(parsed)
        parsed?.let {
            assertEquals(150000L, it.amountMinor)
            assertEquals(TransactionType.EXPENSE, it.type)
            assertEquals("SWIGGY", it.merchantOrPayee)
            assertEquals("**1234", it.accountTail)
        }
    }

    @Test
    fun `parse bank credit or salary notification`() {
        val message = "INR 85,000.00 credited to Acct XX9876 on 30-OCT-26 by TECH CORP SALARY"
        val parsed = BankMessageParser.parse(message)

        assertNotNull(parsed)
        parsed?.let {
            assertEquals(8500000L, it.amountMinor)
            assertEquals(TransactionType.INCOME, it.type)
            assertTrue(it.merchantOrPayee.contains("TECH CORP SALARY"))
            assertEquals("XX9876", it.accountTail)
        }
    }

    @Test
    fun `otp and security codes are strictly rejected`() {
        val otp1 = "Your OTP for transaction at Amazon is 549102. Do not share with anyone."
        val otp2 = "482019 is your secret verification code to log in."

        assertNull(BankMessageParser.parse(otp1))
        assertNull(BankMessageParser.parse(otp2))
    }

    @Test
    fun `marketing and spam messages are strictly rejected`() {
        val spam = "Congratulations! You have pre-approved personal loan of Rs. 5,00,000. Click here to claim now."

        assertNull(BankMessageParser.parse(spam))
    }
}
