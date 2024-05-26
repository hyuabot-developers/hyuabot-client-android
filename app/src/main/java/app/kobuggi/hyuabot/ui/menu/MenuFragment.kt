package app.kobuggi.hyuabot.ui.menu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentMenuBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MenuFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentMenuBinding.inflate(layoutInflater) }
    private val menuList = listOf(
        MenuItem(R.drawable.ic_book, R.string.menu_book),
        MenuItem(R.drawable.ic_map, R.string.menu_map),
        MenuItem(R.drawable.ic_contact, R.string.menu_contact),
        MenuItem(R.drawable.ic_calendar, R.string.menu_calendar),
        MenuItem(R.drawable.ic_notice, R.string.menu_notice),
        MenuItem(R.drawable.ic_settings, R.string.menu_settings),
        MenuItem(R.drawable.ic_chat, R.string.menu_chat),
        MenuItem(R.drawable.ic_donate, R.string.menu_donate),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val menuAdapter = MenuListAdapter(requireContext(), menuList, ::onClickMenu)
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        binding.menuRecyclerView.apply {
            adapter = menuAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(decoration)
        }
        return binding.root
    }

    private fun onClickMenu(menuItem: MenuItem) {
        when(menuItem.titleResource) {
            R.string.menu_book -> {
                MenuFragmentDirections.actionMenuFragmentToReadingRoomFragment().also {
                    findNavController().navigate(it)
                }
            }
            R.string.menu_map -> {
                MenuFragmentDirections.actionMenuFragmentToMapFragment().also {
                    findNavController().navigate(it)
                }
            }
            R.string.menu_contact -> {
                MenuFragmentDirections.actionMenuFragmentToContactFragment().also {
                    findNavController().navigate(it)
                }
            }
            R.string.menu_calendar -> {
                MenuFragmentDirections.actionMenuFragmentToCalendarFragment().also {
                    findNavController().navigate(it)
                }
            }
            R.string.menu_notice -> {
                MenuFragmentDirections.actionMenuFragmentToNoticeFragment().also {
                    findNavController().navigate(it)
                }
            }
            R.string.menu_settings -> {
                MenuFragmentDirections.actionMenuFragmentToSettingFragment().also {
                    findNavController().navigate(it)
                }
            }
            R.string.menu_chat -> {
                val url = "https://open.kakao.com/o/sW2kAinb"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            R.string.menu_donate -> {
                val url = "https://qr.kakaopay.com/FWxVPo8iO"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            else -> {}
        }
    }
}
