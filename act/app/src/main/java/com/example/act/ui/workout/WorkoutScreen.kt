package com.example.act.ui.workout

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.act.R
import kotlinx.coroutines.delay

// Modern Color Palette
val PrimaryBlue = Color(0xFF03F4FF)
val SecondaryOrange = Color(0xFF03F4FF)
val GlassWhite = Color.White.copy(alpha = 0.15f)
val SurfaceWhite = Color.White.copy(alpha = 0.92f)
val Greentext = Color(0xFF0FF300)
val DarkGreyText = Color(0xFF313030)
val HighlightCyan = Color(0xFF00E5FF)

@Composable
fun WorkoutScreen(
    uid: String,
    viewModel: WorkoutViewModel = viewModel(key = uid)
) {
    var selectedMode by remember { mutableStateOf<String?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    val randomBackground = remember {
        val backgrounds = listOf(R.drawable.bg1, R.drawable.bg2, R.drawable.bg3)
        backgrounds.random()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = randomBackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay for better readability
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        AnimatedContent(
            targetState = selectedMode,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            },
            label = "mode_transition"
        ) { mode ->
            when (mode) {
                null -> WorkoutModeSelection(onModeSelected = { selectedMode = it })
                "running" -> RunningModeScreen(
                    onBack = { selectedMode = null },
                    onSave = { duration, distance, calories ->
                        viewModel.saveRunningRecord(duration, distance, calories)
                        selectedMode = null
                    }
                )
                "weight" -> WeightTrainingScreen(
                    viewModel = viewModel,
                    onBack = { selectedMode = null },
                    onSave = { muscle, name, sets, reps, weight ->
                        viewModel.saveWeightRecord(muscle, name, sets, reps, weight)
                        selectedMode = null
                    }
                )
            }
        }

        if (selectedMode == null) {
            FloatingActionButton(
                onClick = { showHistory = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).padding(bottom = 100.dp),
                containerColor = Color(0xFFC5F3F3),
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History")
            }
        }

        if (showHistory) {
            WorkoutHistoryDialog(
                history = viewModel.workoutHistory,
                onDismiss = { showHistory = false }
            )
        }
    }
}

@Composable
fun WorkoutModeSelection(onModeSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "WORKOUT",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 6.sp
        )
        Text(
            "LEVEL UP YOUR BODY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = HighlightCyan,
            letterSpacing = 3.sp
        )
        
        Spacer(modifier = Modifier.height(60.dp))

        ModernWorkoutCard(
            title = "RUNNING",
            subtitle = "Track your pace & distance",
            icon = Icons.Default.DirectionsRun,
            gradient = listOf(Color(0xFF0077B6), Color(0xFF00B4D8)),
            onClick = { onModeSelected("running") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ModernWorkoutCard(
            title = "WEIGHT TRAINING",
            subtitle = "Build strength & muscles",
            icon = Icons.Default.FitnessCenter,
            gradient = listOf(Color(0xFFE85D04), Color(0xFFFF9F1C)),
            onClick = { onModeSelected("weight") }
        )
    }
}

@Composable
fun ModernWorkoutCard(title: String, subtitle: String, icon: ImageVector, gradient: List<Color>, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() }
            .shadow(20.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(gradient))) {
            // Abstract circles for design
            Box(modifier = Modifier.offset(x = 250.dp, y = (-20).dp).size(200.dp).background(Color.White.copy(alpha = 0.1f), CircleShape))
            
            Row(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                }
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}

