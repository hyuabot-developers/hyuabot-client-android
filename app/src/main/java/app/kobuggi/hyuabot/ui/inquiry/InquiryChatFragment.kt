package app.kobuggi.hyuabot.ui.inquiry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import app.kobuggi.hyuabot.R
import app.kobuggi.hyuabot.databinding.FragmentInquiryChatBinding
import app.kobuggi.hyuabot.service.InquiryMessage
import app.kobuggi.hyuabot.service.InquiryService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InquiryChatFragment @Inject constructor() : Fragment() {
    private val args: InquiryChatFragmentArgs by navArgs()
    private val binding by lazy { FragmentInquiryChatBinding.inflate(layoutInflater) }
    private val messageAdapter = InquiryMessageAdapter(emptyList())

    @Inject
    lateinit var inquiryService: InquiryService

    private var threadId: String? = null
    private var lastMessages: List<InquiryMessage> = emptyList()
    private var streamJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding.inquiryMessageList.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
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

    private companion object {
        const val POLL_INTERVAL_MS = 30_000L
        const val STREAM_RECONNECT_DELAY_MS = 1_000L
    }
}
