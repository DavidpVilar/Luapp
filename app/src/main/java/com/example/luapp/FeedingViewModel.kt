package com.example.luapp

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FeedingViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("feedings_pref", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _feedings = mutableStateListOf<Feeding>()
    val feedings: List<Feeding> get() = _feedings

    init {
        loadFeedings()
    }

    fun addActivity(type: ActivityType, amountMl: Int = 0, timestamp: Long) {
        val nextId = (_feedings.maxOfOrNull { it.id } ?: 0) + 1
        _feedings.add(0, Feeding(nextId, type, amountMl, timestamp))
        saveFeedings()
    }

    fun updateActivity(id: Int, amountMl: Int, timestamp: Long) {
        val index = _feedings.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldActivity = _feedings[index]
            _feedings[index] = oldActivity.copy(amountMl = amountMl, timestamp = timestamp)
            saveFeedings()
        }
    }

    fun deleteActivity(id: Int) {
        _feedings.removeIf { it.id == id }
        saveFeedings()
    }

    fun addFeeding(amountMl: Int, timestamp: Long) {
        addActivity(ActivityType.FEEDING, amountMl, timestamp)
    }

    private fun saveFeedings() {
        val json = gson.toJson(_feedings.toList())
        sharedPreferences.edit().putString("feedings_list", json).apply()
    }

    private fun loadFeedings() {
        val json = sharedPreferences.getString("feedings_list", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Feeding>>() {}.type
            val list: List<Feeding> = gson.fromJson(json, type)
            _feedings.clear()
            _feedings.addAll(list)
        }
    }
}
