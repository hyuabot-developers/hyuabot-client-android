package app.kobuggi.hyuabot.widget

import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.cafeteria.CafeteriaRepository
import com.apollographql.apollo.ApolloClient
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CafeteriaWidgetEntryPoint {
    fun cafeteriaRepository(): CafeteriaRepository
    fun apolloClient(): ApolloClient
    fun userPreferencesRepository(): UserPreferencesRepository
}
