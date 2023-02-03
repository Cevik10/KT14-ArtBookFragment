package com.hakancevik.artbookfragment.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Blob

@Entity
class Art(

    @ColumnInfo(name = "artName")
    val artName: String,

    @ColumnInfo(name = "artistName")
    val artistName: String,

    @ColumnInfo(name = "year")
    val year: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray? = null


) : java.io.Serializable {

    @PrimaryKey(autoGenerate = true)
    var id = 0


}