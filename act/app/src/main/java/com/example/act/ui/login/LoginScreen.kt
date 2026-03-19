package com.example.act.ui.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.act.R
import com.example.act.firebase.FirebaseManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    loginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    val gso = GoogleSignInOptions.Builder(
        GoogleSignInOptions.DEFAULT_SIGN_IN
    )
        .requestIdToken("812832413234-e7long6n8uian3i004gc1bsbb4d917qs.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseManager.auth.signInWithCredential(credential)
                        .addOnCompleteListener { taskAuth ->
                            isLoading = false
                            if (taskAuth.isSuccessful) {
                                Toast.makeText(context, "Login สำเร็จ!", Toast.LENGTH_SHORT).show()
                                loginSuccess()
                            } else {
                                Toast.makeText(context, "Firebase Auth Error: ${taskAuth.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    isLoading = false
                    Toast.makeText(context, "Error: idToken is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                isLoading = false
                Toast.makeText(context, "Google Error: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
            isLoading = false
            // แจ้งเตือนถ้า Google Activity ส่งค่ากลับมาไม่สำเร็จ (เช่น Error 12500 หรือปิดไปเฉยๆ)
            Toast.makeText(context, "Sign-in intent failed (Result: ${result.resultCode})", Toast.LENGTH_SHORT).show()
        }
    }

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
            Text(text = "Welcome To", fontSize = 40.sp, color = Color.Black)
            Text(text = "Act", fontSize = 100.sp, fontWeight = FontWeight.Medium, color = Color(0xFF90E4FF))
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF90E4FF))
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        // สั่ง signOut จาก Google Client ก่อนเพื่อล้างเซสชันเก่า
                        // จะทำให้หน้าต่างเลือกบัญชี (Account Picker) ปรากฏขึ้นมาทุกครั้ง
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90E4FF), contentColor = Color.Black)
                ) {
                    Text(text = "Login", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
