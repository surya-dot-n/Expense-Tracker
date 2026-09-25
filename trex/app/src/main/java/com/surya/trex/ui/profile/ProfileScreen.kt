package com.surya.trex.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {

    // ==================================================
    // STATE
    // ==================================================

    val profile by
    viewModel.profile.collectAsState()

    val isLoading by
    viewModel.isLoading.collectAsState()

    val isSaving by
    viewModel.isSaving.collectAsState()

    val errorMessage by
    viewModel.errorMessage.collectAsState()


    var isEditing by
    remember {
        mutableStateOf(false)
    }

    var editedName by
    remember {
        mutableStateOf("")
    }

    var showThemeDialog by
    remember {
        mutableStateOf(false)
    }


    // ==================================================
    // LOAD PROFILE
    // ==================================================

    LaunchedEffect(Unit) {

        viewModel.loadProfile()
    }


    // ==================================================
    // UPDATE EDITED NAME
    // ==================================================

    LaunchedEffect(profile) {

        profile?.let {

            editedName =
                it.name
        }
    }


    // ==================================================
    // SCREEN
    // ==================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        // ==================================================
        // TOP BAR
        // ==================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
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
                        "Back"
                )
            }

            Text(
                text = "Profile",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )
        }


        // ==================================================
        // LOADING
        // ==================================================

        if (isLoading) {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                contentAlignment =
                    Alignment.Center
            ) {

                CircularProgressIndicator()
            }

        }


        // ==================================================
        // ERROR
        // ==================================================

        else if (
            profile == null &&
            errorMessage != null
        ) {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),

                contentAlignment =
                    Alignment.Center
            ) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            "Unable to load profile",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            errorMessage
                                ?: "Something went wrong",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Button(
                        onClick = {

                            viewModel.clearError()

                            viewModel.loadProfile()
                        }
                    ) {

                        Text("Retry")
                    }
                }
            }

        }


        // ==================================================
        // PROFILE CONTENT
        // ==================================================

        else if (profile != null) {

            val user =
                profile!!

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            horizontal = 20.dp
                        ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                // ==================================================
                // AVATAR
                // ==================================================

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Box(
                    modifier =
                        Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            user.name
                                .firstOrNull()
                                ?.uppercase()
                                ?: "?",

                        style =
                            MaterialTheme
                                .typography
                                .headlineLarge,

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                // ==================================================
                // USER NAME
                // ==================================================

                Text(
                    text =
                        user.name,

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,

                    fontWeight =
                        FontWeight.Bold
                )


                // ==================================================
                // USER EMAIL
                // ==================================================

                Text(
                    text =
                        user.email,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                Spacer(
                    modifier =
                        Modifier.height(28.dp)
                )


                // ==================================================
                // PROFILE INFORMATION
                // ==================================================

                Text(
                    text =
                        "Profile Information",

                    modifier =
                        Modifier.fillMaxWidth(),

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                // ==================================================
                // EDIT MODE
                // ==================================================

                if (isEditing) {

                    OutlinedTextField(
                        value =
                            editedName,

                        onValueChange = {

                            editedName =
                                it
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text("Name")
                        },

                        singleLine = true,

                        trailingIcon = {

                            Icon(
                                imageVector =
                                    Icons.Default.Edit,

                                contentDescription =
                                    "Edit name"
                            )
                        }
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    OutlinedTextField(
                        value =
                            user.email,

                        onValueChange = {},

                        modifier =
                            Modifier.fillMaxWidth(),

                        label = {

                            Text("Email")
                        },

                        singleLine = true,

                        readOnly = true,

                        trailingIcon = {

                            Icon(
                                imageVector =
                                    Icons.Default.Lock,

                                contentDescription =
                                    "Email cannot be changed"
                            )
                        }
                    )


                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )


                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.End
                    ) {

                        TextButton(
                            onClick = {

                                editedName =
                                    user.name

                                isEditing =
                                    false

                                viewModel.clearError()
                            }
                        ) {

                            Text("Cancel")
                        }


                        Button(
                            onClick = {

                                viewModel.updateName(
                                    editedName
                                )

                                isEditing =
                                    false
                            },

                            enabled =
                                editedName.isNotBlank()
                                        && !isSaving
                        ) {

                            if (isSaving) {

                                CircularProgressIndicator(
                                    modifier =
                                        Modifier.size(18.dp),

                                    strokeWidth = 2.dp
                                )

                            } else {

                                Text("Save")
                            }
                        }
                    }

                }


                // ==================================================
                // NORMAL MODE
                // ==================================================

                else {

                    ProfileInfoRow(
                        label =
                            "Name",

                        value =
                            user.name,

                        showEdit =
                            true,

                        onEdit = {

                            editedName =
                                user.name

                            isEditing =
                                true
                        }
                    )


                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )


                    ProfileInfoRow(
                        label =
                            "Email",

                        value =
                            user.email,

                        showEdit =
                            false
                    )
                }


                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                HorizontalDivider()


                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )


                // ==================================================
                // THEME
                // ==================================================

                ProfileMenuItem(
                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Palette,

                            contentDescription =
                                "Theme"
                        )
                    },

                    title =
                        "Theme",

                    onClick = {

                        showThemeDialog =
                            true
                    }
                )


                // ==================================================
                // SETTINGS
                // ==================================================

                ProfileMenuItem(
                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Settings,

                            contentDescription =
                                "Settings"
                        )
                    },

                    title =
                        "Settings",

                    onClick =
                        onSettingsClick
                )


                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )


                // ==================================================
                // LOGOUT
                // ==================================================

                Button(
                    onClick =
                        onLogout,

                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(12.dp)
                ) {

                    Text("Logout")
                }


                // ==================================================
                // ERROR AFTER PROFILE LOADED
                // ==================================================

                if (errorMessage != null) {

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            errorMessage!!,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            }
        }
    }


    // ==================================================
    // THEME DIALOG
    // ==================================================

    if (showThemeDialog) {

        AlertDialog(

            onDismissRequest = {

                showThemeDialog =
                    false
            },

            title = {

                Text(
                    text =
                        "Choose Theme"
                )
            },

            text = {

                Column {

                    ThemeOption(
                        title =
                            "System default",

                        selected =
                            currentTheme == "SYSTEM",

                        onClick = {

                            onThemeChanged(
                                "SYSTEM"
                            )

                            showThemeDialog =
                                false
                        }
                    )


                    ThemeOption(
                        title =
                            "Light",

                        selected =
                            currentTheme == "LIGHT",

                        onClick = {

                            onThemeChanged(
                                "LIGHT"
                            )

                            showThemeDialog =
                                false
                        }
                    )


                    ThemeOption(
                        title =
                            "Dark",

                        selected =
                            currentTheme == "DARK",

                        onClick = {

                            onThemeChanged(
                                "DARK"
                            )

                            showThemeDialog =
                                false
                        }
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showThemeDialog =
                            false
                    }
                ) {

                    Text("Close")
                }
            }
        )
    }
}


