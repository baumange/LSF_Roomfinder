package com.example.roomfinder.ui

import android.content.res.Resources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.example.roomfinder.LSF
import com.example.roomfinder.ViewModel
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

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
    val roomTextWidth = 100.dp
    val hourWidth = 90.dp

    val time = GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY)
    val timeDp =
        hourWidth * time[GregorianCalendar.HOUR_OF_DAY] + hourWidth / 60 * time[GregorianCalendar.MINUTE]
    val rowScrollState =
        rememberScrollState(((timeDp - hourWidth).value * Resources.getSystem().displayMetrics.density).toInt())

    Column {
        TimeRow(roomTextWidth = roomTextWidth, rowScrollState = rowScrollState, hourWidth = hourWidth)
        RoomList(roomList = roomList, roomTextWidth = roomTextWidth, rowScrollState = rowScrollState, time = time, hourWidth = hourWidth)
    }
}

/**
 * The RoomList root View
 *
 * @param viewModel The ViewModel containing the Rooms
 */
@Composable
fun RoomListView(viewModel: ViewModel) {
    val roomList by viewModel.roomList.observeAsState(listOf())
    val roomTextWidth = 100.dp
    val hourWidth = 90.dp

    val time = GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY)
    val timeDp =
        hourWidth * time[GregorianCalendar.HOUR_OF_DAY] + hourWidth / 60 * time[GregorianCalendar.MINUTE]
    val rowScrollState =
        rememberScrollState(((timeDp - hourWidth).value * Resources.getSystem().displayMetrics.density).toInt())

    Column {
        TimeRow(roomTextWidth = roomTextWidth, rowScrollState = rowScrollState, hourWidth = hourWidth)
        RoomList(roomList = roomList, roomTextWidth = roomTextWidth, rowScrollState = rowScrollState, time = time, hourWidth = hourWidth)
    }
}

/**
 * The List of Rooms with their Events
 *
 * @param roomList The Rooms to show
 * @param roomTextWidth The width of the Bar showing the Room names
 * @param rowScrollState The ScrollState given to each row
 * @param time The time on which to display a red indicator
 * @param hourWidth The width of one hour
 */
@Composable
fun RoomList(roomList: List<LSF.Room>, roomTextWidth: Dp, rowScrollState: ScrollState, time: GregorianCalendar, hourWidth: Dp) {
    val rowHeight = 60.dp

    LazyColumn {
        items(roomList) {
            Row {
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
                Row (modifier = Modifier.horizontalScroll(rowScrollState)) {
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

/**
 * The Text on the Time markings
 *
 * @param roomTextWidth The width of the Bar showing the Room names
 * @param rowScrollState The ScrollState given to the text row
 * @param hourWidth The width of one hour
 */
@Composable
fun TimeRow(roomTextWidth: Dp, rowScrollState: ScrollState, hourWidth: Dp) {
    val spacerModifier = Modifier.width(roomTextWidth)
    val timeRowModifier = Modifier.horizontalScroll(rowScrollState, false)
    val boxModifier = Modifier.width(hourWidth)

    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = spacerModifier)
        Row(modifier = timeRowModifier) {
            Spacer(modifier = Modifier.width(hourWidth))
            for (i in 1..23) {
                Box(modifier = boxModifier) {
                    Text(
                        text = "$i:00",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    )
                }
            }
        }

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

/**
 * A Row with the name of a Room and all Events on the day given
 *
 * @param room The Room to display
 * @param time The day to display and the time on which to display a red line
 * @param hourWidth The width of one hour
 * @param rowHeight The height of the Row
 */
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

    Box(modifier = boxModifier) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = rowInBoxModifier) {
            for (i in 1..23) {
                VerticalDivider(modifier = dividerModifier, thickness = 1.dp)
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
            Card(modifier = cardModifier, colors = cardColors, border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer)) {
                Text(text = it.title, modifier = Modifier.padding(8.dp, 6.dp, 6.dp, 4.dp), fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
        val nowDividerModifier = Modifier
            .fillMaxHeight()
        Row {
            Spacer(modifier = Modifier.width(timeDp))
            VerticalDivider(modifier = nowDividerModifier, thickness = 1.dp, color = Color.Red)
        }

    }
}
