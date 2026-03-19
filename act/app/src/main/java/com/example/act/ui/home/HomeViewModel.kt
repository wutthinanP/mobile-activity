package com.example.act.ui.home

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.act.firebase.FirebaseManager
import com.example.act.firebase.FirestoreManager
import com.example.act.ui.diary.DiaryTask
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class HomeState(
    val nickname: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val bmi: Double = 0.0,
    val bmiStatus: String = "",
    val hasDiary: Boolean = false,
    val isLoading: Boolean = true,
    val breakfastRecommend: String = "",
    val lunchRecommend: String = "",
    val dinnerRecommend: String = "",
    val randomGreeting: String = "",
    val greetingColor: Color = Color.Black,
    val todayTasks: List<DiaryTask> = emptyList()
)

class HomeViewModel : ViewModel() {
    var state by mutableStateOf(HomeState())
        private set

    private var tasksListener: ListenerRegistration? = null
    private var isUpdating = false 

    private val dbDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    init {
        refreshData()
    }

    // ฟังก์ชันสำหรับโหลดข้อมูลใหม่ทั้งหมด (ใช้เมื่อเปิดแอปหรือสลับบัญชี)
    fun refreshData() {
        tasksListener?.remove()
        state = HomeState() // ล้าง State เดิมทิ้งทั้งหมดก่อน
        loadUserData()
        loadTodayTasks()
    }

    fun loadUserData() {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        state = state.copy(isLoading = true)

        FirestoreManager.db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nickname = document.getString("nickname") ?: ""
                    calculateInfo(document, nickname)
                } else {
                    state = state.copy(isLoading = false)
                }
                checkDiary(uid)
            }
            .addOnFailureListener {
                state = state.copy(isLoading = false)
            }
    }

    fun updateUserData(nickname: String, weight: Double, height: Double) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        
        val updates = mapOf(
            "nickname" to nickname,
            "weight" to weight,
            "height" to height
        )

        FirestoreManager.db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                loadUserData()
            }
    }

    fun loadTodayTasks() {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        val todayStr = dbDateFormat.format(Date())

        tasksListener?.remove()
        tasksListener = FirestoreManager.db.collection("users").document(uid).collection("diaries")
            .whereEqualTo("date", todayStr)
            .addSnapshotListener { snapshot, error ->
                if (error != null || isUpdating) return@addSnapshotListener
                
                if (snapshot != null) {
                    val tasks = snapshot.mapNotNull { doc ->
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
                    }.sortedBy { it.time }
                    state = state.copy(todayTasks = tasks)
                }
            }
    }

    fun toggleTask(task: DiaryTask) {
        val uid = FirebaseManager.auth.currentUser?.uid ?: return
        val newStatus = !task.isCompleted
        val nowTime = if (newStatus) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) else ""
        
        isUpdating = true 

        val updatedTasks = state.todayTasks.map {
            if (it.id == task.id) it.copy(isCompleted = newStatus, completedTime = nowTime) else it
        }
        state = state.copy(todayTasks = updatedTasks)

        val updates = mapOf(
            "isCompleted" to newStatus,
            "completedTime" to nowTime
        )
        
        FirestoreManager.db.collection("users").document(uid).collection("diaries").document(task.id)
            .update(updates)
            .addOnCompleteListener {
                isUpdating = false
            }
            .addOnFailureListener {
                isUpdating = false
                loadTodayTasks()
            }
    }

    private fun checkDiary(uid: String) {
        FirestoreManager.db.collection("users").document(uid).collection("diaries")
            .limit(1).get()
            .addOnSuccessListener { 
                state = state.copy(hasDiary = !it.isEmpty, isLoading = false)
            }
    }

    private fun calculateInfo(document: DocumentSnapshot, nickname: String) {
        val weight = document.getDouble("weight") ?: 0.0
        val height = document.getDouble("height") ?: 0.0
        val bmi = if (height > 0) weight / ((height / 100) * (height / 100)) else 0.0
        
        val (status, recommend) = getRecommendations(bmi)
        val (greeting, color) = generateRandomGreeting(nickname)
        
        state = state.copy(
            nickname = nickname,
            weight = weight,
            height = height,
            bmi = bmi,
            bmiStatus = status,
            breakfastRecommend = recommend.breakfast,
            lunchRecommend = recommend.lunch,
            dinnerRecommend = recommend.dinner,
            randomGreeting = greeting,
            greetingColor = color
        )
    }

    private fun generateRandomGreeting(name: String): Pair<String, Color> {
        val greetings = listOf(
            "วันนี้ $name ทำตาม Diary รึยังน้า?",
            "สวัสดี $name ออกกำลังกายหรือยังนะวันนี้",
            "$name วันนี้ลืมทำอะไรมั้ยนะ?",
            "อย่าลืมทานข้าวด้วยนะ $name"
        )
        val colors = listOf(
            Color(0xFF03A9F4),
            Color(0xFF4CAF50),
            Color(0xFFFFC107),
            Color(0xFFE91E63)
        )
        
        val randomText = greetings[Random.nextInt(greetings.size)]
        val randomColor = colors[Random.nextInt(colors.size)]
        
        return randomText to randomColor
    }

    private fun getRecommendations(bmi: Double): Pair<String, FoodRecommend> {
        return when {
            bmi <= 0 -> "รอดึงข้อมูล..." to FoodRecommend("", "", "")
            bmi < 18.5 -> "น้ำหนักน้อย" to FoodRecommend(
                "ข้าวต้มหมูสับใส่ไข่ + นมสด",
                "ข้าวมันไก่ + ต้มจืด",
                "สเต็กปลาแซลมอน + มันบด"
            )
            bmi < 23.0 -> "สมส่วน" to FoodRecommend(
                "แซนวิชโฮลวีทอกไก่ + กาแฟดำ",
                "เส้นหมี่น้ำใสอกไก่",
                "ข้าวกล้อง + ปลานึ่ง + ผักต้ม"
            )
            bmi < 25.0 -> "น้ำหนักเกิน" to FoodRecommend(
                "โจ๊กข้าวกล้องอกไก่",
                "ราดหน้าเส้นหมี่ผักเยอะๆ",
                "สลัดทูน่าน้ำใส"
            )
            else -> "อ้วน" to FoodRecommend(
                "โยเกิร์ตกราโนล่า + ผลไม้",
                "เกาเหลาผักเยอะๆ (ไม่กระเทียมเจียว)",
                "สลัดอกไก่ + น้ำสลัดใส"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        tasksListener?.remove()
    }
}

data class FoodRecommend(val breakfast: String, val lunch: String, val dinner: String)
