
package com.surya.trex.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Category
import com.surya.trex.data.model.Transaction
import com.surya.trex.data.repository.CategoryRepository
import com.surya.trex.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun TransactionsScreen(
    tokenManager: TokenManager,
    onBack: () -> Unit
) {

    var transactions by remember {
        mutableStateOf<List<Transaction>>(emptyList())
    }

    var categories by remember {
        mutableStateOf<List<Category>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isRefreshing by remember {
        mutableStateOf(false)
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

    val transactionRepository =
        remember {
            TransactionRepository(tokenManager)
        }

    val categoryRepository =
        remember {
            CategoryRepository(tokenManager)
        }

    val scope =
        rememberCoroutineScope()


    // ==========================================================
    // LOAD TRANSACTIONS
    // ==========================================================

    suspend fun loadTransactions(
        showFullLoading: Boolean = false
    ) {

        if (showFullLoading) {
            isLoading = true
        } else {
            isRefreshing = true
        }

        errorMessage = null

        try {

            val transactionResult =
                transactionRepository.getTransactions()

            if (transactionResult.isSuccess) {

                transactions =
                    transactionResult.getOrNull()
                        ?: emptyList()

            } else {

                throw Exception(
                    transactionResult
                        .exceptionOrNull()
                        ?.message
                        ?: "Unable to load transactions"
                )
            }


            val categoryResult =
                categoryRepository.getCategories()

            if (categoryResult.isSuccess) {

                categories =
                    categoryResult.getOrNull()
                        ?: emptyList()

            } else {

                throw Exception(
                    categoryResult
                        .exceptionOrNull()
                        ?.message
                        ?: "Unable to load categories"
                )
            }

        } catch (e: Exception) {

            errorMessage =
                e.message
                    ?: "Something went wrong"

        } finally {

            isLoading = false
            isRefreshing = false
        }
    }


    // ==========================================================
    // INITIAL LOAD
    // ==========================================================

    LaunchedEffect(Unit) {

        loadTransactions(
            showFullLoading = true
        )
    }


    // ==========================================================
    // FILTER TRANSACTIONS
    // ==========================================================

    val filteredTransactions =
        remember(
            transactions,
            searchQuery,
            selectedFilter
        ) {

            transactions.filter { transaction ->

                val matchesType =
                    when (selectedFilter) {

                        "Expense" ->
                            transaction.transaction_type
                                .equals(
                                    "expense",
                                    ignoreCase = true
                                )

                        "Income" ->
                            transaction.transaction_type
                                .equals(
                                    "income",
                                    ignoreCase = true
                                )

                        else ->
                            true
                    }


                val categoryName =
                    categories
                        .find {
                            it.id ==
                                    transaction.category_id
                        }
                        ?.category_name
                        ?: ""


                val searchText =
                    listOf(
                        transaction.description ?: "",
                        categoryName,
                        transaction.transaction_type
                    )
                        .joinToString(" ")
                        .lowercase()


                val matchesSearch =
                    searchQuery
                        .trim()
                        .lowercase()
                        .let { query ->

                            query.isEmpty() ||
                                    searchText.contains(query)
                        }


                matchesType &&
                        matchesSearch
            }
        }


    // ==========================================================
    // TOTALS
    // ==========================================================

    val totalIncome =
        filteredTransactions
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
        filteredTransactions
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
    // SCREEN
    // ==========================================================

    Scaffold(

        containerColor =
            Color(0xFFF7F9FC),

        topBar = {

            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {

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
                                Icons.Default.ArrowBack,

                            contentDescription =
                                "Back",

                            tint =
                                Color(0xFF0F172A)
                        )
                    }


                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(

                            text =
                                "Transactions",

                            fontSize =
                                22.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                Color(0xFF0F172A)
                        )

                        Text(

                            text =
                                "Your complete financial activity",

                            fontSize =
                                12.sp,

                            color =
                                Color(0xFF94A3B8)
                        )
                    }


                    IconButton(

                        enabled =
                            !isRefreshing,

                        onClick = {

                            scope.launch {

                                loadTransactions(
                                    showFullLoading =
                                        false
                                )
                            }
                        }
                    ) {

                        if (isRefreshing) {

                            CircularProgressIndicator(

                                modifier =
                                    Modifier.size(20.dp),

                                strokeWidth =
                                    2.dp,

                                color =
                                    Color(0xFF2563EB)
                            )

                        } else {

                            Icon(

                                imageVector =
                                    Icons.Default.Refresh,

                                contentDescription =
                                    "Refresh",

                                tint =
                                    Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }
        }

    ) { innerPadding ->


        LazyColumn(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),

            contentPadding =
                PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = 30.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {


            // ==================================================
            // SUMMARY
            // ==================================================

            if (!isLoading && errorMessage == null) {

                item {

                    TransactionSummaryCard(

                        transactionCount =
                            filteredTransactions.size,

                        totalIncome =
                            totalIncome,

                        totalExpense =
                            totalExpense
                    )
                }
            }


            // ==================================================
            // SEARCH
            // ==================================================

            item {

                OutlinedTextField(

                    value =
                        searchQuery,

                    onValueChange = {
                        searchQuery = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true,

                    placeholder = {

                        Text(
                            text =
                                "Search transactions..."
                        )
                    },

                    leadingIcon = {

                        Icon(
                            imageVector =
                                Icons.Default.Search,

                            contentDescription =
                                null
                        )
                    },

                    trailingIcon = {

                        if (searchQuery.isNotEmpty()) {

                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                }
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Clear,

                                    contentDescription =
                                        "Clear search"
                                )
                            }
                        }
                    },

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                                Color(0xFF2563EB),

                            unfocusedBorderColor =
                                Color(0xFFE2E8F0),

                            focusedContainerColor =
                                Color.White,

                            unfocusedContainerColor =
                                Color.White
                        )
                )
            }


            // ==================================================
            // FILTERS
            // ==================================================

            item {

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

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
                        text = "Expense",
                        selected =
                            selectedFilter == "Expense",
                        onClick = {
                            selectedFilter = "Expense"
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
                }
            }


            // ==================================================
            // LOADING
            // ==================================================

            if (isLoading) {

                item {

                    TransactionsLoading()
                }
            }


            // ==================================================
            // ERROR
            // ==================================================

            else if (errorMessage != null) {

                item {

                    TransactionsError(

                        message =
                            errorMessage
                                ?: "Unable to load transactions",

                        onRetry = {

                            scope.launch {

                                loadTransactions(
                                    showFullLoading =
                                        true
                                )
                            }
                        }
                    )
                }
            }


            // ==================================================
            // EMPTY
            // ==================================================

            else if (filteredTransactions.isEmpty()) {

                item {

                    EmptyTransactions()
                }
            }


            // ==================================================
            // TRANSACTIONS
            // ==================================================

            else {

                item {

                    Text(

                        text =
                            "${filteredTransactions.size} transaction${
                                if (
                                    filteredTransactions.size != 1
                                ) "s"
                                else ""
                            }",

                        fontSize =
                            14.sp,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            Color(0xFF64748B)
                    )
                }


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

                        categoryName =
                            categories
                                .find {
                                    it.id ==
                                            transaction.category_id
                                }
                                ?.category_name
                                ?: "Other"
                    )
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
    transactionCount: Int,
    totalIncome: Double,
    totalExpense: Double
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    3.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(20.dp)
        ) {

            Row(

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(

                    modifier =
                        Modifier.size(44.dp),

                    shape =
                        CircleShape,

                    color =
                        Color(0xFFEFF6FF)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.ReceiptLong,

                            contentDescription =
                                null,

                            tint =
                                Color(0xFF2563EB),

                            modifier =
                                Modifier.size(23.dp)
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )


                Column {

                    Text(

                        text =
                            "Transaction overview",

                        fontSize =
                            16.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFF0F172A)
                    )

                    Text(

                        text =
                            "$transactionCount recorded",

                        fontSize =
                            12.sp,

                        color =
                            Color(0xFF94A3B8)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                SummaryAmount(

                    modifier =
                        Modifier.weight(1f),

                    title =
                        "Income",

                    amount =
                        totalIncome,

                    color =
                        Color(0xFF16A34A),

                    background =
                        Color(0xFFE8F8F0)
                )


                SummaryAmount(

                    modifier =
                        Modifier.weight(1f),

                    title =
                        "Expense",

                    amount =
                        totalExpense,

                    color =
                        Color(0xFFDC2626),

                    background =
                        Color(0xFFFFECEC)
                )
            }
        }
    }
}


