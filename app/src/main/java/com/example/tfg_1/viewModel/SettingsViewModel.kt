package com.example.tfg_1.viewModel

import androidx.lifecycle.ViewModel
import com.example.tfg_1.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _homeId = MutableStateFlow("")
    val homeId : StateFlow<String> = _homeId.asStateFlow()

    private val _members = MutableStateFlow<List<UserModel>>(emptyList())
    val members : StateFlow<List<UserModel>> = _members.asStateFlow()

    init {
        loadHome()
    }

    //obtener hogar y usuarios
    private fun loadHome() {
        val uid = auth.currentUser!!.uid

        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc->
                val homeId = doc.getString("homeId").orEmpty()
                _homeId.value = homeId //consigo el actual homeId

                if (homeId.isNotEmpty()) {
                    firestore.collection("usuarios")//busco usuarios con el  = homeID
                        .whereEqualTo("homeId", homeId)
                        .get()
                        .addOnSuccessListener { doc2 ->
                            _members.value = doc2.documents.mapNotNull { usr ->
                                UserModel(
                                    id = usr.id,
                                    name = usr.getString("name").orEmpty(),
                                    email = usr.getString("email").orEmpty(),
                                    homeId = homeId,
                                    birthDate = usr.getString("birthDate").orEmpty(),
                                )
                            }

                        }

                }
            }
    }










}