@Composable
fun RunningModeScreen(onBack: () -> Unit, onSave: (String, Double, Int) -> Unit) {
    var timeSec by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var distanceInput by remember { mutableStateOf("") }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            timeSec++
        }
    }

    val minutes = timeSec / 60
    val seconds = timeSec % 60
    val timeStr = String.format("%02d:%02d", minutes, seconds)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Header(title = "Running Mode", onBack = onBack)

        Spacer(modifier = Modifier.height(80.dp))

        // Timer Display
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            CircularProgressIndicator(
                progress = { (timeSec % 60) / 60f },
                modifier = Modifier.fillMaxSize(),
                color = HighlightCyan,
                strokeWidth = 10.dp,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeStr, fontSize = 72.sp, fontWeight = FontWeight.Light, color = Color.White)
                Text("ELAPSED TIME", fontSize = 14.sp, color = HighlightCyan, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 140.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!isRunning && timeSec > 0) {
                ModernButton(
                    text = "FINISH",
                    color = HighlightCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { showResultDialog = true }
                )
            }

            ModernButton(
                text = if(isRunning) "STOP" else "START",
                color = if(isRunning) Color(0xFFFF3D00) else Color(0xFF00E676),
                modifier = Modifier.weight(1f),
                onClick = { isRunning = !isRunning }
            )
        }
    }

    if (showResultDialog) {
        ModernInputDialog(
            title = "SAVE ACTIVITY",
            onDismiss = { showResultDialog = false },
            content = {
                Column {
                    Text("Duration: $timeStr", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    ModernTextField(
                        value = distanceInput,
                        onValueChange = { distanceInput = it },
                        label = "Distance (km)",
                        keyboardType = KeyboardType.Number,
                        textColor = Color.Black,
                        labelColor = Color.Gray
                    )
                    val dist = distanceInput.toDoubleOrNull() ?: 0.0
                    val cals = (dist * 60).toInt()
                    if (dist > 0) {
                        Text("Approx. Calories: $cals kcal", color = Color(0xFF00C853), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            onConfirm = {
                val dist = distanceInput.toDoubleOrNull() ?: 0.0
                val cals = (dist * 60).toInt()
                onSave(timeStr, dist, cals)
                showResultDialog = false
            }
        )
    }
}

@Composable
fun WeightTrainingScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onSave: (String, String, Int, Int, Double) -> Unit
) {
    var selectedMuscle by remember { mutableStateOf<String?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    
    val workoutData = viewModel.exercisesByMuscle
    val userWeight = viewModel.userWeight

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Header(title = "Weight Training", onBack = {
            if (selectedExercise != null) selectedExercise = null
            else if (selectedMuscle != null) selectedMuscle = null
            else onBack()
        })

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedMuscle == null) {
            Text("TARGET MUSCLE", fontSize = 16.sp, color = HighlightCyan, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
            val muscles = workoutData.keys.toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(muscles) { muscle ->
                    MuscleCard(muscle = muscle) { selectedMuscle = muscle }
                }
            }
        } else if (selectedExercise == null) {
            Text("EXERCISES FOR $selectedMuscle", fontSize = 16.sp, color = HighlightCyan, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(16.dp))
            val exercises = workoutData[selectedMuscle] ?: emptyList()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(exercises) { exercise ->
                    ExerciseCard(exercise = exercise) { selectedExercise = exercise }
                }
            }
        } else {
            WeightExerciseDetail(
                exercise = selectedExercise!!,
                userWeight = userWeight,
                onSave = { sets, reps, weight ->
                    onSave(selectedMuscle!!, selectedExercise!!.name, sets, reps, weight)
                }
            )
        }
    }
}

@Composable
fun Header(title: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
fun MuscleCard(muscle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(muscle, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val imageId = getResourceId(exercise.imageResName)
            Image(
                painter = painterResource(id = imageId),
                contentDescription = null,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(exercise.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Recommended: ${exercise.weightPercentRange}", fontSize = 14.sp, color = DarkGreyText, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun WeightExerciseDetail(exercise: Exercise, userWeight: Double, onSave: (Int, Int, Double) -> Unit) {
    var sets by remember { mutableIntStateOf(3) }
    var reps by remember { mutableIntStateOf(12) }
    var weightInput by remember { mutableStateOf("") }
    
    val minW = userWeight * exercise.minWeightPercent
    val maxW = userWeight * exercise.maxWeightPercent
    
    LaunchedEffect(Unit) {
        weightInput = String.format("%.1f", minW)
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                val imageId = getResourceId(exercise.imageResName)
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                Text(exercise.name, fontSize = 36.sp, fontWeight = FontWeight.Black, color = DarkGreyText)
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, HighlightCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("IDEAL WEIGHT FOR YOU", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                        Text("${String.format("%.1f - %.1f", minW, maxW)} kg", fontSize = 32.sp, color = Greentext, fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                Text("INSTRUCTIONS", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), letterSpacing = 1.5.sp)
                exercise.instructions.forEachIndexed { index, s ->
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("${index + 1}.", fontWeight = FontWeight.Black, color = HighlightCyan, modifier = Modifier.width(28.dp), fontSize = 16.sp)
                        Text(s, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 140.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("LOG YOUR SET", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                
                ModernTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = "Weight (kg)",
                    keyboardType = KeyboardType.Number
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                CounterSection("Sets", sets) { sets = it }
                CounterSection("Reps", reps) { reps = it }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                ModernButton(
                    text = "SAVE PROGRESS",
                    color = HighlightCyan,
                    onClick = {
                        val w = weightInput.toDoubleOrNull() ?: 0.0
                        onSave(sets, reps, w)
                    }
                )
            }
        }
    }
}

@Composable
fun CounterSection(label: String, count: Int, onCountChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
            IconButton(onClick = { if(count > 1) onCountChange(count - 1) }) { Icon(Icons.Default.Remove, null, tint = Color.White) }
            Text("$count", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
            IconButton(onClick = { onCountChange(count + 1) }) { Icon(Icons.Default.Add, null, tint = Color.White) }
        }
    }
}

@Composable
fun ModernButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp).fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
    ) {
        Text(text, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    textColor: Color = Color.White,
    labelColor: Color = Color.White.copy(alpha = 0.7f)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = labelColor, fontWeight = FontWeight.Bold) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = HighlightCyan,
            unfocusedBorderColor = if (textColor == Color.White) Color.White.copy(alpha = 0.4f) else Color.Gray,
            cursorColor = HighlightCyan,
            focusedLabelColor = HighlightCyan,
            unfocusedLabelColor = labelColor
        )
    )
}

@Composable
fun ModernInputDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Black, fontSize = 22.sp) },
        text = content,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("SAVE", fontWeight = FontWeight.Black, color = HighlightCyan, fontSize = 16.sp) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.DarkGray, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@Composable
