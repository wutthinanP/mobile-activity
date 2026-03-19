package com.example.act.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.act.firebase.FirebaseManager
import com.example.act.firebase.FirestoreManager

@Composable
fun CategorySelectScreen(
    onComplete: () -> Unit
) {
    val categories = listOf(
        "ดูหนัง", "เล่นเกม", "ฟังเพลง", "ไปเที่ยว", "สังสรรค์", 
        "ดูยูทูป", "อ่านหนังสือ", "ออกกำลังกาย", "ตะลุยกิน",
        "ถ่ายรูป", "วาดรูป", "ทำอาหาร", "ช้อปปิ้ง", "นอนพักผ่อน",
        "เดินเล่น", "ปีนเขา", "ว่ายน้ำ", "โยคะ", "จัดสวน", "เลี้ยงสัตว์"
    )
    
    val selectedCategories = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "เลือกกิจกรรมที่คุณสนใจ",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategories.contains(category)
                    
                    Row(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF90E4FF) else Color.Gray,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .background(
                                color = if (isSelected) Color(0xFFE0F7FF) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                if (isSelected) selectedCategories.remove(category)
                                else selectedCategories.add(category)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = category,
                            fontSize = 14.sp,
                            color = Color.Black,
                            maxLines = 1
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                val uid = FirebaseManager.auth.currentUser?.uid ?: return@Button
                
                FirestoreManager.db.collection("users").document(uid)
                    .update(
                        mapOf(
                            "favoriteCategories" to selectedCategories.toList()
                        )
                    )
                    .addOnSuccessListener {
                        isLoading = false
                        onComplete()
                    }
                    .addOnFailureListener {
                        isLoading = false
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF90E4FF),
                contentColor = Color.Black
            ),
            enabled = selectedCategories.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            } else {
                Text(
                    text = "ถัดไป",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}
