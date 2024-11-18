package com.sensordatalabeler.file

import java.io.File
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipData {
    /**
     * Create ZIP archive from file location.
     *
     * @param zipFile
     * @param location
     */
    fun zip(zipFile: File, location: File) {
        if (!location.exists() && !location.isDirectory)
            throw java.lang.IllegalStateException("Location must be directory and exist")

        val locationPath = location.absolutePath.let {
            if (!it.endsWith(File.separator)) "$it${File.separator}"
            else it
        }
        val filesToZip = mutableListOf<File>()

        Files.walk(Paths.get(locationPath))
            .filter { item -> item.toString().endsWith(".csv") || item.toString().endsWith(".txt") }
            .forEach { item -> filesToZip.add(File(item.toString())) }

        zip(zipFile, filesToZip)

    }

    private fun zip(zipFile: File, filesToZip: List<File>) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { outStream ->
            zip(outStream, filesToZip)
        }
    }

    private fun zip(outputStream: ZipOutputStream, filesToZip: List<File>) {
        filesToZip.forEach { file ->
            outputStream.putNextEntry(ZipEntry(file.name))
            BufferedInputStream(FileInputStream(file)).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /**
     * Clean up the files from location.
     *
     * @param location
     */
    fun cleanUp(location: File) {
        if (!location.exists() && !location.isDirectory)
            throw java.lang.IllegalStateException("Location must be directory and exist")

        val locationPath = location.absolutePath.let {
            if (!it.endsWith(File.separator)) "$it${File.separator}"
            else it
        }

        Files.walk(Paths.get(locationPath))
            .filter { item -> item.toString().endsWith(".csv") }
            .forEach { item -> item.toFile().delete() }
    }
}
