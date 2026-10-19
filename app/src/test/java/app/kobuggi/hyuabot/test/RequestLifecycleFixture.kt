package app.kobuggi.hyuabot.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
class RequestLifecycleFixture(scope: TestScope) : AutoCloseable {
    val transport = ScriptedNetworkTransport()
    val client = ApolloClient.Builder().networkTransport(transport)
        .dispatcher(StandardTestDispatcher(scope.testScheduler)).build()
    private val preferencesFactory = UserPreferencesRepositoryTestFactory(scope)
    val preferences = preferencesFactory.create()
    private val viewModels = mutableListOf<ViewModel>()

    fun <T : ViewModel> track(viewModel: T): T = viewModel.also { viewModels += it }

    override fun close() {
        viewModels.forEach { it.viewModelScope.cancel() }
        client.close()
        preferencesFactory.cleanUp()
    }
}
