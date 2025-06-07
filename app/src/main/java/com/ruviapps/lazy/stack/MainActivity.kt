package com.ruviapps.lazy.stack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruviapps.lazy.stack.ui.theme.LazyStackDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LazyStackDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UseLazyStackLayout(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UseLazyStackLayout(modifier: Modifier = Modifier) {
    val myDataItems = List(3) { index -> "Item ${index + 1}" }
    val state = rememberLazyStackState(myDataItems.size)
    LazyStackLayout(
        modifier = modifier,
        state = state
    ) {
        items(myDataItems) { value ->
            Card(
                modifier = Modifier
                    .size(200.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = value, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewStack() {
    UseLazyStackLayout()
}