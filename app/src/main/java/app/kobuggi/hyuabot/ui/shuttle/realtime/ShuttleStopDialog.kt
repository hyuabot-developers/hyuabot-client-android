package app.kobuggi.hyuabot.ui.shuttle.realtime

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.DialogShuttleStopBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdate
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelLayerOptions
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class ShuttleStopDialog @Inject constructor() : BottomSheetDialogFragment() {
    private val binding by lazy { DialogShuttleStopBinding.inflate(layoutInflater) }
    private val viewModel: ShuttleStopDialogViewModel by viewModels()
    private val args : ShuttleStopDialogArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val stopID = args.stopID
        viewModel.fetchData(
            when(stopID) {
                R.string.shuttle_tab_dormitory_out -> "dormitory_o"
                R.string.shuttle_tab_shuttlecock_out -> "shuttlecock_o"
                R.string.shuttle_tab_station -> "station"
                R.string.shuttle_tab_terminal -> "terminal"
                R.string.shuttle_tab_jungang_station -> "jungang_stn"
                R.string.shuttle_tab_shuttlecock_in -> "shuttlecock_i"
                else -> "dormitory_o"
            }
        )
        binding.apply {
            toolbar.apply {
                title = getString(stopID)
                setOnMenuItemClickListener {
                    dismiss()
                    true
                }
            }
            stopMapView.start(object: MapLifeCycleCallback() {
                override fun onMapDestroy() {
                    Log.d("ShuttleStopDialog", "Map Destroy")
                }

                override fun onMapError(exception: Exception?) {
                    Log.e("ShuttleStopDialog", exception.toString())
                }
            }, object: KakaoMapReadyCallback() {
                override fun onMapReady(map: KakaoMap) {
                    map.apply {
                        setGestureEnable(GestureType.Pan, false)
                        setGestureEnable(GestureType.Rotate, false)
                        setGestureEnable(GestureType.Tilt, false)
                    }

                    val labelManager = map.labelManager
                    val layer = labelManager?.layer
                    val style = labelManager?.addLabelStyles(
                        LabelStyles.from(LabelStyle.from(
                            R.drawable.ic_bus_marker
                        ).apply {
                            setAnchorPoint(0.5f, 1.0f)
                            isApplyDpScale = true
                        })
                    )
                    viewModel.result.observe(viewLifecycleOwner) {
                        if (it != null) {
                            map.moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(it.latitude, it.longitude)))
                            layer!!.addLabel(LabelOptions.from(
                                LatLng.from(it.latitude, it.longitude)
                            ).setStyles(style))
                        }
                    }
                }

                override fun getZoomLevel(): Int = 17
            })
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
             behavior.isDraggable = false
        }
        return dialog
    }

    override fun onResume() {
        super.onResume()
        binding.stopMapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.stopMapView.pause()
    }
}
