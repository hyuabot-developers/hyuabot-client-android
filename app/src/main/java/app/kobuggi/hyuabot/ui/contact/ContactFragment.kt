package app.kobuggi.hyuabot.ui.contact

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentContactBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : Fragment() {
    companion object {
        fun newInstance() = ContactFragment()
    }
    private val viewModel: ContactViewModel by viewModels()
    private val binding by lazy { FragmentContactBinding.inflate(layoutInflater) }
}