package app.kobuggi.hyuabot.service

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class GraphQLModule {
    companion object {
        const val BASE_URL = "${SdBuildConfig.API_URL}/graphql"
        var mInstance: ApolloClient? = null

        private fun apolloClient(): ApolloClient {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build()

            return ApolloClient.Builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build()
        }

        fun getInstance(): ApolloClient {
            if (mInstance == null) {
                mInstance = apolloClient()
            }
            return mInstance!!
        }
    }
}
