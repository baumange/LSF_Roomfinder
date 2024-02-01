package com.example.roomfinder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ViewModel"

class ViewModel(application: Application) : AndroidViewModel(application) {

    private val networkDispatcher = Dispatchers.IO

    private val lsf = LSF()

    private val _buildingList : MutableLiveData<List<LSF.Building>> = MutableLiveData(listOf())
    val buildingList : LiveData<List<LSF.Building>> = _buildingList

    private val _roomTypeList : MutableLiveData<List<LSF.RoomType>> = MutableLiveData(listOf())
    val roomTypeList : LiveData<List<LSF.RoomType>> = _roomTypeList

    private val _roomList : MutableLiveData<List<LSF.Room>> = MutableLiveData(listOf())
    val roomList : LiveData<List<LSF.Room>> = _roomList

    private val _selectedBuilding : MutableLiveData<LSF.Building> = MutableLiveData()
    val selectedBuilding : LiveData<LSF.Building> = _selectedBuilding

    private val _selectedRoomType : MutableLiveData<LSF.RoomType> = MutableLiveData()
    val selectedRoomType : LiveData<LSF.RoomType> = _selectedRoomType

    private val _searchEnabled : MutableLiveData<Boolean> = MutableLiveData()
    val searchEnabled : LiveData<Boolean> = _searchEnabled

    fun selectBuilding(building: LSF.Building) {
        _selectedBuilding.value = building
        checkSearchEnabled()
    }

    fun selectRoomType(roomType: LSF.RoomType) {
        _selectedRoomType.value = roomType
        checkSearchEnabled()
    }

    fun checkSearchEnabled() {
        _searchEnabled.value = selectedBuilding.value != null && selectedRoomType.value != null
    }

    fun loadBuildingList() {
        viewModelScope.launch {
            withContext(networkDispatcher) {
                _buildingList.postValue(lsf.getBuildings())
            }
        }
    }

    fun loadRoomTypeList() {
        viewModelScope.launch {
            withContext(networkDispatcher) {
                _roomTypeList.postValue(lsf.getRoomTypes())
            }
        }
    }

    fun loadRooms(building: LSF.Building, type: LSF.RoomType) {
        viewModelScope.launch {
            withContext(networkDispatcher) {
                _roomList.postValue(lsf.getRooms(building, type))
            }
        }
    }

    fun loadEvents(room: LSF.Room) {
        viewModelScope.launch {
            withContext(networkDispatcher) {
                lsf.getRoomPlan(room)
            }
        }
    }

    fun loadEvents() {
        _roomList.value?.forEach {
            loadEvents(it)
        }
    }
}