package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.SalesCoachRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.SalesCoachApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database and repository inside application lifecycle
        val database = AppDatabase.getInstance(applicationContext)
        val repository = SalesCoachRepository(database.salesCoachDao())

        // Feed the factory to construct ViewModel with correct dependencies
        val viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository)
        )[MainViewModel::class.java]

        setContent {
            MyApplicationTheme {
                SalesCoachApp(viewModel = viewModel)
            }
        }
    }
}
