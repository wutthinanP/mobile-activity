package com.example.act.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}
