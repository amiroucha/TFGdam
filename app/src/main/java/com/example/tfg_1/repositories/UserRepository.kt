package com.example.tfg_1.repositories

import android.content.Context
import android.util.Log
import com.example.tfg_1.R
import com.example.tfg_1.model.ChatMessageModel
import com.example.tfg_1.model.ExpensesModel
import com.example.tfg_1.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.GoogleAuthProvider



class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
)
{

    //usuario actual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    //usuario id
    fun getCurrentUserId(): String = auth.currentUser?.uid.orEmpty()

    // nombre del usuario
    suspend fun getCurrentUserName(): String {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) {
            // Usuario no logueado no  consultar Firestore
            return ""
        }
        val user = getUserDoc(uid)
        return user.getString("name").orEmpty()
    }

    //id del hogar del user actual
    //lo uso en el chat,, es importante no tocar
    suspend fun getCurrentUserHomeId(): String {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) {
            // Usuario no logueado no consultar Firestore
            return ""
        }
        val user = getUserDoc(uid)
        return user.getString("homeId").orEmpty()
    }


    suspend fun getUserDoc(uid: String): DocumentSnapshot {
        return firestore.collection("usuarios")
            .document(uid)
            .get()
            .await()
    }
    //ACTUALIZAR EL home id DEL usuario, SE CAMBIA DE CASA
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
        val uid = getCurrentUserId()
        if (uid.isEmpty()) {
            // Usuario no logueado no consultar Firestore
            return ""
        }
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

    //login ----------------------------------------------------------------------------
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun loginWithGoogle(context: Context): Result<FirebaseUser?> {
        try {
            val credentialManager = CredentialManager.create(context)  // inicializas aquí

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.idWeb))
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = auth.currentUser

            // Guardar datos usuario
            val userMap = hashMapOf(
                "email" to (user?.email ?: ""),
                "uid" to user?.uid,
                "name" to (user?.displayName ?: "")
            )
            user?.uid?.let {
                firestore.collection("usuarios").document(it).set(userMap, SetOptions.merge()).await()
            }

            return Result.success(user)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    //registro-------------------------------------------------------------------
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        birthDate: String
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            user?.uid?.let { uid ->
                val userModel = UserModel(
                    id = uid,
                    name = name,
                    email = email,
                    homeId = "",
                    birthDate = birthDate,
                    image = ""
                )
                firestore.collection("usuarios")
                    .document(uid)
                    .set(userModel)
                    .await()
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    //chat--------------------------------------------------------------------
    fun escucharMensajes(homeId: String, onMessagesChanged: (List<ChatMessageModel>) -> Unit): ListenerRegistration {
        return firestore.collection("hogares")
            .document(homeId)
            .collection("mensajes")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("UserRepository", "Error escuchando mensajes", error)
                    onMessagesChanged(emptyList())
                    return@addSnapshotListener
                }

                val mensajes = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(ChatMessageModel::class.java)
                    } catch (e: Exception) {
                        Log.e("UserRepository", "Error parsing message: ${doc.id}", e)
                        null
                    }
                }

                onMessagesChanged(mensajes)
            }
    }
    suspend fun enviarMensaje(homeId: String, message: ChatMessageModel): Boolean {
        return try {
            firestore.collection("hogares")
                .document(homeId)
                .collection("mensajes")
                .add(message)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error enviando mensaje", e)
            false
        }
    }


    //expenses ------------------------------------------------------------------
    fun escucharHomeIdUsuarioActual(onChange: (String?) -> Unit): ListenerRegistration? {
        val uid = auth.currentUser?.uid ?: return null

        return firestore.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("UserRepository", "Error escuchando usuario", error)
                    onChange(null)
                    return@addSnapshotListener
                }

                val homeId = snapshot.getString("homeId")
                Log.d("UserRepository", "Snapshot usuario -> homeId=$homeId")
                onChange(homeId)
            }
    }


    //obtener y actualizar gastos
    fun escucharGastos(homeId: String, onChange: (List<ExpensesModel>) -> Unit): ListenerRegistration {
        return firestore.collection("hogares").document(homeId)
            .collection("gastos")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    Log.e("ExpensesViewModel", "Error escuchando gastos", err)
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val gastos = snap.documents.mapNotNull { d ->
                    d.toObject(ExpensesModel::class.java)?.copy(id = d.id)
                }
                onChange(gastos)
            }
    }
    //añadir gasto
    suspend fun addExpense(homeId: String, gasto: ExpensesModel): Boolean {
        val data = mapOf(
            "categoria"  to gasto.categoria.lowercase(),
            "asignadoA"  to gasto.asignadoA,
            "descripcion" to gasto.descripcion,
            "fecha"      to gasto.fecha,
            "homeId"     to homeId,
            "importe"    to gasto.importe
        )

        return try {
            firestore.collection("hogares").document(homeId)
                .collection("gastos")
                .add(data)
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al guardar gasto", e)
            false
        }
    }
    //eliminar gasto
    suspend fun deleteExpense(homeId: String, gastoId: String): Boolean {
        return try {
            firestore.collection("hogares")
                .document(homeId)
                .collection("gastos")
                .document(gastoId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al eliminar gasto", e)
            false
        }
    }
}
