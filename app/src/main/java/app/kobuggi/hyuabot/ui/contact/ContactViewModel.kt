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
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator
import app.kobuggi.hyuabot.util.QueryError
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: AppDatabase
): ViewModel() {
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
            val response = apolloClient.query(ContactPageVersionQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.phonebook != null) {
                val localVersion = userPreferencesRepository.contactVersion.first()
                if (localVersion != response.data?.phonebook?.version || database.contactDao().count() == 0) {
                    fetchContacts()
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
            val response = apolloClient.query(ContactPageQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
            if (response.data == null || response.exception != null) {
                _queryError.value = QueryError.SERVER_ERROR
            } else if (response.data?.phonebook != null) {
                dao.deleteAll()
                response.data?.phonebook?.let { phonebook ->
                    val contacts = phonebook.categories.flatMap { category ->
                        category.entries.map {
                            Contact(
                                contactID = it.seq,
                                campusID = it.campus,
                                name = it.name,
                                phone = it.phone
                            )
                        }
                    }
                    dao.insertAll(*contacts.toTypedArray())
                    userPreferencesRepository.setContactVersion(phonebook.version)
                    translateContactsInCache(contacts)
                }
                _queryError.value = null
            } else {
                _queryError.value = QueryError.UNKNOWN_ERROR
            }
            _updating.postValue(false)
        }
    }

    private fun translateContactsInCache(contacts: List<Contact>) {
        viewModelScope.launch {
            val translatedContacts = contacts.map {
                it.copy(name = DynamicTextTranslator.translateForCurrentAppLocale(it.name))
            }
            database.contactDao().insertAll(*translatedContacts.toTypedArray())
        }
    }

    fun searchContacts(query: String, campusID: Int) {
        viewModelScope.launch {
            database.contactDao().findByNameOrPhoneAndCampusID("%$query%", "%$query%", campusID).collect {
                searchResults.postValue(it)
            }
        }
    }
}
