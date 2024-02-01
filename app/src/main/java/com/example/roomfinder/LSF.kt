package com.example.roomfinder

import android.text.Html
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

private const val TAG = "LSF"

class LSF {
    private val buildingListUrl = "https://lsf.uni-heidelberg.de/qisserver/rds?state=change&type=6&moduleParameter=raumSelectGeb&next=SearchSelect.vm&subdir=raum"
    private val roomSearchUrl = "https://lsf.uni-heidelberg.de/qisserver/rds?state=change&type=5&moduleParameter=raumSearch&next=search.vm&subdir=raum"
    private val roomListUrl = "https://lsf.uni-heidelberg.de/qisserver/rds?state=change&type=6&moduleParameter=raumSelect&next=SearchSelect.vm&subdir=raum&raum.raumartid="
    private val roomPlanUrl = "https://lsf.uni-heidelberg.de/qisserver/rds?state=wplan&act=Raum&pool=Raum&raum.rgid="


    fun getBuildings() : List<Building> {
        val doc = Jsoup.connect(buildingListUrl).get()
        val buildingElements = doc.getElementsByAttributeValueContaining("href", "raum.gebid")
        val list : MutableList<Building> = mutableListOf()
        buildingElements.forEach { element ->
            val name = element.text()
            val href = element.attr("href")
            val arguments = href.split("?")[1].split("&")
            arguments.forEach {
                val pair = it.split("=")
                if (pair[0].contains("raum.gebid")) {
                    list.add(Building(pair[1], name))
                }
            }
        }
        return list
    }

    fun getRoomTypes() : List<RoomType> {
        val doc = Jsoup.connect(roomSearchUrl).get()
        val raumArtSelect = doc.getElementById("raum.raumartid")
        val list : MutableList<RoomType> = mutableListOf()
        raumArtSelect?.children()?.forEach {
            list.add(RoomType(it.attr("value"), it.text()))
        }
        return list
    }

    fun getRooms(building: Building = Building("", ""), type : RoomType = RoomType("", "")) : List<Room> {
        val doc = Jsoup.connect("$roomListUrl${type.id}&raum.gebid=${building.id}").get()
        val roomElements = doc.getElementsByAttributeValueContaining("href", "raum.rgid")
        val list : MutableList<Room> = mutableListOf()
        roomElements.forEach { element ->
            val nameAndBuilding = element.text().split("_____")[0].split("/")
            val name = if (nameAndBuilding.size > 1) {
                nameAndBuilding[1].trim()
            } else {
                nameAndBuilding[0]
            }
            val href = element.attr("href")
            val arguments = href.split("?")[1].split("&")
            arguments.forEach {
                val pair = it.split("=")
                if (pair[0].contains("raum.rgid")) {
                    list.add(Room(pair[1], name, MutableLiveData(listOf())))
                }
            }
        }
        return list
    }

    fun getRoomPlan(room: Room) {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        Log.d(TAG, "Loading events of ${room.name}")
        val doc = Jsoup.connect("$roomPlanUrl${room.id}").get()
        val planEntryLinks = doc.getElementsByAttributeValueContaining("href", "publishid")
        val eventList : MutableList<Event> = mutableListOf()
        planEntryLinks.forEach {planEntryLink ->
            val title = Html.fromHtml(planEntryLink.attr("title"), Html.FROM_HTML_MODE_COMPACT).toString()
            var timeString = ""
            val planEntry = planEntryLink.parent()?.parent()?.siblingElements()
            planEntry?.let {
                if (it.isNotEmpty()) {
                    timeString = it[0].text()
                }
            }
            val timeStringSplitted = timeString.split(",").map { it.trim() }
            val weekDayNumber = when (timeStringSplitted[0]) {
                "Montag" -> GregorianCalendar.MONDAY
                "Dienstag" -> GregorianCalendar.TUESDAY
                "Mittwoch" -> GregorianCalendar.WEDNESDAY
                "Donnerstag" -> GregorianCalendar.THURSDAY
                "Freitag" -> GregorianCalendar.FRIDAY
                else -> GregorianCalendar.SUNDAY
            }
            val startEndTime = timeStringSplitted[1].split("-").map { it.trim() }
            val startTime = GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY)
            val endTime = GregorianCalendar(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY)
            startTime[GregorianCalendar.DAY_OF_WEEK] = weekDayNumber
            endTime[GregorianCalendar.DAY_OF_WEEK] = weekDayNumber
            val startHourMinutes = startEndTime[0].split(":")
            val endHourMinutes = startEndTime[1].split(":")
            startTime[GregorianCalendar.HOUR_OF_DAY] = startHourMinutes[0].toInt()
            startTime[GregorianCalendar.MINUTE] = startHourMinutes[1].toInt()
            startTime[GregorianCalendar.SECOND] = 0
            endTime[GregorianCalendar.HOUR_OF_DAY] = endHourMinutes[0].toInt()
            endTime[GregorianCalendar.MINUTE] = endHourMinutes[1].toInt()
            endTime[GregorianCalendar.SECOND] = 0

            val event = Event(title, startTime, endTime)
            eventList.add(event)
            Log.d(TAG, event.title + ", " + fmt.format(event.startTime.time) + ", " + event.startTime.timeZone.getDisplayName(false, TimeZone.SHORT))
        }
        room.events.postValue(eventList)
    }

    data class Building (
        val id : String,
        val name : String
    )

    data class RoomType (
        val id : String,
        val name : String
    )

    data class Room (
        val id : String,
        val name : String,
        var events : MutableLiveData<List<Event>>
    )

    data class Event (
        val title : String,
        val startTime : GregorianCalendar,
        val endTime : GregorianCalendar
    )
}