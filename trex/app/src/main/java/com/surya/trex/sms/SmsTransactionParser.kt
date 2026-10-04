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

    // =========================================================
    // AMOUNT PATTERNS
    // =========================================================

    private val amountPatterns = listOf(

        // Rs.1.00
        // Rs 1.00
        // INR 1.00
        // ₹1.00
        Regex(
            """(?:RS\.?|INR|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        ),

        // Sent Rs.500
        // Debited Rs.500
        // Credited Rs.500
        // Received Rs.500
        Regex(
            """(?:sent|debited|credited|spent|paid|received)\s+(?:rs\.?|inr|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        ),

        // 500 debited
        // 500 credited
        Regex(
            """([0-9,]+(?:\.[0-9]{1,2})?)\s+(?:debited|credited|spent|paid|received)""",
            RegexOption.IGNORE_CASE
        ),

        // amount 500
        // amount of 500
        // amt: 500
        Regex(
            """(?:amount|amt)\s*(?:is|of|:)?\s*(?:rs\.?|inr|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
            RegexOption.IGNORE_CASE
        )
    )

    // =========================================================
    // EXPENSE KEYWORDS
    // =========================================================

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
        "payment",
        "transferred",
        "transfer"
    )

    // =========================================================
    // INCOME KEYWORDS
    // =========================================================

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

    // =========================================================
    // MAIN PARSER
    // =========================================================

    fun parse(
        message: String,
        timestampMillis: Long
    ): ParsedSmsTransaction? {

        val cleanedMessage = normalizeMessage(message)

        if (cleanedMessage.isBlank()) {
            return null
        }

        // -----------------------------------------------------
        // Extract amount
        // -----------------------------------------------------

        val amount = extractAmount(cleanedMessage)
            ?: return null

        // -----------------------------------------------------
        // Detect type
        // -----------------------------------------------------

        val transactionType =
            detectTransactionType(cleanedMessage)

        // -----------------------------------------------------
        // Extract merchant
        // -----------------------------------------------------

        val merchant =
            if (transactionType == "Income") {
                extractCreditSender(cleanedMessage)
            } else {
                extractDebitReceiver(cleanedMessage)
            }

        // -----------------------------------------------------
        // Create description
        // -----------------------------------------------------

        val description =
            createDescription(
                transactionType = transactionType,
                merchant = merchant
            )

        return ParsedSmsTransaction(
            amount = amount,
            transactionType = transactionType,
            merchant = merchant,
            description = description,
            dateTimeMillis = timestampMillis
        )
    }

    // =========================================================
    // NORMALIZE MESSAGE
    // =========================================================

    private fun normalizeMessage(
        message: String
    ): String {

        return message
            .replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // =========================================================
    // AMOUNT EXTRACTION
    // =========================================================

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

    // =========================================================
    // TRANSACTION TYPE
    // =========================================================

    private fun detectTransactionType(
        message: String
    ): String {

        val text =
            message.lowercase()

        val strongDebit =
            Regex(
                """\b(debited|debit|sent|spent|paid|withdrawn|withdrawal)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(text)

        val strongCredit =
            Regex(
                """\b(credited|credit|received|deposit|deposited|cashback|refund)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(text)

        return when {

            strongDebit && !strongCredit ->
                "Expense"

            strongCredit && !strongDebit ->
                "Income"

            strongDebit && strongCredit -> {

                val debitMatch =
                    Regex(
                        """\b(debited|debit|sent|spent|paid|withdrawn|withdrawal)\b""",
                        RegexOption.IGNORE_CASE
                    ).find(text)

                val creditMatch =
                    Regex(
                        """\b(credited|credit|received|deposit|deposited|cashback|refund)\b""",
                        RegexOption.IGNORE_CASE
                    ).find(text)

                when {

                    debitMatch == null ->
                        "Income"

                    creditMatch == null ->
                        "Expense"

                    debitMatch.range.first <
                            creditMatch.range.first ->
                        "Expense"

                    else ->
                        "Income"
                }
            }

            else ->
                "Expense"
        }
    }

    // =========================================================
    // CREDIT / INCOME SENDER
    // =========================================================

    private fun extractCreditSender(
        message: String
    ): String? {

        // =====================================================
        // PATTERN 1
        //
        // credited by JOHN DOE
        //
        // Example:
        // A/c XX1234 credited by JOHN DOE with Rs.500
        // =====================================================

        val creditedByPattern =
            Regex(
                """\bcredited\s+by\s+(.+?)(?=\s+(?:with|for|on|at|via|using|rs|inr|₹|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        creditedByPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val sender =
                    cleanCreditSender(candidate)

                if (isValidCreditSender(sender)) {
                    return sender
                }
            }

        // =====================================================
        // PATTERN 2
        //
        // credited ... by JOHN DOE
        //
        // YOUR INDIAN BANK FORMAT:
        //
        // Your A/c *XXX is credited with Rs.1.00
        // on 02-10-26 by Surya Nagarajan.
        // RRN ...
        // =====================================================

        val creditedAnythingByPattern =
            Regex(
                """\bcredited\b.*?\bby\s+(.+?)(?=\s+(?:rrn|utr|ref|reference|available|avl|balance)\b|[.;]|$)""",
                RegexOption.IGNORE_CASE
            )

        creditedAnythingByPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val sender =
                    cleanCreditSender(candidate)

                if (isValidCreditSender(sender)) {
                    return sender
                }
            }

        // =====================================================
        // PATTERN 3
        //
        // credited from JOHN DOE
        // =====================================================

        val creditedFromPattern =
            Regex(
                """\bcredited\s+from\s+(.+?)(?=\s+(?:with|for|on|at|via|using|rs|inr|₹|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        creditedFromPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val sender =
                    cleanCreditSender(candidate)

                if (isValidCreditSender(sender)) {
                    return sender
                }
            }

        // =====================================================
        // PATTERN 4
        //
        // received from JOHN DOE
        // =====================================================

        val receivedFromPattern =
            Regex(
                """\breceived\s+from\s+(.+?)(?=\s+(?:with|for|on|at|via|using|rs|inr|₹|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        receivedFromPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val sender =
                    cleanCreditSender(candidate)

                if (isValidCreditSender(sender)) {
                    return sender
                }
            }

        // =====================================================
        // PATTERN 5
        //
        // deposited by JOHN DOE
        // =====================================================

        val depositedByPattern =
            Regex(
                """\bdeposited\s+by\s+(.+?)(?=\s+(?:with|for|on|at|via|using|rs|inr|₹|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        depositedByPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val sender =
                    cleanCreditSender(candidate)

                if (isValidCreditSender(sender)) {
                    return sender
                }
            }

        // =====================================================
        // IMPORTANT
        //
        // Do NOT use a generic "from" pattern here.
        //
        // Otherwise messages containing:
        //
        // "from your account"
        // "from your bank"
        //
        // can incorrectly become the sender.
        // =====================================================

        return null
    }

    // =========================================================
    // CREDIT SENDER CLEANING
    // =========================================================

    private fun cleanCreditSender(
        value: String
    ): String {

        var result =
            value.trim()

        // -----------------------------------------------------
        // Remove amount
        // -----------------------------------------------------

        result =
            result.replace(
                Regex(
                    """(?:rs\.?|inr|₹)\s*[0-9,]+(?:\.[0-9]{1,2})?""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // -----------------------------------------------------
        // Remove date
        // -----------------------------------------------------

        result =
            result.replace(
                Regex(
                    """\b\d{1,2}[-/]\d{1,2}[-/]\d{2,4}\b"""
                ),
                ""
            )

        // -----------------------------------------------------
        // Remove time
        // -----------------------------------------------------

        result =
            result.replace(
                Regex(
                    """\b\d{1,2}:\d{2}(?::\d{2})?\s*(?:am|pm)?\b""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // -----------------------------------------------------
        // Remove RRN / UTR / reference
        // -----------------------------------------------------

        result =
            result.replace(
                Regex(
                    """\b(?:rrn|utr|ref|reference)\s*[:#-]?\s*[A-Za-z0-9]+\b""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // -----------------------------------------------------
        // Remove available balance and everything after it
        // -----------------------------------------------------

        result =
            result.replace(
                Regex(
                    """\b(?:available\s+balance|avl\s+bal|balance)\b.*$""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // -----------------------------------------------------
        // Remove common bank names
        // -----------------------------------------------------

        result =
            result.replace(
                Regex(
                    """\s*-\s*(?:Indian Bank|SBI|HDFC Bank|ICICI Bank|Axis Bank).*$""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // -----------------------------------------------------
        // Remove common banking words
        // -----------------------------------------------------

        result =
            result.replace(
                Regex(
                    """\b(?:credited|credit|received|deposit|deposited|amount|balance|available|avl)\b""",
                    RegexOption.IGNORE_CASE
                ),
                " "
            )

        // -----------------------------------------------------
        // Remove punctuation
        // -----------------------------------------------------

        result =
            result
                .replace(Regex("[,:;.]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        return result
    }

    // =========================================================
    // VALIDATE CREDIT SENDER
    // =========================================================

    private fun isValidCreditSender(
        value: String?
    ): Boolean {

        if (value.isNullOrBlank()) {
            return false
        }

        val text =
            value.trim()

        if (text.length < 2) {
            return false
        }

        val lower =
            text.lowercase()

        // -----------------------------------------------------
        // Things that must NEVER be a sender
        // -----------------------------------------------------

        val invalidSenderPhrases =
            listOf(
                "your",
                "yours",
                "your account",
                "your a/c",
                "your ac",
                "your bank",
                "account",
                "a/c",
                "balance",
                "available balance",
                "avl bal",
                "bank",
                "amount",
                "transaction",
                "credited",
                "credit",
                "received",
                "deposit",
                "deposited",
                "rrn",
                "utr",
                "reference"
            )

        if (
            invalidSenderPhrases.any { phrase ->

                lower == phrase ||
                        lower.contains(phrase)
            }
        ) {
            return false
        }

        // -----------------------------------------------------
        // Reject pure numbers / masked account numbers
        // -----------------------------------------------------

        if (
            text.matches(
                Regex(
                    """[\d\sXx*+\-]+"""
                )
            )
        ) {
            return false
        }

        return true
    }

    // =========================================================
    // DEBIT / EXPENSE RECEIVER
    // =========================================================

    private fun extractDebitReceiver(
        message: String
    ): String? {

        // =====================================================
        // 1. Sent to NAME
        //
        // Sent Rs.1.00 to JOHN DOE
        // =====================================================

        val sentToPattern =
            Regex(
                """\bsent\s+(?:rs\.?|inr|₹)?\s*[0-9,]*(?:\.[0-9]{1,2})?\s*(?:to|towards)\s+(.+?)(?=\s+(?:on|at|via|using|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        sentToPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val receiver =
                    cleanDebitReceiver(candidate)

                if (isValidDebitReceiver(receiver)) {
                    return receiver
                }
            }

        // =====================================================
        // 2. Paid to NAME
        // =====================================================

        val paidToPattern =
            Regex(
                """\bpaid\s+(?:rs\.?|inr|₹)?\s*[0-9,]*(?:\.[0-9]{1,2})?\s*(?:to|towards)\s+(.+?)(?=\s+(?:on|at|via|using|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        paidToPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val receiver =
                    cleanDebitReceiver(candidate)

                if (isValidDebitReceiver(receiver)) {
                    return receiver
                }
            }

        // =====================================================
        // 3. Transferred to NAME
        // =====================================================

        val transferredToPattern =
            Regex(
                """\btransferred\s+(?:rs\.?|inr|₹)?\s*[0-9,]*(?:\.[0-9]{1,2})?\s*(?:to|towards)\s+(.+?)(?=\s+(?:on|at|via|using|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        transferredToPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val receiver =
                    cleanDebitReceiver(candidate)

                if (isValidDebitReceiver(receiver)) {
                    return receiver
                }
            }

        // =====================================================
        // 4. Generic "to"
        // =====================================================

        val toPattern =
            Regex(
                """\b(?:to|towards)\s+(.+?)(?=\s+(?:on|at|via|using|from|ref|reference|rrn|utr|avl|available)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        toPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val receiver =
                    cleanDebitReceiver(candidate)

                if (isValidDebitReceiver(receiver)) {
                    return receiver
                }
            }

        // =====================================================
        // 5. Merchant / payee
        // =====================================================

        val merchantPattern =
            Regex(
                """\b(?:merchant|payee)\s*[:\-]?\s*(.+?)(?=\s+(?:on|at|via|using|ref|reference|rrn|utr)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        merchantPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val receiver =
                    cleanDebitReceiver(candidate)

                if (isValidDebitReceiver(receiver)) {
                    return receiver
                }
            }

        // =====================================================
        // 6. Payment at NAME
        // =====================================================

        val atPattern =
            Regex(
                """\bat\s+(.+?)(?=\s+(?:on|via|using|from|ref|reference|rrn|utr)\b|[.;,]|$)""",
                RegexOption.IGNORE_CASE
            )

        atPattern
            .find(message)
            ?.groupValues
            ?.getOrNull(1)
            ?.let { candidate ->

                val receiver =
                    cleanDebitReceiver(candidate)

                if (isValidDebitReceiver(receiver)) {
                    return receiver
                }
            }

        return null
    }

    // =========================================================
    // DEBIT RECEIVER CLEANING
    // =========================================================

    private fun cleanDebitReceiver(
        value: String
    ): String {

        var result =
            value.trim()

        // Remove amount

        result =
            result.replace(
                Regex(
                    """(?:rs\.?|inr|₹)\s*[0-9,]+(?:\.[0-9]{1,2})?""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // Remove date

        result =
            result.replace(
                Regex(
                    """\b\d{1,2}[-/]\d{1,2}[-/]\d{2,4}\b"""
                ),
                ""
            )

        // Remove reference numbers

        result =
            result.replace(
                Regex(
                    """\b(?:rrn|utr|ref|reference)\s*[:#-]?\s*[A-Za-z0-9]+\b""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // Remove available balance

        result =
            result.replace(
                Regex(
                    """\b(?:available\s+balance|avl\s+bal|balance)\b.*$""",
                    RegexOption.IGNORE_CASE
                ),
                ""
            )

        // Remove punctuation

        result =
            result
                .replace(Regex("[,:;.]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        return result
    }

    // =========================================================
    // VALIDATE DEBIT RECEIVER
    // =========================================================

    private fun isValidDebitReceiver(
        value: String?
    ): Boolean {

        if (value.isNullOrBlank()) {
            return false
        }

        val text =
            value.trim()

        if (text.length < 2) {
            return false
        }

        // Don't accept pure account numbers

        if (
            text.matches(
                Regex(
                    """[\d\sXx*+\-]+"""
                )
            )
        ) {
            return false
        }

        return true
    }

    // =========================================================
    // DESCRIPTION
    // =========================================================

    private fun createDescription(
        transactionType: String,
        merchant: String?
    ): String {

        if (!merchant.isNullOrBlank()) {

            return if (
                transactionType.equals(
                    "Income",
                    ignoreCase = true
                )
            ) {
                "Received from $merchant"
            } else {
                "Sent to $merchant"
            }
        }

        // -----------------------------------------------------
        // IMPORTANT:
        // Never use the complete SMS as description.
        // -----------------------------------------------------

        return if (
            transactionType.equals(
                "Income",
                ignoreCase = true
            )
        ) {
            "Money received"
        } else {
            "Payment made"
        }
    }
}