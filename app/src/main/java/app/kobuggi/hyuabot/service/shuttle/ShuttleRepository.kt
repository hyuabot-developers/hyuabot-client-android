package app.kobuggi.hyuabot.service.shuttle

import app.kobuggi.hyuabot.ShuttleLocationQuery
import app.kobuggi.hyuabot.ShuttleRealtimePageQuery
import app.kobuggi.hyuabot.ShuttleWidgetQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

interface ShuttleRepository {
    suspend fun fetchDepartures(
        after: LocalTime,
        fetchPolicy: FetchPolicy = FetchPolicy.CacheFirst,
    ): Result<List<ShuttleWidgetQuery.Stop>>

    suspend fun fetchRealtimePage(
        query: ShuttleRealtimePageQuery,
        fetchPolicy: FetchPolicy = FetchPolicy.NetworkOnly,
    ): Result<ShuttleRealtimePageQuery.Data>

    suspend fun fetchLocations(
        fetchPolicy: FetchPolicy = FetchPolicy.NetworkOnly,
    ): Result<ShuttleLocationQuery.Shuttle>
}

class ApolloShuttleRepository @Inject constructor(
    private val apolloClient: ApolloClient,
) : ShuttleRepository {
    override suspend fun fetchDepartures(
        after: LocalTime,
        fetchPolicy: FetchPolicy,
    ): Result<List<ShuttleWidgetQuery.Stop>> = try {
        val response = apolloClient.query(ShuttleWidgetQuery(Optional.present(after)))
            .fetchPolicy(fetchPolicy)
            .execute()
        val data = response.data
        if (response.exception != null || data == null) {
            Result.failure(response.exception ?: IllegalStateException("Empty shuttle departures response"))
        } else {
            Result.success(data.shuttle.stops)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun fetchRealtimePage(
        query: ShuttleRealtimePageQuery,
        fetchPolicy: FetchPolicy,
    ): Result<ShuttleRealtimePageQuery.Data> = try {
        val response = apolloClient.query(query).fetchPolicy(fetchPolicy).execute()
        val data = response.data
        if (response.exception != null || data == null) {
            Result.failure(response.exception ?: IllegalStateException("Empty shuttle realtime response"))
        } else {
            Result.success(data)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun fetchLocations(fetchPolicy: FetchPolicy): Result<ShuttleLocationQuery.Shuttle> = try {
        val response = apolloClient.query(ShuttleLocationQuery()).fetchPolicy(fetchPolicy).execute()
        val data = response.data
        if (response.exception != null || data == null) {
            Result.failure(response.exception ?: IllegalStateException("Empty shuttle locations response"))
        } else {
            Result.success(data.shuttle)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ShuttleRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindShuttleRepository(impl: ApolloShuttleRepository): ShuttleRepository
}
