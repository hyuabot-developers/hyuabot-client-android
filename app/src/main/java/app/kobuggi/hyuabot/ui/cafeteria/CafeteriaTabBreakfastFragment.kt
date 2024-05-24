package app.kobuggi.hyuabot.ui.cafeteria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.kobuggi.hyuabot.databinding.FragmentCafeteriaTabBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CafeteriaTabBreakfastFragment : Fragment() {
    private val binding by lazy { FragmentCafeteriaTabBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }
}
