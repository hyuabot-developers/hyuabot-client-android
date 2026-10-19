package app.kobuggi.hyuabot.test

import app.kobuggi.hyuabot.BusStopCoordinatesQuery
import app.kobuggi.hyuabot.ShuttleServiceNoticeQuery
import app.kobuggi.hyuabot.ShuttleLocationQuery
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloNetworkException
import com.apollographql.apollo.network.NetworkTransport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Tests explicitly release each page request, including out-of-order and failed responses. */
class ScriptedNetworkTransport : NetworkTransport {
    class Pending(val operation: Operation<*>) {
        internal val response = CompletableDeferred<Operation.Data?>()
        internal var offline = false
        internal var thrown: Exception? = null

        fun succeed(data: Operation.Data) { response.complete(data) }
        fun fail() { offline = true; response.complete(null) }
        fun throwException() { thrown = IllegalStateException("scripted failure"); response.complete(null) }
    }

    private val requests = Channel<Pending>(Channel.UNLIMITED)
    private val pending = mutableListOf<Pending>()

    suspend fun next(): Pending = requests.receive()

    override fun <D : Operation.Data> execute(request: ApolloRequest<D>): Flow<ApolloResponse<D>> = flow {
        // Auxiliary location queries are independent of the page response being exercised.
        val auxiliary = when (request.operation) {
            is ShuttleServiceNoticeQuery -> ShuttleServiceNoticeQuery.Data(ShuttleServiceNoticeQuery.Shuttle("Shuttle", emptyList()))
            is BusStopCoordinatesQuery -> BusStopCoordinatesQuery.Data(emptyList())
            is ShuttleLocationQuery -> ShuttleLocationQuery.Data(ShuttleLocationQuery.Shuttle("Shuttle", emptyList(), emptyList()))
            else -> null
        }
        val script = Pending(request.operation)
        val data = if (auxiliary != null) auxiliary else {
            pending += script
            requests.send(script)
            script.response.await()
        }
        script.thrown?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        val response = ApolloResponse.Builder(request.operation, request.requestUuid)
            .data(data as D?)
            .exception(if (script.offline) ApolloNetworkException("offline") else null)
            .isLast(true)
            .build()
        emit(response)
    }

    override fun dispose() {
        pending.forEach { it.response.cancel() }
        requests.cancel()
    }
}
