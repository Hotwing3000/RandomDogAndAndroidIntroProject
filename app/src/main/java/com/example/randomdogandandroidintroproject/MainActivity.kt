package com.example.randomdogandandroidintroproject

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.randomdogandandroidintroproject.ui.theme.RandomDogAndAndroidIntroProjectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RandomDogAndAndroidIntroProjectTheme {
                MyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    Surface(modifier) {
        if (viewModel.shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = { viewModel.onContinueClicked() })
        } else {
            Column {
                Row(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.toggleNamesMode() }
                    ) {
                        Text(if (viewModel.isRomanMode) "Show Numeric" else "Show Roman")
                    }
                }
                Greetings(
                    dogItems = viewModel.dogItems,
                    onExpand = { item -> 
                        viewModel.fetchImageForItem(item)
                        viewModel.fetchNameForItem(item)
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to the Basics Codelab!")
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = onContinueClicked
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    dogItems: List<DogItem>,
    onExpand: (DogItem) -> Unit
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = dogItems) { item ->
            Greeting(dogItem = item, onExpand = onExpand)
        }
    }
}

@Composable
fun Greeting(dogItem: DogItem, onExpand: (DogItem) -> Unit, modifier: Modifier = Modifier) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    // Use LaunchedEffect to automatically fetch the image if the card is expanded
    // and the data is currently missing (e.g. after a mode toggle).
    LaunchedEffect(expanded, dogItem.imageUrl, dogItem.name) {
        if (expanded) {
            onExpand(dogItem)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = if (expanded) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(durationMillis = 300, easing = LinearEasing)
        }
    )
    val contentColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = if (expanded) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        } else {
            tween(durationMillis = 300, easing = LinearEasing)
        }
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        CardContent(
            dogItem = dogItem,
            expanded = expanded,
            onExpandClicked = { expanded = !expanded }
        )
    }
}

@Composable
private fun CardContent(dogItem: DogItem, expanded: Boolean, onExpandClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(24.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(text = "Hello, ")
            Text(text = dogItem.name ?: "Fetching name...", style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold
            ))

            if (expanded) {
                if (dogItem.imageUrl != null) {
                    AsyncImage(
                        model = dogItem.imageUrl,
                        contentDescription = "Random Dog Image",
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = ColorPainter(Color.LightGray),
                        error = ColorPainter(Color.Red),
                        onError = { state ->
                            Log.e("AsyncImage", "Error loading image: ${state.result.throwable}")
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray, shape = MaterialTheme.shapes.medium)
                    )
                }
                
                val breedText = if (dogItem.breedDisplay != null) {
                    "This dog is a ${dogItem.breedDisplay}"
                } else {
                    "Identifying dog breed..."
                }
                
                Text(
                    text = breedText,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        IconButton(onClick = onExpandClicked) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.show_less)
                } else {
                    stringResource(R.string.show_more)
                }
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 320)
@Preview(
    showBackground = true,
    widthDp = 320,
    uiMode = UI_MODE_NIGHT_YES,
    name = "GreetingPreviewDark"
)
@Composable
fun GreetingPreview() {
    RandomDogAndAndroidIntroProjectTheme {
        Greetings(dogItems = listOf(DogItem(0, "1"), DogItem(1, "2")), onExpand = {})
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun GreetingExpandedPreview() {
    RandomDogAndAndroidIntroProjectTheme {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            CardContent(
                dogItem = DogItem(
                    index = 0,
                    name = "I",
                    imageUrl = "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_494.jpg",
                    breedDisplay = "Tibetan Terrier"
                ),
                expanded = true,
                onExpandClicked = {}
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    RandomDogAndAndroidIntroProjectTheme {
        OnboardingScreen(onContinueClicked = {})
    }
}
