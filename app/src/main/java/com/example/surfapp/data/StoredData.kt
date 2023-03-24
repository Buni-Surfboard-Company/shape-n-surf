package com.example.surfapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StoredData(
    @PrimaryKey
    val coordinate: String,
    val timeStamp: Long
) : java.io.Serializable
