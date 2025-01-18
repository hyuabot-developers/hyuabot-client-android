package app.kobuggi.hyuabot.ui.contact

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.kobuggi.hyuabot.ContactPageQuery
import app.kobuggi.hyuabot.ContactPageVersionQuery
import app.kobuggi.hyuabot.service.database.AppDatabase
import app.kobuggi.hyuabot.service.database.entity.Contact
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: AppDatabase
): ViewModel() {
    private val contactVersion = userPreferencesRepository.contactVersion.asLiveData()
    private val _contacts = database.contactDao().getAll().asLiveData()
    private val _updating = MutableLiveData(false)
    private val _queryError = MutableLiveData<QueryError?>(null)

    val updating get() = _updating
    val contacts get() = _contacts
    val campusID = userPreferencesRepository.campusID.asLiveData()
    val searchResults = MutableLiveData<List<Contact>>()
    val queryError get() = _queryError

    fun fetchContactVersion() {
        _updating.postValue(true)
        viewModelScope.launch {
            val response = apolloClient.query(ContactPageVersionQuery()).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.contact != null) {
                contactVersion.observeForever {
                    if (it != response.data?.contact?.version) {
                        fetchContacts()
                    }
                }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _updating.postValue(false)
        }
    }

    private fun fetchContacts() {
        val dao = database.contactDao()
        viewModelScope.launch {
            val response = apolloClient.query(ContactPageQuery()).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.contact != null) {
                dao.deleteAll()
                response.data?.contact?.version?.let { userPreferencesRepository.setContactVersion(it) }
                response.data?.contact?.data?.map {
                    Contact(contactID = it.id, campusID = it.campusID, name = it.name, phone = it.phone)
                }?.let { dao.insertAll(*it.toTypedArray()) }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _updating.postValue(false)
        }
    }

    fun searchContacts(query: String, campusID: Int) {
        viewModelScope.launch {
            database.contactDao().findByNameAndCampusID("%$query%", campusID).collect {
                searchResults.postValue(it)
            }
        }
    }
}
