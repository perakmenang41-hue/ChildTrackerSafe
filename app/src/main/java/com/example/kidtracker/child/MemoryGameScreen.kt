package com.example.kidtracker.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.random.Random

@Composable
fun MemoryGameScreen(onExit: () -> Unit) {
    val cardPairs = listOf("🐶","🐱","🐭","🦊","🐻","🐼") // 6 pairs => 12 cards
    val cards = remember { (cardPairs + cardPairs).shuffled() }

    var flippedIndices by remember { mutableStateOf(listOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(listOf<Int>()) }
    var moves by remember { mutableStateOf(0) }

    // This LaunchedEffect reacts whenever two cards are flipped
    LaunchedEffect(flippedIndices) {
        if (flippedIndices.size == 2) {
            moves += 1
            val first = flippedIndices[0]
            val second = flippedIndices[1]
            if (cards[first] != cards[second]) {
                kotlinx.coroutines.delay(1000)
                flippedIndices = listOf()
            } else {
                matchedIndices = matchedIndices + listOf(first, second)
                flippedIndices = listOf()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Memory Game", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Moves: $moves", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        val columns = 3
        for (row in 0 until cards.size / columns) {
            Row {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    MemoryCard(
                        content = cards[index],
                        isFlipped = flippedIndices.contains(index) || matchedIndices.contains(index),
                        onClick = {
                            if (flippedIndices.size < 2 && !flippedIndices.contains(index) && !matchedIndices.contains(index)) {
                                flippedIndices = flippedIndices + index
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (matchedIndices.size == cards.size) {
            Text("🎉 You Win! 🎉", fontSize = 20.sp, color = Color.Green)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onExit() }) { Text("Back") }
        }
    }
}


@Composable
fun MemoryCard(content: String, isFlipped: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(80.dp)
            .background(if (isFlipped) Color.White else Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isFlipped) {
            Text(content, fontSize = 32.sp)
        }
    }
}
