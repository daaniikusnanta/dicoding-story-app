package com.daaniikusnanta.storyapp.misc

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import com.daaniikusnanta.storyapp.R
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFile
}

fun createFile(application: Application): File {
    val mediaDir = application.externalMediaDirs.firstOrNull()?.let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir

    return File(outputDirectory, "$timeStamp.jpg")
}

fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
    val matrix = Matrix()
    return if (isBackCamera) {
        matrix.postRotate(90f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > 1000000)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

fun getElapsedTimeString(startingTime: String, context: Context) : String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" , Locale.getDefault())
    val date = dateFormat.parse(startingTime)
    val elapsedTime = System.currentTimeMillis() - (date?.time ?: 0) - (1000 * 60 * 60 * 7)

    val seconds = elapsedTime / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = weeks / 4
    val years = months / 12

    val yearString = context.getString(R.string.year)
    val yearsString = context.getString(R.string.years)
    val monthString = context.getString(R.string.month)
    val monthsString = context.getString(R.string.months)
    val weekString = context.getString(R.string.week)
    val weeksString = context.getString(R.string.weeks)
    val dayString = context.getString(R.string.day)
    val daysString = context.getString(R.string.days)
    val hourString = context.getString(R.string.hour)
    val hoursString = context.getString(R.string.hours)
    val minuteString = context.getString(R.string.minute)
    val minutesString = context.getString(R.string.minutes)
    val secondString = context.getString(R.string.second)
    val secondsString = context.getString(R.string.seconds)
    val agoString = context.getString(R.string.ago)

    return when {
        years > 0 -> "$years ${if (years == 1L) yearString else yearsString} $agoString"
        months > 0 -> "$months ${if (months == 1L) monthString else monthsString} $agoString"
        weeks > 0 -> "$weeks ${if (weeks == 1L) weekString else weeksString} $agoString"
        days > 0 -> "$days ${if (days == 1L) dayString else daysString} $agoString"
        hours > 0 -> "$hours ${if (hours == 1L) hourString else hoursString} $agoString"
        minutes > 0 -> "$minutes ${if (minutes == 1L) minuteString else minutesString} $agoString"
        seconds > 0 -> "$seconds ${if (seconds == 1L) secondString else secondsString} $agoString"
        else -> context.getString(R.string.a_moment_ago)
    }
}