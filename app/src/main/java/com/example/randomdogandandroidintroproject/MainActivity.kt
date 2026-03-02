package com.example.randomdogandandroidintroproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.randomdogandandroidintroproject.ui.theme.RandomDogAndAndroidIntroProjectTheme

// You can only use imports which is defined in gradle/libs.versions.toml and app/build.gradle.kts first.
// Then Gradle handles it, by fetching the libs, and setting the dependency, so the libs can be used in code (write imports first)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { // setContent is a composable function that sets the content of the activity. You can call other composable functions from here.
            RandomDogAndAndroidIntroProjectTheme {
                MyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    names: List<String> = listOf("ColumnUser1", "ColumnUser2", "shortname")
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(vertical = (8/2).dp)) {
            for (name in names) {
                Greeting(name = name)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) { // Composable function names are capitalized. They can't return anything.
    // "Always" include modifier as a parameter, for it to be reusable. Do not hardcode placement for component.
    // pass the modifier parameter to the root UI element of the component
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = (8/2).dp, horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) { // common items like this are Row, Column and Box.
                                                                        // New Modifier for child modifier. dp = density-independent pixels
            Text(text = "Hello,")
                // The internal modifier handles content, which is only related to the component.
                // Modifier is universal. Same modifier can be used for multiple elements (e.g. box, column, surface and so on). Common modifiers: padding, margin, size, fillMaxSize, background, border, clickable, shadow, etc.
                // Some modification require a specific parent to work. E.g. .align(Alignment.Center) with Box parent
                // The "Text" function (and other (composable) functions) has some of it own "modifications", that cannot be found in the "Modifier". E.g. fontSize, fontWeight, fontFamily, etc.
            Text(text = "my name is")
            Text(text = "$name!")
        }

    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 600)
@Composable
fun GreetingPreview() {
    RandomDogAndAndroidIntroProjectTheme {
        MyApp()
    }
}

