package com.surya.trex.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.PendingTransactionCreateRequest
import com.surya.trex.data.model.Transaction
import com.surya.trex.data.repository.CategoryRepository
import com.surya.trex.data.repository.PendingTransactionRepository
import com.surya.trex.data.repository.SettingsRepository
import com.surya.trex.data.repository.TransactionRepository
import com.surya.trex.notifications.TransactionNotificationHelper
import com.surya.trex.data.repository.DashboardRefreshManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {

    companion object {

        private const val TAG = "TREX_SMS"

        private const val MODE_AUTO = "AUTO"
        private const val MODE_APPROVAL = "APPROVAL"
        private const val MODE_MANUAL = "MANUAL"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (
            intent.action !=
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        ) {
            return
        }

        val pendingResult =
            goAsync()

        val appContext =
            context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {

            try {

                processSms(
                    context = appContext,
                    intent = intent
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Error processing SMS",
                    e
                )

            } finally {

                pendingResult.finish()
            }
        }
    }

    private suspend fun processSms(
        context: Context,
        intent: Intent
    ) {

        // ==========================================
        // 1. Read SMS
        // ==========================================

        val messages =
            Telephony.Sms.Intents
                .getMessagesFromIntent(intent)

        if (messages.isNullOrEmpty()) {

            Log.d(
                TAG,
                "No SMS messages found."
            )

            return
        }

        val sender =
            messages
                .firstOrNull()
                ?.displayOriginatingAddress
                ?.trim()

        if (sender.isNullOrBlank()) {

            Log.d(
                TAG,
                "SMS sender is empty."
            )

            return
        }

        val messageBody =
            messages
                .joinToString(separator = "") {
                    it.messageBody ?: ""
                }
                .trim()

        if (messageBody.isBlank()) {

            Log.d(
                TAG,
                "SMS message body is empty."
            )

            return
        }

        val timestamp =
            messages
                .firstOrNull()
                ?.timestampMillis
                ?: System.currentTimeMillis()

        Log.d(
            TAG,
            "======================================"
        )

        Log.d(
            TAG,
            "TREX SMS RECEIVED"
        )

        Log.d(
            TAG,
            "Sender: $sender"
        )

        Log.d(
            TAG,
            "Message: $messageBody"
        )

        Log.d(
            TAG,
            "Timestamp: $timestamp"
        )

        Log.d(
            TAG,
            "Local Date/Time: ${timestampToIsoString(timestamp)}"
        )

        Log.d(
            TAG,
            "======================================"
        )

        // ==========================================
        // 2. Get authentication token
        // ==========================================

        val tokenManager =
            TokenManager(context)

        val token =
            tokenManager.getToken()

        if (token.isNullOrBlank()) {

            Log.d(
                TAG,
                "No authentication token. SMS ignored."
            )

            return
        }

        // ==========================================
        // 3. Load settings
        // ==========================================

        val settingsRepository =
            SettingsRepository()

        val settings =
            try {

                settingsRepository
                    .getSettings(token)

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Failed to load user settings.",
                    e
                )

                return
            }

        // ==========================================
        // 4. Check SMS detection
        // ==========================================

        if (!settings.sms_detection_enabled) {

            Log.d(
                TAG,
                "SMS detection is disabled."
            )

            return
        }

        // ==========================================
        // 5. Get detection mode
        // ==========================================

        val detectionMode =
            settings.detection_mode
                .trim()
                .uppercase()

        Log.d(
            TAG,
            "Detection mode: $detectionMode"
        )

        if (detectionMode == MODE_MANUAL) {

            Log.d(
                TAG,
                "MANUAL mode selected."
            )

            Log.d(
                TAG,
                "SMS will not create a transaction automatically."
            )

            return
        }

        // ==========================================
        // 6. Load transaction sources
        // ==========================================

        val sources =
            try {

                settingsRepository
                    .getSources(token)

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Failed to load transaction sources.",
                    e
                )

                return
            }

        if (sources.isEmpty()) {

            Log.d(
                TAG,
                "No transaction sources configured."
            )

            return
        }

        // ==========================================
        // 7. Find matching SMS source
        // ==========================================

        val normalizedSender =
            normalizeSender(sender)

        val matchingSource =
            sources.firstOrNull { source ->

                if (!source.enabled) {
                    return@firstOrNull false
                }

                val sourceType =
                    source.source_type
                        .trim()
                        .uppercase()

                if (
                    sourceType != "SMS" &&
                    sourceType != "BOTH"
                ) {
                    return@firstOrNull false
                }

                source.sender_ids.isEmpty() ||
                        source.sender_ids.any { senderId ->

                            senderMatches(
                                smsSender = normalizedSender,
                                configuredSender = senderId
                            )
                        }
            }

        if (matchingSource == null) {

            Log.d(
                TAG,
                "SMS sender is not a configured transaction source."
            )

            return
        }

        Log.d(
            TAG,
            "======================================"
        )

        Log.d(
            TAG,
            "TRANSACTION SOURCE MATCHED"
        )

        Log.d(
            TAG,
            "Source ID: ${matchingSource.id}"
        )

        Log.d(
            TAG,
            "App: ${matchingSource.app_name}"
        )

        Log.d(
            TAG,
            "Package: ${matchingSource.package_name}"
        )

        Log.d(
            TAG,
            "Source type: ${matchingSource.source_type}"
        )

        Log.d(
            TAG,
            "======================================"
        )

        // ==========================================
        // 8. Parse transaction
        // ==========================================

        val parsedTransaction =
            SmsTransactionParser.parse(
                message = messageBody,
                timestampMillis = timestamp
            )

        if (parsedTransaction == null) {

            Log.d(
                TAG,
                "Could not parse SMS as a transaction."
            )

            return
        }

        Log.d(
            TAG,
            "======================================"
        )

        Log.d(
            TAG,
            "TRANSACTION PARSED"
        )

        Log.d(
            TAG,
            "Amount: ${parsedTransaction.amount}"
        )

        Log.d(
            TAG,
            "Type: ${parsedTransaction.transactionType}"
        )

        Log.d(
            TAG,
            "Merchant: ${parsedTransaction.merchant}"
        )

        Log.d(
            TAG,
            "Description: ${parsedTransaction.description}"
        )

        Log.d(
            TAG,
            "Date/Time: ${
                timestampToIsoString(
                    parsedTransaction.dateTimeMillis
                )
            }"
        )

        Log.d(
            TAG,
            "======================================"
        )

        // ==========================================
        // 9. Handle detection mode
        // ==========================================

        when (detectionMode) {

            MODE_APPROVAL -> {

                createPendingTransaction(
                    context = context,
                    token = token,
                    sourceId = matchingSource.id,
                    appName = matchingSource.app_name,
                    packageName = matchingSource.package_name,
                    sender = sender,
                    messageBody = messageBody,
                    timestamp = timestamp,
                    parsedTransaction = parsedTransaction
                )
            }

            MODE_AUTO -> {

                handleAutoMode(
                    context = context,
                    tokenManager = tokenManager,
                    token = token,
                    sourceId = matchingSource.id,
                    appName = matchingSource.app_name,
                    packageName = matchingSource.package_name,
                    sender = sender,
                    messageBody = messageBody,
                    timestamp = timestamp,
                    parsedTransaction = parsedTransaction
                )
            }

            else -> {

                Log.d(
                    TAG,
                    "Unknown detection mode: $detectionMode"
                )
            }
        }
    }

    // ==========================================
    // APPROVAL MODE
    // ==========================================

    private suspend fun createPendingTransaction(
        context: Context,
        token: String,
        sourceId: Int,
        appName: String,
        packageName: String?,
        sender: String,
        messageBody: String,
        timestamp: Long,
        parsedTransaction: ParsedSmsTransaction
    ) {

        val fingerprint =
            createFingerprint(
                sender = sender,
                messageBody = messageBody,
                timestamp = timestamp
            )

        val request =
            PendingTransactionCreateRequest(

                source_id = sourceId,

                app_name = appName,

                package_name = packageName,

                source_type = "SMS",

                sender_id = sender,

                message_body = messageBody,

                amount =
                    parsedTransaction.amount
                        .toPlainString(),

                transaction_type =
                    parsedTransaction.transactionType,

                description =
                    parsedTransaction.description,

                category_id = null,

                transaction_date =
                    timestampToIsoString(timestamp),

                confidence = "1.0",

                fingerprint = fingerprint
            )

        Log.d(
            TAG,
            "Creating pending transaction..."
        )

        Log.d(
            TAG,
            "Pending date/time: ${
                timestampToIsoString(timestamp)
            }"
        )

        val repository =
            PendingTransactionRepository()

        val result =
            repository.createPendingTransaction(
                token = token,
                request = request
            )

        result
            .onSuccess { pending ->

                Log.d(
                    TAG,
                    "======================================"
                )

                Log.d(
                    TAG,
                    "PENDING TRANSACTION CREATED"
                )

                Log.d(
                    TAG,
                    "Pending ID: ${pending.id}"
                )

                Log.d(
                    TAG,
                    "Amount: ${pending.amount}"
                )

                Log.d(
                    TAG,
                    "Type: ${pending.transaction_type}"
                )

                Log.d(
                    TAG,
                    "Status: ${pending.status}"
                )

                val notificationSettings =
                    try {

                        SettingsRepository()
                            .getSettings(token)

                    } catch (_: Exception) {

                        null
                    }

                if (
                    notificationSettings
                        ?.notifications_enabled == true
                ) {

                    TransactionNotificationHelper.show(
                        context = context,
                        title = "Transaction detected",
                        text =
                            "₹${parsedTransaction.amount.toPlainString()} detected from SMS. Open TREX to review."
                    )
                }

                Log.d(
                    TAG,
                    "======================================"
                )
            }
            .onFailure { error ->

                Log.e(
                    TAG,
                    "Failed to create pending transaction.",
                    error
                )
            }
    }

    // ==========================================
    // AUTO MODE
    // ==========================================

    private suspend fun handleAutoMode(
        context: Context,
        tokenManager: TokenManager,
        token: String,
        sourceId: Int,
        appName: String,
        packageName: String?,
        sender: String,
        messageBody: String,
        timestamp: Long,
        parsedTransaction: ParsedSmsTransaction
    ) {

        Log.d(
            TAG,
            "AUTO mode selected."
        )

        // ==========================================
        // 1. Get categories
        // ==========================================

        val categoryRepository =
            CategoryRepository(tokenManager)

        val categoriesResult =
            categoryRepository.getCategories()

        val categories =
            categoriesResult
                .getOrNull()
                ?: emptyList()

        if (categories.isEmpty()) {

            Log.e(
                TAG,
                "No categories found for AUTO mode. Falling back to APPROVAL mode."
            )

            createPendingTransaction(
                context = context,
                token = token,
                sourceId = sourceId,
                appName = appName,
                packageName = packageName,
                sender = sender,
                messageBody = messageBody,
                timestamp = timestamp,
                parsedTransaction = parsedTransaction
            )

            return
        }

        // ==========================================
        // 2. Find category
        // ==========================================

        val type =
            parsedTransaction.transactionType

        val merchant =
            parsedTransaction.merchant ?: ""

        val description =
            parsedTransaction.description

        val filteredCategories =
            categories.filter {

                it.category_type.equals(
                    type,
                    ignoreCase = true
                )
            }

        val category =
            filteredCategories.firstOrNull {

                it.category_name.equals(
                    merchant,
                    ignoreCase = true
                ) ||
                        description.contains(
                            it.category_name,
                            ignoreCase = true
                        )
            }
                ?: filteredCategories.firstOrNull {

                    it.category_name.equals(
                        "Other",
                        ignoreCase = true
                    ) ||
                            it.category_name.equals(
                                "Uncategorized",
                                ignoreCase = true
                            )
                }
                ?: filteredCategories.firstOrNull()
                ?: categories.firstOrNull()

        if (category == null) {

            Log.e(
                TAG,
                "Could not find any suitable category for AUTO mode. Falling back to APPROVAL mode."
            )

            createPendingTransaction(
                context = context,
                token = token,
                sourceId = sourceId,
                appName = appName,
                packageName = packageName,
                sender = sender,
                messageBody = messageBody,
                timestamp = timestamp,
                parsedTransaction = parsedTransaction
            )

            return
        }

        Log.d(
            TAG,
            "Selected category: ${category.category_name} (ID: ${category.id})"
        )

        // ==========================================
        // 3. Create transaction
        // ==========================================

        val transactionRepository =
            TransactionRepository(tokenManager)

        /*
         * IMPORTANT:
         *
         * This uses the local phone date/time.
         */
        val isoString =
            timestampToIsoString(timestamp)

        val dateParts =
            isoString.split("T")

        val date =
            dateParts
                .getOrNull(0)

        val time =
            dateParts
                .getOrNull(1)

        val transaction =
            Transaction(
                category_id = category.id,
                amount =
                    parsedTransaction.amount
                        .toDouble(),
                description =
                    parsedTransaction.description,
                transaction_type =
                    parsedTransaction.transactionType,
                transaction_date = date,
                transaction_time = time
            )

        Log.d(
            TAG,
            "======================================"
        )

        Log.d(
            TAG,
            "CREATING AUTO TRANSACTION"
        )

        Log.d(
            TAG,
            "Amount: ${transaction.amount}"
        )

        Log.d(
            TAG,
            "Type: ${transaction.transaction_type}"
        )

        Log.d(
            TAG,
            "Description: ${transaction.description}"
        )

        Log.d(
            TAG,
            "Date: ${transaction.transaction_date}"
        )

        Log.d(
            TAG,
            "Time: ${transaction.transaction_time}"
        )

        Log.d(
            TAG,
            "======================================"
        )

        val result =
            transactionRepository
                .createTransaction(transaction)

        result
            .onSuccess {

                Log.d(
                    TAG,
                    "Transaction created automatically: ${it.id}"
                )

                // Notify HomeScreen that a new transaction
                // has been successfully created.
                DashboardRefreshManager.refresh()
            }
            .onFailure { error ->

                Log.e(
                    TAG,
                    "Failed to create transaction automatically. Falling back to APPROVAL mode.",
                    error
                )

                createPendingTransaction(
                    context = context,
                    token = token,
                    sourceId = sourceId,
                    appName = appName,
                    packageName = packageName,
                    sender = sender,
                    messageBody = messageBody,
                    timestamp = timestamp,
                    parsedTransaction = parsedTransaction
                )
            }
    }

    // ==========================================
    // Fingerprint
    // ==========================================

    private fun createFingerprint(
        sender: String,
        messageBody: String,
        timestamp: Long
    ): String {

        val normalizedMessage =
            messageBody
                .trim()
                .lowercase()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        val roundedTimestamp =
            timestamp -
                    (timestamp % 60_000L)

        val raw =
            "$sender|$normalizedMessage|$roundedTimestamp"

        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    raw.toByteArray()
                )

        return digest.joinToString("") {
            "%02x".format(it)
        }
    }

    // ==========================================
    // SMS TIMESTAMP → LOCAL PHONE DATE/TIME
    // ==========================================

    private fun timestampToIsoString(
        timestamp: Long
    ): String {

        /*
         * IMPORTANT:
         *
         * Do NOT use:
         *
         * Instant.ofEpochMilli(timestamp).toString()
         *
         * because that returns UTC time.
         *
         * SimpleDateFormat uses the phone's default
         * timezone, so an SMS received at 11:45 AM
         * in India will remain 11:45 AM.
         */

        val formatter =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            )

        formatter.timeZone =
            java.util.TimeZone.getDefault()

        return formatter.format(
            Date(timestamp)
        )
    }

    // ==========================================
    // Sender normalization
    // ==========================================

    private fun normalizeSender(
        sender: String
    ): String {

        return sender
            .trim()
            .uppercase()
            .replace(
                " ",
                ""
            )
            .replace(
                "-",
                ""
            )
    }

    // ==========================================
    // Sender matching
    // ==========================================

    private fun senderMatches(
        smsSender: String,
        configuredSender: String
    ): Boolean {

        val normalizedConfiguredSender =
            normalizeSender(
                configuredSender
            )

        if (
            normalizedConfiguredSender.isEmpty()
        ) {
            return false
        }

        if (
            smsSender ==
            normalizedConfiguredSender
        ) {
            return true
        }

        return smsSender.contains(
            normalizedConfiguredSender
        ) ||
                normalizedConfiguredSender.contains(
                    smsSender
                )
    }
}
