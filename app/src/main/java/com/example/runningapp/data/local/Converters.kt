package com.example.runningapp.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

object Converters {

    @TypeConverter
    fun Bitmap.convertToByteArray(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun ByteArray.convertToBitmap(): Bitmap =
        BitmapFactory.decodeByteArray(this, 0, this.size)

}
