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
import com.example.kidtracker.system.DataStoreManager
import com.example.kidtracker.system.InboxMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InboxScreen(dataStoreManager: DataStoreManager) {
    val scope = rememberCoroutineScope()

    var messages by remember { mutableStateOf<List<InboxMessage>>(emptyList()) }

    // Load messages on open
    LaunchedEffect(Unit) {
        messages = dataStoreManager.getInboxMessages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Inbox", fontSize = 26.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No messages yet", color = Color.Gray)
            }
        } else {
            messages.sortedByDescending { it.timestamp }.forEach { msg ->
                InboxMessageCard(
                    message = msg,
                    onClick = {
                        // Mark as read
                        scope.launch {
                            val updated = messages.map {
                                if (it.id == msg.id) it.copy(isRead = true) else it
                            }
                            messages = updated
                            dataStoreManager.saveInboxMessages(updated)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun InboxMessageCard(message: InboxMessage, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault())
    val time = sdf.format(Date(message.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (message.isRead) Color(0xFFEFEFEF)
                else Color(0xFFFFF6C8),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Text(message.title, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(message.message, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
