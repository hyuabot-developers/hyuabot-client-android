package app.kobuggi.hyuabot.widget

import com.apollographql.apollo.ApolloClient
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ShuttleWidgetEntryPoint {
    fun apolloClient(): ApolloClient
}
