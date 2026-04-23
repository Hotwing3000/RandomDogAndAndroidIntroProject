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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.randomdogandandroidintroproject.ui.theme.RandomDogAndAndroidIntroProjectTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for the Dog Discovery application.
 * This activity sets up the Compose UI and uses Hilt for dependency injection.
 */
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

/**
 * Top-level Composable that manages the high-level state of the app,
 * switching between the Onboarding screen and the main Dog Discovery screen.
 *
 * @param viewModel The shared [MainViewModel] instance.
 */
@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    Surface(modifier, color = MaterialTheme.colorScheme.background) {
        if (viewModel.shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = { viewModel.onContinueClicked() })
        } else {
            DogDiscoveryScreen(viewModel = viewModel)
        }
    }
}

/**
 * The primary screen of the application where users can discover dogs,
 * filter for liked dogs, view statistics, and access settings.
 */
@Composable
fun DogDiscoveryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.toggleShowOnlyLiked() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.showOnlyLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (viewModel.showOnlyLiked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(if (viewModel.showOnlyLiked) "Show All Dogs" else "Show Liked Dogs")
            }
            Button(
                onClick = { viewModel.toggleShowLikedStats() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.showLikedStats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (viewModel.showLikedStats) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(if (viewModel.showLikedStats) "Show All Dogs" else "Show Your Dog Stats")
            }
            IconButton(
                onClick = { showSettingsDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showSettingsDialog) {
            SettingsDialog(
                onDismiss = { showSettingsDialog = false },
                isRomanMode = viewModel.isRomanMode,
                onToggleNamesMode = { viewModel.toggleNamesMode() }
            )
        }

        if (viewModel.showLikedStats) {
            LikedStatsScreen(likedDogs = viewModel.likedDogItems)
        } else {
            if (viewModel.showOnlyLiked && viewModel.likedDogItems.isEmpty()) {
                EmptyLikedDogsMessage()
            } else {
                Greetings(
                    dogItems = if (viewModel.showOnlyLiked) viewModel.likedDogItems else viewModel.dogItems,
                    onToggleExpand = { item -> 
                        viewModel.toggleExpanded(item)
                    },
                    onLikeClicked = { item ->
                        viewModel.onLikeClicked(item)
                    }
                )
            }
        }
    }
}

/**
 * Centered message shown when the user filters for liked dogs but has not liked any yet.
 */
@Composable
fun EmptyLikedDogsMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You have not liked any dogs yet.\n\nThey must feel abandoned :(\n\nGo back, try to call on a dog and like it using the heart icon <3",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A dialog allowing users to toggle application preferences, such as the numbering mode.
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    isRomanMode: Boolean,
    onToggleNamesMode: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App Preferences")
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onToggleNamesMode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(if (isRomanMode) "Show Numeric Numeration" else "Show Roman Numeration")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * A screen that displays statistics about the dogs the user has liked,
 * including total count and a breakdown by breed and group.
 */
@Composable
fun LikedStatsScreen(likedDogs: List<DogItem>) {
    val breedCounts = likedDogs.mapNotNull { it.breed }.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
    val groupCounts = likedDogs.mapNotNull { it.group }.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dog Stats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Liked Dogs",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${likedDogs.size}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column for Liked Breeds
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Breeds",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    if (breedCounts.isEmpty()) {
                        Text(
                            text = "None yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        breedCounts.forEach { (breed, count) ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = breed,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Column for Liked Groups
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Groups",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    if (groupCounts.isEmpty()) {
                        Text(
                            text = "None yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        groupCounts.forEach { (group, count) ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = group,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen displayed when the app is first launched, providing an introduction to the user.
 */
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

/**
 * A scrollable list of dog items. Uses [LazyColumn] for efficient rendering.
 */
@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    dogItems: List<DogItem>,
    onToggleExpand: (DogItem) -> Unit,
    onLikeClicked: (DogItem) -> Unit
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = dogItems, key = { it.index }) { item ->
            Greeting(dogItem = item, onToggleExpand = onToggleExpand, onLikeClicked = onLikeClicked)
        }
    }
}

/**
 * An individual dog card that can be in a collapsed or expanded state.
 * It animates its size and colors based on whether it has been opened or expanded.
 */
@Composable
fun Greeting(
    dogItem: DogItem, 
    onToggleExpand: (DogItem) -> Unit, 
    onLikeClicked: (DogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            dogItem.isExpanded -> MaterialTheme.colorScheme.primaryContainer
            dogItem.hasBeenOpened -> MaterialTheme.colorScheme.secondaryContainer
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
            dogItem.isExpanded -> MaterialTheme.colorScheme.onPrimaryContainer
            dogItem.hasBeenOpened -> MaterialTheme.colorScheme.onSecondaryContainer
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
            defaultElevation = if (dogItem.isExpanded) 8.dp else 2.dp
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        CardContent(
            dogItem = dogItem,
            onExpandClicked = { onToggleExpand(dogItem) },
            onLikeClicked = { onLikeClicked(dogItem) }
        )
    }
}

/**
 * The content within a [Greeting] card. 
 * - Collapsed: Shows a simple "Call" prompt or the dog's name if already visited.
 * - Expanded: Shows the dog's name, a random image fetched from the API, and its breed.
 */
@Composable
private fun CardContent(
    dogItem: DogItem, 
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
            if (dogItem.isExpanded) {
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
                        tint = if (dogItem.hasBeenOpened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    
                    val collapsedText = if (!dogItem.hasBeenOpened) {
                        "Would you like to meet dog no. ${dogItem.id}?"
                    } else {
                        dogItem.name?.let { "$it (Dog no. ${dogItem.id})" } ?: "No dog came?"
                    }
                    Text(
                        text = collapsedText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (dogItem.hasBeenOpened) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (dogItem.hasBeenOpened) {
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
            if (dogItem.hasBeenOpened || dogItem.isExpanded) {
                IconButton(onClick = onLikeClicked) {
                    Icon(
                        imageVector = if (dogItem.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like dog",
                        tint = if (dogItem.isLiked) Color.Red else MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            if (dogItem.isExpanded) {
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
                        containerColor = if (dogItem.hasBeenOpened) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (dogItem.hasBeenOpened) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = if (dogItem.hasBeenOpened) "Visit" else "Call",
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
        Greetings(dogItems = listOf(DogItem(0, "1")), onToggleExpand = {}, onLikeClicked = {})
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
                    breedDisplay = "Tibetan Terrier",
                    isExpanded = true,
                    hasBeenOpened = true
                ),
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
