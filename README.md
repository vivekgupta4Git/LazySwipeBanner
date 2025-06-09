# LazySwipeBanner

A custom Jetpack Compose layout that mimics a swipeable looping card/banner stack. Built on top of `LazyLayout`, it supports horizontal or vertical swipes, smooth animations, and efficient item management.
<div align="center">
  	<img src="https://github.com/user-attachments/assets/1b248fb6-a5a7-42ce-934b-06b389c1d655" width=200 height=400 alt="LazySwipeBanner"/>
	<img src="https://github.com/user-attachments/assets/ed39f638-daf4-4e22-b885-2e5585d3d4dd" width=200 height=400 alt="Rotation"/>
	
</div>

## Customize Animation
<div align="center">
    	<img src="https://github.com/user-attachments/assets/aec5595d-a476-4338-b75b-885b59262c0d" width=200 height=400 alt="Default"/>
        <img src="https://github.com/user-attachments/assets/f0cefe42-475b-4870-a57c-fb649416168e" width=200 height=400 alt="horizontal flip"/>
	<img src="https://github.com/user-attachments/assets/7c52e8a5-fadc-4832-8638-d432e5b02062" width=200 height=400 alt="horizontal flip and swipe"/>
        <img src="https://github.com/user-attachments/assets/0ff37ae4-6e81-4fea-9269-27a2914930e2" width=200 height=400 alt="vertical flip and swipe"/>
	<img src="https://github.com/user-attachments/assets/7782f966-6362-42f3-83fe-393171e4b183" width=200 height=400 alt="vertical flip"/>
 	<img src="https://github.com/user-attachments/assets/2dd9ca03-e0f4-480c-9ff4-db3908ac0ead" width=200 height=400 alt="vertical without rotation"/>
	
</div>


---

## ✨ Features

- 🔁 Infinite looping through items
- 🎞️ Swipeable card stack layout (horizontal or vertical)
- 🎯 Centered item focus with peeked side items
- 🎨 Customizable animations:
  - Scale
  - Alpha
  - Rotation
  - Camera distance
  - Transform origin
  - Translation
- ⚙️ Lazy loading support with dynamic item content
- 🔒 State saving via `Saver`

---

## 📦 Installation

```gradle
dependencies {
	implementation 'com.ruviapps:lazy.swipe:1.0.4'
}
```
---

## 🛠️ Usage

```kotlin
val myDataItems = List(100) { index -> "Item $index" }
    val state = rememberLazyStackState(myDataItems.size, orientation = Orientation.Horizontal)
    LazySwipeBanner(
        modifier = modifier.fillMaxSize(),
        state = state,
        itemOffset = 100.dp
    ) {
        items(myDataItems) { value ->
            Card(
                modifier = Modifier
                    .size(200.dp)
                    .lazySwipeBannerAnimatedItem(
                        isCenterItem = index == state.currentIndex,
                        state = state,
                        enableRotation = true,
                    ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
