package app.kobuggi.hyuabot.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.kobuggi.hyuabot.databinding.FragmentMapBinding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraUpdateFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment @Inject constructor() : Fragment() {
    private val binding by lazy { FragmentMapBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.mapView.start(object: MapLifeCycleCallback() {
            override fun onMapDestroy() {

            }

            override fun onMapError(exception: Exception?) {

            }
        }, object: KakaoMapReadyCallback() {
            override fun onMapReady(map: KakaoMap) {
                map.apply {
                    moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(37.29753535479288, 126.83544659517665)))
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
