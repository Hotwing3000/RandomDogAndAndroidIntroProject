package com.example.randomdogandandroidintroproject.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Defines the shape scheme for the application's Material 3 theme.
 * 
 * These shapes determine the corner rounding for various UI components across the app:
 * - **Small**: Typically used for small components like Tooltips and Text Fields.
 * - **Medium**: Often used for Cards and small Dialogs.
 * - **Large**: Used for larger surfaces like Menus or Navigation Drawers.
 * - **Extra Large**: Used for prominent surfaces like Bottom Sheets or large containers.
 */
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
