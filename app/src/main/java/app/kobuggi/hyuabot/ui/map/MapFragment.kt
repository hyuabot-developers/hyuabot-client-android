package app.kobuggi.hyuabot.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentMapBinding
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentMapBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MapViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val labelStyle = LabelStyles.from("default", listOf(
            LabelStyle.from(R.drawable.map_marker)
        ))
        binding.mapView.start(object: MapLifeCycleCallback() {
            override fun onMapDestroy() {}
            override fun onMapError(exception: Exception?) {}
        }, object: KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                map.apply {
                    moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(37.29753535479288, 126.83544659517665)))
                    setGestureEnable(GestureType.Rotate, false)
                    setGestureEnable(GestureType.Tilt, false)
                    setOnCameraMoveEndListener { map, position, _ ->
                        val northLng = map.fromScreenPoint(0, 0)?.latitude
                        val southLng = map.fromScreenPoint(0, map.viewport.height())?.latitude
                        val westLat = map.fromScreenPoint(0, 0)?.longitude
                        val eastLat = map.fromScreenPoint(map.viewport.width(), 0)?.longitude
                        viewModel.fetchBuildings(northLng, southLng, westLat, eastLat)
                    }
                }

                viewModel.buildings.observe(viewLifecycleOwner) { buildings ->
                    map.apply {
                        labelManager?.apply {
                            clearAll()
                            buildings.forEach { building ->
                                layer?.addLabel(LabelOptions.from(building.name, LatLng.from(building.latitude, building.longitude)).setStyles(labelStyle))
                            }
                        }
                    }
                }
            }

            override fun getZoomLevel(): Int = 16
        })
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.pause()
    }
}
