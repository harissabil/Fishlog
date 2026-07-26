package com.harissabil.fisch.feature.profile.domain.usecase

import com.google.firebase.storage.FirebaseStorage
import com.harissabil.fisch.core.common.util.Resource
import com.harissabil.fisch.core.firebase.firestore.data.dto.LogbookResponse
import com.harissabil.fisch.core.firebase.firestore.domain.usecase.GetLogbooks
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private const val MAX_PHOTO_DOWNLOAD_BYTES = 10L * 1024 * 1024

private val CSV_HEADER = listOf(
    "Date", "Species", "Count", "Location", "Weight (kg)", "Length (cm)", "Bait", "Released",
    "Notes", "Photo",
)

class ExportUserDataToZip @Inject constructor(
    private val getLogbooks: GetLogbooks,
) {
    suspend operator fun invoke(outputStream: OutputStream): Resource<Boolean> {
        return try {
            val logbooksResult = getLogbooks().first { it !is Resource.Loading<*> }
            val logbooks = (logbooksResult as? Resource.Success)?.data
                ?: return Resource.Error(logbooksResult.message ?: "Something went wrong!")

            ZipOutputStream(outputStream).use { zip ->
                zip.putNextEntry(ZipEntry("catches.csv"))
                zip.write(buildCsv(logbooks).toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                logbooks.forEach { logbook ->
                    val url = logbook.fotoIkan ?: return@forEach
                    val id = logbook.id ?: return@forEach
                    try {
                        val bytes = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(url)
                            .getBytes(MAX_PHOTO_DOWNLOAD_BYTES)
                            .await()
                        zip.putNextEntry(ZipEntry("photos/$id.jpg"))
                        zip.write(bytes)
                        zip.closeEntry()
                    } catch (e: Exception) {
                        Timber.tag("ExportUserData").e(e, "Failed to download photo for $id")
                    }
                }
            }

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Something went wrong!")
        }
    }

    private fun buildCsv(logbooks: List<LogbookResponse>): String {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

        val rows = logbooks.map { logbook ->
            listOf(
                logbook.waktuPenangkapan?.toDate()?.let { dateFormatter.format(it) } ?: "",
                logbook.jenisIkan ?: "",
                logbook.jumlahIkan?.toString() ?: "",
                logbook.tempatPenangkapan ?: "",
                logbook.beratIkan?.toString() ?: "",
                logbook.panjangIkan?.toString() ?: "",
                logbook.umpan ?: "",
                if (logbook.dilepaskan == true) "Yes" else "No",
                logbook.catatan ?: "",
                if (logbook.fotoIkan != null) "photos/${logbook.id}.jpg" else "",
            )
        }

        return buildString {
            appendLine(CSV_HEADER.joinToString(",") { it.escapeCsvField() })
            rows.forEach { row -> appendLine(row.joinToString(",") { it.escapeCsvField() }) }
        }
    }

    private fun String.escapeCsvField(): String {
        return if (any { it == ',' || it == '"' || it == '\n' }) {
            "\"${replace("\"", "\"\"")}\""
        } else {
            this
        }
    }
}
