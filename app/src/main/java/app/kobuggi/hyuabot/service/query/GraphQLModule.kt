package app.kobuggi.hyuabot.service.query

import app.kobuggi.hyuabot.BuildConfig
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object GraphQLModule {
    private const val BASE_URL = "${BuildConfig.API_URL}/query"

    private val apolloClient = {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        ApolloClient.Builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    fun provideApolloClient(): ApolloClient = apolloClient()
}
