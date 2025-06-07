package com.ruviapps.lazy.stack

import androidx.compose.runtime.Composable

class LazyStackItemScopeImpl() : LazyStackItemScope {
    private val _items = mutableListOf<@Composable () -> Unit>()
    val items: List<@Composable () -> Unit> get() = _items

    override fun items(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: LazyStackLayoutComposable
    ) {
        repeat(count) { index ->
            _items.add { this@LazyStackItemScopeImpl.itemContent(index) }
        }
    }
}