package com.isitveganapp.data.local

import androidx.room.TypeConverter
import com.isitveganapp.data.model.VeganStatus

class VeganStatusConverter {
    @TypeConverter
    fun fromVeganStatus(status: VeganStatus): String = status.name

    @TypeConverter
    fun toVeganStatus(name: String): VeganStatus = VeganStatus.valueOf(name)
}
