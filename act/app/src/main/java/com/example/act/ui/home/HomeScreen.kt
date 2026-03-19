package com.example.act.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.act.R
import com.example.act.firebase.FirebaseManager
import com.example.act.ui.diary.DiaryTask

// Elite Theme Colors
val AccentBlue = Color(0xFF00B4D8)
val DarkSurface = Color(0xFF1B263B)
val GlassEffect = Color.White.copy(alpha = 0.12f)

@Composable
fun HomeScreen(
    uid: String, // รับ UID มาเป็น Key
    viewModel: HomeViewModel = viewModel(key = uid) // ใช้ UID เป็นกุญแจในการสร้าง ViewModel
) {
    val state = viewModel.state
    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient Overlay
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.7f)))
        ))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                // Elite Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("WELCOME BACK,", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(state.nickname.uppercase(), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                    Surface(
                        onClick = { showEditDialog = true },
                        shape = CircleShape,
                        color = GlassEffect,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.padding(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Premium BMI Card
                EliteBMICard(state.bmi, state.bmiStatus)

                Spacer(modifier = Modifier.height(32.dp))

                // Today's Goal Section
                Text("TODAY'S PLAN", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (state.todayTasks.isEmpty()) {
                    EmptyPlanCard()
                } else {
                    state.todayTasks.forEach { task ->
                        EliteTaskItem(task) { viewModel.toggleTask(task) }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Food Recommendation
                Text("NUTRITION GUIDE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(16.dp))
                EliteFoodGrid(state)

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }

    if (showEditDialog) {
        EliteEditDialog(
            initialNickname = state.nickname,
            initialWeight = state.weight,
            initialHeight = state.height,
            onDismiss = { showEditDialog = false },
            onSave = { nickname, weight, height ->
                viewModel.updateUserData(nickname, weight, height)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EliteBMICard(bmi: Double, status: String) {
    val statusColor = when (status) {
        "น้ำหนักน้อย" -> Color(0xFF4FC3F7)
        "สมส่วน" -> Color(0xFF66BB6A)
        "น้ำหนักเกิน" -> Color(0xFFFFCA28)
        "อ้วน" -> Color(0xFFFF7043)
        else -> AccentBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(28.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CURRENT BMI", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(String.format("%.1f", bmi), color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Black)
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (bmi.toFloat() / 40f).coerceIn(0f, 1f) },
                    modifier = Modifier.size(80.dp),
                    color = statusColor,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun EliteTaskItem(task: DiaryTask, onToggle: () -> Unit) {
    val isCompleted = task.isCompleted
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onToggle() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // เปลี่ยนสีพื้นหลังตอนติ๊ก
            containerColor = if(isCompleted) Color(0xFFDFF8DF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, if(isCompleted) Color.Transparent else Color.White.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if(isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                null,
                tint = if(isCompleted) Color(0xFF61FC68) else Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title, 
                    color = if(isCompleted) Color.White else Color.White,
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    textDecoration = if(isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                // เปลี่ยนสีเวลาให้เป็นสีขาวตอนยังไม่ติ๊ก
                Text(
                    task.time, 
                    color = if(isCompleted) Color.DarkGray else Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun EliteFoodGrid(state: HomeState) {
    val meals = listOf(
        Triple("BREAKFAST", state.breakfastRecommend, Icons.Default.LightMode),
        Triple("LUNCH", state.lunchRecommend, Icons.Default.WbSunny),
        Triple("DINNER", state.dinnerRecommend, Icons.Default.NightsStay)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        meals.forEach { (title, food, icon) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = AccentBlue.copy(alpha = 0.1f)) {
                        Icon(icon, null, tint = AccentBlue, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(food, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPlanCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassEffect)
    ) {
        Text("ยังไม่มีเป้าหมายวันนี้\nลองเพิ่มใน Diary ดูนะ", color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(24.dp), textAlign = TextAlign.Center, fontSize = 14.sp,fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EliteEditDialog(initialNickname: String, initialWeight: Double, initialHeight: Double, onDismiss: () -> Unit, onSave: (String, Double, Double) -> Unit) {
    var nickname by remember { mutableStateOf(initialNickname) }
    var weight by remember { mutableStateOf(initialWeight.toString()) }
    var height by remember { mutableStateOf(initialHeight.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("EDIT PROFILE", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Nickname") }, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = {
                        FirebaseManager.auth.signOut()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOGOUT", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val w = weight.toDoubleOrNull() ?: initialWeight
                val h = height.toDoubleOrNull() ?: initialHeight
                onSave(nickname, w, h)
            }, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue), shape = RoundedCornerShape(12.dp)) { Text("SAVE CHANGES") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) } },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}
