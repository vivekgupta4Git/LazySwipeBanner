package com.ruviapps.lazy.stack

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@OptIn(ExperimentalFoundationApi::class)
class LazyStackItemProvider(
    private val composables: List<@Composable () -> Unit>
) : LazyLayoutItemProvider {
    override val itemCount: Int
        get() = composables.size

    @Composable
    override fun Item(index: Int, key: Any) {
        composables[index]()
    }

    override fun getKey(index: Int): Any = index
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberLazyStackItemProvider(
    key1: Any? = null,
    content: LazyStackItemScope.() -> Unit
): LazyLayoutItemProvider {
    val scope = LazyStackItemScopeImpl()
    scope.content()
    return remember(key1) {
        LazyStackItemProvider(scope.items)
    }

}