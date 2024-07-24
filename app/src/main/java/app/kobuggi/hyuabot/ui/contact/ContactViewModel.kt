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
import com.apollographql.apollo3.ApolloClient
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
    private val _updating = MutableLiveData(false)

    val updating get() = _updating

    fun fetchContactVersion() {
        _updating.postValue(true)
        viewModelScope.launch {
            val response = try {
                apolloClient.query(ContactPageVersionQuery()).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            if (contactVersion.value != response?.data?.contact?.version) {
                fetchContacts()
            } else {
                _updating.postValue(false)
            }
        }
    }

    private fun fetchContacts() {
        val dao = database.contactDao()
        viewModelScope.launch {
            val response = try {
                apolloClient.query(ContactPageQuery()).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            dao.deleteAll()
            response?.data?.contact?.data?.map {
                Contact(contactID = it.id, campusID = it.campusID, name = it.name, phone = it.phone)
            }?.let { dao.insertAll(*it.toTypedArray()) }
            _updating.postValue(false)
        }
    }
}