// ==================================================
// THEME OPTION
// ==================================================

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(
            selected =
                selected,

            onClick =
                onClick
        )

        Spacer(
            modifier =
                Modifier.size(8.dp)
        )

        Text(
            text =
                title,

            style =
                MaterialTheme
                    .typography
                    .bodyLarge
        )
    }
}


// ==================================================
// PROFILE INFORMATION ROW
// ==================================================

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    showEdit: Boolean,
    onEdit: () -> Unit = {}
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    label,

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )

            Text(
                text =
                    value,

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )
        }


        if (showEdit) {

            IconButton(
                onClick =
                    onEdit
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Edit,

                    contentDescription =
                        "Edit $label"
                )
            }

        } else {

            Icon(
                imageVector =
                    Icons.Default.Lock,

                contentDescription =
                    "Not editable",

                modifier =
                    Modifier.size(18.dp),

                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// ==================================================
// PROFILE MENU ITEM
// ==================================================

@Composable
private fun ProfileMenuItem(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {

    TextButton(
        onClick =
            onClick,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            icon()

            Spacer(
                modifier =
                    Modifier.size(16.dp)
            )

            Text(
                text =
                    title,

                modifier =
                    Modifier.weight(1f),

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            Text(
                text =
                    "›",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )
        }
    }
}