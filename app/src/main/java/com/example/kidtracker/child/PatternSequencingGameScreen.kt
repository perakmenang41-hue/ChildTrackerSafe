package com.example.kidtracker.child

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.kidtracker.system.DataStoreManager
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun PatternSequencingGameScreen(
    dataStoreManager: DataStoreManager,
    onExit: () -> Unit
) {
    val colorsList = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow)
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var playerInput by remember { mutableStateOf(listOf<Int>()) }
    var isShowingSequence by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf(1) }
    var message by remember { mutableStateOf("") }
    var gameOver by remember { mutableStateOf(false) }
    var highLevel by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()

    // Load high score on start
    LaunchedEffect(Unit) {
        highLevel = dataStoreManager.getPatternGameHighLevel()
    }

    fun nextRound() {
        val next = Random.nextInt(colorsList.size)
        sequence = sequence + next
        playerInput = listOf()
        message = ""
        isShowingSequence = true
        gameOver = false

        scope.launch {
            for (index in sequence) {
                playerInput = listOf(index) // highlight current color
                kotlinx.coroutines.delay(600) // visible long enough
                playerInput = listOf()
                kotlinx.coroutines.delay(200)
            }
            isShowingSequence = false
        }
    }

    fun resetGame() {
        sequence = listOf()
        playerInput = listOf()
        level = 1
        message = ""
        gameOver = false
        nextRound()
    }

    // Check player's input
    LaunchedEffect(playerInput) {
        if (!isShowingSequence && playerInput.isNotEmpty() && !gameOver) {
            val currentIndex = playerInput.lastIndex
            if (playerInput[currentIndex] != sequence[currentIndex]) {
                message = "❌ Wrong! Game Over."
                gameOver = true
                // Update high score if needed
                scope.launch {
                    val currentHigh = dataStoreManager.getPatternGameHighLevel()
                    if (currentHigh == null || level - 1 > currentHigh) {
                        dataStoreManager.savePatternGameHighLevel(level - 1)
                        highLevel = level - 1
                    }
                }
            } else if (playerInput.size == sequence.size) {
                message = "✅ Correct!"
                level += 1
                nextRound()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pattern Sequencing Game", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Level: $level", fontSize = 20.sp)
        Text("Highest Level: ${highLevel ?: "-"}", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            colorsList.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .background(
                            if (playerInput.contains(index)) Color.White else color,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (playerInput.contains(index)) 4.dp else 0.dp,
                            color = Color.Yellow,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = !isShowingSequence && !gameOver) {
                            playerInput = playerInput + index
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            message,
            fontSize = 20.sp,
            color = if (message.contains("❌")) Color.Red else Color.Green
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { resetGame() }) { Text("Reload") }
            Button(onClick = onExit) { Text("Exit") }
        }
    }
}
