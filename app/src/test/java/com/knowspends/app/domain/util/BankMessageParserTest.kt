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

    @Test
    fun `detect bank names, upi apps and infer categories accurately`() {
        // HDFC + Swiggy (Food & Dining)
        val hdfcMsg = "Dear UPI user, Rs. 340.00 debited from HDFC Bank A/c **4321 to Swiggy via PhonePe on 01-Nov-26"
        val hdfcParsed = BankMessageParser.parse(hdfcMsg, sender = "VM-HDFCBK")
        assertNotNull(hdfcParsed)
        assertEquals("HDFC Bank", hdfcParsed?.bankName)
        assertEquals("PhonePe", hdfcParsed?.upiOrWalletApp)
        assertEquals("Food & Dining", hdfcParsed?.suggestedCategoryName)

        // SBI + Uber (Transportation)
        val sbiMsg = "Rs 450.00 debited from SBI a/c no. XX8899 on 01Nov26 at Uber India via Google Pay."
        val sbiParsed = BankMessageParser.parse(sbiMsg, sender = "AD-SBIINB")
        assertNotNull(sbiParsed)
        assertEquals("State Bank of India", sbiParsed?.bankName)
        assertEquals("Google Pay", sbiParsed?.upiOrWalletApp)
        assertEquals("Transportation", sbiParsed?.suggestedCategoryName)

        // ICICI + Blinkit (Groceries)
        val iciciMsg = "Acct XX5511 debited by INR 620.00 on 01-Nov-26 for transaction at Blinkit Groceries"
        val iciciParsed = BankMessageParser.parse(iciciMsg)
        assertNotNull(iciciParsed)
        assertEquals("Groceries", iciciParsed?.suggestedCategoryName)

        // Axis Bank + Net Banking Salary (Salary)
        val axisMsg = "Axis Bank: INR 95,000 credited to a/c XX1234 on 01-Nov-26 by Salary Oct 2026"
        val axisParsed = BankMessageParser.parse(axisMsg)
        assertNotNull(axisParsed)
        assertEquals("Axis Bank", axisParsed?.bankName)
        assertEquals(TransactionType.INCOME, axisParsed?.type)
        assertEquals("Salary", axisParsed?.suggestedCategoryName)
    }

    @Test
    fun `parse real-world sbi, icici, axis and vpa formats`() {
        // SBI format with debited by ... transfer to
        val sbi = "Dear SBI UPI User, A/C 1234 debited by 500.0 on 28Oct26 transfer to ZOMATO Ref No 123456. If not you, call 1800."
        val sbiParsed = BankMessageParser.parse(sbi, sender = "VK-SBIUPI")
        assertNotNull(sbiParsed)
        assertEquals(50000L, sbiParsed?.amountMinor)
        assertEquals(TransactionType.EXPENSE, sbiParsed?.type)
        assertEquals("ZOMATO", sbiParsed?.merchantOrPayee)
        assertEquals("State Bank of India", sbiParsed?.bankName)

        // ICICI format with debited for
        val icici = "Your A/C ending with 5678 has been debited for INR 1,250.00 on 28-Oct-26. Info: UPI-SWIGGY-swiggy@axis. Available Balance is INR 24,000.00."
        val iciciParsed = BankMessageParser.parse(icici, sender = "BP-ICICIB")
        assertNotNull(iciciParsed)
        assertEquals(125000L, iciciParsed?.amountMinor)
        assertEquals("SWIGGY", iciciParsed?.merchantOrPayee)
        assertEquals("ICICI Bank", iciciParsed?.bankName)

        // Axis format with debited with ... towards
        val axis = "Your A/C no. XX1234 is debited with INR 899.00 on 29-10-26 towards AMAZON. UPI Ref 123456."
        val axisParsed = BankMessageParser.parse(axis, sender = "AD-AXISBK")
        assertNotNull(axisParsed)
        assertEquals(89900L, axisParsed?.amountMinor)
        assertEquals("AMAZON", axisParsed?.merchantOrPayee)
        assertEquals("Axis Bank", axisParsed?.bankName)
    }
}