fun getResourceId(name: String): Int {
    val context = LocalContext.current
    return remember(name) {
        // แปลงชื่อให้เป็นตัวเล็กและแทนที่ช่องว่าง/ขีดด้วย underscore เพื่อป้องกัน Error
        val sanitizedName = name.lowercase()
            .replace(" ", "_")
            .replace("-", "_")
        val id = context.resources.getIdentifier(sanitizedName, "drawable", context.packageName)
        if (id == 0) R.drawable.ic_launcher_background else id
    }
}

@Composable
fun WorkoutHistoryDialog(history: List<WorkoutRecord>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ACTIVITY HISTORY", fontWeight = FontWeight.Black, fontSize = 22.sp) },
        text = {
            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("No activities recorded yet", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 450.dp)) {
                    items(history) { record ->
                        HistoryItem(record)
                    }
                }
            }
        },
        confirmButton = {
            ModernButton(text = "CLOSE", color = HighlightCyan, onClick = onDismiss, modifier = Modifier.padding(16.dp).height(54.dp))
        },
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White
    )
}

@Composable
fun HistoryItem(record: WorkoutRecord) {
    val isRunning = record.type == "Running"
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F5))
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = (if(isRunning) HighlightCyan else Color(0xFFFF9100)).copy(alpha = 0.15f)
            ) {
                Icon(
                    if(isRunning) Icons.Default.DirectionsRun else Icons.Default.FitnessCenter,
                    null,
                    tint = if(isRunning) HighlightCyan else Color(0xFFFF9100),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(record.type, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.Black)
                Text(record.date, fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                if (isRunning) {
                    Text("${record.duration} • ${record.distance} km", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                } else {
                    Text("${record.muscleGroup}: ${record.exerciseName}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text("${record.sets} sets x ${record.reps} reps • ${record.weightUsed} kg", fontSize = 14.sp, color = Color(0xFFFF6D00), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
