package com.example.act.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.act.R
import com.example.act.firebase.FirebaseManager
import com.example.act.firebase.FirestoreManager

@Composable
fun NicknameScreen(
    onComplete: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg3),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ให้เราเรียกคุณว่าอะไรดี ?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ระบุชื่อเล่นของคุณ") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF90E4FF),
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White.copy(alpha = 0.7f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF90E4FF))
            } else {
                Button(
                    onClick = {
                        if (nickname.isNotBlank()) {
                            isLoading = true
                            val uid = FirebaseManager.auth.currentUser?.uid ?: return@Button
                            
                            FirestoreManager.db.collection("users").document(uid)
                                .update(
                                    mapOf(
                                        "nickname" to nickname,
                                        "setupComplete" to true
                                    )
                                )
                                .addOnSuccessListener {
                                    isLoading = false
                                    onComplete()
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF90E4FF), 
                        contentColor = Color.Black
                    ),
                    enabled = nickname.isNotBlank()
                ) {
                    Text(text = "เริ่มต้นใช้งาน", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
