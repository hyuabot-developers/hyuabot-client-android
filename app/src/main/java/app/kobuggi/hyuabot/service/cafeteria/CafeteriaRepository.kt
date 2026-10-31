package app.kobuggi.hyuabot.service.cafeteria

import app.kobuggi.hyuabot.CafeteriaPageQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

interface CafeteriaRepository {
    suspend fun fetchCafeterias(
        date: LocalDate,
        campusId: Int,
        fetchPolicy: FetchPolicy = FetchPolicy.CacheFirst,
    ): Result<List<CafeteriaPageQuery.Cafeterium>>
}

class ApolloCafeteriaRepository @Inject constructor(
    private val apolloClient: ApolloClient,
) : CafeteriaRepository {
    override suspend fun fetchCafeterias(
        date: LocalDate,
        campusId: Int,
        fetchPolicy: FetchPolicy,
    ): Result<List<CafeteriaPageQuery.Cafeterium>> = try {
        val response = apolloClient.query(CafeteriaPageQuery(date, campusId)).fetchPolicy(fetchPolicy).execute()
        val data = response.data
        if (response.exception != null || data == null) {
            Result.failure(response.exception ?: IllegalStateException("Empty cafeteria response"))
        } else {
            Result.success(data.cafeteria)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CafeteriaRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCafeteriaRepository(impl: ApolloCafeteriaRepository): CafeteriaRepository
}
