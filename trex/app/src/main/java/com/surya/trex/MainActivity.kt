package com.surya.trex

import android.content.Intent
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel

import com.surya.trex.data.local.ThemeManager
import com.surya.trex.data.local.TokenManager
import com.surya.trex.data.model.Transaction
import com.surya.trex.data.repository.DashboardRefreshManager

import com.surya.trex.ui.auth.LoginScreen
import com.surya.trex.ui.category.CategoryScreen
import com.surya.trex.ui.category.CategoryViewModel
import com.surya.trex.ui.category.CategoryViewModelFactory
import com.surya.trex.ui.home.HomeScreen
import com.surya.trex.ui.pending.PendingTransactionsScreen
import com.surya.trex.ui.profile.ProfileScreen
import com.surya.trex.ui.profile.ProfileViewModel
import com.surya.trex.ui.settings.SettingsScreen
import com.surya.trex.ui.theme.TrexTheme
import com.surya.trex.ui.transaction.AddTransactionScreen
import com.surya.trex.ui.transaction.TransactionsScreen


class MainActivity : ComponentActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var themeManager: ThemeManager

    private var isLoggedIn =
        mutableStateOf(false)

    private var currentScreen =
        mutableStateOf(Screen.HOME)

    private var selectedTheme =
        mutableStateOf("SYSTEM")


    // =====================================================
    // Transaction being edited
    // =====================================================

    private var editingTransaction =
        mutableStateOf<Transaction?>(null)


    // =====================================================
    // Category refresh
    //
    // Whenever CategoryScreen is closed after CRUD,
    // this value increases and AddTransactionScreen
    // reloads categories.
    // =====================================================

    private var categoryRefreshKey =
        mutableIntStateOf(0)


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        // -----------------------------------------
        // Initialize managers
        // -----------------------------------------

        tokenManager =
            TokenManager(this)

        themeManager =
            ThemeManager(this)


        // -----------------------------------------
        // Load saved theme
        // -----------------------------------------

        selectedTheme.value =
            themeManager.getTheme()


        // -----------------------------------------
        // Check login state
        // -----------------------------------------

        isLoggedIn.value =
            tokenManager.getToken() != null


        // -----------------------------------------
        // Handle OAuth callback
        // -----------------------------------------

        handleOAuthCallback(intent)

        enableEdgeToEdge()


        // =================================================
        // Compose UI
        // =================================================

        setContent {

            TrexTheme(

                darkTheme =
                    when (selectedTheme.value) {

                        "DARK" ->
                            true

                        "LIGHT" ->
                            false

                        else ->
                            isSystemInDarkTheme()
                    }
            ) {

                // =================================================
                // LOGIN
                // =================================================

                if (!isLoggedIn.value) {

                    LoginScreen(

                        viewModel =
                            viewModel(),

                        onLoginSuccess = {

                            isLoggedIn.value =
                                true

                            currentScreen.value =
                                Screen.HOME
                        }
                    )

                } else {

                    // =================================================
                    // MAIN NAVIGATION
                    // =================================================

                    when (currentScreen.value) {

                        // =============================================
                        // HOME
                        // =============================================

                        Screen.HOME -> {

                            HomeScreen(

                                tokenManager =
                                    tokenManager,

                                onLogout = {

                                    tokenManager.clearToken()

                                    isLoggedIn.value =
                                        false

                                    currentScreen.value =
                                        Screen.HOME
                                },

                                onAddTransaction = {

                                    editingTransaction.value =
                                        null

                                    currentScreen.value =
                                        Screen.ADD_TRANSACTION
                                },

                                onViewTransactions = {

                                    currentScreen.value =
                                        Screen.TRANSACTIONS
                                },

                                onProfileClick = {

                                    currentScreen.value =
                                        Screen.PROFILE
                                },

                                onPendingTransactions = {

                                    currentScreen.value =
                                        Screen.PENDING_TRANSACTIONS
                                }
                            )
                        }


                        // =============================================
                        // ADD / EDIT TRANSACTION
                        // =============================================

                        Screen.ADD_TRANSACTION -> {

                            AddTransactionScreen(

                                tokenManager =
                                    tokenManager,

                                transaction =
                                    editingTransaction.value,

                                categoryRefreshKey =
                                    categoryRefreshKey.intValue,

                                onManageCategories = {

                                    currentScreen.value =
                                        Screen.CATEGORY
                                },

                                onBack = {

                                    if (
                                        editingTransaction.value != null
                                    ) {

                                        editingTransaction.value =
                                            null

                                        currentScreen.value =
                                            Screen.TRANSACTIONS

                                    } else {

                                        DashboardRefreshManager.refresh()

                                        currentScreen.value =
                                            Screen.HOME
                                    }
                                },

                                onEditSaved = {

                                    DashboardRefreshManager.refresh()

                                    editingTransaction.value =
                                        null

                                    currentScreen.value =
                                        Screen.TRANSACTIONS
                                }
                            )
                        }


                        // =============================================
                        // TRANSACTIONS
                        // =============================================

                        Screen.TRANSACTIONS -> {

                            TransactionsScreen(

                                tokenManager =
                                    tokenManager,

                                onBack = {

                                    DashboardRefreshManager.refresh()

                                    currentScreen.value =
                                        Screen.HOME
                                },

                                onEditTransaction = { transaction ->

                                    editingTransaction.value =
                                        transaction

                                    currentScreen.value =
                                        Screen.ADD_TRANSACTION
                                }
                            )
                        }


                        // =============================================
                        // PROFILE
                        // =============================================

                        Screen.PROFILE -> {

                            val profileViewModel:
                                    ProfileViewModel =
                                viewModel()

                            ProfileScreen(

                                viewModel =
                                    profileViewModel,

                                onBack = {

                                    currentScreen.value =
                                        Screen.HOME
                                },

                                onSettingsClick = {

                                    currentScreen.value =
                                        Screen.SETTINGS
                                },

                                currentTheme =
                                    selectedTheme.value,

                                onThemeChanged = { theme ->

                                    selectedTheme.value =
                                        theme

                                    themeManager.saveTheme(
                                        theme
                                    )
                                },

                                onLogout = {

                                    tokenManager.clearToken()

                                    isLoggedIn.value =
                                        false

                                    currentScreen.value =
                                        Screen.HOME
                                }
                            )
                        }


                        // =============================================
                        // SETTINGS
                        // =============================================

                        Screen.SETTINGS -> {

                            SettingsScreen(

                                onBack = {

                                    currentScreen.value =
                                        Screen.PROFILE
                                }
                            )
                        }


                        // =============================================
                        // CATEGORY MANAGEMENT
                        // =============================================

                        Screen.CATEGORY -> {

                            val categoryViewModel:
                                    CategoryViewModel =
                                viewModel(
                                    factory =
                                        CategoryViewModelFactory(
                                            tokenManager
                                        )
                                )

                            CategoryScreen(

                                viewModel =
                                    categoryViewModel,

                                onBack = {

                                    // ---------------------------------
                                    // Tell AddTransactionScreen that
                                    // categories changed.
                                    // ---------------------------------

                                    categoryRefreshKey.intValue++

                                    currentScreen.value =
                                        Screen.ADD_TRANSACTION
                                }
                            )
                        }


                        // =============================================
                        // PENDING TRANSACTIONS
                        // =============================================

                        Screen.PENDING_TRANSACTIONS -> {

                            PendingTransactionsScreen(

                                tokenManager =
                                    tokenManager,

                                onBack = {

                                    DashboardRefreshManager.refresh()

                                    currentScreen.value =
                                        Screen.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }


    // =====================================================
    // OAuth callback when Activity is created
    // =====================================================

    override fun onNewIntent(
        intent: Intent
    ) {

        super.onNewIntent(intent)

        setIntent(intent)

        handleOAuthCallback(intent)
    }


    // =====================================================
    // Handle Google OAuth callback
    // =====================================================

    private fun handleOAuthCallback(
        intent: Intent?
    ) {

        val uri =
            intent?.data
                ?: return


        if (
            uri.scheme == "trex" &&
            uri.host == "auth" &&
            uri.path == "/callback"
        ) {

            val token =
                uri.getQueryParameter("token")


            if (!token.isNullOrEmpty()) {

                tokenManager.saveToken(token)

                isLoggedIn.value =
                    true

                currentScreen.value =
                    Screen.HOME


                println(
                    "OAuth login Successful"
                )


                println(
                    "JWT received and saved"
                )
            }
        }
    }
}


// =========================================================
// Screen Navigation
// =========================================================

private enum class Screen {

    HOME,

    ADD_TRANSACTION,

    TRANSACTIONS,

    PROFILE,

    SETTINGS,

    CATEGORY,

    PENDING_TRANSACTIONS
}