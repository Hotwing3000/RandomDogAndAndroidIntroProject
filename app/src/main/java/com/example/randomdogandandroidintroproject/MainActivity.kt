package com.example.randomdogandandroidintroproject

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
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
    Surface(modifier, color = MaterialTheme.colorScheme.background) {
        if (viewModel.shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = { viewModel.onContinueClicked() })
        } else {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.toggleNamesMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(if (viewModel.isRomanMode) "Show Numeric Numeration" else "Show Roman Numeration")
                    }
                }
                Greetings(
                    dogItems = viewModel.dogItems,
                    onExpand = { item -> 
                        viewModel.loadDetailsForItem(item)
                    },
                    onLikeClicked = { item ->
                        viewModel.onLikeClicked(item)
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
        Text(
            "Welcome to the Dog Discovery!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = onContinueClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Start Exploring")
        }
    }
}

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    dogItems: List<DogItem>,
    onExpand: (DogItem) -> Unit,
    onLikeClicked: (DogItem) -> Unit
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = dogItems, key = { it.index }) { item ->
            Greeting(dogItem = item, onExpand = onExpand, onLikeClicked = onLikeClicked)
        }
    }
}

@Composable
fun Greeting(
    dogItem: DogItem, 
    onExpand: (DogItem) -> Unit, 
    onLikeClicked: (DogItem) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by rememberSaveable { mutableStateOf(false) }
    var hasBeenOpened by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            hasBeenOpened = true
            onExpand(dogItem)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            expanded -> MaterialTheme.colorScheme.primaryContainer
            hasBeenOpened -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CardBackgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            expanded -> MaterialTheme.colorScheme.onPrimaryContainer
            hasBeenOpened -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 300),
        label = "CardContentColor"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        CardContent(
            dogItem = dogItem,
            expanded = expanded,
            hasBeenOpened = hasBeenOpened,
            onExpandClicked = { expanded = !expanded },
            onLikeClicked = { onLikeClicked(dogItem) }
        )
    }
}

@Composable
private fun CardContent(
    dogItem: DogItem, 
    expanded: Boolean, 
    hasBeenOpened: Boolean,
    onExpandClicked: () -> Unit,
    onLikeClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            if (expanded) {
                // Header with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = dogItem.name?.let { "Say hello to $it" } ?: "Calling the dog...",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Styled Image Container
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    if (dogItem.imageUrl != null) {
                        AsyncImage(
                            model = dogItem.imageUrl,
                            contentDescription = "Random Dog Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest),
                            error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
                            onError = { state ->
                                Log.e("AsyncImage", "Error loading image: ${state.result.throwable}")
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Fetching photo...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Breed Badge
                val breedText = if (dogItem.breedDisplay != null) {
                    dogItem.name?.let { "$it is a ${dogItem.breedDisplay}" } ?: "This dog is a ${dogItem.breedDisplay}"
                } else {
                    "Identifying dog breed..."
                }
                
                Surface(
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = breedText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            } else {
                // Collapsed State
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (hasBeenOpened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    
                    val collapsedText = if (!hasBeenOpened) {
                        "Would you like to meet dog no. ${dogItem.id}?"
                    } else {
                        dogItem.name?.let { "$it (Dog no. ${dogItem.id})" } ?: "No dog came?"
                    }
                    Text(
                        text = collapsedText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (hasBeenOpened) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (hasBeenOpened) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }
            }
        }
        
        // Heart Icon and Expand/Call Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onLikeClicked) {
                Icon(
                    imageVector = if (dogItem.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like dog",
                    tint = if (dogItem.isLiked) Color.Red else MaterialTheme.colorScheme.outline
                )
            }
            
            if (expanded) {
                IconButton(
                    onClick = onExpandClicked
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExpandLess,
                        contentDescription = stringResource(R.string.show_less),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Button(
                    onClick = onExpandClicked,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasBeenOpened) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (hasBeenOpened) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (hasBeenOpened) "Visit" else "Call",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
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
        Greetings(dogItems = listOf(DogItem(0, "1")), onExpand = {}, onLikeClicked = {})
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
fun GreetingExpandedPreview() {
    RandomDogAndAndroidIntroProjectTheme {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            CardContent(
                dogItem = DogItem(
                    index = 0,
                    id = "I",
                    name = "Doggy",
                    imageUrl = "https://images.dog.ceo/breeds/terrier-tibetan/n02097474_494.jpg",
                    breedDisplay = "Tibetan Terrier"
                ),
                expanded = true,
                hasBeenOpened = true,
                onExpandClicked = {},
                onLikeClicked = {}
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
