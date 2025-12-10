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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidtracker.system.DataStoreManager
import com.example.kidtracker.system.InboxMessage
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MemoryGameScreen(
    dataStoreManager: DataStoreManager,
    onExit: () -> Unit
) {
    val cardPairs = listOf("🐶","🐱","🐭","🦊","🐻","🐼")
    val cards = remember { (cardPairs + cardPairs).shuffled() }

    var flippedIndices by remember { mutableStateOf(listOf<Int>()) }
    var matchedIndices by remember { mutableStateOf(listOf<Int>()) }
    var moves by remember { mutableStateOf(0) }
    var gameWon by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var highScore by remember { mutableStateOf<Int?>(null) }

    // Load high score
    LaunchedEffect(Unit) {
        highScore = dataStoreManager.getMemoryGameHighScore()
    }

    // Check flipped cards
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

        // Check win
        if (matchedIndices.size == cards.size && !gameWon) {
            gameWon = true

            scope.launch {
                val previousHigh = dataStoreManager.getMemoryGameHighScore()

                // Save best score
                dataStoreManager.saveMemoryGameHighScore(moves)
                highScore = dataStoreManager.getMemoryGameHighScore()

                // Send inbox message ONLY if new record
                if (previousHigh == null || moves < previousHigh) {
                    // Ensure oldMessages is not null
                    val oldMessages = dataStoreManager.getInboxMessages() ?: emptyList()
                    val newMsg = InboxMessage(
                        id = System.currentTimeMillis(),
                        title = "🎉 New High Score!",
                        message = "You finished the Memory Game in $moves moves!",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    dataStoreManager.saveInboxMessages(oldMessages + newMsg)
                }
            }
        }

    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Memory Game", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Moves: $moves", fontSize = 18.sp)
        Text("Best Moves: ${highScore ?: "-"}", fontSize = 16.sp, color = Color.Gray)

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
                            if (!gameWon &&
                                flippedIndices.size < 2 &&
                                !flippedIndices.contains(index) &&
                                !matchedIndices.contains(index)
                            ) {
                                flippedIndices = flippedIndices + index
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (gameWon) {
            Text("🎉 You Win! 🎉", fontSize = 20.sp, color = Color.Green)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                moves = 0
                matchedIndices = listOf()
                flippedIndices = listOf()
                gameWon = false
            }) { Text("Restart") }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { onExit() }) { Text("Exit") }
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
