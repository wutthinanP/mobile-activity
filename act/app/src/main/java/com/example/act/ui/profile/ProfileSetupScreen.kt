package com.example.act.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.act.R
import com.example.act.firebase.FirebaseManager
import com.example.act.firebase.FirestoreManager

@Composable
fun ProfileSetupScreen(
    setupComplete: () -> Unit
) {
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg1),
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
                text = "BMI",
                fontSize = 60.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ช่องกรอก Age
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                placeholder = { Text("Age", color = Color.DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF90E4FF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ช่องกรอก Weight
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                placeholder = { Text("Weight", color = Color.DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF90E4FF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ช่องกรอก Height
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                placeholder = { Text("Height", color = Color.DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color(0xFF90E4FF)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (age.isBlank() || weight.isBlank() || height.isBlank()) return@Button
                    isLoading = true
                    val uid = FirebaseManager.auth.currentUser?.uid ?: return@Button

                    val data: Map<String, Any> = mapOf(
                        "age" to (age.toIntOrNull() ?: 0),
                        "weight" to (weight.toDoubleOrNull() ?: 0.0),
                        "height" to (height.toDoubleOrNull() ?: 0.0),
                        "setupComplete" to false
                    )

                    FirestoreManager.db
                        .collection("users")
                        .document(uid)
                        .set(data)
                        .addOnSuccessListener {
                            isLoading = false
                            setupComplete()
                        }
                        .addOnFailureListener {
                            isLoading = false
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90E4FF),
                    contentColor = Color.Black
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text("บันทึก", fontSize = 20.sp, fontWeight = FontWeight.Normal)
                }
            }
        }
    }
}
