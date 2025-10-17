package services.storage

import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import models.SignedUrlReq
import models.SignedUrlRes
import java.util.concurrent.TimeUnit


interface StorageService {
    suspend fun createV4SignedUrl(req: SignedUrlReq): SignedUrlRes
}


class GcsStorageService(
    private val bucket: String,
    private val projectId: String
) : StorageService {

    private val storage: Storage = StorageOptions.getDefaultInstance().service

    override suspend fun createV4SignedUrl(req: SignedUrlReq): SignedUrlRes {
        val blobInfo = com.google.cloud.storage.BlobInfo
            .newBuilder(bucket, req.fileName)
            .setContentType(req.contentType)
            .build()

        val uploadUrl = storage.signUrl(
            blobInfo,
            15, TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withV4Signature(),
            Storage.SignUrlOption.withContentType()
        ).toString()

        val publicUrl = "https://storage.googleapis.com/$bucket/${req.fileName}"
        return SignedUrlRes(uploadUrl, publicUrl)
    }
}