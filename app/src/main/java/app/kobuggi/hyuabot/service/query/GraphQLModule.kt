package app.kobuggi.hyuabot.service.query

import app.kobuggi.hyuabot.BuildConfig
import com.apollographql.apollo3.ApolloClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object GraphQLModule {
    private const val BASE_URL = "${BuildConfig.API_URL}/query"
    private val apolloClient = ApolloClient.Builder().serverUrl(BASE_URL).build()

    @Provides
    fun provideApolloClient(): ApolloClient {
        return apolloClient
    }
}
