package com.example.act.ui.diary

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.act.firebase.FirebaseManager
import com.example.act.firebase.FirestoreManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

data class DiaryTask(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val date: String = "",
    val completedTime: String = "",
    val timestamp: Timestamp? = null,
    val isCompleted: Boolean = false
)

class DiaryViewModel : ViewModel() {
    var tasks = mutableStateListOf<DiaryTask>()
        private set

    var isLoading by mutableStateOf(false)
        private set

    private var tasksListener: ListenerRegistration? = null
    private val dbDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    init {
        observeTasks()
    }

    private fun observeTasks() {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        isLoading = true
        
        tasksListener?.remove()
        tasksListener = FirestoreManager.db.collection("users").document(uid).collection("diaries")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    tasks.clear()
                    val taskList = snapshot.mapNotNull { doc ->
                        try {
                            DiaryTask(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                time = doc.getString("time") ?: "",
                                date = doc.getString("date") ?: "",
                                completedTime = doc.getString("completedTime") ?: "",
                                timestamp = doc.getTimestamp("timestamp"),
                                isCompleted = doc.getBoolean("isCompleted") ?: false
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    tasks.addAll(taskList)
                }
                isLoading = false
            }
    }

    fun addTask(title: String, date: Calendar, time: String) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        val timestamp = createTimestamp(date, time)
        val dateStr = dbDateFormat.format(date.time)

        val newTask = hashMapOf(
            "title" to title,
            "time" to time,
            "date" to dateStr,
            "timestamp" to timestamp,
            "isCompleted" to false,
            "completedTime" to ""
        )

        FirestoreManager.db.collection("users").document(uid).collection("diaries").add(newTask)
    }

    fun updateTask(taskId: String, title: String, date: Calendar, time: String) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        val timestamp = createTimestamp(date, time)
        val dateStr = dbDateFormat.format(date.time)

        val updates = mapOf(
            "title" to title,
            "time" to time,
            "date" to dateStr,
            "timestamp" to timestamp
        )

        FirestoreManager.db.collection("users").document(uid).collection("diaries").document(taskId).update(updates)
    }

    private fun createTimestamp(date: Calendar, timeStr: String): Timestamp {
        val timeParts = timeStr.split(":")
        val cal = (date.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, if (timeParts.size > 0) timeParts[0].toIntOrNull() ?: 0 else 0)
            set(Calendar.MINUTE, if (timeParts.size > 1) timeParts[1].toIntOrNull() ?: 0 else 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(cal.time)
    }
//
//    fun toggleTaskCompletion(task: DiaryTask) {
//        val uid = FirebaseManager.auth.currentUser?.uid ?: return
//        val newStatus = !task.isCompleted
//        val nowTime = if (newStatus) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) else ""
//
//        val updates = mapOf(
//            "isCompleted" to newStatus,
//            "completedTime" to nowTime
//        )
//
//        FirestoreManager.db.collection("users").document(uid).collection("diaries").document(task.id)
//            .update(updates)
//    }
//
//    fun deleteTask(taskId: String) {
//        val uid = FirebaseManager.auth.currentUser?.uid ?: return
//        FirestoreManager.db.collection("users").document(uid).collection("diaries").document(taskId).delete()
//    }

    override fun onCleared() {
        super.onCleared()
        tasksListener?.remove()
    }
}
