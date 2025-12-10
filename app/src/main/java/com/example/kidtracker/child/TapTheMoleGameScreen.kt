package com.example.kidtracker.child

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.kidtracker.system.DataStoreManager
import kotlin.random.Random

@Composable
fun TapTheMoleGameScreen(
    dataStoreManager: DataStoreManager,
    onExit: () -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(30) }
    var moleIndex by remember { mutableStateOf(Random.nextInt(9)) }
    var isGameOver by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) }   // ⬅ NEW COUNTDOWN STATE
    var gameStarted by remember { mutableStateOf(false) }

    var highScore by remember { mutableStateOf(0) }

    // Load high score
    LaunchedEffect(Unit) {
        highScore = dataStoreManager.getMoleGameHighScore() ?: 0
    }

    // ⬅ COUNTDOWN TIMER BEFORE GAME START
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        gameStarted = true // Start game now
    }

    // MAIN GAME TIMER
    LaunchedEffect(gameStarted) {
        if (!gameStarted) return@LaunchedEffect

        while (timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
        }
        isGameOver = true

        dataStoreManager.saveMoleGameHighScore(score)
    }

    // MOLE MOVEMENT
    LaunchedEffect(score, gameStarted) {
        if (!gameStarted) return@LaunchedEffect

        val speed = (600 - (score * 10)).coerceAtLeast(250)

        while (!isGameOver) {
            delay(speed.toLong())
            moleIndex = Random.nextInt(9)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Whack-A-Mole", fontSize = 26.sp)
        Spacer(Modifier.height(12.dp))

        if (!gameStarted) {
            // SHOW COUNTDOWN UI
            Text("Game starts in...", fontSize = 20.sp)
            Text(
                text = if (countdown == 0) "GO!" else countdown.toString(),
                fontSize = 40.sp,
                color = Color.Red
            )
            Spacer(Modifier.height(20.dp))
        } else {
            Text("Time Left: $timeLeft", fontSize = 20.sp)
            Text("Score: $score", fontSize = 20.sp)
            Text("High Score: $highScore", fontSize = 18.sp, color = Color.Green)
        }

        Spacer(Modifier.height(24.dp))

        // 3x3 GRID
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col

                        MoleCell(
                            isMole = index == moleIndex,
                            enabled = gameStarted && !isGameOver,
                            onHit = {
                                score++
                                moleIndex = Random.nextInt(9)
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (isGameOver) {
            Text("🎉 Time's Up! Final Score: $score", fontSize = 20.sp, color = Color.Red)
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = {
                // Restart everything
                score = 0
                timeLeft = 30
                moleIndex = Random.nextInt(9)
                countdown = 3
                gameStarted = false
                isGameOver = false
            }) {
                Text("Restart")
            }

            Button(onClick = onExit) {
                Text("Exit")
            }
        }
    }
}


@Composable
fun MoleCell(isMole: Boolean, enabled: Boolean, onHit: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                if (isMole) Color(0xFFFFD700) else Color(0xFFAAAAAA),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled && isMole) { onHit() },
        contentAlignment = Alignment.Center
    ) {
        if (isMole) {
            Text("🐹", fontSize = 36.sp)
        }
    }
}
