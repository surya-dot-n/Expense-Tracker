package com.surya.trex.ui.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surya.trex.data.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onBack: () -> Unit
) {

    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var editingCategory by remember {
        mutableStateOf<Category?>(null)
    }

    var deletingCategory by remember {
        mutableStateOf<Category?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    LaunchedEffect(successMessage) {

        successMessage?.let {

            snackbarHostState.showSnackbar(it)

            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(errorMessage) {

        errorMessage?.let {

            snackbarHostState.showSnackbar(it)

            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Text("Categories")
                },
                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Text(
                            text = "‹",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                actions = {

                    IconButton(
                        onClick = {
                            viewModel.loadCategories()
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
        },

        floatingActionButton = {

            androidx.compose.material3.FloatingActionButton(
                onClick = {
                    showAddDialog = true
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Category"
                )
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            if (isLoading && categories.isEmpty()) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    CircularProgressIndicator()

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text("Loading categories...")
                }

            } else if (categories.isEmpty()) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text(
                        text = "No categories found",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Tap + to create your first category"
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = categories,
                        key = { it.id }
                    ) { category ->

                        CategoryItem(
                            category = category,

                            onEdit = {
                                editingCategory = category
                            },

                            onDelete = {
                                deletingCategory = category
                            }
                        )
                    }
                }
            }
        }
    }


    // ============================================================
    // ADD CATEGORY DIALOG
    // ============================================================

    if (showAddDialog) {

        CategoryDialog(
            title = "Add Category",
            confirmText = "Add",

            onDismiss = {
                showAddDialog = false
            },

            onConfirm = { name, type ->

                viewModel.addCategory(
                    categoryName = name,
                    categoryType = type
                )

                showAddDialog = false
            }
        )
    }


    // ============================================================
    // EDIT CATEGORY DIALOG
    // ============================================================

    editingCategory?.let { category ->

        CategoryDialog(
            title = "Edit Category",
            confirmText = "Update",

            initialName = category.category_name,
            initialType = category.category_type,

            onDismiss = {
                editingCategory = null
            },

            onConfirm = { name, type ->

                viewModel.updateCategory(
                    categoryId = category.id,
                    categoryName = name,
                    categoryType = type
                )

                editingCategory = null
            }
        )
    }


    // ============================================================
    // DELETE CONFIRMATION
    // ============================================================

    deletingCategory?.let { category ->

        AlertDialog(
            onDismissRequest = {
                deletingCategory = null
            },

            title = {
                Text("Delete Category")
            },

            text = {
                Text(
                    "Are you sure you want to delete \"${category.category_name}\"?"
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        viewModel.deleteCategory(
                            categoryId = category.id
                        )

                        deletingCategory = null
                    }
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        deletingCategory = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}


// ================================================================
// CATEGORY ITEM
// ================================================================

@Composable
private fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = category.category_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = category.category_type
                        .replaceFirstChar {
                            it.uppercase()
                        },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = onEdit
            ) {

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Category"
                )
            }

            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Category"
                )
            }
        }
    }
}


// ================================================================
// ADD / EDIT CATEGORY DIALOG
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    title: String,
    confirmText: String,
    initialName: String = "",
    initialType: String = "expense",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {

    var categoryName by remember {
        mutableStateOf(initialName)
    }

    var categoryType by remember {
        mutableStateOf(initialType)
    }

    var typeExpanded by remember {
        mutableStateOf(false)
    }

    val isNameValid = categoryName.trim().isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(title)
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Category name")
                    },
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = {
                        typeExpanded = !typeExpanded
                    }
                ) {

                    OutlinedTextField(
                        value = categoryType.replaceFirstChar {
                            it.uppercase()
                        },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = {
                            Text("Category type")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = typeExpanded
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = {
                            typeExpanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("Expense")
                            },
                            onClick = {

                                categoryType = "expense"
                                typeExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Income")
                            },
                            onClick = {

                                categoryType = "income"
                                typeExpanded = false
                            }
                        )
                    }
                }
            }
        },

        confirmButton = {

            Button(
                enabled = isNameValid,
                onClick = {

                    onConfirm(
                        categoryName.trim(),
                        categoryType
                    )
                }
            ) {

                Text(confirmText)
            }
        },

        dismissButton = {

            OutlinedButton(
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
}
