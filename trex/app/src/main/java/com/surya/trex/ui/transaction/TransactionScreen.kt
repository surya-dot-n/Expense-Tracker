package com.surya.trex.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Transaction
import com.surya.trex.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    tokenManager: TokenManager,
    onBack: () -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {

    // ==========================================================
    // REPOSITORY
    // ==========================================================

    val transactionRepository =
        remember {
            TransactionRepository(tokenManager)
        }

    val scope =
        rememberCoroutineScope()


    // ==========================================================
    // STATE
    // ==========================================================

    var transactions by remember {
        mutableStateOf<List<Transaction>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("All")
    }

    var transactionToDelete by remember {
        mutableStateOf<Transaction?>(null)
    }

    var isDeleting by remember {
        mutableStateOf(false)
    }


    // ==========================================================
    // LOAD TRANSACTIONS
    // ==========================================================

    fun loadTransactions() {

        scope.launch {

            isLoading = true
            errorMessage = null

            transactionRepository
                .getTransactions()
                .onSuccess {

                    transactions = it

                }
                .onFailure {

                    errorMessage =
                        it.message
                            ?: "Failed to load transactions"
                }

            isLoading = false
        }
    }


    // ==========================================================
    // INITIAL LOAD
    // ==========================================================

    LaunchedEffect(Unit) {
        loadTransactions()
    }


    // ==========================================================
    // TOTALS
    // ==========================================================

    val totalIncome =
        transactions
            .filter {
                it.transaction_type.equals(
                    "income",
                    ignoreCase = true
                )
            }
            .sumOf {
                it.amount
            }

    val totalExpense =
        transactions
            .filter {
                it.transaction_type.equals(
                    "expense",
                    ignoreCase = true
                )
            }
            .sumOf {
                it.amount
            }


    // ==========================================================
    // FILTER TRANSACTIONS
    // ==========================================================

    val filteredTransactions =
        transactions.filter { transaction ->

            val matchesSearch =
                searchQuery.isBlank() ||
                        transaction.description
                            ?.contains(
                                searchQuery,
                                ignoreCase = true
                            ) == true

            val matchesType =
                when (selectedFilter) {

                    "Income" ->
                        transaction.transaction_type
                            .equals(
                                "income",
                                ignoreCase = true
                            )

                    "Expense" ->
                        transaction.transaction_type
                            .equals(
                                "expense",
                                ignoreCase = true
                            )

                    else -> true
                }

            matchesSearch && matchesType
        }


    // ==========================================================
    // DELETE DIALOG
    // ==========================================================

    transactionToDelete?.let { transaction ->

        AlertDialog(

            onDismissRequest = {

                if (!isDeleting) {
                    transactionToDelete = null
                }
            },

            icon = {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },

            title = {

                Text(
                    text = "Delete Transaction",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        "Are you sure you want to delete this transaction? This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },

            confirmButton = {

                Button(

                    enabled = !isDeleting,

                    onClick = {

                        val transactionId =
                            transaction.id

                        if (transactionId == null) {
                            transactionToDelete = null
                            return@Button
                        }

                        scope.launch {

                            isDeleting = true

                            transactionRepository
                                .deleteTransaction(
                                    transactionId
                                )
                                .onSuccess {

                                    transactionToDelete = null

                                    loadTransactions()
                                }
                                .onFailure {

                                    errorMessage =
                                        it.message
                                            ?: "Failed to delete transaction"

                                    transactionToDelete = null
                                }

                            isDeleting = false
                        }
                    },

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme.colorScheme.error
                        ),

                    shape =
                        RoundedCornerShape(10.dp)
                ) {

                    if (isDeleting) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(
                            "Delete",
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            },

            dismissButton = {

                TextButton(

                    enabled = !isDeleting,

                    onClick = {
                        transactionToDelete = null
                    }
                ) {

                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }


    // ==========================================================
    // SCREEN
    // ==========================================================

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {

                Column {

                    // ==================================================
                    // HEADER
                    // ==================================================

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 8.dp
                                ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = onBack
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Close,

                                contentDescription =
                                    "Back",

                                tint =
                                    MaterialTheme.colorScheme.onBackground
                            )
                        }


                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text = "Transactions",

                                fontSize = 21.sp,

                                fontWeight =
                                    FontWeight.ExtraBold,

                                color =
                                    MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text =
                                    "${transactions.size} transaction${if (transactions.size == 1) "" else "s"}",

                                fontSize = 12.sp,

                                color =
                                    MaterialTheme.colorScheme.outline
                            )
                        }


                        IconButton(
                            onClick = {
                                loadTransactions()
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Refresh,

                                contentDescription =
                                    "Refresh",

                                tint =
                                    MaterialTheme.colorScheme.primary
                            )
                        }


                        Box(

                            modifier =
                                Modifier
                                    .size(42.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.ReceiptLong,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }


                    // ==================================================
                    // SUMMARY
                    // ==================================================

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 18.dp
                                ),

                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        TransactionSummaryCard(
                            title = "Income",
                            amount = totalIncome,
                            amountColor =
                                MaterialTheme.colorScheme.tertiary,
                            backgroundColor =
                                MaterialTheme.colorScheme.tertiaryContainer,
                            modifier =
                                Modifier.weight(1f)
                        )


                        TransactionSummaryCard(
                            title = "Expense",
                            amount = totalExpense,
                            amountColor =
                                MaterialTheme.colorScheme.error,
                            backgroundColor =
                                MaterialTheme.colorScheme.errorContainer,
                            modifier =
                                Modifier.weight(1f)
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    // ==================================================
                    // SEARCH
                    // ==================================================

                    OutlinedTextField(

                        value =
                            searchQuery,

                        onValueChange = {
                            searchQuery = it
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 18.dp
                                ),

                        placeholder = {
                            Text(
                                "Search transactions..."
                            )
                        },

                        leadingIcon = {

                            Icon(
                                imageVector =
                                    Icons.Default.Search,

                                contentDescription =
                                    "Search",

                                tint =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },

                        singleLine = true,

                        shape =
                            RoundedCornerShape(16.dp),

                        colors =
                            OutlinedTextFieldDefaults.colors(

                                unfocusedContainerColor =
                                    MaterialTheme.colorScheme.surfaceVariant,

                                focusedContainerColor =
                                    MaterialTheme.colorScheme.surfaceVariant,

                                unfocusedBorderColor =
                                    MaterialTheme.colorScheme.outline,

                                focusedBorderColor =
                                    MaterialTheme.colorScheme.primary
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    // ==================================================
                    // FILTERS
                    // ==================================================

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 18.dp
                                ),

                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        TransactionFilterChip(
                            text = "All",
                            selected =
                                selectedFilter == "All",
                            onClick = {
                                selectedFilter = "All"
                            }
                        )

                        TransactionFilterChip(
                            text = "Income",
                            selected =
                                selectedFilter == "Income",
                            onClick = {
                                selectedFilter = "Income"
                            }
                        )

                        TransactionFilterChip(
                            text = "Expense",
                            selected =
                                selectedFilter == "Expense",
                            onClick = {
                                selectedFilter = "Expense"
                            }
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )
                }
            }
        }

    ) { paddingValues ->

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
        ) {

            when {

                // ==================================================
                // LOADING
                // ==================================================

                isLoading -> {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),

                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }


                // ==================================================
                // ERROR
                // ==================================================

                errorMessage != null -> {

                    Column(

                        modifier =
                            Modifier
                                .align(
                                    Alignment.Center
                                )
                                .padding(24.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .size(64.dp)
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        CircleShape
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.ReceiptLong,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme.colorScheme.error,

                                modifier =
                                    Modifier.size(30.dp)
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )


                        Text(
                            text =
                                errorMessage
                                    ?: "Something went wrong",

                            color =
                                MaterialTheme.colorScheme.error,

                            fontSize =
                                14.sp,

                            fontWeight =
                                FontWeight.Medium
                        )


                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )


                        Button(
                            onClick =
                                {
                                    loadTransactions()
                                },

                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.primary
                                ),

                            shape =
                                RoundedCornerShape(12.dp)
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Refresh,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(18.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Text("Try Again")
                        }
                    }
                }


                // ==================================================
                // EMPTY
                // ==================================================

                filteredTransactions.isEmpty() -> {

                    Column(

                        modifier =
                            Modifier
                                .align(
                                    Alignment.Center
                                )
                                .padding(24.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .size(74.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.ReceiptLong,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme.colorScheme.primary,

                                modifier =
                                    Modifier.size(34.dp)
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )


                        Text(
                            text =
                                if (
                                    searchQuery.isNotBlank() ||
                                    selectedFilter != "All"
                                ) {
                                    "No matching transactions"
                                } else {
                                    "No transactions yet"
                                },

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme.colorScheme.onBackground
                        )


                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )


                        Text(
                            text =
                                if (
                                    searchQuery.isNotBlank() ||
                                    selectedFilter != "All"
                                ) {
                                    "Try changing your search or filter"
                                } else {
                                    "Your transactions will appear here"
                                },

                            fontSize =
                                13.sp,

                            color =
                                MaterialTheme.colorScheme.outline
                        )
                    }
                }


                // ==================================================
                // TRANSACTION LIST
                // ==================================================

                else -> {

                    LazyColumn(

                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            PaddingValues(
                                horizontal = 18.dp,
                                vertical = 16.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        items(

                            items =
                                filteredTransactions,

                            key = {
                                it.id ?: 0
                            }

                        ) { transaction ->

                            TransactionListCard(

                                transaction =
                                    transaction,

                                onEdit = {

                                    onEditTransaction(
                                        transaction
                                    )
                                },

                                onDelete = {

                                    transactionToDelete =
                                        transaction
                                }
                            )
                        }


                        item {

                            Spacer(
                                modifier =
                                    Modifier.height(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================================
// SUMMARY CARD
// ==========================================================

@Composable
private fun TransactionSummaryCard(
    title: String,
    amount: Double,
    amountColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {

    Card(

        modifier = modifier,

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    backgroundColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(14.dp)
        ) {

            Text(
                text = title,

                fontSize = 12.sp,

                fontWeight =
                    FontWeight.Medium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )


            Text(

                text =
                    "₹${"%.2f".format(amount)}",

                fontSize = 18.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    amountColor
            )
        }
    }
}


// ==========================================================
// FILTER CHIP
// ==========================================================

@Composable
private fun TransactionFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    FilterChip(

        selected =
            selected,

        onClick =
            onClick,

        label = {

            Text(
                text = text,

                fontSize = 12.sp,

                fontWeight =
                    FontWeight.Bold
            )
        },

        leadingIcon =
            if (selected) {

                {
                    Icon(
                        imageVector =
                            Icons.Default.Tune,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(15.dp)
                    )
                }

            } else {
                null
            },

        shape =
            RoundedCornerShape(12.dp),

        colors =
            FilterChipDefaults.filterChipColors(

                selectedContainerColor =
                    MaterialTheme.colorScheme.primaryContainer,

                selectedLabelColor =
                    MaterialTheme.colorScheme.primary,

                selectedLeadingIconColor =
                    MaterialTheme.colorScheme.primary,

                containerColor =
                    MaterialTheme.colorScheme.surface,

                labelColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),

        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor =
                    MaterialTheme.colorScheme.outline,
                selectedBorderColor =
                    MaterialTheme.colorScheme.primary,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.dp
            )
    )
}


// ==========================================================
// TRANSACTION CARD
// ==========================================================

@Composable
private fun TransactionListCard(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val isIncome =
        transaction.transaction_type
            .equals(
                "income",
                ignoreCase = true
            )


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    1.dp
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // ==================================================
                // ICON
                // ==================================================

                Box(

                    modifier =
                        Modifier
                            .size(44.dp)
                            .background(

                                color =
                                    if (isIncome) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    },

                                shape =
                                    CircleShape
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.ReceiptLong,

                        contentDescription =
                            null,

                        tint =
                            if (isIncome) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.error
                            },

                        modifier =
                            Modifier.size(21.dp)
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )


                // ==================================================
                // DETAILS
                // ==================================================

                Column(

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(

                        text =
                            transaction.description
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Transaction",

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme.colorScheme.onBackground,

                        maxLines =
                            1
                    )


                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )


                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.CalendarToday,

                            contentDescription =
                                null,

                            tint =
                                MaterialTheme.colorScheme.outline,

                            modifier =
                                Modifier.size(13.dp)
                        )


                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )


                        Text(

                            text =
                                formatTransactionDate(
                                    transaction.transaction_date
                                ),

                            fontSize =
                                12.sp,

                            color =
                                MaterialTheme.colorScheme.outline
                        )


                        transaction.transaction_time
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let {

                                Spacer(
                                    modifier =
                                        Modifier.width(8.dp)
                                )

                                Text(
                                    text =
                                        formatTransactionTime(
                                            it
                                        ),

                                    fontSize =
                                        12.sp,

                                    color =
                                        MaterialTheme.colorScheme.outline
                                )
                            }
                    }
                }


                // ==================================================
                // EDIT
                // ==================================================

                IconButton(

                    onClick =
                        onEdit,

                    modifier =
                        Modifier.size(36.dp)
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Edit,

                        contentDescription =
                            "Edit transaction",

                        tint =
                            MaterialTheme.colorScheme.onSurfaceVariant,

                        modifier =
                            Modifier.size(18.dp)
                    )
                }


                // ==================================================
                // DELETE
                // ==================================================

                IconButton(

                    onClick =
                        onDelete,

                    modifier =
                        Modifier.size(36.dp)
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Delete,

                        contentDescription =
                            "Delete transaction",

                        tint =
                            MaterialTheme.colorScheme.error,

                        modifier =
                            Modifier.size(18.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            // ==================================================
            // BOTTOM
            // ==================================================

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.End,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text =
                        if (isIncome) {

                            "+₹${"%.2f".format(
                                transaction.amount
                            )}"

                        } else {

                            "-₹${"%.2f".format(
                                transaction.amount
                            )}"
                        },

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    color =
                        if (isIncome) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                )
            }
        }
    }
}


// ==========================================================
// DATE
// ==========================================================

private fun formatTransactionDate(
    date: String?
): String {

    if (date.isNullOrBlank()) {
        return ""
    }

    return try {

        val inputFormatter =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

        val outputFormatter =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )

        val parsedDate =
            inputFormatter.parse(date)

        if (parsedDate != null) {
            outputFormatter.format(parsedDate)
        } else {
            date
        }

    } catch (e: Exception) {

        date
    }
}


// ==========================================================
// TIME
// ==========================================================

private fun formatTransactionTime(
    time: String
): String {

    return try {

        val inputFormatter =
            SimpleDateFormat(
                "HH:mm:ss",
                Locale.US
            )

        val outputFormatter =
            SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            )

        val parsedTime =
            inputFormatter.parse(time)

        if (parsedTime != null) {
            outputFormatter.format(parsedTime)
        } else {
            time
        }

    } catch (e: Exception) {

        time
    }
}
