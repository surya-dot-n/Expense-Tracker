package com.surya.trex.ui.settings

import android.Manifest
import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.compose.ui.platform.LocalContext

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.surya.trex.data.model.InstalledApp
import com.surya.trex.data.model.TransactionSource
import com.surya.trex.data.model.UserSettings


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showAppDialog by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
        viewModel.loadSources()
        viewModel.loadInstalledApps()
    }

    LaunchedEffect(errorMessage, successMessage) {
        val message = errorMessage ?: successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading && settings == null) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                item {
                    SettingsSectionTitle(
                        "Notifications",
                        Icons.Default.Notifications
                    )
                }

                item {
                    NotificationCard(
                        settings = settings,
                        onNotificationsChanged = { enabled ->
                            viewModel.setNotificationsEnabled(enabled)

                            if (enabled) {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                                )
                            }
                        }
                    )
                }

                item {
                    SettingsSectionTitle(
                        "Transaction Detection",
                        Icons.Default.Tune
                    )
                }

                item {
                    DetectionCard(
                        settings = settings,
                        onSmsDetectionChanged = { enabled ->
                            viewModel.setSmsDetectionEnabled(enabled)

                            if (enabled) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.READ_SMS
                                    )
                                )
                            }
                        },
                        onModeChanged = viewModel::setDetectionMode
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsSectionTitle(
                            title = "Transaction Sources",
                            icon = Icons.Default.PhoneAndroid,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                viewModel.loadInstalledApps()
                                showAppDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add installed app"
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(
                                    AndroidSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable app notification access")
                    }
                }

                if (sources.isEmpty()) {
                    item {
                        Text(
                            "The default SMS source is created automatically. " +
                                    "Add an installed app only when you want TREX to read its notifications.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    items(
                        items = sources,
                        key = { it.id }
                    ) { source ->
                        SourceCard(
                            source = source,
                            onEnabledChanged = {
                                viewModel.setSourceEnabled(source, it)
                            },
                            onDelete = {
                                viewModel.deleteSource(source)
                            }
                        )
                    }
                }

                item { InfoCard() }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }

    if (showAppDialog) {
        InstalledAppDialog(
            apps = installedApps,
            existingSources = sources,
            onDismiss = { showAppDialog = false },
            onSelect = { app ->
                viewModel.addAppSource(app)
                showAppDialog = false
            }
        )
    }
}


@Composable
private fun SettingsSectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
private fun NotificationCard(
    settings: UserSettings?,
    onNotificationsChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Transaction notifications",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Turn this off if you do not want TREX to show transaction alerts. " +
                            "It does not remove the default SMS source.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Switch(
                checked = settings?.notifications_enabled ?: true,
                onCheckedChange = onNotificationsChanged
            )
        }
    }
}


@Composable
private fun DetectionCard(
    settings: UserSettings?,
    onSmsDetectionChanged: (Boolean) -> Unit,
    onModeChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "SMS transaction detection",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "TREX includes SMS as a default source. You can disable detection here.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Switch(
                    checked = settings?.sms_detection_enabled ?: true,
                    onCheckedChange = onSmsDetectionChanged
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Detection mode",
                fontWeight = FontWeight.SemiBold
            )

            DetectionModeOption(
                title = "Automatically add",
                description = "Detected transactions are added automatically.",
                selected = settings?.detection_mode?.uppercase() == "AUTO",
                onClick = { onModeChanged("AUTO") }
            )

            DetectionModeOption(
                title = "Ask for approval",
                description = "Create a pending transaction for review.",
                selected = settings?.detection_mode?.uppercase() == "APPROVAL",
                onClick = { onModeChanged("APPROVAL") }
            )

            DetectionModeOption(
                title = "Manual",
                description = "Do not create transactions from detected messages.",
                selected = settings?.detection_mode?.uppercase() == "MANUAL",
                onClick = { onModeChanged("MANUAL") }
            )
        }
    }
}


@Composable
private fun DetectionModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
private fun SourceCard(
    source: TransactionSource,
    onEnabledChanged: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isDefaultSms =
        source.source_type.equals("SMS", true) &&
                source.app_name == "SMS (Default)"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (source.source_type.equals("SMS", true))
                        Icons.Default.Sms
                    else
                        Icons.Default.PhoneAndroid,
                    contentDescription = null
                )

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        source.app_name,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (isDefaultSms)
                            "Default SMS source"
                        else
                            "App notifications",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!source.package_name.isNullOrBlank()) {
                        Text(
                            source.package_name,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Switch(
                    checked = source.enabled,
                    onCheckedChange = onEnabledChanged
                )
            }

            if (isDefaultSms) {
                Text(
                    "SMS sender IDs are automatic; TREX checks parseable transaction SMS.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete source",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
private fun InstalledAppDialog(
    apps: List<InstalledApp>,
    existingSources: List<TransactionSource>,
    onDismiss: () -> Unit,
    onSelect: (InstalledApp) -> Unit
) {
    var search by remember { mutableStateOf("") }

    val filtered = remember(apps, search) {
        apps.filter {
            search.isBlank() ||
                    it.appName.contains(search, true) ||
                    it.packageName.contains(search, true)
        }.filter { app ->
            existingSources.none {
                it.package_name == app.packageName
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select installed app") },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search apps") },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp)
                ) {
                    items(filtered) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(app) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    app.appName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    app.packageName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "How TREX detects transactions",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "SMS is included automatically. For apps, choose an installed app and " +
                        "grant Android notification access. TREX then parses transaction " +
                        "notifications from the selected package.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
