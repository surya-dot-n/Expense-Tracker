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
import androidx.compose.material.icons.filled.Edit
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
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    tokenManager: TokenManager,
    transaction: Transaction? = null,
    onBack: () -> Unit,
    onEditSaved: () -> Unit = {},
    onManageCategories: () -> Unit = {},
    categoryRefreshKey: Int = 0
) {

    // ==========================================================
    // MODE
    // ==========================================================

    val isEditMode =
        transaction != null


    // ==========================================================
    // FORM STATE
    // ==========================================================

    var amount by remember {
        mutableStateOf(
            transaction?.amount
                ?.let {
                    "%.2f".format(it)
                }
                ?: ""
        )
    }

    var description by remember {
        mutableStateOf(
            transaction?.description ?: ""
        )
    }

    var transactionType by remember {
        mutableStateOf(
            transaction?.transaction_type
                ?: "expense"
        )
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


    // ==========================================================
    // DATE
    // ==========================================================

    var selectedDate by remember {

        mutableStateOf(

            transaction?.transaction_date
                ?.let { dateString ->

                    try {

                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.US
                        ).apply {

                            timeZone =
                                TimeZone.getTimeZone(
                                    "UTC"
                                )

                        }.parse(dateString)

                    } catch (e: Exception) {

                        null
                    }

                }
                ?: Date()
        )
    }


    var showDatePicker by remember {
        mutableStateOf(false)
    }


    // ==========================================================
    // EXISTING TRANSACTION TIME
    // ==========================================================

    val existingTransactionTime =
        transaction?.transaction_time


    // ==========================================================
    // FORMATTERS
    // ==========================================================

    val dateFormatter =
        remember {

            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )
        }


    val backendDateFormatter =
        remember {

            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).apply {

                timeZone =
                    TimeZone.getTimeZone(
                        "UTC"
                    )
            }
        }


    // ==========================================================
    // REPOSITORIES
    // ==========================================================

    val scope =
        rememberCoroutineScope()


    val categoryRepository =
        remember {

            CategoryRepository(
                tokenManager
            )
        }


    val transactionRepository =
        remember {

            TransactionRepository(
                tokenManager
            )
        }


    // ==========================================================
    // LOAD / REFRESH CATEGORIES
    // ==========================================================

    LaunchedEffect(categoryRefreshKey) {

        isLoadingCategories = true

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


    // ==========================================================
    // SELECT EXISTING CATEGORY AFTER CATEGORIES LOAD
    // ==========================================================

    LaunchedEffect(
        categories,
        transaction
    ) {

        if (
            transaction != null &&
            categories.isNotEmpty()
        ) {

            selectedCategory =
                categories.find {

                    it.id ==
                            transaction.category_id
                }
        }
    }


    // ==========================================================
    // DATE PICKER
    // ==========================================================

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


                        showDatePicker =
                            false

                        message =
                            null

                        isSuccess =
                            false
                    }
                ) {

                    Text(

                        text =
                            "Select",

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }
            },


            dismissButton = {

                TextButton(

                    onClick = {

                        showDatePicker =
                            false
                    }
                ) {

                    Text(

                        text =
                            "Cancel",

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        ) {

            DatePicker(
                state =
                    datePickerState
            )
        }
    }


    // ==========================================================
    // SCREEN
    // ==========================================================

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,


        topBar = {

            Surface(

                color =
                    MaterialTheme.colorScheme.surface,

                shadowElevation =
                    2.dp
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

                        onClick =
                            onBack
                    ) {

                        Icon(

                            imageVector =
                                Icons.Default.ArrowBack,

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

                            text =
                                if (isEditMode)
                                    "Edit Transaction"
                                else
                                    "Add Transaction",

                            fontSize =
                                21.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                MaterialTheme.colorScheme.onBackground
                        )


                        Text(

                            text =
                                if (isEditMode)
                                    "Update your transaction details"
                                else
                                    "Record your income or expense",

                            fontSize =
                                12.sp,

                            color =
                                MaterialTheme.colorScheme.outline
                        )
                    }


                    Box(

                        modifier =
                            Modifier
                                .size(42.dp)
                                .background(

                                    color =
                                        MaterialTheme.colorScheme.primaryContainer,

                                    shape =
                                        CircleShape
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(

                            imageVector =
                                if (isEditMode)
                                    Icons.Default.Edit
                                else
                                    Icons.Default.ReceiptLong,

                            contentDescription =
                                null,

                            tint =
                                MaterialTheme.colorScheme.primary
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
                    .imePadding()
                    .navigationBarsPadding()
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


            // ==================================================
            // AMOUNT CARD
            // ==================================================

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
                            2.dp
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
                                            MaterialTheme.colorScheme.primaryContainer,

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
                                    MaterialTheme.colorScheme.primary,

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

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Bold,

                            letterSpacing =
                                0.8.sp,

                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
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

                            text =
                                "₹",

                            fontSize =
                                34.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                MaterialTheme.colorScheme.primary
                        )


                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )


                        OutlinedTextField(

                            value =
                                amount,

                            onValueChange = {

                                if (
                                    it.isEmpty() ||
                                    it.matches(
                                        Regex(
                                            "^\\d*\\.?\\d{0,2}$"
                                        )
                                    )
                                ) {

                                    amount =
                                        it

                                    message =
                                        null

                                    isSuccess =
                                        false
                                }
                            },


                            placeholder = {

                                Text(

                                    text =
                                        "0.00",

                                    fontSize =
                                        30.sp,

                                    fontWeight =
                                        FontWeight.Bold,

                                    color =
                                        MaterialTheme.colorScheme.outline
                                )
                            },


                            singleLine =
                                true,


                            textStyle =
                                LocalTextStyle.current.copy(

                                    fontSize =
                                        30.sp,

                                    fontWeight =
                                        FontWeight.ExtraBold,

                                    color =
                                        MaterialTheme.colorScheme.onBackground
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
                                            MaterialTheme.colorScheme.surfaceVariant,

                                        focusedContainerColor =
                                            MaterialTheme.colorScheme.surfaceVariant,

                                        unfocusedBorderColor =
                                            MaterialTheme.colorScheme.outline,

                                        focusedBorderColor =
                                            MaterialTheme.colorScheme.primary
                                    )
                        )
                    }
                }
            }


            // ==================================================
            // TRANSACTION TYPE
            // ==================================================

            Text(

                text =
                    "Transaction type",

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onBackground
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
                        transactionType ==
                                "expense",

                    icon =
                        "↓",

                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        transactionType =
                            "expense"

                        selectedCategory =
                            null

                        message =
                            null

                        isSuccess =
                            false
                    }
                )


                TransactionTypeCard(

                    title =
                        "Income",

                    subtitle =
                        "Money received",

                    selected =
                        transactionType ==
                                "income",

                    icon =
                        "↑",

                    modifier =
                        Modifier.weight(1f),

                    onClick = {

                        transactionType =
                            "income"

                        selectedCategory =
                            null

                        message =
                            null

                        isSuccess =
                            false
                    }
                )
            }


            // ==================================================
            // CATEGORY HEADER
            // ==================================================

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text =
                        "Category",

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        MaterialTheme.colorScheme.onBackground,

                    modifier =
                        Modifier.weight(1f)
                )


                TextButton(

                    onClick = {

                        expanded =
                            false

                        message =
                            null

                        isSuccess =
                            false

                        onManageCategories()
                    }
                ) {

                    Text(

                        text =
                            "Manage",

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }


            // ==================================================
            // CATEGORY DROPDOWN
            // ==================================================

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
                                MaterialTheme.colorScheme.surface
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

                            strokeWidth =
                                2.dp,

                            color =
                                MaterialTheme.colorScheme.primary
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

                        readOnly =
                            true,

                        singleLine =
                            true,

                        leadingIcon = {

                            Box(

                                modifier =
                                    Modifier
                                        .size(34.dp)
                                        .background(

                                            MaterialTheme.colorScheme.surfaceVariant,

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
                                        MaterialTheme.colorScheme.onSurfaceVariant,

                                    modifier =
                                        Modifier.size(19.dp)
                                )
                            }
                        },


                        trailingIcon = {

                            Icon(

                                imageVector =
                                    Icons.Default.ExpandMore,

                                contentDescription =
                                    "Select category",

                                tint =
                                    MaterialTheme.colorScheme.onSurfaceVariant
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
                                        MaterialTheme.colorScheme.surface,

                                    focusedContainerColor =
                                        MaterialTheme.colorScheme.surface,

                                    unfocusedBorderColor =
                                        MaterialTheme.colorScheme.outline,

                                    focusedBorderColor =
                                        MaterialTheme.colorScheme.primary
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

                                it.category_type
                                    .equals(
                                        transactionType,
                                        ignoreCase =
                                            true
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

                                                category.category_name,

                                                fontWeight =
                                                    FontWeight.Medium
                                            )
                                        },


                                        leadingIcon = {

                                            Icon(

                                                imageVector =
                                                    Icons.Default.ReceiptLong,

                                                contentDescription =
                                                    null
                                            )
                                        },


                                        onClick = {

                                            selectedCategory =
                                                category

                                            expanded =
                                                false

                                            message =
                                                null

                                            isSuccess =
                                                false
                                        }
                                    )
                                }
                        }
                    }
                }
            }


            // ==================================================
            // DATE
            // ==================================================

            Text(

                text =
                    "Transaction date",

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onBackground
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
                                        MaterialTheme.colorScheme.primaryContainer,

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
                                MaterialTheme.colorScheme.primary,

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

                            fontSize =
                                11.sp,

                            color =
                                MaterialTheme.colorScheme.outline
                        )


                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )


                        Text(

                            text =
                                dateFormatter
                                    .format(selectedDate),

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.ExtraBold,

                            color =
                                MaterialTheme.colorScheme.primary
                        )
                    }


                    Text(

                        text =
                            "Change",

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }
            }


            // ==================================================
            // DESCRIPTION
            // ==================================================

            Text(

                text =
                    "Description",

                fontSize =
                    15.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme.colorScheme.onBackground
            )


            OutlinedTextField(

                value =
                    description,

                onValueChange = {

                    description =
                        it

                    message =
                        null

                    isSuccess =
                        false
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
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },


                modifier =
                    Modifier.fillMaxWidth(),


                minLines =
                    2,


                maxLines =
                    3,


                shape =
                    RoundedCornerShape(16.dp),


                colors =
                    OutlinedTextFieldDefaults
                        .colors(

                            unfocusedContainerColor =
                                MaterialTheme.colorScheme.surface,

                            focusedContainerColor =
                                MaterialTheme.colorScheme.surface,

                            unfocusedBorderColor =
                                MaterialTheme.colorScheme.outline,

                            focusedBorderColor =
                                MaterialTheme.colorScheme.primary
                        )
            )


            // ==================================================
            // STATUS MESSAGE
            // ==================================================

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

                                    MaterialTheme.colorScheme.tertiaryContainer

                                } else {

                                    MaterialTheme.colorScheme.errorContainer
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
                                    Icons.Default.CheckCircle,

                                contentDescription =
                                    null,

                                tint =
                                    MaterialTheme.colorScheme.tertiary
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(10.dp)
                            )
                        }


                        Text(

                            text =
                                text,

                            fontSize =
                                14.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                if (isSuccess) {

                                    MaterialTheme.colorScheme.onTertiaryContainer

                                } else {

                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                        )
                    }
                }
            }


            // ==================================================
            // SAVE / UPDATE BUTTON
            // ==================================================

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


                    val transactionDate =
                        backendDateFormatter.format(
                            selectedDate
                        )


                    scope.launch {

                        isLoading =
                            true

                        message =
                            null

                        isSuccess =
                            false


                        // ==================================================
                        // BUILD TRANSACTION
                        // ==================================================

                        val updatedTransaction =
                            Transaction(

                                id =
                                    transaction?.id,

                                user_id =
                                    transaction?.user_id,

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
                                    transactionType,

                                transaction_date =
                                    transactionDate,

                                transaction_time =
                                    if (isEditMode) {

                                        existingTransactionTime

                                    } else {

                                        null
                                    },

                                created_at =
                                    transaction?.created_at,

                                updated_at =
                                    transaction?.updated_at
                            )


                        // ==================================================
                        // ADD
                        // ==================================================

                        if (!isEditMode) {

                            val result =
                                transactionRepository
                                    .createTransaction(
                                        updatedTransaction
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


                        } else {

                            // ==================================================
                            // UPDATE
                            // ==================================================

                            val transactionId =
                                transaction?.id


                            if (transactionId == null) {

                                isLoading =
                                    false

                                message =
                                    "Transaction ID is missing"

                                isSuccess =
                                    false

                                return@launch
                            }


                            val result =
                                transactionRepository
                                    .updateTransaction(

                                        transactionId =
                                            transactionId,

                                        request =
                                            updatedTransaction
                                    )


                            isLoading =
                                false


                            result.onSuccess {

                                onEditSaved()

                            }.onFailure {

                                message =
                                    "Failed to update transaction: ${it.message}"

                                isSuccess =
                                    false
                            }
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
                            MaterialTheme.colorScheme.primary,

                        contentColor =
                            MaterialTheme.colorScheme.onPrimary
                    )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.size(22.dp),

                        strokeWidth =
                            2.dp,

                        color =
                            MaterialTheme.colorScheme.surface
                    )

                } else {

                    Icon(

                        imageVector =
                            if (isEditMode)
                                Icons.Default.Edit
                            else
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
                            if (isEditMode)
                                "Update Transaction"
                            else
                                "Save Transaction",

                        fontSize =
                            16.sp,

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
// TRANSACTION TYPE CARD
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

                        MaterialTheme.colorScheme.primaryContainer

                    } else {

                        MaterialTheme.colorScheme.surface
                    }
            ),

        border =
            if (selected) {

                androidx.compose.foundation
                    .BorderStroke(

                        width =
                            1.5.dp,

                        color =
                            MaterialTheme.colorScheme.primary
                    )

            } else {

                androidx.compose.foundation
                    .BorderStroke(

                        width =
                            1.dp,

                        color =
                            MaterialTheme.colorScheme.outline
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

                                    MaterialTheme.colorScheme.primary

                                } else {

                                    MaterialTheme.colorScheme.surfaceVariant
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

                    fontSize =
                        20.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    color =
                        if (selected) {

                            MaterialTheme.colorScheme.onPrimary

                        } else {

                            MaterialTheme.colorScheme.onSurfaceVariant
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

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        MaterialTheme.colorScheme.onBackground
                )


                Text(

                    text =
                        subtitle,

                    fontSize =
                        11.sp,

                    color =
                        MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}