package com.example.roomfinder

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roomfinder.ui.RoomListView
import com.example.roomfinder.ui.search.BuildingSelector
import com.example.roomfinder.ui.search.RoomTypeSelector
import com.example.roomfinder.ui.search.SearchInput
import com.example.roomfinder.ui.theme.RoomfinderTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

const val DEST_SEARCH_INPUT = "searchInput"
const val DEST_BUILDING_SELECTOR = "buildingSelector"
const val DEST_ROOM_TYPE_SELECTOR = "roomTypeSelector"
const val DEST_ROOM_LIST = "roomList"

private val buildingNamePreference = stringPreferencesKey("building_name")
private val buildingIdPreference = stringPreferencesKey("building_id")
private val roomTypeNamePreference = stringPreferencesKey("room_type_name")
private val roomTypeIdPreference = stringPreferencesKey("room_type_id")

class MainActivity : ComponentActivity() {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    }

    private val viewModel: ViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val savedBuilding = readSelectedBuilding()
            if (savedBuilding.id != "") {
                viewModel.selectBuilding(savedBuilding)
            }
            val savedRoomType = readSelectedRoomType()
            if (savedRoomType.id != "") {
                viewModel.selectRoomType(savedRoomType)
            }
        }
        val selectedBuildingObserver = Observer<LSF.Building> {
            lifecycleScope.launch {
                saveSelectedBuilding(it)
            }
        }
        val selectedRoomTypeObserver = Observer<LSF.RoomType> {
            lifecycleScope.launch {
                saveSelectedRoomType(it)
            }
        }
        viewModel.selectedBuilding.observe(this, selectedBuildingObserver)
        viewModel.selectedRoomType.observe(this, selectedRoomTypeObserver)
        viewModel.loadBuildingList()
        viewModel.loadRoomTypeList()
        viewModel.checkSearchEnabled()
        val roomListObserver = Observer<List<LSF.Room>> {
            viewModel.loadEvents()
        }
        viewModel.roomList.observe(this, roomListObserver)

        setContent {
            val navController = rememberNavController()
            RoomfinderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = DEST_SEARCH_INPUT) {
                        composable(DEST_SEARCH_INPUT) { SearchInput(viewModel, navController) }
                        composable(DEST_BUILDING_SELECTOR) {
                            BuildingSelector(
                                viewModel,
                                navController
                            )
                        }
                        composable(DEST_ROOM_TYPE_SELECTOR) {
                            RoomTypeSelector(
                                viewModel,
                                navController
                            )
                        }
                        composable(DEST_ROOM_LIST) {
                            RoomListView(viewModel)
                        }
                    }
                }
            }
        }
    }

    suspend fun readSelectedBuilding(): LSF.Building {
        val nameDataFlow: Flow<String> = applicationContext.dataStore.data.map { preferences ->
            preferences[buildingNamePreference] ?: ""
        }
        val idDataFlow: Flow<String> = applicationContext.dataStore.data.map { preferences ->
            preferences[buildingIdPreference] ?: ""
        }
        return LSF.Building(idDataFlow.firstOrNull() ?: "", nameDataFlow.firstOrNull() ?: "")
    }

    suspend fun readSelectedRoomType(): LSF.RoomType {
        val nameDataFlow: Flow<String> = applicationContext.dataStore.data.map { preferences ->
            preferences[roomTypeNamePreference] ?: ""
        }
        val idDataFlow: Flow<String> = applicationContext.dataStore.data.map { preferences ->
            preferences[roomTypeIdPreference] ?: ""
        }
        return LSF.RoomType(idDataFlow.firstOrNull() ?: "", nameDataFlow.firstOrNull() ?: "")
    }

    suspend fun saveSelectedBuilding(building: LSF.Building) {
        applicationContext.dataStore.edit { settings ->
            settings[buildingNamePreference] = building.name
            settings[buildingIdPreference] = building.id
        }
    }

    suspend fun saveSelectedRoomType(roomType: LSF.RoomType) {
        applicationContext.dataStore.edit { settings ->
            settings[roomTypeNamePreference] = roomType.name
            settings[roomTypeIdPreference] = roomType.id
        }
    }
}