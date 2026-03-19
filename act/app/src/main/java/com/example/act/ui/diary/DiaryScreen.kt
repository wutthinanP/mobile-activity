package com.example.act.ui.diary

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.act.R
import com.example.act.notification.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime

// Modern Theme Colors
val DiaryPrimary = Color(0xFF0288D1)
val DiaryAccent = Color(0xFF00B4D8)
val DiarySurface = Color.White.copy(alpha = 0.95f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    uid: String,
    viewModel: DiaryViewModel = viewModel(key = uid)
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<DiaryTask?>(null) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    
    val dbFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    val todayStr = dbFormat.format(Date())

    val activeTodayTasks = viewModel.tasks.filter { it.date == todayStr && !it.isCompleted }
    val activeUpcomingTasks = viewModel.tasks.filter { 
        val taskDate = try { dbFormat.parse(it.date) } catch(e: Exception) { null }
        taskDate != null && taskDate.after(Date()) && it.date != todayStr && !it.isCompleted
    }
    
    val completedTasks = viewModel.tasks.filter { it.isCompleted }.sortedByDescending { it.timestamp }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = "DIARY & PLANNER",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = DiaryAccent,
                letterSpacing = 2.sp
            )
            Text(
                text = "Your Daily Schedule",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    if (activeTodayTasks.isNotEmpty()) {
                        item {
                            SectionHeader("TODAY'S TASKS", DiaryAccent)
                        }
                        items(activeTodayTasks) { task ->
                            ModernDiaryCard(task, onEdit = { taskToEdit = task })
                        }
                    }

                    if (activeUpcomingTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SectionHeader("UPCOMING", Color.DarkGray)
                        }
                        items(activeUpcomingTasks) { task ->
                            ModernDiaryCard(task, onEdit = { taskToEdit = task })
                        }
                    }
                    
                    if (activeTodayTasks.isEmpty() && activeUpcomingTasks.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No pending activities", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }


        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showHistoryDialog = true },
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History")
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DiaryAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD TASK", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showAddDialog || taskToEdit != null) {
            ModernAddTaskBottomSheet(
                task = taskToEdit,
                onDismiss = { 
                    showAddDialog = false
                    taskToEdit = null
                },
                onAdd = { title, date, time ->
                    if (taskToEdit != null) {
                        viewModel.updateTask(taskToEdit!!.id, title, date, time)
                    } else {
                        viewModel.addTask(title, date, time)
                    }

                    // Schedule Notification
                    val timeParts = time.split(":")
                    val scheduleCal = (date.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                        set(Calendar.MINUTE, timeParts[1].toInt())
                        set(Calendar.SECOND, 0)
                    }
                    
                    if (scheduleCal.after(Calendar.getInstance())) {
                        NotificationHelper.scheduleNotification(
                            context = context,
                            title = "คุณมีกิจกรรมที่ต้องทำนะ",
                            message = title,
                            calendar = scheduleCal
                        )
                    }

                    showAddDialog = false
                    taskToEdit = null
                }
            )
        }

        if (showHistoryDialog) {
            ModernHistoryDialog(
                tasks = completedTasks,
                onDismiss = { showHistoryDialog = false }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = color,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun ModernDiaryCard(task: DiaryTask, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DiarySurface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(DiaryPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccessTime, null, tint = DiaryPrimary, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${task.date} • ${task.time}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Surface(
                onClick = onEdit,
                shape = CircleShape,
                color = Color.Gray.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.Gray,
                    modifier = Modifier.padding(10.dp).size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ModernHistoryDialog(tasks: List<DiaryTask>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("COMPLETED TASKS", fontWeight = FontWeight.Black, fontSize = 20.sp) },
        text = {
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No history yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(task.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Plan: ${task.date}", fontSize = 12.sp, color = Color.Gray)
                                    if (task.completedTime.isNotBlank()) {
                                        Text("Done at: ${task.completedTime}", fontSize = 13.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DiaryPrimary)
            ) {
                Text("CLOSE", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAddTaskBottomSheet(
    task: DiaryTask? = null,
    onDismiss: () -> Unit,
    onAdd: (String, Calendar, String) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(task?.time ?: SimpleDateFormat("HH:mm").format(Date())) }
    
    val timePickerState = rememberTimePickerState(
        initialHour = try { task?.time?.split(":")?.get(0)?.toInt() ?: 8 } catch(e: Exception) { 8 },
        initialMinute = try { task?.time?.split(":")?.get(1)?.toInt() ?: 0 } catch(e: Exception) { 0 }
    )
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).fillMaxWidth()) {
            Text(
                if (task == null) "NEW ACTIVITY" else "EDIT ACTIVITY",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What are you planning?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DiaryAccent,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("SELECT DATE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(14) { i ->
                    val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
                    val dayNum = SimpleDateFormat("dd").format(date.time)
                    val dayName = SimpleDateFormat("EEE", Locale("th", "TH")).format(date.time)
                    val isSelected = SimpleDateFormat("ddMM").format(date.time) == SimpleDateFormat("ddMM").format(selectedDate.time)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) DiaryAccent else Color(0xFFF1F3F5))
                            .clickable { selectedDate = date }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(dayNum, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else Color.Black, fontSize = 18.sp)
                        Text(dayName, fontSize = 12.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("SET TIME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF1F3F5),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = DiaryAccent)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(selectedTime, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
            
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) { Text("DONE", fontWeight = FontWeight.Bold, color = DiaryAccent) }
                    },
                    text = { TimePicker(state = timePickerState) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onAdd(title, selectedDate, selectedTime) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DiaryAccent),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) { 
                Text(if (task == null) "CREATE PLAN" else "SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 16.sp) 
            }
        }
    }
}
