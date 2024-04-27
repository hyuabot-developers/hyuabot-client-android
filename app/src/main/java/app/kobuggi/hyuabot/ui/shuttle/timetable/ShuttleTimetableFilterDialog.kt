package app.kobuggi.hyuabot.ui.shuttle.timetable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogShuttleTimetableFilterBinding
import app.kobuggi.hyuabot.util.UIUtility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShuttleTimetableFilterDialog : DialogFragment() {
    private val binding by lazy { DialogShuttleTimetableFilterBinding.inflate(layoutInflater) }
    private var checkedPeriod: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding.toolbar.setOnMenuItemClickListener { _ -> dismiss(); true }
        binding.shuttlePeriodGroup.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if(isChecked) {
                when (checkedId) {
                    binding.semesterButton.id -> {
                        checkedPeriod = "semester"
                        binding.semesterButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, R.color.hanyang_blue, null))
                            setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
                        }
                        binding.vacationButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                        }
                        binding.vacationSessionButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                        }
                    }
                    binding.vacationButton.id -> {
                        checkedPeriod = "vacation"
                        binding.semesterButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                        }
                        binding.vacationButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, R.color.hanyang_blue, null))
                            setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
                        }
                        binding.vacationSessionButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                        }
                    }
                    binding.vacationSessionButton.id -> {
                        checkedPeriod = "vacation_session"
                        binding.semesterButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                        }
                        binding.vacationButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                            setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                                context.getColor(android.R.color.white)
                            } else {
                                context.getColor(android.R.color.black)
                            })
                        }
                        binding.vacationSessionButton.apply {
                            background.setTint(ResourcesCompat.getColor(resources, R.color.hanyang_blue, null))
                            setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, null))
                        }
                    }
                }
            } else {
                checkedPeriod = null
                binding.semesterButton.apply {
                    background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                    setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                        context.getColor(android.R.color.white)
                    } else {
                        context.getColor(android.R.color.black)
                    })
                }
                binding.vacationButton.apply {
                    background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                    setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                        context.getColor(android.R.color.white)
                    } else {
                        context.getColor(android.R.color.black)
                    })
                }
                binding.vacationSessionButton.apply {
                    background.setTint(ResourcesCompat.getColor(resources, android.R.color.transparent, null))
                    setTextColor(if (UIUtility.isDarkModeOn(context.resources)) {
                        context.getColor(android.R.color.white)
                    } else {
                        context.getColor(android.R.color.black)
                    })
                }
            }
        }
        binding.confirmButton.setOnClickListener {
            if (checkedPeriod != null) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("period", checkedPeriod)
            }
            dismiss()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
