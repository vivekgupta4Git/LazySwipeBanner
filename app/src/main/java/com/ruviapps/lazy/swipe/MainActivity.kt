package com.ruviapps.lazy.swipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ruviapps.lazy.swipe.ui.theme.LazyStackDemoTheme
import kotlinx.coroutines.launch

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
    val state = rememberLazyStackState(myDataItems.size, orientation = Orientation.Vertical)
    LazySwipeBanner(
        modifier = modifier,
        state = state,
        itemOffset = 100.dp
    ) {
        items(myDataItems.size) { index ->
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .lazySwipeBannerAnimatedItem(
                        isCenterItem = index == state.currentIndex,
                        state = state,
                        enableRotation = false
                    ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = myDataItems[index], style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
    val scope = rememberCoroutineScope()
    Button(onClick = {
        scope.launch {
            state.animateTo(state.currentIndex + 5, tween(500, easing = LinearOutSlowInEasing))
        }
    }) {
        Text("Animate")
    }
}

@Preview
@Composable
private fun PreviewStack() {
    UseLazyStackLayout()
}