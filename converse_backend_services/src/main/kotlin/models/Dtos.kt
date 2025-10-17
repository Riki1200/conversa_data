package models

import kotlinx.serialization.Serializable

@Serializable data class SignedUrlReq(val fileName: String, val contentType: String)
@Serializable data class SignedUrlRes(val uploadUrl: String, val publicUrl: String)

@Serializable data class IngestReq(val gcsPath: String? = null, val fileUrl: String? = null, val title: String? = null)
@Serializable data class IngestRes(val indexed: Int)

@Serializable data class QueryReq(val query: String, val topK: Int = 3)

@Serializable data class Hit(val title: String, val snippet: String, val url: String, val score: Double)
@Serializable data class SearchRes(val results: List<Hit>)

@Serializable data class Citation(val title: String, val url: String, val snippet: String)
@Serializable data class AnswerRes(val answer: String, val citations: List<Citation>)