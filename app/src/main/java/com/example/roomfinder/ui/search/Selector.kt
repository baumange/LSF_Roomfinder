package com.example.roomfinder.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.roomfinder.DEST_SEARCH_INPUT
import com.example.roomfinder.LSF
import com.example.roomfinder.ViewModel

@Composable
fun BuildingSelector(viewModel: ViewModel, navController: NavController) {
    val buildingList by viewModel.buildingList.observeAsState(listOf())
    LazyColumn(Modifier.fillMaxSize()) {
        items(buildingList) {
            BuildingSelectorListItem(it, viewModel, navController)
        }
    }
}

@Composable
fun BuildingSelectorListItem(
    building: LSF.Building,
    viewModel: ViewModel,
    navController: NavController
) {
    val listItemModifier = Modifier.clickable {
        viewModel.selectBuilding(building)
        navController.navigate(DEST_SEARCH_INPUT)
    }
    ListItem(
        headlineContent = { Text(text = building.name) },
        modifier = listItemModifier
    )
}

@Composable
fun RoomTypeSelector(viewModel: ViewModel, navController: NavController) {
    val roomTypeList by viewModel.roomTypeList.observeAsState(listOf())
    LazyColumn(Modifier.fillMaxSize()) {
        items(roomTypeList) {
            RoomTypeSelectorListItem(it, viewModel, navController)
        }
    }
}

@Composable
fun RoomTypeSelectorListItem(
    roomType: LSF.RoomType,
    viewModel: ViewModel,
    navController: NavController
) {
    val listItemModifier = Modifier.clickable {
        viewModel.selectRoomType(roomType)
        navController.navigate(DEST_SEARCH_INPUT)
    }
    ListItem(
        headlineContent = { Text(text = roomType.name) },
        modifier = listItemModifier
    )
}