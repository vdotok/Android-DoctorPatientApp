package com.vdotok.connectApp.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.vdotok.connect.models.MediaType
import com.vdotok.connectApp.utils.ApplicationConstants.AUDIO_DIRECTORY
import com.vdotok.connectApp.utils.ApplicationConstants.DOCS_DIRECTORY
import com.vdotok.connectApp.utils.ApplicationConstants.IMAGES_DIRECTORY
import com.vdotok.connectApp.utils.ApplicationConstants.VIDEO_DIRECTORY
import com.vdotok.connectApp.utils.ImageUtils.copyFileToInternalStorage
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths


/**
 * Created By: VdoTok
 * Date & Time: On 1/20/21 At 3:31 PM in 2021
 *
 * File Utils class to write file information related functions
 * such as filePath, file conversion, temp file creation etc
 */

/**
 * Function to get the real path of a file using URI
 * @return Returns a string path of the file
 * */
fun getRealPathFromURI(context: Context?, contentUri: Uri?): String? {
    var cursor: Cursor? = null
    return try {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context?.contentResolver?.query(contentUri!!, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        cursor.getString(column_index)
    } catch (e: java.lang.Exception) {
        Log.i("path", "getRealPathFromURI Exception : $e")
        ""
    } finally {
        cursor?.close()
    }
}

fun converFileToByteArray(path: String?): ByteArray? {
    val fis = FileInputStream(path)
    val bos = ByteArrayOutputStream()
    val b = ByteArray(1024)
    var readNum: Int
    while (fis.read(b).also { readNum = it } != -1) {
        bos.write(b, 0, readNum)
    }
    return bos.toByteArray()
}

/**
 * Function to convert File to ByteArray and requires min Android.O
 * @return Returns a ByteArray of the file
 * */
@RequiresApi(Build.VERSION_CODES.O)
fun convertFileToBytes(context: Context?, fileUri: Uri?): ByteArray? {

    // file to byte[], Path
    val bytes = Files.readAllBytes(Paths.get(getRealPathFromURI(context, fileUri)))
    Log.e("ByteTest", "onImageSelectedFromGallery: $bytes")
    return bytes

    // file to byte[], File -> Path

    // file to byte[], File -> Path
//        val file = File(filePath)
//        val bytes = Files.readAllBytes(file.toPath())

}

/**
 * Function to get file data
 * @return Returns a file
 * */
fun getFileData(context: Context?, uri: Uri?, type: MediaType): File? {
    return uri?.let { context?.let { it1 -> getPathFromInputStreamUri(it1, it, type) } }
}

/**
 * Function to get a file using URI
 * @return Returns a file
 * */
fun getPathFromInputStreamUri(context: Context, uri: Uri, type: MediaType): File? {
    var inputStream: InputStream? = null
    var photoFile: File? = null
    var extFile: File? = null
    uri.authority?.let {
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            extFile = File(copyFileToInternalStorage(context, uri, "temp"))
            photoFile = createTemporalFileFrom(inputStream, type, extFile?.extension!!)
        } catch (ignored: IOException) {
            ignored.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return photoFile
}




/**
 * Function to create a temp file in external storage
 * @return Returns a file
 * */
@Throws(IOException::class)
private fun createTemporalFileFrom(inputStream: InputStream?, type: MediaType, extension: String): File? {
    var targetFile: File? = null
    if (inputStream != null) {
        var read: Int
        val buffer = ByteArray(8 * 1024)
        targetFile = getImageFile(type, extension)
        val outputStream: OutputStream = FileOutputStream(targetFile)
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        try {
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return targetFile
}

/**
 * Function to get a temp file name
 * @return Returns a file
 * */
fun getImageFile(type: MediaType, extension: String): File {
    val childName: String? = System.currentTimeMillis().toString() + ".$extension"
//    val childName: String? = when (type) {
//        MediaType.IMAGE -> System.currentTimeMillis().toString() + ".jpg"
//        MediaType.VIDEO -> System.currentTimeMillis().toString() + ".mp4"
//        MediaType.AUDIO -> System.currentTimeMillis().toString() + ".mp3"
//        MediaType.FILE -> System.currentTimeMillis().toString() + ".pdf"
//        else -> System.currentTimeMillis().toString() + ".jpg"
//    }
    return File(
        createAppDirectory(type.value) + "/",
        childName
    )
}

/**
 * Function to create a directory in external storage
 * @return Returns a String of absolute path of the created directory
 * */
fun createAppDirectory(type: Int): String? {
    val dir: File? = when (type) {
        MediaType.IMAGE.value -> File(Environment.getExternalStorageDirectory().absolutePath + IMAGES_DIRECTORY)
        MediaType.VIDEO.value -> File(Environment.getExternalStorageDirectory().absolutePath + VIDEO_DIRECTORY)
        MediaType.AUDIO.value -> File(Environment.getExternalStorageDirectory().absolutePath + AUDIO_DIRECTORY)
        MediaType.FILE.value -> File(Environment.getExternalStorageDirectory().absolutePath + DOCS_DIRECTORY)
        else -> File(Environment.getExternalStorageDirectory().absolutePath + IMAGES_DIRECTORY)
    }
    if (dir != null) {
        if (!dir.exists()) dir.mkdirs()
    }
    return dir?.absolutePath
}

/**
 * Function to save Image on External storage
 * @param filePath String file path of the image
 * @param fileData ByteArray of the file after reading
 * @return Returns a File after storing to the external location
 * */

fun saveFileDataOnExternalData(filePath: String, fileData: ByteArray?): File? {
    try {
        val f = File(filePath)
        if (f.exists()) f.delete()
        f.createNewFile()
        val fos = FileOutputStream(f)
        fos.write(fileData)
        fos.flush()
        fos.close()
        return f
        // File Saved
    } catch (e: FileNotFoundException) {
        println("FileNotFoundException")
        e.printStackTrace()
    } catch (e: IOException) {
        println("IOException")
        e.printStackTrace()
    }
    return null
    // File Not Saved
}




/////

fun getPath(context: Context, uri: Uri): String? {
    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            }

            // TODO handle non-primary volumes
        } else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            else{
                contentUri =  MediaStore.Files.getContentUri("external")
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(
                split[1]
            )
            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {

        // Return the remote address
        return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
            context,
            uri,
            null,
            null
        )
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }
    return null
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri The Uri to query.
 * @param selection (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
fun getDataColumn(
    context: Context, uri: Uri?, selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )
    try {
        cursor = context.contentResolver.query(
            uri!!, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}


/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}
