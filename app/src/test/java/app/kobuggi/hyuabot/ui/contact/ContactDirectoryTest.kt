package app.kobuggi.hyuabot.ui.contact

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactDirectoryTest {
    private fun entry(id: Int, name: String, phone: String): ContactEntry {
        val (department, title) = ContactDirectory.split(name)
        return ContactEntry(id, name, department, title, phone)
    }

    private val sample = listOf(
        entry(1, "학술정보관-시설/기자재", "031-400-4245"),
        entry(2, "학습지원센터-미디어", "031-400-4284"),
        entry(3, "학습지원센터-LMS(학습관리시스템)", "031-400-4285"),
        entry(4, "학사팀-졸업학적", "031-400-4215"),
        entry(5, "소프트융합대학-경영/총무", "031-400-1003"),
        entry(6, "문의처", "031-400-9999"),
    )

    @Test
    fun splitUsesFirstDash() {
        assertEquals("학사팀" to "수업-수강", ContactDirectory.split("학사팀-수업-수강"))
        assertEquals(null to "문의처", ContactDirectory.split("문의처"))
        assertEquals(null to "-업무", ContactDirectory.split("-업무"))
    }

    @Test
    fun departmentsAreRankedCollegeTeamCenterOtherEtc() {
        assertEquals(
            listOf("소프트융합대학", "학사팀", "학습지원센터", "학술정보관", ContactDirectory.OTHER),
            ContactDirectory.departments(sample),
        )
    }

    @Test
    fun searchIsCaseInsensitiveAndMatchesDepartmentTitleAndPhone() {
        val lms = sample[2]
        assertTrue(ContactDirectory.matches(lms, "lms"))
        assertTrue(ContactDirectory.matches(lms, "학습지원센터 LMS"))
        assertTrue(ContactDirectory.matches(lms, " 4285 "))
        assertFalse(ContactDirectory.matches(lms, "zzz"))
    }

    @Test
    fun digitsOnlyQuerySkipsHyphens() {
        val grad = sample[3]
        assertTrue(ContactDirectory.matches(grad, "4004215"))
        assertTrue(ContactDirectory.matches(grad, "400 4215"))
        assertEquals(4..11, ContactDirectory.phoneMatch(grad.phone, "4004215"))
        assertEquals("400-4215", grad.phone.substring(ContactDirectory.phoneMatch(grad.phone, "4004215")!!))
    }

    @Test
    fun highlightCoversTitleOnly() {
        assertEquals(0..2, ContactDirectory.titleMatch("LMS(학습관리시스템)", "lms"))
        assertNull(ContactDirectory.titleMatch("미디어", "lms"))
    }

    @Test
    fun sectionsFilterByDepartmentAndSortByPhone() {
        val sections = ContactDirectory.sections(sample, "", "학습지원센터")
        assertEquals(1, sections.size)
        assertEquals(listOf(2, 3), sections.single().rows.map { it.entry.id })
        val other = ContactDirectory.sections(sample, "", ContactDirectory.OTHER)
        assertNull(other.single().department)
    }
}
