package app.kobuggi.hyuabot.ui.contact

import app.kobuggi.hyuabot.service.database.entity.Contact

/** 연락처 한 건. 서버 이름 "부서-업무"를 첫 '-'에서 나눈 결과. 부서가 없으면 [department]는 null(기타). */
data class ContactEntry(
    val id: Int,
    val name: String,
    val department: String?,
    val title: String,
    val phone: String,
)

/** 검색어와 일치한 부분. 없으면 null. */
data class ContactRow(
    val entry: ContactEntry,
    val titleMatch: IntRange?,
    val phoneMatch: IntRange?,
)

data class ContactSection(val department: String?, val rows: List<ContactRow>)

/** 연락처 나누기·정렬·검색·강조 규칙. iOS ContactDirectory와 같은 규칙을 따르는 순수 함수 모음. */
object ContactDirectory {
    /** 부서가 없는 연락처를 가리키는 필터 키. null은 "전체"에 쓴다. */
    const val OTHER = ""

    fun split(name: String): Pair<String?, String> {
        val trimmed = name.trim()
        val index = trimmed.indexOf('-')
        if (index < 0) return null to trimmed
        val department = trimmed.substring(0, index).trim()
        val title = trimmed.substring(index + 1).trim()
        if (department.isEmpty() || title.isEmpty()) return null to trimmed
        return department to title
    }

    fun entry(contact: Contact): ContactEntry {
        val (department, title) = split(contact.name)
        return ContactEntry(contact.contactID, contact.name, department, title, contact.phone)
    }

    private val byPhone = compareBy<ContactEntry>({ it.phone }, { it.id })

    /** 부서 순서: 대학 → 팀 → 센터 → 그 밖 → 기타. 같은 순위는 번호순으로 먼저 나온 순서. */
    fun departments(entries: List<ContactEntry>): List<String> {
        val firstSeen = LinkedHashSet<String>()
        entries.sortedWith(byPhone).forEach { firstSeen.add(it.department ?: OTHER) }
        return firstSeen.withIndex()
            .sortedWith(compareBy({ rank(it.value) }, { it.index }))
            .map { it.value }
    }

    private fun rank(department: String): Int = when {
        department == OTHER -> 4
        department.endsWith("대학") -> 0
        department.endsWith("팀") -> 1
        department.endsWith("센터") -> 2
        else -> 3
    }

    private fun compact(query: String): String = query.filterNot { it == '-' || it.isWhitespace() }

    private fun digitsOnly(query: String): String? =
        compact(query).takeIf { it.isNotEmpty() && it.all { c -> c in '0'..'9' } }

    fun matches(entry: ContactEntry, query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        val candidates = listOfNotNull(entry.title, entry.department, entry.department?.let { "$it ${entry.title}" }, entry.phone)
        if (candidates.any { it.contains(q, ignoreCase = true) }) return true
        val digits = digitsOnly(q) ?: return false
        return entry.phone.replace("-", "").contains(digits)
    }

    fun titleMatch(title: String, query: String): IntRange? {
        val q = query.trim()
        if (q.isEmpty()) return null
        val start = title.indexOf(q, ignoreCase = true)
        return if (start < 0) null else start until start + q.length
    }

    /** 번호는 하이픈을 건너뛰어 강조한다: 4004215 → 400-4215. */
    fun phoneMatch(phone: String, query: String): IntRange? {
        titleMatch(phone, query)?.let { return it }
        val digits = digitsOnly(query.trim()) ?: return null
        val kept = phone.indices.filter { phone[it] != '-' && !phone[it].isWhitespace() }
        val joined = kept.map { phone[it] }.joinToString("")
        val at = joined.indexOf(digits)
        if (at < 0) return null
        return kept[at]..kept[at + digits.length - 1]
    }

    /** 부서 키([OTHER] 또는 부서 이름, null=전체)·검색어로 걸러 부서별로 묶는다. 행은 번호순. */
    fun sections(entries: List<ContactEntry>, query: String, department: String?): List<ContactSection> {
        val hits = entries
            .filter { (department == null || (it.department ?: OTHER) == department) && matches(it, query) }
            .sortedWith(byPhone)
        return departments(hits).map { dep ->
            ContactSection(
                dep.takeIf { it != OTHER },
                hits.filter { (it.department ?: OTHER) == dep }
                    .map { ContactRow(it, titleMatch(it.title, query), phoneMatch(it.phone, query)) },
            )
        }
    }
}
