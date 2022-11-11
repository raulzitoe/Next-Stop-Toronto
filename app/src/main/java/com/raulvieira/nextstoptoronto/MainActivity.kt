package com.raulvieira.nextstoptoronto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.raulvieira.nextstoptoronto.navigation.MyAppNavHost
import com.raulvieira.nextstoptoronto.navigation.Screen
import com.raulvieira.nextstoptoronto.ui.theme.NextStopTorontoTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = this.packageName

        setContent {
            NextStopTorontoTheme {
                MyAppNavHost(startDestination = Screen.Home.route)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        val permissionsToRequest = ArrayList<String>();
        var i = 0;
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i]);
            i++;
        }
        if (permissionsToRequest.size > 0) {
            this.requestPermissions(
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
        super.onRequestPermissionsResult(
            requestCode,
            permissionsToRequest.toTypedArray(),
            grantResults
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NextStopTorontoTheme {
        
    }
}