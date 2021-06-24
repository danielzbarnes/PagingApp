package com.danielzbarnes.pagingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// this class keeps track of the keys
@Entity(tableName = "remote_keys")
data class RemoteKeys(
        @PrimaryKey val repoId: Long,
        val prevKey: Int?,
        val nextKey: Int?)