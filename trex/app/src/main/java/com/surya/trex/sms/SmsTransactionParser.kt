package com.surya.trex.sms

import java.math.BigDecimal

data class ParsedSmsTransaction(
    val amount: BigDecimal,
    val transactionType: String,
    val merchant: String?,
    val description: String,
    val dateTimeMillis: Long
)

object SmsTransactionParser {

    private val amountPatterns = listOf(

        // Rs. 1,250.00
        Regex(
            """(?:RS\.?|INR|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        ),

        // 1,250.00 debited
        Regex(
            """(?:debited|credited|spent|paid|received).*?([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        ),

        // amount of 1250
        Regex(
            """(?:amount|amt)\s*(?:is|of|:)?\s*(?:rs\.?|inr|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        )
    )

    private val expenseKeywords = listOf(
        "debited",
        "debit",
        "spent",
        "spend",
        "paid",
        "purchase",
        "purchased",
        "withdrawn",
        "withdrawal",
        "sent",
        "payment"
    )

    private val incomeKeywords = listOf(
        "credited",
        "credit",
        "received",
        "deposit",
        "deposited",
        "refund",
        "cashback",
        "reverted"
    )

    private val merchantPatterns = listOf(

        Regex(
            """(?:at|to|from|merchant)\s+([A-Za-z0-9 &.'@_-]{2,50})""",
            RegexOption.IGNORE_CASE
        ),

        Regex(
            """(?:for|towards)\s+([A-Za-z0-9 &.'@_-]{2,50})""",
            RegexOption.IGNORE_CASE
        )
    )

    fun parse(
        message: String,
        timestampMillis: Long
    ): ParsedSmsTransaction? {

        val cleanedMessage =
            message
                .replace("\n", " ")
                .replace("\r", " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        if (cleanedMessage.isBlank()) {
            return null
        }

        val amount =
            extractAmount(cleanedMessage)
                ?: return null

        val transactionType =
            detectTransactionType(cleanedMessage)

        val merchant =
            extractMerchant(cleanedMessage)

        return ParsedSmsTransaction(
            amount = amount,
            transactionType = transactionType,
            merchant = merchant,
            description = cleanedMessage,
            dateTimeMillis = timestampMillis
        )
    }

    private fun extractAmount(
        message: String
    ): BigDecimal? {

        for (pattern in amountPatterns) {

            val match =
                pattern.find(message)
                    ?: continue

            val amountText =
                match.groupValues
                    .getOrNull(1)
                    ?.replace(",", "")
                    ?.trim()
                    ?: continue

            try {

                val amount =
                    BigDecimal(amountText)

                if (amount > BigDecimal.ZERO) {
                    return amount
                }

            } catch (_: NumberFormatException) {
                // Try next pattern
            }
        }

        return null
    }

    private fun detectTransactionType(
        message: String
    ): String {

        val normalized =
            message.lowercase()

        val hasIncomeKeyword =
            incomeKeywords.any {
                normalized.contains(it)
            }

        val hasExpenseKeyword =
            expenseKeywords.any {
                normalized.contains(it)
            }

        return when {

            hasIncomeKeyword &&
                    !hasExpenseKeyword ->
                "Income"

            hasExpenseKeyword &&
                    !hasIncomeKeyword ->
                "Expense"

            else ->
                "Expense"
        }
    }

    private fun extractMerchant(
        message: String
    ): String? {

        val candidates = mutableListOf<String>()

        val splitRegex =
            Regex(
                """\s+\b(to|from|on|at|using|via|with|ref|reference)\b\s+""",
                RegexOption.IGNORE_CASE
            )

        for (pattern in merchantPatterns) {

            val matches =
                pattern.findAll(message)

            for (match in matches) {

                val fullMatch =
                    match.groupValues
                        .getOrNull(1)
                        ?.trim()

                if (!fullMatch.isNullOrBlank()) {

                    // Split by keywords to avoid "Account to Merchant" issues
                    val parts =
                        fullMatch.split(splitRegex)

                    candidates.addAll(
                        parts.map { it.trim() }
                    )
                }
            }
        }

        // 1. Try to find candidates that are NOT account numbers
        val filtered =
            candidates
                .map { cleanMerchant(it) }
                .filter {
                    it.isNotBlank() &&
                            it.length >= 2 &&
                            !isLikelyAccount(it)
                }

        if (filtered.isNotEmpty()) {
            return filtered.first()
        }

        // 2. Fallback to any candidate that doesn't look like an account
        val fallback =
            candidates
                .map { cleanMerchant(it) }
                .firstOrNull {
                    it.isNotBlank() &&
                            !isLikelyAccount(it)
                }

        return fallback
            ?: candidates
                .firstOrNull()
                ?.let { cleanMerchant(it) }
    }

    private fun isLikelyAccount(
        text: String
    ): Boolean {

        val normalized =
            text.lowercase()

        // "AC XXXXXX1234", "A/c 1234", etc.
        if (
            normalized.contains(
                Regex("""\bac\b|\baccount\b|\ba/c\b""")
            )
        ) {
            return true
        }

        // Just numbers, X's, spaces, hyphens and stars (often used for masking)
        if (
            normalized.matches(
                Regex("""[x\d\s*-]{4,}""")
            )
        ) {
            return true
        }

        return false
    }

    private fun cleanMerchant(
        merchant: String
    ): String {

        return merchant
            .replace(
                Regex(
                    """\s+(on|using|via|with|from|to|ref|reference|at)\b.*$""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )
            .trim()
            .trim('.', ',', ';', ':')
    }
}
