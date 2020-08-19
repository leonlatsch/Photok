package dev.leonlatsch.photok.ui.importing

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.launch
import java.util.*

class ImportViewModel @ViewModelInject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    var importState: MutableLiveData<ImportState> = MutableLiveData()

    fun importImages(context: Context, uris: List<Uri>) = viewModelScope.launch {
        importState.postValue(ImportState.IMPORTING)

        for (image in uris) {

            // Load Bytes
            val imgBytes = load(context.contentResolver, image)
            if (imgBytes == null) {
                importState.postValue(ImportState.FAILED)
                return@launch
            }

            // Encrypt Bytes
            val encryptedImgBytes = encrypt(imgBytes)

            //SAVE
            //INSERT
        }

        importState.postValue(ImportState.FINISHED)
    }

    private fun load(contentResolver: ContentResolver, imageUri: Uri): ByteArray? {
        return contentResolver.openInputStream(imageUri)?.readBytes()
    }

    private fun encrypt(imgBytes: ByteArray): ByteArray {
        // TODO
        return imgBytes
    }

    private fun save(context: Context, encryptedImgBytes: ByteArray): Photo? {
        val id = UUID.randomUUID().toString()

        val files = context.getExternalFilesDir(null)?.absolutePath
        // SAVE

        return Photo(id, "PLACE URI", System.currentTimeMillis())
    }
/*

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }*/
}