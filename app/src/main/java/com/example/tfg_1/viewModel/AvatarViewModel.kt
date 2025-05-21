package com.example.tfg_1.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tfg_1.model.Character

class AvatarViewModel : ViewModel() {
    var avatarList by mutableStateOf<List<Character>>(emptyList())
        private set

    init {
        loadAvatars()
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
}