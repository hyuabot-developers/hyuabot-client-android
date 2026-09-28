package app.kobuggi.hyuabot.ui.inquiry

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentInquiryChatBinding
import app.kobuggi.hyuabot.service.InquiryMessage
import app.kobuggi.hyuabot.service.InquiryService
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InquiryChatFragment @Inject constructor() : Fragment(), OnMapReadyCallback {
    private val args: InquiryChatFragmentArgs by navArgs()
    private var viewBinding: FragmentInquiryChatBinding? = null
    private val binding get() = requireNotNull(viewBinding)
    private val messageAdapter = InquiryMessageAdapter(emptyList())

    @Inject
    lateinit var inquiryService: InquiryService

    private var threadId: String? = null
    private var lastMessages: List<InquiryMessage> = emptyList()
    private var streamJob: Job? = null
    private var noticeDismissed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentInquiryChatBinding.inflate(inflater, container, false)
        noticeDismissed = false
        binding.root.requestFocus()
        binding.inquiryMessageList.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        }
        binding.inquiryOfficeMapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@InquiryChatFragment)
        }
        binding.inquiryNoticeClose.setOnClickListener {
            noticeDismissed = true
            binding.inquiryNoticeCard.visibility = View.GONE
        }
        binding.inquiryOfficeMapButton.setOnClickListener { openOfficeMap() }
        binding.inquiryOfficeCallButton.setOnClickListener { openOfficeDialer() }
        binding.inquiryInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            binding.inquiryNoticeCard.visibility =
                if (hasFocus || noticeDismissed) View.GONE else View.VISIBLE
        }
        binding.inquirySendButton.setOnClickListener { sendMessage() }
        binding.inquiryToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.inquiryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
        return binding.root
    }

    override fun onMapReady(map: NaverMap) {
        val currentBinding = viewBinding ?: return
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        map.uiSettings.isZoomControlEnabled = false
        map.uiSettings.isLocationButtonEnabled = false
        Marker().apply {
            position = OFFICE_COORDINATE
            captionText = getString(R.string.inquiry_shuttle_lost_office)
            this.map = map
        }
        // Keep the marker and its caption fully visible in the compact map preview.
        val previewCenter = LatLng(OFFICE_COORDINATE.latitude + 0.00015, OFFICE_COORDINATE.longitude)
        var didMoveCamera = false
        fun focusOffice() {
            if (didMoveCamera || viewBinding !== currentBinding) return
            didMoveCamera = true
            map.moveCamera(CameraUpdate.scrollTo(previewCenter))
            map.moveCamera(CameraUpdate.zoomTo(16.5))
        }
        currentBinding.inquiryOfficeMapView.post { focusOffice() }
        map.addOnLoadListener { focusOffice() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            val thread = inquiryService.openThread(
                subject = null,
                entryScreen = args.entryScreen,
                entryScreenName = args.entryScreenName,
            )
            if (thread == null) {
                showLoadFailed()
                return@launch
            }
            threadId = thread.id
            refreshMessages(markRead = true)
            startStream(thread.id)
            pollMessages()
        }
    }

    private fun startStream(currentThreadId: String) {
        streamJob?.cancel()
        streamJob = viewLifecycleOwner.lifecycleScope.launch {
            while (viewLifecycleOwner.lifecycleScope.isActive) {
                try {
                    inquiryService.streamEvents { event ->
                        if (event.threadId == currentThreadId) {
                            viewLifecycleOwner.lifecycleScope.launch { refreshMessages(markRead = true) }
                        }
                    }
                } catch (_: Exception) {
                    // The polling fallback keeps the conversation current while SSE reconnects.
                }
                delay(STREAM_RECONNECT_DELAY_MS)
            }
        }
    }

    private suspend fun pollMessages() {
        val currentThreadId = threadId ?: return
        while (viewLifecycleOwner.lifecycleScope.isActive) {
            delay(POLL_INTERVAL_MS)
            val previousAdminCount = lastMessages.count { it.senderType == "ADMIN" }
            val updated = inquiryService.messages(currentThreadId)
            applyMessages(updated)
            val newAdminCount = updated.count { it.senderType == "ADMIN" }
            if (newAdminCount > previousAdminCount) {
                inquiryService.markRead(currentThreadId)
            }
        }
    }

    private suspend fun refreshMessages(markRead: Boolean) {
        val currentThreadId = threadId ?: return
        val updated = inquiryService.messages(currentThreadId)
        applyMessages(updated)
        if (markRead) {
            inquiryService.markRead(currentThreadId)
        }
    }

    private fun applyMessages(messages: List<InquiryMessage>) {
        lastMessages = messages
        messageAdapter.updateData(messages)
        binding.inquiryEmptyView.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
        if (messageAdapter.itemCount > 0) {
            binding.inquiryMessageList.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun openOfficeMap() {
        val label = Uri.encode(getString(R.string.inquiry_shuttle_lost_office))
        val coordinates = "${OFFICE_COORDINATE.latitude},${OFFICE_COORDINATE.longitude}"
        val geoIntent = Intent(Intent.ACTION_VIEW, "geo:$coordinates?q=$coordinates($label)".toUri())
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://www.google.com/maps/search/?api=1&query=$coordinates".toUri(),
        )
        try {
            startActivity(geoIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(webIntent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(requireContext(), R.string.map_external_error, Toast.LENGTH_SHORT).show()
            }
        } catch (_: SecurityException) {
            Toast.makeText(requireContext(), R.string.map_external_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openOfficeDialer() {
        val intent = Intent(Intent.ACTION_DIAL, "tel:0314004412".toUri())
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.inquiry_phone_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage() {
        val currentThreadId = threadId ?: return
        val text = binding.inquiryInput.text?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return
        binding.inquiryInput.text?.clear()
        viewLifecycleOwner.lifecycleScope.launch {
            val sent = inquiryService.send(currentThreadId, text)
            if (sent == null) {
                showLoadFailed()
            } else {
                refreshMessages(markRead = false)
            }
        }
    }

    private fun showLoadFailed() {
        if (!isAdded) return
        Toast.makeText(requireContext(), R.string.inquiry_load_failed, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        binding.inquiryOfficeMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.inquiryOfficeMapView.onResume()
    }

    override fun onPause() {
        binding.inquiryOfficeMapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.inquiryOfficeMapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.inquiryOfficeMapView.onDestroy()
        super.onDestroyView()
        viewBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding?.inquiryOfficeMapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewBinding?.inquiryOfficeMapView?.onLowMemory()
    }

    private companion object {
        val OFFICE_COORDINATE = LatLng(37.29275316695924, 126.83714484865253)
        const val POLL_INTERVAL_MS = 30_000L
        const val STREAM_RECONNECT_DELAY_MS = 1_000L
    }
}
