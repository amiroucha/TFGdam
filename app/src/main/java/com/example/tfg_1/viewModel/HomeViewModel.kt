package com.example.tfg_1.viewModel

import androidx.lifecycle.ViewModel
import com.example.tfg_1.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel(){
    //firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    //estado de navegacion
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    //variables de tipo home
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _address  = MutableStateFlow("")
    val address : StateFlow<String> = _address.asStateFlow()

    private val _code  = MutableStateFlow("")
    val code : StateFlow<String> = _code.asStateFlow()

    //lista de miembros del hogar
    private val _members = MutableStateFlow<List<UserModel>>(emptyList())
    val members : StateFlow<List<UserModel>> = _members.asStateFlow()


    //act informacion al momento
    fun changeName(at:String){ _name.value = at}
    fun changeAdress(at:String){ _address.value = at}
    fun actCode(at:String){ _code.value = at}

    //al crear gome se lanza la carga 1
    init {
        loadUser()
    }

    // carga inicial del usuario y ver si tiene un hogar
    private fun loadUser() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading //lo ponemos en carga

            val firebaseUser = auth.currentUser
            //primero compruebo si esta logueado
            if (firebaseUser == null) {
                _uiState.value = UiState.NotLogged  //no esta logueado
                return@launch //se sale
            }

            firestore.collection("usuarios") //accedo a la coleccion de usuarios
                .document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val homeId = doc.getString("hogarId").orEmpty()
                    if (homeId.isBlank()) {
                        // si el user no tiene hogar asignado va a la pantalla de settings
                        _uiState.value = UiState.NotHome
                    } else {
                        _uiState.value = UiState.HasHome(homeId) //usuario con casa , va a tareas
                        listMembers(homeId) //carga lista de usuarios
                    }
                }
                .addOnFailureListener { e ->
                    _uiState.value = UiState.Error(e.localizedMessage)
                }
        }
    }

    //crear hogar en base datos y asignarlo al usuario
    fun createHome() {
        val homeName = name.value.trim()
        if (homeName.isEmpty()) return

        //creo un higar en hogares
        val newHomeRef = firestore.collection("hogares").document()
        val data = mapOf(
            "id_hogar"       to newHomeRef.id,
            "nombre_hogar"   to homeName,
            "direccion"      to address.value.trim(),
            "fecha_creacion" to System.currentTimeMillis()
        )
        newHomeRef.set(data)
            .addOnSuccessListener {
                //actuañizo en campo idhome del usuario
                updateUserHome(newHomeRef.id)
            }
            .addOnFailureListener {
                _uiState.value = UiState.Error("No se pudo crear el hogar")
            }
    }

    // unir a un hogar existente por un codigo
    fun joinHome() {
        val codeVal = code.value.trim()
        if (codeVal.isEmpty()) return //si esta en blanco no me sirve

        firestore.collection("hogares")
            .document(codeVal)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) { //si existe ese codigo asociadno a un hogar
                    updateUserHome(codeVal)  //se asocia al usuario
                } else { //no se encuentra un higar con ese cod
                    _uiState.value = UiState.Error("Código de hogar inválido")
                }
            }
            .addOnFailureListener {
                _uiState.value = UiState.Error("Error al buscar el hogar")
            }
    }

    //actualiza el campo hogarId del usuario
    private fun updateUserHome(homeId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("usuarios")
            .document(uid)
            .update("hogarId", homeId)
            .addOnSuccessListener {
                // Vuelve a cargar el usuario para disparar la navegación
                loadUser()
            }
            .addOnFailureListener {
                _uiState.value = UiState.Error("No se pudo unir al hogar")
            }
    }

    //lista de miembros de x hogar
    private fun listMembers(homeId: String) {
        firestore.collection("usuarios")
            .whereEqualTo("hogarId", homeId)
            .get()
            .addOnSuccessListener { snap ->
                _members.value = snap.documents.mapNotNull { d ->
                    UserModel(
                        id     = d.id,
                        name   = d.getString("nombre").orEmpty(),
                        email  = d.getString("correo").orEmpty(),
                        homeId = homeId
                    )
                }
            }
            .addOnFailureListener {e ->
                _uiState.value = UiState.Error("No se han podido cargar los miembros: ${e.localizedMessage}")
            }
    }

    sealed class UiState {
        data object Loading: UiState()  //comprobar usuario y home
        data object NotLogged: UiState()  // no logueado
        data object NotHome: UiState()  //sin casa y logueado
        data class HasHome(val homeId: String): UiState() //si tiene casa
        data class Error(val message: String?): UiState()  //error
    }
}