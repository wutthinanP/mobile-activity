package com.example.act

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.act.firebase.FirebaseManager
import com.example.act.firebase.FirestoreManager
import com.example.act.ui.diary.DiaryScreen
import com.example.act.ui.home.HomeScreen
import com.example.act.ui.login.LoginScreen
import com.example.act.ui.profile.CategorySelectScreen
import com.example.act.ui.profile.NicknameScreen
import com.example.act.ui.profile.ProfileSetupScreen
import com.example.act.ui.theme.ActTheme
import com.example.act.ui.workout.WorkoutScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ActTheme {
                val context = this
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(context, "กรุณาเปิดการแจ้งเตือนเพื่อให้ระบบแจ้งเตือนกิจกรรมของคุณ", Toast.LENGTH_LONG).show()
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val auth = FirebaseManager.auth
                    var page by remember { 
                        mutableStateOf(if (auth.currentUser == null) "login" else "loading") 
                    }
                    var currentUserState by remember { mutableStateOf(auth.currentUser) }

                    DisposableEffect(Unit) {
                        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                            val newUser = firebaseAuth.currentUser
                            if (newUser != null && currentUserState?.uid != newUser.uid) {
                                page = "loading"
                            }
                            currentUserState = newUser
                        }
                        auth.addAuthStateListener(listener)
                        onDispose { auth.removeAuthStateListener(listener) }
                    }

                    LaunchedEffect(currentUserState, page) {
                        val user = currentUserState
                        if (user == null) {
                            page = "login"
                        } else if (page == "loading") {
                            FirestoreManager.db.collection("users").document(user.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val isComplete = document.getBoolean("setupComplete") ?: false
                                        page = if (isComplete) "main" else "setup"
                                    } else {
                                        page = "setup"
                                    }
                                }
                                .addOnFailureListener {
                                    page = "setup"
                                }
                        }
                    }

                    val uid = currentUserState?.uid ?: ""
                    
                    key(uid) {
                        when (page) {
                            "loading" -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            "login" -> LoginScreen { page = "loading" }
                            "setup" -> ProfileSetupScreen { page = "category" }
                            "category" -> CategorySelectScreen { page = "nickname" }
                            "nickname" -> NicknameScreen { page = "main" }
                            "main" -> MainScaffold(uid = uid)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScaffold(uid: String) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        TabItem("Home", Icons.Default.Home),
        TabItem("Diary", Icons.Default.DateRange),
        TabItem("Workout", Icons.Default.Settings)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Content Area
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeScreen(uid = uid)
                1 -> DiaryScreen(uid = uid)
                2 -> WorkoutScreen(uid = uid)
            }
        }

        // Floating Glass Bottom Navigation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(36.dp),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedTab == index
                        val color = if (isSelected) Color(0xFF00B4D8) else Color.Gray
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTab = index }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = color,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = tab.title,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(val title: String, val icon: ImageVector)
