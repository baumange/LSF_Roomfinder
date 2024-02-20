package com.example.roomfinder.ui.search

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.roomfinder.DEST_BUILDING_SELECTOR
import com.example.roomfinder.DEST_ROOM_LIST
import com.example.roomfinder.DEST_ROOM_TYPE_SELECTOR
import com.example.roomfinder.LSF
import com.example.roomfinder.R
import com.example.roomfinder.ViewModel

@Preview(showBackground = true)
@Composable
fun SearchInputPreview() {
    SearchInput(ViewModel(Application()), rememberNavController())
}

@Composable
fun SearchInput(viewModel: ViewModel, navController: NavController) {
    val currentBuilding by viewModel.selectedBuilding.observeAsState(
        LSF.Building(
            "",
            stringResource(
                R.string.not_selected
            )
        )
    )
    val currentRoomType by viewModel.selectedRoomType.observeAsState(
        LSF.RoomType(
            "",
            stringResource(
                R.string.not_selected
            )
        )
    )
    val buttonEnabled by viewModel.searchEnabled.observeAsState(false)
    val buttonModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    Column {
        BuildingSelectorCard(currentBuilding.name, navController)
        RoomTypeSelectorCard(currentRoomType.name, navController)
        Button(onClick = {
            viewModel.selectedBuilding.value?.let {
                viewModel.loadRooms(
                    it,
                    viewModel.selectedRoomType.value ?: LSF.RoomType("", "")
                )
            }
            navController.navigate(DEST_ROOM_LIST)
        }, modifier = buttonModifier, enabled = buttonEnabled) {
            Text(text = stringResource(R.string.search))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BuildingSelectorCardPreview() {
    BuildingSelectorCard("INF 205", rememberNavController())
}

@Composable
fun BuildingSelectorCard(buildingName: String, navController: NavController) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable { navController.navigate(DEST_BUILDING_SELECTOR) }
    val textModifier = Modifier
        .padding(start = 16.dp)
        .width(80.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(R.string.building), modifier = textModifier)
        Card(modifier = cardModifier) {
            Text(text = buildingName, modifier = Modifier.padding(8.dp))
        }
    }

}

@Preview(showBackground = true)
@Composable
fun RoomTypeSelectorCardPreview() {
    RoomTypeSelectorCard("Seminarraum", rememberNavController())
}

@Composable
fun RoomTypeSelectorCard(roomTypeName: String, navController: NavController) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable { navController.navigate(DEST_ROOM_TYPE_SELECTOR) }
    val textModifier = Modifier
        .padding(start = 16.dp)
        .width(80.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(R.string.room_type), modifier = textModifier)
        Card(modifier = cardModifier) {
            Text(text = roomTypeName, modifier = Modifier.padding(8.dp))
        }
    }
}