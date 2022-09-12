package com.raveline.petinlove.data.database.converter

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): Timestamp {
        val timestamp = object : TypeToken<Timestamp>() {}.type
        return gson.fromJson(value, timestamp)
    }

    @TypeConverter
    fun stringToTimeStamp(value: Timestamp): String {
        return gson.toJson(value)
    }

}