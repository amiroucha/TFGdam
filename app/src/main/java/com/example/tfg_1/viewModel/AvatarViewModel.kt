package com.example.tfg_1.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.Character
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AvatarViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    var avatarList by mutableStateOf<List<Character>>(emptyList())
        private set

    var selectedAvatar by mutableStateOf<String?>(null)
        private set

    private var avatarAsignadoAutomaticamente = false // <--- FLAG GLOBAL

    private val authListener = FirebaseAuth.AuthStateListener {
        // cada vez que cambia el usuario logueado, se recarga la imgen
        //asi no se queda en memoria la dek usuario anterior
        avatarAsignadoAutomaticamente = false
        selectedAvatar = null
        loadFirestore()
    }

    init {
        loadAvatars()
        auth.addAuthStateListener(authListener)
        loadFirestore()
    }

    private fun loadAvatars() {
        val names = listOf(
            "Jack", "Ryker", "Aiden", "Ryan", "Andrea",
            "Liam", "Brian", "Leah", "Sawyer", "Christian",
            "Maria", "Jocelyn", "Kingston", "Kimberly", "Valentina",
            "Caleb", "Sara", "Aidan", "Robert", "Mason"
        )

        avatarList = names.mapIndexed { index, name ->
            Character(
                id = index + 1,
                name = name,
                image = "https://api.dicebear.com/6.x/adventurer/png?seed=${name.replace(" ", "%20")}"
            )
        }
    }

    fun guardarAvatar(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("usuarios").document(userId)

        viewModelScope.launch {
            try {
                userRef.update("image", imageUrl).await()
                selectedAvatar = imageUrl
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    private fun loadFirestore() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val imageUrl = firestoreGetAvatarUrl(userId)
            if (!imageUrl.isNullOrEmpty()) {
                selectedAvatar = imageUrl
            } else if (!avatarAsignadoAutomaticamente) {
                // solo asigno random si no hay uno guardado ya
                val randomAvatar = avatarList.randomOrNull()?.image
                if (randomAvatar != null) {
                    avatarAsignadoAutomaticamente = true
                    guardarAvatar(randomAvatar)
                }
            }
        }
    }
    private suspend fun firestoreGetAvatarUrl(userId: String): String? {
        return try {
            val doc = Firebase.firestore.collection("usuarios")
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                doc.getString("image")
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}