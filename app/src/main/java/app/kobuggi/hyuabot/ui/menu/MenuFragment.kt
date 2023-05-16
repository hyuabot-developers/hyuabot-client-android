package app.kobuggi.hyuabot.ui.menu

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.MainActivity
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentMenuBinding
import app.kobuggi.hyuabot.ui.menu.campus.CampusDialog
import app.kobuggi.hyuabot.ui.menu.info.AppInfoDialog
import app.kobuggi.hyuabot.ui.menu.language.AppLanguageDialog
import app.kobuggi.hyuabot.ui.menu.theme.AppThemeDialog
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment : Fragment(), DialogInterface.OnDismissListener{
    companion object {
        fun newInstance() = MenuFragment()
    }
    private val vm by viewModels<MenuViewModel>()
    private lateinit var binding: FragmentMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = vm

        val menuList = listOf(
            MenuButton(R.string.reading_room, R.drawable.ic_reading_room),
            MenuButton(R.string.map, R.drawable.ic_map),
            MenuButton(R.string.contact, R.drawable.ic_phone),
            MenuButton(R.string.calendar, R.drawable.ic_calendar)
        )
        val menuListAdapter = MenuListAdapter(requireContext(), menuList){
            stringID -> vm.moveToSomewhere(stringID)
        }
        binding.menuList.adapter = menuListAdapter
        binding.menuList.layoutManager = object : LinearLayoutManager(requireContext()){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        val divider = MenuListDivider(requireContext(), R.drawable.recyclerview_divider, 20)
        binding.menuList.addItemDecoration(divider)

        val settingList = listOf(
            MenuButton(R.string.campus, R.drawable.ic_campus),
            MenuButton(R.string.language, R.drawable.ic_language),
            MenuButton(R.string.app_theme, R.drawable.ic_dark_mode),
            MenuButton(R.string.donation, R.drawable.ic_donation),
            MenuButton(R.string.scoring, R.drawable.ic_scoring),
            MenuButton(R.string.developer_email, R.drawable.ic_email),
            MenuButton(R.string.developer_chat, R.drawable.ic_chat),
            MenuButton(R.string.about, R.drawable.ic_info),
        )
        val settingListAdapter = MenuListAdapter(requireContext(), settingList){
            stringID -> vm.moveToSomewhere(stringID)
        }
        binding.settingList.adapter = settingListAdapter
        binding.settingList.layoutManager = object : LinearLayoutManager(requireContext()){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        binding.settingList.addItemDecoration(divider)
        vm.moveEvent.observe(viewLifecycleOwner){
            when(it){
                R.string.reading_room -> {
                    val action = MenuFragmentDirections.openLibraryFragment()
                    findNavController().navigate(action)
                }
                R.string.map -> {
                    val action = MenuFragmentDirections.openMapFragment()
                    findNavController().navigate(action)
                }
                R.string.contact -> {
                    val action = MenuFragmentDirections.openContactFragment()
                    findNavController().navigate(action)
                }
                R.string.calendar -> {
                    val action = MenuFragmentDirections.openCalendarFragment()
                    findNavController().navigate(action)
                }
                R.string.campus -> {
                    val dialog = CampusDialog()
                    dialog.show(childFragmentManager, "CampusDialog")
                }
                R.string.language -> {
                    val dialog = AppLanguageDialog()
                    dialog.show(childFragmentManager, "AppLanguageDialog")
                }
                R.string.app_theme -> {
                    val dialog = AppThemeDialog()
                    dialog.show(childFragmentManager, "AppThemeDialog")
                }
                R.string.donation -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://qr.kakaopay.com/FWxVPo8iO"))
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), R.string.no_internet_app, Toast.LENGTH_SHORT).show()
                    }
                }
                R.string.scoring -> {
                    val uri = Uri.parse("market://details?id=app.kobuggi.hyuabot")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=app.kobuggi.hyuabot")))
                    }
                }
                R.string.developer_email -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:jil8885@hanyang.ac.kr"))
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show()
                    }
                }
                R.string.developer_chat -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.kakao.com/o/sW2kAinb"))
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), R.string.no_internet_app, Toast.LENGTH_SHORT).show()
                    }
                }
                R.string.about -> {
                    val dialog = AppInfoDialog()
                    dialog.show(childFragmentManager, "AppInfoDialog")
                }
            }
        }
        return binding.root
    }
    
    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            (activity as MainActivity).getAnalytics().logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, "메뉴")
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MenuFragment")
            })
        }
    }

    override fun onPause() {
        super.onPause()
        vm.moveToSomewhere(0)
    }

    override fun onDetach() {
        super.onDetach()
        vm.moveToSomewhere(0)
    }

    override fun onDismiss(dialogInterface: DialogInterface?) {
        vm.moveToSomewhere(0)
        if (requireActivity() is MainActivity){
            requireActivity().runOnUiThread {
                (requireActivity() as MainActivity).onDismiss(dialogInterface)
            }
        }
    }
}