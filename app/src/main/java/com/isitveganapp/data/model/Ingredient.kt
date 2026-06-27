package com.isitveganapp.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "ingredients",
    indices = [Index(value = ["normalized_name"], unique = true)]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String,
    @ColumnInfo(name = "aliases") val aliases: String,
    @ColumnInfo(name = "vegan_status") val veganStatus: VeganStatus,
    @ColumnInfo(name = "reason") val reason: String
) : Parcelable
