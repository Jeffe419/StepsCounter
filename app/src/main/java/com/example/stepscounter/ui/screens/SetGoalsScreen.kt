package com.example.stepscounter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.ranges.contains

@Composable
fun SetGoalsScreen(navController: NavController) {
    var age by remember { mutableStateOf("") }
    var recommendedSteps by remember { mutableStateOf(0) } // Start with zero until a valid age is entered

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = age,
            onValueChange = { input ->
                age = input
                input.toIntOrNull()?.let {
                    if (it in 5..100) { // Check if age is within a valid range
                        recommendedSteps = calculateRecommendedSteps(it)
                    } else {
                        recommendedSteps = 0 // Reset to zero if not a valid age
                    }
                } ?: run { recommendedSteps = 0 } // Reset to zero if input is not a number
            },
            label = { Text("Enter Your Age") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Recommended Steps: $recommendedSteps", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { if (recommendedSteps > 500) recommendedSteps -= 500 },
                enabled = recommendedSteps > 500
            ) {
                Text("-")
            }
            Button(
                onClick = { recommendedSteps += 500 },
                enabled = age.toIntOrNull()?.let { it in 5..100 } ?: false
            ) {
                Text("+")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (age.toIntOrNull()?.let { it in 5..100 } == true) { // Ensure age is valid before navigating
                    navController.navigate("progress/$recommendedSteps")
                }
            },
            enabled = age.toIntOrNull()?.let { it in 5..100 } ?: false // Enable submit button only if age is valid
        ) {
            Text("Submit")
        }
    }
}


fun calculateRecommendedSteps(age: Int?): Int {
    return when (age) {
        in 5..12 -> 10000 // For children
        in 13..17 -> 12000 // Teenagers
        in 18..64 -> 10000 // Adults
        in 65..100 -> 7000 // Older adults
        else -> 4000
    }
}
