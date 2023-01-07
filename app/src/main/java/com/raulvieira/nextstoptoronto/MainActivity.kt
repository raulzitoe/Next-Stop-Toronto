package com.raulvieira.nextstoptoronto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.raulvieira.nextstoptoronto.navigation.MyAppNavHost
import com.raulvieira.nextstoptoronto.navigation.Screen
import com.raulvieira.nextstoptoronto.ui.theme.NextStopTorontoTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = this.packageName

        setContent {
            NextStopTorontoTheme {
                MyAppNavHost(startDestination = Screen.Home.route)
            }
        }
    }
}