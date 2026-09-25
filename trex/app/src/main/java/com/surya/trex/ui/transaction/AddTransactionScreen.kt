package com.surya.trex.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    tokenManager: TokenManager,
    onBack: () -> Unit
) {

    var amount by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var transactionType by remember {
        mutableStateOf("expense")
    }

    var categories by remember {
        mutableStateOf<List<Category>>(emptyList())
    }

    var selectedCategory by remember {
        mutableStateOf<Category?>(null)
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var isLoadingCategories by remember {
        mutableStateOf(true)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    var isSuccess by remember {
        mutableStateOf(false)
    }

    // ==========================================
    // Date
    // ==========================================

    var selectedDate by remember {
        mutableStateOf(Date())
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val dateFormatter = remember {
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        )
    }

    // ==========================================
    // Repositories
    // ==========================================

    val scope = rememberCoroutineScope()

    val categoryRepository = remember {
        CategoryRepository(tokenManager)
    }

    val transactionRepository = remember {
        TransactionRepository(tokenManager)
    }

    // ==========================================
    // Load Categories
    // ==========================================

    LaunchedEffect(Unit) {

        categoryRepository
            .getCategories()
            .onSuccess {

                categories = it

                isLoadingCategories = false
            }
            .onFailure {

                message =
                    "Failed to load categories: ${it.message}"

                isLoadingCategories = false
            }
    }

    // ==========================================
    // Date Picker
    // ==========================================

    if (showDatePicker) {

        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    selectedDate.time
            )

        DatePickerDialog(

            onDismissRequest = {

                showDatePicker = false
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let { millis ->

                                selectedDate =
                                    Date(millis)
                            }

                        showDatePicker = false

                        message = null
                        isSuccess = false
                    }
                ) {

                    Text(
                        text = "Select",
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            Color(0xFF2563EB)
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        showDatePicker = false
                    }
                ) {

                    Text(
                        text = "Cancel",
                        color =
                            Color(0xFF64748B)
                    )
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }

    // ==========================================
    // Screen
    // ==========================================

    Scaffold(

        containerColor =
            Color(0xFFF7F9FC),

        topBar = {

            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {

                Row(

                    modifier = Modifier
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
                                "Add Transaction",

                            fontSize = 21.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                Color(0xFF0F172A)
                        )

                        Text(
                            text =
                                "Record your income or expense",

                            fontSize = 12.sp,

                            color =
                                Color(0xFF94A3B8)
                        )
                    }

                    Box(

                        modifier =
                            Modifier
                                .size(42.dp)
                                .background(
                                    color =
                                        Color(0xFFEFF6FF),

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
                                Color(0xFF2563EB)
                        )
                    }
                }
            }
        }

    ) { paddingValues ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(
                        horizontal = 18.dp
                    )
                    .verticalScroll(
                        rememberScrollState()
                    ),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            // ==========================================
            // Amount Card
            // ==========================================

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
                        defaultElevation = 2.dp
                    )
            ) {

                Column(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(22.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(

                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .background(
                                        color =
                                            Color(0xFFEFF6FF),

                                        shape =
                                            CircleShape
                                    ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Payments,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFF2563EB),

                                modifier =
                                    Modifier.size(20.dp)
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.width(10.dp)
                        )

                        Text(
                            text =
                                "TRANSACTION AMOUNT",

                            fontSize = 12.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing = 0.8.sp,

                            color =
                                Color(0xFF64748B)
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.Center
                    ) {

                        Text(
                            text = "₹",

                            fontSize = 34.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                Color(0xFF2563EB)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        OutlinedTextField(

                            value = amount,

                            onValueChange = {

                                if (
                                    it.isEmpty() ||
                                    it.matches(
                                        Regex(
                                            "^\\d*\\.?\\d{0,2}$"
                                        )
                                    )
                                ) {

                                    amount = it

                                    message = null

                                    isSuccess = false
                                }
                            },

                            placeholder = {

                                Text(
                                    text = "0.00",

                                    fontSize = 30.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        Color(0xFFCBD5E1)
                                )
                            },

                            singleLine = true,

                            textStyle =
                                LocalTextStyle.current.copy(

                                    fontSize = 30.sp,

                                    fontWeight =
                                        FontWeight.ExtraBold,

                                    color =
                                        Color(0xFF0F172A)
                                ),

                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Decimal
                                ),

                            modifier =
                                Modifier.weight(1f),

                            shape =
                                RoundedCornerShape(16.dp),

                            colors =
                                OutlinedTextFieldDefaults
                                    .colors(

                                        unfocusedContainerColor =
                                            Color(0xFFF8FAFC),

                                        focusedContainerColor =
                                            Color(0xFFF8FAFC),

                                        unfocusedBorderColor =
                                            Color(0xFFE2E8F0),

                                        focusedBorderColor =
                                            Color(0xFF2563EB)
                                    )
                        )
                    }
                }
            }

            // ==========================================
            // Transaction Type
            // ==========================================

            Text(
                text =
                    "Transaction type",

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF0F172A)
            )

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                TransactionTypeCard(

                    title =
                        "Expense",

                    subtitle =
                        "Money spent",

                    selected =
                        transactionType == "expense",

                    icon =
                        "↓",

                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        transactionType =
                            "expense"

                        selectedCategory = null

                        message = null

                        isSuccess = false
                    }
                )

                TransactionTypeCard(

                    title =
                        "Income",

                    subtitle =
                        "Money received",

                    selected =
                        transactionType == "income",

                    icon =
                        "↑",

                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        transactionType =
                            "income"

                        selectedCategory = null

                        message = null

                        isSuccess = false
                    }
                )
            }

            // ==========================================
            // Category
            // ==========================================

            Text(
                text =
                    "Category",

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF0F172A)
            )

            if (isLoadingCategories) {

                Card(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        )
                ) {

                    Box(

                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        CircularProgressIndicator(

                            modifier =
                                Modifier.size(22.dp),

                            strokeWidth = 2.dp,

                            color =
                                Color(0xFF2563EB)
                        )
                    }
                }

            } else {

                ExposedDropdownMenuBox(

                    expanded =
                        expanded,

                    onExpandedChange = {

                        expanded =
                            !expanded
                    }
                ) {

                    OutlinedTextField(

                        value =
                            selectedCategory
                                ?.category_name
                                ?: "Select a category",

                        onValueChange = {},

                        readOnly = true,

                        singleLine = true,

                        leadingIcon = {

                            Box(

                                modifier =
                                    Modifier
                                        .size(34.dp)
                                        .background(
                                            Color(0xFFF1F5F9),
                                            CircleShape
                                        ),

                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(

                                    imageVector =
                                        Icons.Default
                                            .ReceiptLong,

                                    contentDescription =
                                        null,

                                    tint =
                                        Color(0xFF475569),

                                    modifier =
                                        Modifier.size(19.dp)
                                )
                            }
                        },

                        trailingIcon = {

                            Icon(

                                imageVector =
                                    Icons.Default
                                        .ExpandMore,

                                contentDescription =
                                    "Select category",

                                tint =
                                    Color(0xFF64748B)
                            )
                        },

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),

                        shape =
                            RoundedCornerShape(16.dp),

                        colors =
                            OutlinedTextFieldDefaults
                                .colors(

                                    unfocusedContainerColor =
                                        Color.White,

                                    focusedContainerColor =
                                        Color.White,

                                    unfocusedBorderColor =
                                        Color(0xFFE2E8F0),

                                    focusedBorderColor =
                                        Color(0xFF2563EB)
                                )
                    )

                    ExposedDropdownMenu(

                        expanded =
                            expanded,

                        onDismissRequest = {

                            expanded =
                                false
                        }
                    ) {

                        val filteredCategories =
                            categories.filter {

                                it.category_type.equals(
                                    transactionType,
                                    ignoreCase = true
                                )
                            }

                        if (
                            filteredCategories.isEmpty()
                        ) {

                            DropdownMenuItem(

                                text = {

                                    Text(
                                        "No categories available"
                                    )
                                },

                                onClick = {

                                    expanded =
                                        false
                                }
                            )

                        } else {

                            filteredCategories
                                .forEach { category ->

                                    DropdownMenuItem(

                                        text = {

                                            Text(
                                                category
                                                    .category_name,

                                                fontWeight =
                                                    FontWeight.Medium
                                            )
                                        },

                                        leadingIcon = {

                                            Icon(

                                                imageVector =
                                                    Icons.Default
                                                        .ReceiptLong,

                                                contentDescription =
                                                    null
                                            )
                                        },

                                        onClick = {

                                            selectedCategory =
                                                category

                                            expanded =
                                                false

                                            message = null

                                            isSuccess =
                                                false
                                        }
                                    )
                                }
                        }
                    }
                }
            }

            // ==========================================
            // Date
            // ==========================================

            Text(
                text =
                    "Transaction date",

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF0F172A)
            )

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(18.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    ),

                onClick = {

                    showDatePicker =
                        true
                }
            ) {

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(15.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(

                        modifier =
                            Modifier
                                .size(44.dp)
                                .background(
                                    color =
                                        Color(0xFFEFF6FF),

                                    shape =
                                        CircleShape
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.CalendarToday,

                            contentDescription =
                                null,

                            tint =
                                Color(0xFF2563EB),

                            modifier =
                                Modifier.size(21.dp)
                        )
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
                                "Selected date",

                            fontSize = 11.sp,

                            color =
                                Color(0xFF94A3B8)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )

                        Text(
                            text =
                                dateFormatter
                                    .format(selectedDate),

                            fontSize = 17.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                Color(0xFF2563EB)
                        )
                    }

                    Text(
                        text =
                            "Change",

                        fontSize = 12.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            Color(0xFF2563EB)
                    )
                }
            }

            // ==========================================
            // Description
            // ==========================================

            Text(
                text =
                    "Description",

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color(0xFF0F172A)
            )

            OutlinedTextField(

                value =
                    description,

                onValueChange = {

                    description =
                        it

                    message = null

                    isSuccess = false
                },

                placeholder = {

                    Text(
                        "What was this transaction for?"
                    )
                },

                leadingIcon = {

                    Icon(

                        imageVector =
                            Icons.Default.Payments,

                        contentDescription =
                            null,

                        tint =
                            Color(0xFF64748B)
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                minLines = 2,

                maxLines = 3,

                shape =
                    RoundedCornerShape(16.dp),

                colors =
                    OutlinedTextFieldDefaults
                        .colors(

                            unfocusedContainerColor =
                                Color.White,

                            focusedContainerColor =
                                Color.White,

                            unfocusedBorderColor =
                                Color(0xFFE2E8F0),

                            focusedBorderColor =
                                Color(0xFF2563EB)
                        )
            )

            // ==========================================
            // Status Message
            // ==========================================

            message?.let { text ->

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        CardDefaults.cardColors(

                            containerColor =
                                if (isSuccess) {

                                    Color(0xFFECFDF5)

                                } else {

                                    Color(0xFFFEF2F2)
                                }
                        )
                ) {

                    Row(

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(14.dp),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        if (isSuccess) {

                            Icon(

                                imageVector =
                                    Icons.Default
                                        .CheckCircle,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFF16A34A)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(10.dp)
                            )
                        }

                        Text(

                            text =
                                text,

                            fontSize = 14.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                if (isSuccess) {

                                    Color(0xFF166534)

                                } else {

                                    Color(0xFFB91C1C)
                                }
                        )
                    }
                }
            }

            // ==========================================
            // Save Button
            // ==========================================

            Button(

                onClick = {

                    val amountValue =
                        amount.toDoubleOrNull()

                    if (
                        amountValue == null ||
                        amountValue <= 0
                    ) {

                        message =
                            "Please enter a valid amount"

                        isSuccess =
                            false

                        return@Button
                    }

                    if (
                        selectedCategory == null
                    ) {

                        message =
                            "Please select a category"

                        isSuccess =
                            false

                        return@Button
                    }

                    scope.launch {

                        isLoading =
                            true

                        message =
                            null

                        isSuccess =
                            false

                        val transaction =
                            Transaction(

                                category_id =
                                    selectedCategory!!.id,

                                amount =
                                    amountValue,

                                description =
                                    description
                                        .ifBlank {
                                            null
                                        },

                                transaction_type =
                                    transactionType
                            )

                        val result =
                            transactionRepository
                                .createTransaction(
                                    transaction
                                )

                        isLoading =
                            false

                        result.onSuccess {

                            message =
                                "Transaction added successfully"

                            isSuccess =
                                true

                            amount =
                                ""

                            description =
                                ""

                            selectedCategory =
                                null

                        }.onFailure {

                            message =
                                "Failed: ${it.message}"

                            isSuccess =
                                false
                        }
                    }
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(58.dp),

                enabled =
                    !isLoading &&
                            !isLoadingCategories,

                shape =
                    RoundedCornerShape(18.dp),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF2563EB),

                        contentColor =
                            Color.White
                    )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.size(22.dp),

                        strokeWidth = 2.dp,

                        color =
                            Color.White
                    )

                } else {

                    Icon(

                        imageVector =
                            Icons.Default.CheckCircle,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(21.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(9.dp)
                    )

                    Text(

                        text =
                            "Save Transaction",

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )
        }
    }
}

// ======================================================
// Transaction Type Card
// ======================================================

@Composable
private fun TransactionTypeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(

        modifier =
            modifier,

        onClick =
            onClick,

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    if (selected) {

                        Color(0xFFEFF6FF)

                    } else {

                        Color.White
                    }
            ),

        border =
            if (selected) {

                androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = Color(0xFF2563EB)
                )

            } else {

                androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0)
                )
            },

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    if (selected) {
                        2.dp
                    } else {
                        0.dp
                    }
            )
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(

                modifier =
                    Modifier
                        .size(40.dp)
                        .background(

                            color =
                                if (selected) {

                                    Color(0xFF2563EB)

                                } else {

                                    Color(0xFFF1F5F9)
                                },

                            shape =
                                CircleShape
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(

                    text =
                        icon,

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    color =
                        if (selected) {

                            Color.White

                        } else {

                            Color(0xFF64748B)
                        }
                )
            }

            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )

            Column {

                Text(

                    text =
                        title,

                    fontSize = 14.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color(0xFF0F172A)
                )

                Text(

                    text =
                        subtitle,

                    fontSize = 11.sp,

                    color =
                        Color(0xFF94A3B8)
                )
            }
        }
    }
}