
package com.surya.trex.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.CategorySummary
import com.surya.trex.data.model.DashboardSummary
import com.surya.trex.data.model.RecentTransaction
import com.surya.trex.data.repository.DashboardRepository
import com.surya.trex.data.repository.DashboardRefreshManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.max


@Composable
fun HomeScreen(
    tokenManager: TokenManager,
    onLogout: () -> Unit,
    onAddTransaction: () -> Unit,
    onViewTransactions: () -> Unit,
    onProfileClick: () -> Unit,
    onPendingTransactions: () -> Unit
) {

    var summary by remember {
        mutableStateOf<DashboardSummary?>(null)
    }

    var categorySummary by remember {
        mutableStateOf<List<CategorySummary>>(emptyList())
    }

    var recentTransactions by remember {
        mutableStateOf<List<RecentTransaction>>(emptyList())
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

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    val repository = remember {
        DashboardRepository(tokenManager)
    }

    val scope = rememberCoroutineScope()

    suspend fun loadDashboard(
        showFullLoading: Boolean = false
    ) {

        if (showFullLoading) {
            isLoading = true
        } else {
            isRefreshing = true
        }

        errorMessage = null

        try {

            // -----------------------------------------
            // Load all dashboard data
            // -----------------------------------------

            val summaryResult =
                repository.getDashboardSummary()

            if (summaryResult.isFailure) {
                throw Exception(
                    "Failed to load balance: ${
                        summaryResult.exceptionOrNull()?.message
                            ?: "Unknown error"
                    }"
                )
            }

            val categoriesResult =
                repository.getDashboardCategories()

            if (categoriesResult.isFailure) {
                throw Exception(
                    "Failed to load spending overview: ${
                        categoriesResult.exceptionOrNull()?.message
                            ?: "Unknown error"
                    }"
                )
            }

            val recentResult =
                repository.getRecentTransactions()

            if (recentResult.isFailure) {
                throw Exception(
                    "Failed to load recent transactions: ${
                        recentResult.exceptionOrNull()?.message
                            ?: "Unknown error"
                    }"
                )
            }

            // -----------------------------------------
            // Update HomeScreen only after all requests
            // succeeded.
            // -----------------------------------------

            summary =
                summaryResult.getOrNull()

            categorySummary =
                categoriesResult.getOrNull()
                    ?: emptyList()

            recentTransactions =
                recentResult.getOrNull()
                    ?: emptyList()

        } catch (e: Exception) {

            errorMessage =
                e.message
                    ?: "Unable to load dashboard"

        } finally {

            isLoading = false
            isRefreshing = false
        }
    }


    // -----------------------------------------
    // Initial dashboard load
    // -----------------------------------------

    LaunchedEffect(Unit) {

        loadDashboard(
            showFullLoading = true
        )
    }


    // -----------------------------------------
    // Live dashboard refresh events
    // -----------------------------------------

    LaunchedEffect(Unit) {

        DashboardRefreshManager.refreshEvents.collect {

            loadDashboard(
                showFullLoading = false
            )
        }
    }


    // -----------------------------------------
    // Logout dialog
    // -----------------------------------------

    if (showLogoutDialog) {

        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },

            title = {
                Text(
                    text = "Log out?"
                )
            },

            text = {
                Text(
                    text =
                        "Are you sure you want to log out of Trex?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showLogoutDialog = false

                        onLogout()
                    }
                ) {

                    Text(
                        text = "Log out",
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }


    // -----------------------------------------
    // Main Scaffold
    // -----------------------------------------

    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        floatingActionButton = {

            ExtendedFloatingActionButton(

                onClick = onAddTransaction,

                icon = {

                    Icon(
                        imageVector =
                            Icons.Default.Add,
                        contentDescription = null
                    )
                },

                text = {

                    Text(
                        text = "Add Transaction",
                        fontWeight = FontWeight.Bold
                    )
                },

                containerColor =
                    MaterialTheme.colorScheme.primary,

                contentColor =
                    MaterialTheme.colorScheme.onPrimary,

                shape =
                    RoundedCornerShape(18.dp)
            )
        }

    ) { innerPadding ->


        LazyColumn(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),

            contentPadding =
                PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 110.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {


            // ==================================================
            // HEADER
            // ==================================================

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // --------------------------------------
                    // Profile button
                    // --------------------------------------

                    Surface(

                        modifier =
                            Modifier.size(44.dp),

                        shape =
                            CircleShape,

                        color =
                            MaterialTheme.colorScheme.surface,

                        shadowElevation =
                            3.dp
                    ) {

                        IconButton(

                            onClick =
                                onProfileClick
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.AccountCircle,

                                contentDescription =
                                    "Profile",

                                tint =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    // --------------------------------------
                    // Pending Transactions button
                    // --------------------------------------

                    Surface(

                        modifier =
                            Modifier.size(44.dp),

                        shape =
                            CircleShape,

                        color =
                            MaterialTheme.colorScheme.surface,

                        shadowElevation =
                            3.dp
                    ) {

                        IconButton(

                            onClick =
                                onPendingTransactions
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.ReceiptLong,

                                contentDescription =
                                    "Pending Transactions",

                                tint =
                                    MaterialTheme.colorScheme.secondary
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    // --------------------------------------
                    // Refresh button
                    // --------------------------------------

                    Surface(

                        modifier =
                            Modifier.size(44.dp),

                        shape =
                            CircleShape,

                        color =
                            MaterialTheme.colorScheme.surface,

                        shadowElevation =
                            3.dp
                    ) {

                        IconButton(

                            enabled =
                                !isRefreshing &&
                                        !isLoading,

                            onClick = {

                                scope.launch {

                                    loadDashboard(
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
                                        MaterialTheme.colorScheme.primary
                                )

                            } else {

                                Icon(

                                    imageVector =
                                        Icons.Default.Refresh,

                                    contentDescription =
                                        "Refresh",

                                    tint =
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )


                    // --------------------------------------
                    // Logout button
                    // --------------------------------------

                    Surface(

                        modifier =
                            Modifier.size(44.dp),

                        shape =
                            CircleShape,

                        color =
                            MaterialTheme.colorScheme.surface,

                        shadowElevation =
                            3.dp
                    ) {

                        IconButton(

                            onClick = {

                                showLogoutDialog =
                                    true
                            }
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.Logout,

                                contentDescription =
                                    "Logout",

                                tint =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }



            // ==================================================
            // INITIAL LOADING
            // ==================================================

            if (isLoading) {

                item {

                    LoadingDashboard()
                }
            }


            // ==================================================
            // ERROR
            // ==================================================

            else if (errorMessage != null) {

                item {

                    DashboardError(

                        message =
                            errorMessage
                                ?: "Something went wrong",

                        onRetry = {

                            scope.launch {

                                loadDashboard(
                                    showFullLoading =
                                        true
                                )
                            }
                        }
                    )
                }
            }


            // ==================================================
            // DASHBOARD CONTENT
            // ==================================================

            else {

                // ----------------------------------------------
                // Balance
                // ----------------------------------------------

                item {

                    summary?.let { data ->

                        BalanceCard(
                            data = data
                        )
                    }
                }


                // ----------------------------------------------
                // Quick stats
                // ----------------------------------------------

                item {

                    summary?.let { data ->

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            QuickStatCard(

                                modifier =
                                    Modifier.weight(1f),

                                title =
                                    "Income",

                                amount =
                                    data.total_income,

                                icon =
                                    Icons.Default.ArrowDownward,

                                iconBackground =
                                    MaterialTheme.colorScheme.tertiaryContainer,

                                iconColor =
                                    MaterialTheme.colorScheme.tertiary
                            )


                            QuickStatCard(

                                modifier =
                                    Modifier.weight(1f),

                                title =
                                    "Expenses",

                                amount =
                                    data.total_expense,

                                icon =
                                    Icons.Default.ArrowUpward,

                                iconBackground =
                                    MaterialTheme.colorScheme.errorContainer,

                                iconColor =
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }


                // ----------------------------------------------
                // Financial overview
                // ----------------------------------------------

                item {

                    summary?.let { data ->

                        val income =
                            max(
                                data.total_income,
                                0.01
                            )

                        val expenseRatio =
                            (
                                    data.total_expense /
                                            income
                                    )
                                .coerceIn(
                                    0.0,
                                    1.0
                                )


                        Card(

                            modifier =
                                Modifier.fillMaxWidth(),

                            shape =
                                RoundedCornerShape(22.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surface
                                ),

                            elevation =
                                CardDefaults.cardElevation(
                                    defaultElevation =
                                        2.dp
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
                                            Modifier.size(42.dp),

                                        shape =
                                            CircleShape,

                                        color =
                                            MaterialTheme.colorScheme.primaryContainer
                                    ) {

                                        Box(

                                            contentAlignment =
                                                Alignment.Center
                                        ) {

                                            Icon(

                                                imageVector =
                                                    Icons.Default.TrendingUp,

                                                contentDescription =
                                                    null,

                                                tint =
                                                    MaterialTheme.colorScheme.primary,

                                                modifier =
                                                    Modifier.size(22.dp)
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
                                                "Financial overview",

                                            fontSize =
                                                16.sp,

                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(

                                            text =
                                                "Your spending compared with income",

                                            fontSize =
                                                12.sp,

                                            color =
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }


                                Spacer(
                                    modifier =
                                        Modifier.height(18.dp)
                                )


                                LinearProgressIndicator(

                                    progress = {
                                        expenseRatio.toFloat()
                                    },

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(9.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    20.dp
                                                )
                                            ),

                                    color =
                                        MaterialTheme.colorScheme.primary,

                                    trackColor =
                                        MaterialTheme.colorScheme.primaryContainer
                                )


                                Spacer(
                                    modifier =
                                        Modifier.height(9.dp)
                                )


                                Row(

                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {

                                    Text(

                                        text =
                                            if (
                                                data.total_income > 0
                                            ) {

                                                "%.0f%% of income spent"
                                                    .format(
                                                        expenseRatio * 100
                                                    )

                                            } else {

                                                "No income recorded yet"
                                            },

                                        fontSize =
                                            12.sp,

                                        color =
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )


                                    Text(

                                        text =
                                            if (
                                                data.balance >= 0
                                            ) {

                                                "Good progress"

                                            } else {

                                                "Watch your spending"
                                            },

                                        fontSize =
                                            12.sp,

                                        fontWeight =
                                            FontWeight.Bold,

                                        color =
                                            if (
                                                data.balance >= 0
                                            ) {

                                                MaterialTheme.colorScheme.tertiary

                                            } else {

                                                MaterialTheme.colorScheme.error
                                            }
                                    )
                                }
                            }
                        }
                    }
                }


                // ----------------------------------------------
                // Spending overview
                // ----------------------------------------------

                item {

                    Text(

                        text =
                            "Spending overview",

                        fontSize =
                            21.sp,

                        fontWeight =
                            FontWeight.ExtraBold,

                        color =
                            MaterialTheme.colorScheme.onBackground
                    )
                }


                if (categorySummary.isEmpty()) {

                    item {

                        EmptyCard(

                            icon =
                                Icons.Default.Category,

                            title =
                                "No spending yet",

                            message =
                                "Add your first expense to see where your money goes."
                        )
                    }

                } else {

                    items(
                        categorySummary
                    ) { category ->

                        CategoryCard(

                            category =
                                category,

                            totalExpense =
                                summary?.total_expense
                                    ?: 0.0
                        )
                    }
                }


                // ----------------------------------------------
                // Recent transactions header
                // ----------------------------------------------

                item {

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column {

                            Text(

                                text =
                                    "Recent transactions",

                                fontSize =
                                    21.sp,

                                fontWeight =
                                    FontWeight.ExtraBold,

                                color =
                                    MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(3.dp)
                            )

                            Text(

                                text =
                                    "Your latest activity",

                                fontSize =
                                    12.sp,

                                color =
                                    MaterialTheme.colorScheme.outline
                            )
                        }


                        TextButton(

                            onClick =
                                onViewTransactions
                        ) {

                            Text(

                                text =
                                    "View all",

                                fontSize =
                                    13.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }


                // ----------------------------------------------
                // Recent transactions
                // ----------------------------------------------

                if (recentTransactions.isEmpty()) {

                    item {

                        EmptyCard(

                            icon =
                                Icons.Default.AccountBalanceWallet,

                            title =
                                "Nothing here yet",

                            message =
                                "Your recent transactions will appear here."
                        )
                    }

                } else {

                    items(
                        recentTransactions
                    ) { transaction ->

                        TransactionCard(
                            transaction =
                                transaction
                        )
                    }
                }


                // ----------------------------------------------
                // Bottom branding
                // ----------------------------------------------

                item {

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Column(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Savings,

                            contentDescription =
                                null,

                            tint =
                                MaterialTheme.colorScheme.primary,

                            modifier =
                                Modifier.size(24.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(

                            text =
                                "Manage smart. Spend better.",

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}


// ==========================================================
// LOADING
// ==========================================================

@Composable
private fun LoadingDashboard() {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 70.dp,
                    bottom = 70.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        CircularProgressIndicator(

            color =
                MaterialTheme.colorScheme.primary,

            strokeWidth =
                4.dp,

            modifier =
                Modifier.size(48.dp)
        )

        Spacer(
            modifier =
                Modifier.height(18.dp)
        )

        Text(

            text =
                "Loading your finances...",

            fontSize =
                15.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(5.dp)
        )

        Text(

            text =
                "Getting your latest financial information",

            fontSize =
                12.sp,

            color =
                MaterialTheme.colorScheme.outline
        )
    }
}


// ==========================================================
// ERROR
// ==========================================================

@Composable
private fun DashboardError(
    message: String,
    onRetry: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    3.dp
            )
    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(26.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(

                modifier =
                    Modifier.size(64.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme.errorContainer
            ) {

                Box(

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            Icons.Default.ErrorOutline,

                        contentDescription =
                            null,

                        tint =
                            MaterialTheme.colorScheme.error,

                        modifier =
                            Modifier.size(32.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            Text(

                text =
                    "Couldn't load your dashboard",

                fontSize =
                    19.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onBackground
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            Text(

                text =
                    message,

                fontSize =
                    13.sp,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,

                textAlign =
                    androidx.compose.ui.text.style.TextAlign.Center
            )


            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )


            Button(

                onClick =
                    onRetry,

                shape =
                    RoundedCornerShape(14.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme.colorScheme.primary
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
                        Modifier.width(8.dp)
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
// BALANCE CARD
// ==========================================================

@Composable
private fun BalanceCard(
    data: DashboardSummary
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(28.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    6.dp
            )
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                        )
                    )
        ) {

            Column(

                modifier =
                    Modifier.padding(24.dp)
            ) {

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column {

                        Text(

                            text =
                                "TOTAL BALANCE",

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                1.2.sp,

                            color =
                                MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.75f
                                )
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(

                            text =
                                "₹%.2f".format(
                                    data.balance
                                ),

                            fontSize =
                                34.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                MaterialTheme.colorScheme.surface
                        )
                    }


                    Surface(

                        modifier =
                            Modifier.size(56.dp),

                        shape =
                            CircleShape,

                        color =
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.16f
                            )
                    ) {

                        Box(

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(

                                imageVector =
                                    Icons.Default.AccountBalanceWallet,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme.colorScheme.onPrimary,

                                modifier =
                                    Modifier.size(28.dp)
                            )
                        }
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(25.dp)
                )


                HorizontalDivider(

                    color =
                        MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.18f
                        )
                )


                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )


                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    WhiteBalanceItem(

                        title =
                            "Income",

                        amount =
                            data.total_income,

                        icon =
                            Icons.Default.ArrowDownward
                    )


                    WhiteBalanceItem(

                        title =
                            "Expenses",

                        amount =
                            data.total_expense,

                        icon =
                            Icons.Default.ArrowUpward
                    )
                }
            }
        }
    }
}


// ==========================================================
// WHITE BALANCE ITEM
// ==========================================================

@Composable
private fun WhiteBalanceItem(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {

    Row(

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Surface(

            modifier =
                Modifier.size(34.dp),

            shape =
                CircleShape,

            color =
                MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.15f
                )
        ) {

            Box(

                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    imageVector =
                        icon,

                    contentDescription =
                        null,

                    tint =
                        MaterialTheme.colorScheme.onPrimary,

                    modifier =
                        Modifier.size(18.dp)
                )
            }
        }


        Spacer(
            modifier =
                Modifier.width(9.dp)
        )


        Column {

            Text(

                text =
                    title,

                fontSize =
                    11.sp,

                color =
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.72f
                    )
            )

            Text(

                text =
                    "₹%.2f".format(
                        amount
                    ),

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.surface
            )
        }
    }
}


// ==========================================================
// QUICK STAT CARD
// ==========================================================

@Composable
private fun QuickStatCard(
    modifier: Modifier,
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    iconColor: Color
) {

    Card(

        modifier =
            modifier,

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(17.dp)
        ) {

            Row(

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(

                    modifier =
                        Modifier.size(38.dp),

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
                                icon,

                            contentDescription =
                                null,

                            tint =
                                iconColor,

                            modifier =
                                Modifier.size(20.dp)
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.width(9.dp)
                )


                Text(

                    text =
                        title,

                    fontSize =
                        13.sp,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    fontWeight =
                        FontWeight.Medium
                )
            }


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Text(

                text =
                    "₹%.2f".format(
                        amount
                    ),

                fontSize =
                    19.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    MaterialTheme.colorScheme.onBackground,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}


// ==========================================================
// CATEGORY CARD
// ==========================================================

@Composable
private fun CategoryCard(
    category: CategorySummary,
    totalExpense: Double
) {

    val percentage =

        if (totalExpense > 0) {

            (
                    category.total_amount /
                            totalExpense
                    )
                .coerceIn(
                    0.0,
                    1.0
                )

        } else {

            0.0
        }


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(18.dp)
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(

                    modifier =
                        Modifier.size(44.dp),

                    shape =
                        CircleShape,

                    color =
                        MaterialTheme.colorScheme.primaryContainer
                ) {

                    Box(

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.Category,

                            contentDescription =
                                null,

                            tint =
                                MaterialTheme.colorScheme.primary,

                            modifier =
                                Modifier.size(22.dp)
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.width(13.dp)
                )


                Column(

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(

                        text =
                            category.category_name,

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme.colorScheme.onBackground
                    )


                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )


                    Text(

                        text =
                            "${(percentage * 100).toInt()}% of expenses",

                        fontSize =
                            11.sp,

                        color =
                            MaterialTheme.colorScheme.outline
                    )
                }


                Text(

                    text =
                        "₹%.2f".format(
                            category.total_amount
                        ),

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    color =
                        MaterialTheme.colorScheme.onBackground
                )
            }


            Spacer(
                modifier =
                    Modifier.height(13.dp)
            )


            LinearProgressIndicator(

                progress = {
                    percentage.toFloat()
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(
                            RoundedCornerShape(
                                10.dp
                            )
                        ),

                color =
                    MaterialTheme.colorScheme.primary,

                trackColor =
                    MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}


// ==========================================================
// TRANSACTION CARD
// ==========================================================

@Composable
private fun TransactionCard(
    transaction: RecentTransaction
) {

    val isIncome =

        transaction.transaction_type
            .equals(
                "income",
                ignoreCase = true
            )


    val iconColor =

        if (isIncome) {

            MaterialTheme.colorScheme.tertiary

        } else {

            MaterialTheme.colorScheme.error
        }


    val iconBackground =

        if (isIncome) {

            MaterialTheme.colorScheme.tertiaryContainer

        } else {

            MaterialTheme.colorScheme.errorContainer
        }


    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
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

            Surface(

                modifier =
                    Modifier.size(46.dp),

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
                            Modifier.size(21.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.width(13.dp)
            )


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
                        FontWeight.SemiBold,

                    color =
                        MaterialTheme.colorScheme.onBackground,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )


                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )


                Surface(

                    shape =
                        RoundedCornerShape(7.dp),

                    color =
                        MaterialTheme.colorScheme.surfaceVariant
                ) {

                    Text(

                        text =
                            transaction
                                .transaction_type
                                .replaceFirstChar {
                                    it.uppercase()
                                },

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
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )


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
// EMPTY CARD
// ==========================================================

@Composable
private fun EmptyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
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
                    Modifier.size(58.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme.primaryContainer
            ) {

                Box(

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            icon,

                        contentDescription =
                            null,

                        tint =
                            MaterialTheme.colorScheme.primary,

                        modifier =
                            Modifier.size(28.dp)
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(13.dp)
            )


            Text(

                text =
                    title,

                fontSize =
                    16.sp,

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
                    message,

                fontSize =
                    12.sp,

                color =
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}
