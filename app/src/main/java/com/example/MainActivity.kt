package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.CalculatorRepository
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.CalculatorViewModelFactory

class MainActivity : ComponentActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    private fun loadInterstitialAd() {
        if (mInterstitialAd != null || isAdLoading) return
        isAdLoading = true

        val isDebuggable = (applicationContext.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val adUnitId = if (isDebuggable) {
            "ca-app-pub-3940256099942544/1033173712" // Test ad unit ID
        } else {
            "ca-app-pub-3767503288694165/1544603914" // Production interstitial ID
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isAdLoading = false
                }
            }
        )
    }

    private fun trackCalculatorOpenAndShowAd() {
        // Track the count
        val prefs = getSharedPreferences("calculator_ad_prefs", android.content.Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val lastDate = prefs.getString("last_open_date", "")
        var count = prefs.getInt("calculator_open_count", 0)

        if (today == lastDate) {
            count++
        } else {
            count = 1
        }

        prefs.edit()
            .putString("last_open_date", today)
            .putInt("calculator_open_count", count)
            .apply()

        android.util.Log.d("AdsterraAdMob", "Calculator opened today: $count times")

        // Limit to showing the full screen ad at most once per day
        val lastAdShowDate = prefs.getString("last_ad_show_date", "")
        if (lastAdShowDate == today) {
            android.util.Log.d("AdsterraAdMob", "Full screen ad already shown today. Skipping.")
            return
        }

        if (count >= 4) {
            val ad = mInterstitialAd
            if (ad != null) {
                ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        // Mark that the full screen ad was shown today
                        prefs.edit().putString("last_ad_show_date", today).apply()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        mInterstitialAd = null
                        loadInterstitialAd() // Preload for subsequent opens/days
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        mInterstitialAd = null
                        loadInterstitialAd()
                    }
                }
                ad.show(this)
            } else {
                // Not loaded yet, load it now
                loadInterstitialAd()
            }
        } else {
            // Preload so it's ready by the 4th open
            loadInterstitialAd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK safely in a background thread to prevent app launch lag
        Thread {
            try {
                com.google.android.gms.ads.MobileAds.initialize(this) {
                    runOnUiThread {
                        loadInterstitialAd()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        // Initialize SQLite Room database, repository and ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CalculatorRepository(database.historyDao(), database.currencyDao())
        val factory = CalculatorViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[CalculatorViewModel::class.java]

        setContent {
            val darkThemeOverride by viewModel.isDarkTheme.collectAsState()
            val useDarkTheme = darkThemeOverride ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDarkTheme) {
                MainLayoutScreen(viewModel) {
                    trackCalculatorOpenAndShowAd()
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainLayoutScreen(viewModel: CalculatorViewModel, onCalculatorOpen: () -> Unit) {
    val activeTab by viewModel.currentTab.collectAsState()

    // Track initial launch
    LaunchedEffect(Unit) {
        onCalculatorOpen()
    }

    val navItems = listOf(
        NavigationItemMeta(AppTab.CALCULATOR, "Calculator", Icons.Default.Calculate, Icons.Outlined.Calculate),
        NavigationItemMeta(AppTab.EXCHANGE_RATE, "Rates", Icons.Default.CurrencyExchange, Icons.Outlined.CurrencyExchange),
        NavigationItemMeta(AppTab.UNIT_CONVERTER, "Converter", Icons.Default.CompareArrows, Icons.Outlined.CompareArrows),
        NavigationItemMeta(AppTab.TOOLS, "Tools", Icons.Default.Dashboard, Icons.Outlined.Dashboard),
        NavigationItemMeta(AppTab.SETTINGS, "Settings", Icons.Default.Settings, Icons.Outlined.Settings)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = activeTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (item.tab == AppTab.CALCULATOR && activeTab != AppTab.CALCULATOR) {
                                onCalculatorOpen()
                            }
                            viewModel.setTab(item.tab)
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(180))
            },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { tab ->
            when (tab) {
                AppTab.CALCULATOR -> ScientificCalculatorView(viewModel)
                AppTab.EXCHANGE_RATE -> ExchangeRateView(viewModel)
                AppTab.UNIT_CONVERTER -> UnitConverterView(viewModel)
                AppTab.TOOLS -> MoreToolsView(viewModel)
                AppTab.SETTINGS -> SettingsView(viewModel)
            }
        }
    }
}

data class NavigationItemMeta(
    val tab: AppTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
