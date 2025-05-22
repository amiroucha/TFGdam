package com.example.tfg_1.repositories

import com.example.tfg_1.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    //usuario actual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    //usuario id
    fun getCurrentUserId(): String = auth.currentUser?.uid.orEmpty()

    // nombre del usuario
    suspend fun getCurrentUserName(): String {
        val user = getUserDoc(getCurrentUserId())
        return user.getString("name").orEmpty()
    }
        //id del hogar del user
    suspend fun getCurrentUserHomeId(): String {
        val user = getUserDoc(getCurrentUserId())
        return user.getString("homeId").orEmpty()
    }


    suspend fun getUserDoc(uid: String): DocumentSnapshot {
        return firestore.collection("usuarios")
            .document(uid)
            .get()
            .await()
    }

    suspend fun updateUserHomeId(uid: String, homeId: String) {
        firestore.collection("usuarios")
            .document(uid)
            .update("homeId", homeId)
            .await()
    }

    //crear un hogar
    suspend fun createHome(homeName: String, address: String): String {
        val newHomeRef = firestore.collection("hogares").document()
        val data = mapOf(
            "homeId" to newHomeRef.id,
            "homeName" to homeName,
            "adress" to address
        )
        newHomeRef.set(data).await()
        return newHomeRef.id
    }

    //obtener el nombre del hogar actual
    suspend fun getCurrentHomeName(): String {
        val uid = getCurrentUserId() ?: return ""
        val userDoc = getUserDoc(uid)
        val homeId = userDoc.getString("homeId") ?: return ""
        val homeDoc = getHomeById(homeId)
        return homeDoc.getString("homeName") ?: ""
    }

    suspend fun getHomeById(homeId: String): DocumentSnapshot {
        return firestore.collection("hogares")
            .document(homeId)
            .get()
            .await()
    }

    suspend fun getMembersByHomeId(homeId: String): List<UserModel> {
        val snap = firestore.collection("usuarios")
            .whereEqualTo("homeId", homeId)
            .get()
            .await()
        return snap.documents.mapNotNull { u ->
            UserModel(
                id = u.id,
                name = u.getString("name").orEmpty(),
                email = u.getString("email").orEmpty(),
                homeId = homeId,
                birthDate = u.getString("birthDate").orEmpty(),
                image = u.getString("image").orEmpty(),
            )
        }
    }
}
