package app.kobuggi.hyuabot.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import app.kobuggi.hyuabot.ContactPageQuery
import app.kobuggi.hyuabot.ContactPageVersionQuery
import app.kobuggi.hyuabot.service.database.AppDatabase
import app.kobuggi.hyuabot.service.database.entity.Contact
import app.kobuggi.hyuabot.service.preferences.UserPreferencesRepository
import app.kobuggi.hyuabot.service.translation.DynamicTextTranslator
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 연락처 동기화 상태. 실패해도 저장된 목록은 그대로 둔다. */
sealed interface ContactSync {
    data object Idle : ContactSync
    data object Checking : ContactSync
    data class Downloading(val version: String) : ContactSync
    data object Failed : ContactSync
}

data class ContactUiState(
    val campusID: Int = 0,
    val query: String = "",
    /** 걸러 낸 부서 키(null=전체, [ContactDirectory.OTHER]=기타). 새 데이터에서 사라진 부서는 전체로 돌아간다. */
    val department: String? = null,
    val departments: List<String> = emptyList(),
    val sections: List<ContactSection> = emptyList(),
    val hasEntries: Boolean = false,
    val sync: ContactSync = ContactSync.Idle,
    val version: String? = null,
) {
    val filtering: Boolean get() = query.isNotBlank() || department != null
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ContactViewModel @Inject constructor(
    private val apolloClient: ApolloClient,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: AppDatabase
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedDepartment = MutableStateFlow<String?>(null)
    private val sync = MutableStateFlow<ContactSync>(ContactSync.Idle)
    private var refreshJob: Job? = null

    private val campusEntries = userPreferencesRepository.campusID
        .flatMapLatest { campus -> database.contactDao().findByCampusID(campus).map { campus to it.map(ContactDirectory::entry) } }

    val uiState: StateFlow<ContactUiState> = combine(
        campusEntries,
        query,
        selectedDepartment,
        sync,
        userPreferencesRepository.contactVersion,
    ) { (campus, entries), query, department, sync, version ->
        val departments = ContactDirectory.departments(entries)
        val effective = department.takeIf { it in departments }
        ContactUiState(
            campusID = campus,
            query = query,
            department = effective,
            departments = departments,
            sections = ContactDirectory.sections(entries, query, effective),
            hasEntries = entries.isNotEmpty(),
            sync = sync,
            version = version?.substringBeforeLast(':')?.takeIf { it.isNotBlank() },
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContactUiState())

    init {
        refresh()
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun selectDepartment(department: String?) {
        selectedDepartment.value = department
    }

    fun resetFilters() {
        query.value = ""
        selectedDepartment.value = null
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            sync.value = ContactSync.Checking
            val ok = try {
                syncContacts()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            sync.value = if (ok) ContactSync.Idle else ContactSync.Failed
        }
    }

    /** 버전이 다르거나 저장된 목록이 비었을 때만 전체를 받아 번역한 뒤 한 번에 바꾼다. 성공하면 true. */
    private suspend fun syncContacts(): Boolean {
        val versionResponse = apolloClient.query(ContactPageVersionQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
        val serverVersion = versionResponse.data?.phonebook?.version
        if (versionResponse.exception != null || versionResponse.data?.phonebook == null) return false
        val localizedVersion = localizedVersion(serverVersion)
        val localVersion = userPreferencesRepository.contactVersion.first()
        val dao = database.contactDao()
        if (localVersion == localizedVersion && dao.count() > 0) return true

        sync.value = ContactSync.Downloading(serverVersion.orEmpty())
        val response = apolloClient.query(ContactPageQuery()).fetchPolicy(FetchPolicy.NetworkOnly).execute()
        val phonebook = response.data?.phonebook
        if (response.exception != null || phonebook == null) return false
        val contacts = phonebook.categories.flatMap { category ->
            category.entries.map { Contact(contactID = it.seq, campusID = it.campus, name = it.name, phone = it.phone) }
        }
        val translated = contacts.map { it.copy(name = translateName(it.name)) }
        database.withTransaction {
            dao.deleteAll()
            dao.insertAll(*translated.toTypedArray())
        }
        userPreferencesRepository.setContactVersion(localizedVersion(phonebook.version))
        return true
    }

    /** "부서-업무"를 따로 번역해 '-' 구분이 번역 뒤에도 남게 한다. */
    private suspend fun translateName(name: String): String {
        val (department, title) = ContactDirectory.split(name)
        val translatedTitle = DynamicTextTranslator.translateForCurrentAppLocale(title)
        if (department == null) return translatedTitle
        return "${DynamicTextTranslator.translateForCurrentAppLocale(department)}-$translatedTitle"
    }

    private fun localizedVersion(version: String?): String =
        "${version.orEmpty()}:${DynamicTextTranslator.currentAppLanguageTag()}"
}
