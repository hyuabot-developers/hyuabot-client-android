package app.kobuggi.hyuabot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BindingFragment<T: ViewDataBinding> : Fragment() {
    @LayoutRes
    abstract fun getLayoutResourceID(): Int

    private lateinit var binding: T

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<T>(inflater, getLayoutResourceID(), container, false).apply {
            binding = this
        }.root
    }
}