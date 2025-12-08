package com.example.kidtracker.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kidtracker.R
import com.example.kidtracker.system.DataStoreManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    childUid: String,
    dataStoreManager: DataStoreManager,
    navController: NavHostController
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var childName by remember { mutableStateOf("") }
    var childEmail by remember { mutableStateOf("") }

    // --- Collect DataStore suspend functions safely ---
    LaunchedEffect(Unit) {
        childName = dataStoreManager.getChildName() // suspend call
        childEmail = dataStoreManager.getChildEmail() // suspend call
    }

    // Show registration success first
    var showSuccessPage by remember { mutableStateOf(false) }

    if (showSuccessPage) {
        RegistrationSuccessPage(childName) {
            showSuccessPage = false
        }
    } else {
        MainInterface(childUid, childName, childEmail, clipboardManager, navController)
    }
}

@Composable
fun RegistrationSuccessPage(childName: String, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, $childName!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You have successfully registered.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun MainInterface(
    childUid: String,
    childName: String,
    childEmail: String,
    clipboardManager: ClipboardManager,
    navController: NavHostController
) {
    var selectedTab by remember { mutableStateOf("Home") }

    Scaffold(
        bottomBar = {
            BottomNav(selectedTab) { selectedTab = it }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            when (selectedTab) {
                "Home" -> HomeTab(childUid, childName, clipboardManager, navController)
                "Inbox" -> SimpleTab("Inbox messages will appear here")
                "Profile" -> ProfileScreen(childName, childUid, childEmail)
            }
        }
    }
}

@Composable
fun HomeTab(
    childUid: String,
    childName: String,
    clipboardManager: ClipboardManager,
    navController: NavHostController
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Hello, $childName!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text("Your Unique ID: $childUid", style = MaterialTheme.typography.bodyLarge)

        Text(
            text = "📋 Copy UID",
            modifier = Modifier.clickable { clipboardManager.setText(AnnotatedString(childUid)) },
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("memory_game") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play Memory Game", fontSize = 18.sp)
        }
    }
}

@Composable
fun SimpleTab(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = Color.Gray)
    }
}

@Composable
fun ProfileScreen(childName: String, childUid: String, childEmail: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("IMG", color = Color.White)
        }

        Text("Name: $childName", fontWeight = FontWeight.Bold)
        Text("UID: $childUid")
        Text("Email: $childEmail")
    }
}

@Composable
fun BottomNav(selectedTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomNavItem(R.drawable.start, "Home", selectedTab == "Home") { onTabSelected("Home") }
        BottomNavItem(R.drawable.mail, "Inbox", selectedTab == "Inbox") { onTabSelected("Inbox") }
        BottomNavItem(R.drawable.profile, "Profile", selectedTab == "Profile") { onTabSelected("Profile") }
    }
}

@Composable
fun BottomNavItem(iconRes: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}
