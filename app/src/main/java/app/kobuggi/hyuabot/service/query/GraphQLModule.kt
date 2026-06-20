package app.kobuggi.hyuabot.service.query

import app.kobuggi.hyuabot.cache.Cache.cache
import app.kobuggi.hyuabot.BuildConfig
import app.kobuggi.hyuabot.SdBuildConfig
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.apollo.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GraphQLModule {
    private val BASE_URL = "${SdBuildConfig.API_URL}/graphql"

    private fun createApolloClient(): ApolloClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .callTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            okHttpClientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            )
        }

        val okHttpClient = okHttpClientBuilder.build()

        return ApolloClient.Builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .cache(MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024))
            .build()
    }

    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient = createApolloClient()
}