// ==========================================================
// SUMMARY AMOUNT
// ==========================================================

@Composable
private fun SummaryAmount(
    modifier: Modifier,
    title: String,
    amount: Double,
    color: Color,
    background: Color
) {

    Surface(

        modifier =
            modifier,

        shape =
            RoundedCornerShape(16.dp),

        color =
            background
    ) {

        Column(

            modifier =
                Modifier.padding(14.dp)
        ) {

            Text(

                text =
                    title,

                fontSize =
                    11.sp,

                fontWeight =
                    FontWeight.Medium,

                color =
                    color
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(

                text =
                    "₹%.2f".format(amount),

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    color,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
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
                text =
                    text,

                fontWeight =
                    if (selected)
                        FontWeight.Bold
                    else
                        FontWeight.Medium
            )
        },

        shape =
            RoundedCornerShape(12.dp),

        colors =
            FilterChipDefaults.filterChipColors(

                selectedContainerColor =
                    Color(0xFF2563EB),

                selectedLabelColor =
                    Color.White,

                containerColor =
                    Color.White,

                labelColor =
                    Color(0xFF64748B)
            )
    )
}


// ==========================================================
// TRANSACTION CARD
// ==========================================================

@Composable
private fun TransactionListCard(
    transaction: Transaction,
    categoryName: String
) {

    val isIncome =
        transaction.transaction_type
            .equals(
                "income",
                ignoreCase = true
            )


    val iconColor =
        if (isIncome) {

            Color(0xFF16A34A)

        } else {

            Color(0xFFDC2626)
        }


    val iconBackground =
        if (isIncome) {

            Color(0xFFE8F8F0)

        } else {

            Color(0xFFFFECEC)
        }


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(17.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // ------------------------------------------
            // ICON
            // ------------------------------------------

            Surface(

                modifier =
                    Modifier.size(48.dp),

                shape =
                    CircleShape,

                color =
                    iconBackground
            ) {

                Box(

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            if (isIncome) {

                                Icons.Default.ArrowDownward

                            } else {

                                Icons.Default.ArrowUpward
                            },

                        contentDescription =
                            null,

                        tint =
                            iconColor,

                        modifier =
                            Modifier.size(22.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.width(13.dp)
            )


            // ------------------------------------------
            // DETAILS
            // ------------------------------------------

            Column(

                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        transaction.description
                            ?.ifBlank {
                                "Transaction"
                            }
                            ?: "Transaction",

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color(0xFF0F172A),

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )


                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )


                Row(

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(

                        shape =
                            RoundedCornerShape(7.dp),

                        color =
                            Color(0xFFF1F5F9)
                    ) {

                        Text(

                            text =
                                categoryName,

                            modifier =
                                Modifier.padding(
                                    horizontal = 7.dp,
                                    vertical = 3.dp
                                ),

                            fontSize =
                                10.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                Color(0xFF64748B)
                        )
                    }


                    Spacer(
                        modifier =
                            Modifier.width(7.dp)
                    )


                    Surface(

                        shape =
                            RoundedCornerShape(7.dp),

                        color =
                            iconBackground
                    ) {

                        Text(

                            text =
                                if (isIncome)
                                    "Income"
                                else
                                    "Expense",

                            modifier =
                                Modifier.padding(
                                    horizontal = 7.dp,
                                    vertical = 3.dp
                                ),

                            fontSize =
                                10.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                iconColor
                        )
                    }
                }


                if (
                    !transaction.created_at
                        .isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
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
                                Color(0xFF94A3B8),

                            modifier =
                                Modifier.size(12.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text(

                            text =
                                formatTransactionDate(
                                    transaction.created_at
                                ),

                            fontSize =
                                10.sp,

                            color =
                                Color(0xFF94A3B8)
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )


            // ------------------------------------------
            // AMOUNT
            // ------------------------------------------

            Text(

                text =
                    if (isIncome) {

                        "+₹%.2f".format(
                            transaction.amount
                        )

                    } else {

                        "-₹%.2f".format(
                            transaction.amount
                        )
                    },

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    iconColor
            )
        }
    }
}


// ==========================================================
// DATE FORMAT
// ==========================================================

private fun formatTransactionDate(
    value: String?
): String {

    if (value.isNullOrBlank()) {
        return ""
    }

    return try {

        val parsedDate =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            ).parse(value)


        if (parsedDate != null) {

            SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(parsedDate)

        } else {

            value
        }

    } catch (e: Exception) {

        value
    }
}


// ==========================================================
// LOADING
// ==========================================================

@Composable
private fun TransactionsLoading() {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 60.dp,
                    bottom = 60.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        CircularProgressIndicator(

            modifier =
                Modifier.size(45.dp),

            strokeWidth =
                4.dp,

            color =
                Color(0xFF2563EB)
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(

            text =
                "Loading transactions...",

            fontSize =
                15.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                Color(0xFF334155)
        )
    }
}


// ==========================================================
// ERROR
// ==========================================================

@Composable
private fun TransactionsError(
    message: String,
    onRetry: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(28.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(

                modifier =
                    Modifier.size(60.dp),

                shape =
                    CircleShape,

                color =
                    Color(0xFFFFECEC)
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.ReceiptLong,

                        contentDescription =
                            null,

                        tint =
                            Color(0xFFDC2626),

                        modifier =
                            Modifier.size(28.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            Text(

                text =
                    "Couldn't load transactions",

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF0F172A)
            )


            Spacer(
                modifier =
                    Modifier.height(7.dp)
            )


            Text(

                text =
                    message,

                fontSize =
                    12.sp,

                color =
                    Color(0xFF64748B)
            )


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            Button(

                onClick =
                    onRetry,

                shape =
                    RoundedCornerShape(13.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2563EB)
                    )
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
                        Modifier.width(7.dp)
                )

                Text(
                    text =
                        "Try Again",

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// ==========================================================
// EMPTY STATE
// ==========================================================

@Composable
private fun EmptyTransactions() {

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 30.dp
                ),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(

                modifier =
                    Modifier.size(70.dp),

                shape =
                    CircleShape,

                color =
                    Color(0xFFEFF6FF)
            ) {

                Box(

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.ReceiptLong,

                        contentDescription =
                            null,

                        tint =
                            Color(0xFF2563EB),

                        modifier =
                            Modifier.size(34.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            Text(

                text =
                    "No transactions found",

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF0F172A)
            )


            Spacer(
                modifier =
                    Modifier.height(7.dp)
            )


            Text(

                text =
                    "Try changing your filter or search for something else.",

                fontSize =
                    12.sp,

                color =
                    Color(0xFF94A3B8)
            )
        }
    }
}

