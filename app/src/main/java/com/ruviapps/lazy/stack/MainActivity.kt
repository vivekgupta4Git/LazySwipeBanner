package com.ruviapps.lazy.stack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    Column(Modifier.padding(innerPadding).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        UseLazyStackLayout()
                    }
                }
            }
        }
    }
}

@Composable
fun UseLazyStackLayout(modifier: Modifier = Modifier) {
    val myDataItems = List(100) { index -> "Item $index" }
    val state = rememberLazyStackState(myDataItems.size,
        animationSpec = tween(100)
    )
    LazyStackLayout(
        modifier = modifier,
        state = state,
        offsetAlpha = 1f,
        offSetItemScale = 0.8f,
        itemOffset = 100.dp
    ) {
        items(myDataItems.size) { index ->
            Card(
                modifier = Modifier
                    .size(220.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = myDataItems[index], style = MaterialTheme.typography.headlineMedium)
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