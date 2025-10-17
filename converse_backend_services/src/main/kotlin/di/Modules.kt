package di

import io.ktor.client.HttpClient
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.koin.dsl.module
import services.embeddings.EmbeddingsService
import services.embeddings.VertexEmbeddingsService
import services.ingest.IngestService
import services.ingest.IngestServiceImpl
import services.llm.GeminiLlmService
import services.llm.LlmService
import services.search.ElasticSearchService
import services.search.ElasticSearchServiceImpl
import services.storage.GcsStorageService
import services.storage.StorageService

fun appModule(
    elasticUrl: String,
    elasticApiKey: String,
    vertexProject: String,
    vertexLocation: String,
    gcsBucket: String,
    accessTokenProvider: suspend () -> String
) = module {


    single<StorageService> { GcsStorageService(bucket = gcsBucket, projectId = vertexProject) }

    single<EmbeddingsService> {
        VertexEmbeddingsService(
            http = get(),
            projectId = vertexProject,
            location = vertexLocation,
            accessTokenProvider = accessTokenProvider,
            dims = 1024 // AJUSTA a tu mapping
        )
    }

    // Elastic
    single<ElasticSearchService> {
        ElasticSearchServiceImpl(
            http = get(),
            baseUrl = elasticUrl,
            apiKey = elasticApiKey,
            indexName = "docs"
        )
    }

    // LLM
    single<LlmService> {
        GeminiLlmService(
            http = get(),
            projectId = vertexProject,
            location = vertexLocation,
            accessTokenProvider = accessTokenProvider
        )
    }

    // Ingest
    single<IngestService> {
        IngestServiceImpl(
            http = get(),
            embeddings = get(),
            elasticBaseUrl = elasticUrl,
            elasticApiKey = elasticApiKey,
            indexName = "docs"
        )
    }
}