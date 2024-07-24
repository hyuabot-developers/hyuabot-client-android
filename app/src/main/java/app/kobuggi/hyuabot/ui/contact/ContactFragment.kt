package app.kobuggi.hyuabot.ui.contact

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.databinding.FragmentContactBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContactFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentContactBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ContactViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.fetchContactVersion()
        viewModel.updating.observe(viewLifecycleOwner) {
            Log.d("ContactFragment", "Updating: $it")
            binding.loadingLayout.visibility = if (it) View.VISIBLE else View.GONE
        }

        return binding.root
    }
}
