package com.example.act.ui.workout

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.act.firebase.FirebaseManager
import com.example.act.firebase.FirestoreManager
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class WorkoutRecord(
    val id: String = "",
    val type: String = "", // "Running" or "Weight Training"
    val duration: String = "",
    val distance: Double = 0.0,
    val calories: Int = 0,
    val sets: Int = 0,
    val reps: Int = 0,
    val muscleGroup: String = "",
    val exerciseName: String = "",
    val weightUsed: Double = 0.0,
    val date: String = "",
    val timestamp: Timestamp? = null
)

data class Exercise(
    val id: String = "",
    val name: String = "",
    val weightPercentRange: String = "",
    val minWeightPercent: Double = 0.0,
    val maxWeightPercent: Double = 0.0,
    val instructions: List<String> = emptyList(),
    val imageResName: String = "ic_launcher_background",
    val muscleGroup: String = ""
)

class WorkoutViewModel : ViewModel() {
    var workoutHistory = mutableStateListOf<WorkoutRecord>()
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    var userWeight by mutableDoubleStateOf(0.0)
        private set

    var exercisesByMuscle = mutableStateMapOf<String, List<Exercise>>()
        private set

    private val dbDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    init {
        fetchUserWeight()
        loadWorkoutHistory()
        loadExerciseTemplates()
    }

    fun fetchUserWeight() {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        FirestoreManager.db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                userWeight = doc.getDouble("weight") ?: 0.0
            }
    }

    fun loadWorkoutHistory() {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        isLoading = true
        
        FirestoreManager.db.collection("users").document(uid).collection("workouts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                workoutHistory.clear()
                val history = result.mapNotNull { doc ->
                    doc.toObject(WorkoutRecord::class.java)?.copy(id = doc.id)
                }
                workoutHistory.addAll(history)
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun loadExerciseTemplates() {
        FirestoreManager.db.collection("exercises")
            .get()
            .addOnSuccessListener { result ->
                val allExercises = result.mapNotNull { doc ->
                    doc.toObject(Exercise::class.java).copy(id = doc.id)
                }
                val grouped = allExercises.groupBy { it.muscleGroup }
                exercisesByMuscle.clear()
                exercisesByMuscle.putAll(grouped)
            }
    }

    fun saveRunningRecord(duration: String, distance: Double, calories: Int) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        val now = Calendar.getInstance()
        val dateStr = dbDateFormat.format(now.time)
        
        val record = hashMapOf(
            "type" to "Running",
            "duration" to duration,
            "distance" to distance,
            "calories" to calories,
            "date" to dateStr,
            "timestamp" to Timestamp(now.time)
        )

        FirestoreManager.db.collection("users").document(uid).collection("workouts")
            .add(record)
            .addOnSuccessListener {
                loadWorkoutHistory()
            }
    }

    fun saveWeightRecord(muscleGroup: String, exerciseName: String, sets: Int, reps: Int, weightUsed: Double) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        val now = Calendar.getInstance()
        val dateStr = dbDateFormat.format(now.time)
        
        val record = hashMapOf(
            "type" to "Weight Training",
            "muscleGroup" to muscleGroup,
            "exerciseName" to exerciseName,
            "sets" to sets,
            "reps" to reps,
            "weightUsed" to weightUsed,
            "date" to dateStr,
            "timestamp" to Timestamp(now.time)
        )

        FirestoreManager.db.collection("users").document(uid).collection("workouts")
            .add(record)
            .addOnSuccessListener {
                loadWorkoutHistory()
            }
    }
}
