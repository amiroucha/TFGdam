package com.example.tfg_1.ui.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tfg_1.viewModel.AvatarViewModel
import androidx.compose.foundation.lazy.grid.items


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSheet(
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val avatarViewModel: AvatarViewModel = viewModel()
    val avatars = avatarViewModel.avatarList

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {BottomSheetDefaults.DragHandle()}
    )
    {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            items(avatars){ character  ->
                AsyncImage(
                    model = character.image,
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable {
                            onAvatarSelected(character.image)
                        }
                )
            }
        }
    }
}