package com.surya.trex.ui.pending

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Category
import com.surya.trex.data.model.PendingTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingTransactionsScreen(
    tokenManager: TokenManager,
    onBack: () -> Unit
) {
    val viewModel: PendingTransactionViewModel = viewModel(
        factory = PendingTransactionViewModelFactory(tokenManager)
    )

    val pendingTransactions by
    viewModel.pendingTransactions.collectAsState()

    val categories by
    viewModel.categories.collectAsState()

    val isLoading by
    viewModel.isLoading.collectAsState()

    val isApproving by
    viewModel.isApproving.collectAsState()

    val isDenying by
    viewModel.isDenying.collectAsState()

    val errorMessage by
    viewModel.errorMessage.collectAsState()

    val successMessage by
    viewModel.successMessage.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var selectedTransaction by remember {
        mutableStateOf<PendingTransaction?>(null)
    }

    var showApproveDialog by remember {
        mutableStateOf(false)
    }

    var showDenyDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(errorMessage) {
        val message = errorMessage

        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successMessage) {
        val message = successMessage

        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pending Transactions",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.loadPendingTransactions()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    MaterialTheme.colorScheme.background
                )
        ) {

            when {

                isLoading &&
                        pendingTransactions.isEmpty() -> {

                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }

                pendingTransactions.isEmpty() -> {

                    EmptyPendingTransactions(
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }

                else -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 16.dp
                        ),
                        verticalArrangement =
                            Arrangement.spacedBy(14.dp)
                    ) {

                        item {
                            PendingHeader(
                                count =
                                    pendingTransactions.size
                            )
                        }

                        items(
                            items = pendingTransactions,
                            key = {
                                    transaction ->
                                transaction.id
                            }
                        ) { transaction ->

                            PendingTransactionCard(
                                transaction = transaction,
                                isApproving = isApproving,
                                isDenying = isDenying,
                                onApprove = {
                                    selectedTransaction =
                                        transaction

                                    showApproveDialog = true
                                },
                                onDeny = {
                                    selectedTransaction =
                                        transaction

                                    showDenyDialog = true
                                },
                                onDelete = {
                                    selectedTransaction =
                                        transaction

                                    showDeleteDialog = true
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

    if (
        showApproveDialog &&
        selectedTransaction != null
    ) {

        ApproveTransactionDialog(
            transaction = selectedTransaction!!,
            categories = categories,
            isApproving = isApproving,
            onDismiss = {

                if (!isApproving) {
                    showApproveDialog = false
                    selectedTransaction = null
                }
            },
            onApprove = { categoryId ->

                viewModel.approveTransaction(
                    pendingId = selectedTransaction!!.id,
                    categoryId = categoryId
                )

                showApproveDialog = false
                selectedTransaction = null
            }
        )
    }

    if (
        showDenyDialog &&
        selectedTransaction != null
    ) {

        AlertDialog(
            onDismissRequest = {

                if (!isDenying) {
                    showDenyDialog = false
                    selectedTransaction = null
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null
                )
            },
            title = {
                Text("Deny Transaction?")
            },
            text = {
                Text(
                    "This detected transaction will be marked as denied and will not be added to your transactions."
                )
            },
            confirmButton = {

                Button(
                    enabled = !isDenying,
                    onClick = {

                        viewModel.denyTransaction(
                            selectedTransaction!!.id
                        )

                        showDenyDialog = false
                        selectedTransaction = null
                    }
                ) {

                    if (isDenying) {

                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(18.dp)
                                .height(18.dp),
                            strokeWidth = 2.dp
                        )

                    } else {
                        Text("Deny")
                    }
                }
            },
            dismissButton = {

                OutlinedButton(
                    enabled = !isDenying,
                    onClick = {
                        showDenyDialog = false
                        selectedTransaction = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (
        showDeleteDialog &&
        selectedTransaction != null
    ) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedTransaction = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            },
            title = {
                Text("Delete Pending Transaction?")
            },
            text = {
                Text(
                    "This pending transaction will be permanently removed."
                )
            },
            confirmButton = {

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme.colorScheme.error
                    ),
                    onClick = {

                        viewModel.deleteTransaction(
                            selectedTransaction!!.id
                        )

                        showDeleteDialog = false
                        selectedTransaction = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {

                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedTransaction = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PendingHeader(
    count: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Detected Transactions",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        "$count transaction(s) waiting for your review",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun PendingTransactionCard(
    transaction: PendingTransaction,
    isApproving: Boolean,
    isDenying: Boolean,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            transaction.transaction_type,
                        style =
                            MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (
                                transaction.transaction_type
                                    .equals(
                                        "Income",
                                        ignoreCase = true
                                    )
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            transaction.app_name ?: "SMS",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text =
                        formatAmount(
                            transaction.amount
                        ),
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            if (!transaction.description.isNullOrBlank()) {

                Text(
                    text = transaction.description,
                    style =
                        MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow =
                        TextOverflow.Ellipsis,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            InfoRow(
                label = "Sender",
                value =
                    transaction.sender_id
                        ?: "Unknown"
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            InfoRow(
                label = "Source",
                value =
                    transaction.source_type
            )

            if (
                !transaction.transaction_date
                    .isNullOrBlank()
            ) {

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                InfoRow(
                    label = "Detected",
                    value =
                        formatDate(
                            transaction.transaction_date
                        )
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    modifier = Modifier.weight(1f),
                    enabled =
                        !isApproving &&
                                !isDenying,
                    onClick = onApprove
                ) {

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Text("Approve")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled =
                        !isApproving &&
                                !isDenying,
                    onClick = onDeny
                ) {
                    Text("Deny")
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDelete
            ) {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text("Delete")
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "$label:",
            modifier = Modifier.width(75.dp),
            style =
                MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style =
                MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme.colorScheme
                    .onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApproveTransactionDialog(
    transaction: PendingTransaction,
    categories: List<Category>,
    isApproving: Boolean,
    onDismiss: () -> Unit,
    onApprove: (Int) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    var selectedCategoryId by remember {
        mutableIntStateOf(
            transaction.category_id ?: -1
        )
    }

    /*
     * Instead of storing selectedCategory as a separate
     * variable, find the category directly from the ID.
     */
    val selectedCategoryName =
        categories
            .firstOrNull {
                it.id == selectedCategoryId
            }
            ?.category_name
            ?: "Select category"

    AlertDialog(
        onDismissRequest = {
            if (!isApproving) {
                onDismiss()
            }
        },

        title = {
            Text(
                text = "Approve Transaction",
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "Amount: ${
                            formatAmount(
                                transaction.amount
                            )
                        }",
                    style =
                        MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text =
                        transaction.transaction_type,
                    style =
                        MaterialTheme.typography.bodyMedium
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = "Select category",
                    style =
                        MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {

                        if (!isApproving) {
                            expanded = !expanded
                        }
                    }
                ) {

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),

                        value = selectedCategoryName,

                        onValueChange = {},

                        readOnly = true,

                        label = {
                            Text("Category")
                        },

                        trailingIcon = {
                            ExposedDropdownMenuDefaults
                                .TrailingIcon(
                                    expanded = expanded
                                )
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        if (categories.isEmpty()) {

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "No categories available"
                                    )
                                },
                                onClick = {
                                    expanded = false
                                }
                            )

                        } else {

                            categories.forEach { category ->

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            category.category_name
                                        )
                                    },
                                    onClick = {

                                        selectedCategoryId =
                                            category.id

                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedCategoryId == -1) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Please select a category before approving.",
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {

            Button(
                enabled =
                    selectedCategoryId != -1 &&
                            !isApproving,

                onClick = {
                    onApprove(
                        selectedCategoryId
                    )
                }
            ) {

                if (isApproving) {

                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp),
                        strokeWidth = 2.dp
                    )

                } else {
                    Text("Approve")
                }
            }
        },

        dismissButton = {

            OutlinedButton(
                enabled = !isApproving,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyPendingTransactions(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier
                .width(60.dp)
                .height(60.dp),
            tint =
                MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "No Pending Transactions",
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                "New transactions detected from your configured SMS sources will appear here.",
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )
    }
}

private fun formatAmount(
    amount: String
): String {

    return try {

        val number =
            amount.toBigDecimal()

        "₹${String.format("%.2f", number)}"

    } catch (_: Exception) {

        "₹$amount"
    }
}

private fun formatDate(
    date: String
): String {

    return try {

        date
            .replace("T", " ")
            .substringBefore(".")
            .substringBefore("+")

    } catch (_: Exception) {

        date
    }
}