package com.sensordatalabeler.file

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.sensordatalabeler.MeasurementValues
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths

object WriteFile {

        /**
         * Write META data.
         */
        fun writeMetaData(applicationContext: Context) {
            val metadata =
                "[NAME, DATA, ACCELERATION(x,y,z), HEART RATE, GYRO(x,y,z), STEPS, (LATITUDE,LONGITUDE)]"
            val metaFileName = "metadata.txt"
            val os = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val dir = File(PATH_TO_DATA)
                if (!dir.exists())
                    dir.mkdirs()
                val file = File(dir, metaFileName)
                if (!file.exists())
                    file.createNewFile()
                FileOutputStream(file)
            } else {
                applicationContext.openFileOutput(metaFileName, FragmentActivity.MODE_PRIVATE)
            }

            val bw = BufferedWriter(OutputStreamWriter(os))
            bw.write(metadata)
            bw.close()

        }

        fun writeZip(filesDir: File) {
            try {
                ZipData.zip(File(PATH_TO_DATA, "Data.zip"), File(PATH_TO_DATA))
                ZipData.cleanUp(File(PATH_TO_DATA))
            } catch (e: IllegalStateException) {

                ZipData.zip(File(filesDir, "Data.zip"), filesDir)
                ZipData.cleanUp(filesDir)
            }
        }

        /**
         * Opens the file to write.
         */
        fun openFile(measurementValues: MeasurementValues, applicationContext: Context) {
            Log.d(TAG, "File opened")
            val time = measurementValues.getTime()
            val os = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val dir = File(applicationContext.getExternalFilesDir(null), "/Data")
                Log.d(TAG, "Path: ${dir.path}")
                Log.d(TAG, "Dir exists: ${dir.exists()}")
                if (!dir.exists())
                    Files.createDirectories(Paths.get(dir.path))
                Log.d(TAG, "Dir exists: ${dir.exists()}")
                val file = File(dir, fileName + time + fileType)
                if (!file.exists())
                    file.createNewFile()
                Log.d(TAG, "Write in sdcard")
                FileOutputStream(file)
            } else {
                applicationContext.openFileOutput(fileName, FragmentActivity.MODE_PRIVATE)
            }
            bufferedWriter = BufferedWriter(OutputStreamWriter(os))

        }

        /**
         * Write data in the file
         */
        fun saveData(measurementValues: MeasurementValues) {
            val nonEmptyData = measurementValues.getData().filter { it.isNotEmpty() }

            if (bufferedWriter == null) {
                Log.e(TAG, "BufferedWriter is null, data cannot be saved.")
                return
            }

            for (data in nonEmptyData) {
                bufferedWriter!!.write(data)
                bufferedWriter!!.newLine()
                Log.d(TAG, "DATA: $data")
            }
            measurementValues.resetData()
            if (!measurementValues.getActiveMeasurement())
                closeFile()
        }

        /**
         * Closed the file
         */
        private fun closeFile() {
            Log.d(TAG, "File Closed")
            bufferedWriter?.close()
        }

        // File
        private var fileName = "data"
        private var fileType = ".csv"
        private var bufferedWriter: BufferedWriter? = null
        private const val TAG = "WriteFile"
        private val PATH_TO_DATA =
            "${Environment.getExternalStorageDirectory()}/Android/data/com.sensordatalabeler/files/Data"
}
