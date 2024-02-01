package com.example.roomfinder

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roomfinder.ui.theme.RoomfinderTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

private const val DEST_SEARCH_INPUT = "searchInput"
private const val DEST_BUILDING_SELECTOR = "buildingSelector"
private const val DEST_ROOM_TYPE_SELECTOR = "roomTypeSelector"
private const val DEST_ROOM_LIST = "roomList"
private const val TAG = "MainActivity"

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

@Composable
fun BuildingSelector(viewModel: ViewModel, navController: NavController) {
    val buildingList by viewModel.buildingList.observeAsState(listOf())
    LazyColumn(Modifier.fillMaxSize()) {
        items(buildingList) {
            BuildingSelectorListItem(it, viewModel, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@Composable
fun RoomTypeSelector(viewModel: ViewModel, navController: NavController) {
    val roomTypeList by viewModel.roomTypeList.observeAsState(listOf())
    LazyColumn(Modifier.fillMaxSize()) {
        items(roomTypeList) {
            RoomTypeSelectorListItem(it, viewModel, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@Preview(showBackground = true)
@Composable
fun RoomListViewPreview() {
    val roomList = mutableListOf<LSF.Room>()
    for (i in 1..10) {
        val events = mutableListOf<LSF.Event>()
        val now = GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY)
        events.add(
            LSF.Event(
                "Event",
                GregorianCalendar(
                    now[GregorianCalendar.YEAR],
                    now[GregorianCalendar.MONTH],
                    now[GregorianCalendar.DAY_OF_MONTH],
                    8 + i,
                    0,
                    0
                ),
                GregorianCalendar(
                    now[GregorianCalendar.YEAR],
                    now[GregorianCalendar.MONTH],
                    now[GregorianCalendar.DAY_OF_MONTH],
                    10 + i,
                    0,
                    0
                )
            )
        )
        events.add(
            LSF.Event(
                "Event",
                GregorianCalendar(
                    now[GregorianCalendar.YEAR],
                    now[GregorianCalendar.MONTH],
                    now[GregorianCalendar.DAY_OF_MONTH],
                    11 + i,
                    0,
                    0
                ),
                GregorianCalendar(
                    now[GregorianCalendar.YEAR],
                    now[GregorianCalendar.MONTH],
                    now[GregorianCalendar.DAY_OF_MONTH],
                    13 + i,
                    0,
                    0
                )
            )
        )
        roomList.add(LSF.Room("", "SR $i", MutableLiveData(events)))
    }
    roomList.add(LSF.Room("", "SR Statistik", MutableLiveData(listOf())))
    RoomList(roomList)
}

@Composable
fun RoomListView(viewModel: ViewModel) {
    val roomList by viewModel.roomList.observeAsState(listOf())
    RoomList(roomList)
}

@Composable
fun RoomList(roomList: List<LSF.Room>) {
    val roomTextWidth = 100.dp
    val hourWidth = 90.dp
    val rowHeight = 60.dp

    val time = GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY)
    val timeDp =
        hourWidth * time[GregorianCalendar.HOUR_OF_DAY] + hourWidth / 60 * time[GregorianCalendar.MINUTE]

    val rowScrollState =
        rememberScrollState(((timeDp - hourWidth).value * Resources.getSystem().displayMetrics.density).toInt())

    val timeRowModifier = Modifier.horizontalScroll(rowScrollState, false)
    val spacerModifier = Modifier.width(roomTextWidth)
    val boxModifier = Modifier.width(hourWidth * 24)

    val nameListState = rememberLazyListState()
    val eventListState = rememberLazyListState()

    LaunchedEffect(nameListState.isScrollInProgress) {
        if (!eventListState.isScrollInProgress && !nameListState.isScrollInProgress)
        eventListState.scrollToItem(nameListState.firstVisibleItemIndex, nameListState.firstVisibleItemScrollOffset)
    }
    LaunchedEffect(eventListState.isScrollInProgress) {
        if (! nameListState.isScrollInProgress && !eventListState.isScrollInProgress)
        nameListState.scrollToItem(eventListState.firstVisibleItemIndex, eventListState.firstVisibleItemScrollOffset)
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = spacerModifier)
            Row(modifier = timeRowModifier) {
                Box(modifier = boxModifier) {
                    for (i in 1..23) {
                        Text(
                            text = "$i:00",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = hourWidth * i)
                        )
                    }
                }
            }

        }
        Row {
            LazyColumn(modifier = Modifier.width(roomTextWidth), state = nameListState) {
                items(roomList) {
                    Box(
                        modifier = Modifier
                            .width(roomTextWidth)
                            .height(rowHeight)
                    ) {
                        val roomNameModifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                        Text(text = it.name, modifier = roomNameModifier)
                    }
                }
            }
            LazyColumn(modifier = Modifier.horizontalScroll(rowScrollState), state = eventListState) {
                items(roomList) {
                    RoomEventBar(
                        room = it,
                        time = time,
                        hourWidth = hourWidth,
                        rowHeight = rowHeight
                    )
                }
            }
        }
    }
}

@Composable
fun RoomListItem(
    room: LSF.Room,
    time: GregorianCalendar,
    textWidth: Dp,
    hourWidth: Dp,
    rowHeight: Dp
) {
    val roomNameModifier = Modifier.width(textWidth)
    val events by room.events.observeAsState(listOf())
    val todayEvents = events.filter {
        it.startTime[GregorianCalendar.DAY_OF_WEEK] == time[GregorianCalendar.DAY_OF_WEEK]
    }
    val roomToday = LSF.Room(room.id, room.name, MutableLiveData(todayEvents))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = room.name, modifier = roomNameModifier)
        RoomEventBar(room = roomToday, time = time, hourWidth = hourWidth, rowHeight = rowHeight)
    }
}

@Preview(showBackground = true)
@Composable
fun RoomEventBarPreview() {
    val events = mutableListOf<LSF.Event>()
    events.add(
        LSF.Event(
            "Event",
            GregorianCalendar(0, 1, 1, 8, 0, 0),
            GregorianCalendar(0, 1, 1, 10, 0, 0)
        )
    )
    events.add(
        LSF.Event(
            "Event",
            GregorianCalendar(0, 1, 1, 11, 0, 0),
            GregorianCalendar(0, 1, 1, 13, 0, 0)
        )
    )
    val room = LSF.Room("", "", MutableLiveData(events))
    val time = GregorianCalendar(0, 1, 1, 9, 0)
    val hourWidth = 60.dp
    val rowHeight = 50.dp
    RoomEventBar(room = room, time = time, hourWidth = hourWidth, rowHeight = rowHeight)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomEventBar(room: LSF.Room, time: GregorianCalendar, hourWidth: Dp, rowHeight: Dp) {
    val events by room.events.observeAsState(listOf())
    val todayEvents = events.filter {
        it.startTime[GregorianCalendar.DAY_OF_WEEK] == time[GregorianCalendar.DAY_OF_WEEK]
    }

    val timeDp =
        hourWidth * time[GregorianCalendar.HOUR_OF_DAY] + hourWidth / 60 * time[GregorianCalendar.MINUTE]

    val boxModifier = Modifier
        .width(hourWidth * 24)
        .height(rowHeight)
    val rowInBoxModifier = Modifier
        .fillMaxSize()
    val dividerModifier = Modifier
        .fillMaxHeight()
        .width(1.dp)

    Box(modifier = boxModifier) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = rowInBoxModifier) {
            for (i in 1..23) {
                Divider(modifier = dividerModifier)
            }
        }
        todayEvents.forEach {
            val startDp =
                hourWidth * it.startTime[GregorianCalendar.HOUR_OF_DAY] + hourWidth * it.startTime[GregorianCalendar.MINUTE] / 60
            val width =
                hourWidth * it.endTime[GregorianCalendar.HOUR_OF_DAY] + hourWidth * it.endTime[GregorianCalendar.MINUTE] / 60 - startDp
            val cardModifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = startDp)
                .fillMaxHeight()
                .width(width)
            val cardColors =
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            PlainTooltipBox(tooltip = { Text(text = it.title) }) {
                Card(modifier = cardModifier.then(Modifier.tooltipAnchor()), colors = cardColors, border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(text = it.title, modifier = Modifier.padding(8.dp, 6.dp, 6.dp, 4.dp), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

        }
        val nowDividerModifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
        Row {
            Spacer(modifier = Modifier.width(timeDp))
            Divider(modifier = nowDividerModifier, color = Color.Red)
        }

    }
}
