package com.knowspends.app.domain.repository

import com.knowspends.app.domain.util.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.security.MessageDigest

class CsvImportExportTest {

    private fun computeSourceHash(dateMillis: Long, amountMinor: Long, payee: String, accountId: Long): String {
        val raw = "$dateMillis:$amountMinor:${payee.trim().lowercase()}:$accountId"
        val bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun sourceHash_isDeterministicAndDetectsDuplicates() {
        val date = 1730000000000L
        val amount = 45000L // ₹450.00
        val payee = "Uber India"
        val accountId = 1L

        val hash1 = computeSourceHash(date, amount, payee, accountId)
        val hash2 = computeSourceHash(date, amount, "  UBER INDIA  ", accountId) // Case & trim insensitive
        val hashDifferentAmount = computeSourceHash(date, 50000L, payee, accountId)

        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hashDifferentAmount)
    }

    @Test
    fun csvRowParsing_handlesQuotedFieldsWithCommas() {
        val line = "\"2026-10-30 10:00:00\",EXPENSE,125050,1250.50,\"Food, Dining & Snacks\",Bank,\"Uber, Taxi\",Morning trip"

        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString())

        assertEquals(8, tokens.size)
        assertEquals("2026-10-30 10:00:00", tokens[0])
        assertEquals("EXPENSE", tokens[1])
        assertEquals("125050", tokens[2])
        assertEquals("1250.50", tokens[3])
        assertEquals("Food, Dining & Snacks", tokens[4]) // Successfully parsed comma inside quotes!
        assertEquals("Bank", tokens[5])
        assertEquals("Uber, Taxi", tokens[6])
        assertEquals("Morning trip", tokens[7])
    }

    @Test
    fun csvAmountParsing_handlesNegativeDebitAmounts() {
        val rawInput = "-1,500.50"
        val parsedPaise = MoneyFormatter.parseToPaise(rawInput)
        assertEquals(-150050L, parsedPaise)

        val isNegative = parsedPaise < 0
        val absPaise = Math.abs(parsedPaise)
        assertEquals(true, isNegative)
        assertEquals(150050L, absPaise)
    }

    @Test
    fun csvFormulaInjection_neutralizesDangerousPrefixesInTextFields() {
        val formulaPrefixes = charArrayOf('=', '+', '-', '@', '\t', '\r', '|')
        fun escapeCsv(value: String, isNumeric: Boolean = false): String {
            var sanitized = value
            if (!isNumeric && sanitized.isNotEmpty()) {
                val firstChar = sanitized[0]
                if (firstChar in formulaPrefixes || firstChar == '\'') {
                    sanitized = "'$sanitized"
                }
            }
            return if (sanitized.contains(",") || sanitized.contains("\"") || sanitized.contains("\n") || sanitized.contains("\r")) {
                "\"" + sanitized.replace("\"", "\"\"") + "\""
            } else {
                sanitized
            }
        }

        // Formula injection payloads in text fields
        assertEquals("'=SUM(A1:A10)", escapeCsv("=SUM(A1:A10)"))
        assertEquals("'+cmd|' /C calc'!A0", escapeCsv("+cmd|' /C calc'!A0"))
        assertEquals("'-discount", escapeCsv("-discount"))
        assertEquals("'@SUM(1+1)", escapeCsv("@SUM(1+1)"))
        assertEquals("'\tmalicious_tab", escapeCsv("\tmalicious_tab"))
        assertEquals("'|cmd", escapeCsv("|cmd"))

        // Numeric fields must not be escaped with single quote
        assertEquals("125050", escapeCsv("125050", isNumeric = true))
        assertEquals("1250.50", escapeCsv("1250.50", isNumeric = true))
    }

    @Test
    fun csvFormulaInjection_desanitizesSafelyOnImport() {
        val formulaPrefixes = charArrayOf('=', '+', '-', '@', '\t', '\r', '|')
        fun desanitizeFormula(value: String): String {
            if (value.startsWith("'") && value.length > 1) {
                val secondChar = value[1]
                if (secondChar in formulaPrefixes || secondChar == '\'') {
                    return value.substring(1)
                }
            }
            return value
        }

        assertEquals("=SUM(A1:A10)", desanitizeFormula("'=SUM(A1:A10)"))
        assertEquals("+cmd", desanitizeFormula("'+cmd"))
        assertEquals("-discount", desanitizeFormula("'-discount"))
        assertEquals("@SUM(A1)", desanitizeFormula("'@SUM(A1)"))
        assertEquals("Regular Store", desanitizeFormula("Regular Store"))
    }

    @Test
    fun csvParsing_handlesRfc4180MultilineNotesAndEscapedQuotes() {
        val csvData = """Date,Type,Amount_Minor,Amount_Formatted,Category,Account,Transfer_Account,Payee,Note
"2026-10-30 10:00:00",EXPENSE,50000,500.00,Food,Bank,,"Starbucks","Morning coffee with
croissant and ""fresh"" juice"
"2026-10-31 11:00:00",EXPENSE,25000,250.00,Transport,Bank,,"Uber","Trip home"
"""

        fun parseCsvRecords(reader: java.io.Reader): List<List<String>> {
            val records = mutableListOf<List<String>>()
            val currentRecord = mutableListOf<String>()
            val currentField = StringBuilder()
            var inQuotes = false
            var isFirstChar = true

            var chInt = reader.read()
            while (chInt != -1) {
                val c = chInt.toChar()
                if (isFirstChar) {
                    isFirstChar = false
                    if (c == '\uFEFF') {
                        chInt = reader.read()
                        continue
                    }
                }
                if (inQuotes) {
                    if (c == '"') {
                        reader.mark(1)
                        val nextInt = reader.read()
                        if (nextInt != -1 && nextInt.toChar() == '"') {
                            currentField.append('"')
                        } else {
                            inQuotes = false
                            reader.reset()
                        }
                    } else {
                        currentField.append(c)
                    }
                } else {
                    when (c) {
                        '"' -> inQuotes = true
                        ',' -> {
                            currentRecord.add(currentField.toString())
                            currentField.setLength(0)
                        }
                        '\r' -> {
                            reader.mark(1)
                            val nextInt = reader.read()
                            if (nextInt != -1 && nextInt.toChar() != '\n') {
                                reader.reset()
                            }
                            currentRecord.add(currentField.toString())
                            currentField.setLength(0)
                            if (currentRecord.any { it.isNotBlank() }) {
                                records.add(ArrayList(currentRecord))
                            }
                            currentRecord.clear()
                        }
                        '\n' -> {
                            currentRecord.add(currentField.toString())
                            currentField.setLength(0)
                            if (currentRecord.any { it.isNotBlank() }) {
                                records.add(ArrayList(currentRecord))
                            }
                            currentRecord.clear()
                        }
                        else -> currentField.append(c)
                    }
                }
                chInt = reader.read()
            }
            if (currentField.isNotEmpty() || currentRecord.isNotEmpty()) {
                currentRecord.add(currentField.toString())
                if (currentRecord.any { it.isNotBlank() }) {
                    records.add(ArrayList(currentRecord))
                }
            }
            return records
        }

        val records = parseCsvRecords(csvData.reader())
        assertEquals(3, records.size) // Header + 2 rows

        val row1 = records[1]
        assertEquals("Starbucks", row1[7])
        assertEquals("Morning coffee with\ncroissant and \"fresh\" juice", row1[8])

        val row2 = records[2]
        assertEquals("Uber", row2[7])
        assertEquals("Trip home", row2[8])
    }

    @Test
    fun csvParsing_stripsUtf8BomAtStreamStart() {
        val bomCsv = "\uFEFFDate,Type,Amount_Minor,Amount_Formatted,Category,Account,Transfer_Account,Payee,Note\n2026-10-30 10:00:00,EXPENSE,10000,100.00,Food,Bank,,Cafe,Snack"

        fun parseCsvRecords(reader: java.io.Reader): List<List<String>> {
            val records = mutableListOf<List<String>>()
            val currentRecord = mutableListOf<String>()
            val currentField = StringBuilder()
            var inQuotes = false
            var isFirstChar = true

            var chInt = reader.read()
            while (chInt != -1) {
                val c = chInt.toChar()
                if (isFirstChar) {
                    isFirstChar = false
                    if (c == '\uFEFF') {
                        chInt = reader.read()
                        continue
                    }
                }
                if (inQuotes) {
                    if (c == '"') {
                        reader.mark(1)
                        val nextInt = reader.read()
                        if (nextInt != -1 && nextInt.toChar() == '"') {
                            currentField.append('"')
                        } else {
                            inQuotes = false
                            reader.reset()
                        }
                    } else {
                        currentField.append(c)
                    }
                } else {
                    when (c) {
                        '"' -> inQuotes = true
                        ',' -> {
                            currentRecord.add(currentField.toString())
                            currentField.setLength(0)
                        }
                        '\r' -> {
                            reader.mark(1)
                            val nextInt = reader.read()
                            if (nextInt != -1 && nextInt.toChar() != '\n') {
                                reader.reset()
                            }
                            currentRecord.add(currentField.toString())
                            currentField.setLength(0)
                            if (currentRecord.any { it.isNotBlank() }) {
                                records.add(ArrayList(currentRecord))
                            }
                            currentRecord.clear()
                        }
                        '\n' -> {
                            currentRecord.add(currentField.toString())
                            currentField.setLength(0)
                            if (currentRecord.any { it.isNotBlank() }) {
                                records.add(ArrayList(currentRecord))
                            }
                            currentRecord.clear()
                        }
                        else -> currentField.append(c)
                    }
                }
                chInt = reader.read()
            }
            if (currentField.isNotEmpty() || currentRecord.isNotEmpty()) {
                currentRecord.add(currentField.toString())
                if (currentRecord.any { it.isNotBlank() }) {
                    records.add(ArrayList(currentRecord))
                }
            }
            return records
        }

        val records = parseCsvRecords(bomCsv.reader())
        assertEquals(2, records.size)
        // First column must NOT contain the BOM character
        assertEquals("Date", records[0][0])
    }
}
