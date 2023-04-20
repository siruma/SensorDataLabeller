package com.sensordatalabeler

import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun zip(zipFile: File, location: File){
    if (!location.exists() && !location.isDirectory)
        throw java.lang.IllegalStateException("Location must be directory and exist")

    val locationPath = location.absolutePath.let {
        if( !it.endsWith(File.separator)) "$it${File.separator}"
        else it
    }
    var filesToZip = mutableListOf<File>()

    Files.walk(Paths.get(locationPath))
        .filter{item -> item.toString().endsWith(".csv") || item.toString().endsWith(".txt") }
        .forEach { item -> filesToZip.add(File(item.toString()))}

    zip(zipFile,filesToZip)

}

private fun zip(zipFile: File, filesToZip: List<File>) {
    ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { outStream ->
        zip(outStream,filesToZip)
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

fun cleanUp(location: File){
    if (!location.exists() && !location.isDirectory)
        throw java.lang.IllegalStateException("Location must be directory and exist")

    val locationPath = location.absolutePath.let {
        if( !it.endsWith(File.separator)) "$it${File.separator}"
        else it
    }

    Files.walk(Paths.get(locationPath))
        .filter { item -> item.toString().endsWith(".csv" )}
        .forEach { item -> item.toFile().delete() }
}
