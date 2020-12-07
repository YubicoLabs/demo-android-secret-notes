package com.yubico.example.secretnotes.ui.main

import android.os.Bundle
import android.util.Base64
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.yubico.example.secretnotes.R
import com.yubico.example.secretnotes.piv.PivDecryptMessageContract
import com.yubico.example.secretnotes.piv.PivGetPublicKeyContract
import com.yubico.yubikit.core.util.Pair
import com.yubico.yubikit.piv.Slot
import javax.crypto.Cipher

private const val ARG_NOTE_ID = "NOTE_ID"

/**
 * A simple [Fragment] subclass.
 * Use the [NoteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NoteFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    private val requestPublicKey = registerForActivityResult(PivGetPublicKeyContract()) {
        viewModel.publicKey = it
    }

    private val requestDecrypt = registerForActivityResult(PivDecryptMessageContract()) {
        if (it != null) {
            contentEditText.setText(it.decodeToString())
        }
    }

    private var noteId: String? = null

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteId = it.getString(ARG_NOTE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.note_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleEditText = view.findViewById(R.id.note_title)
        contentEditText = view.findViewById(R.id.note_content)

        noteId?.let {
            titleEditText.setText(it)
            titleEditText.isEnabled = false
            val note = viewModel.getNote(it)
            contentEditText.setText(note)

            requestDecrypt.launch(Pair(Slot.KEY_MANAGEMENT, Base64.decode(note, Base64.DEFAULT)))
        }

        setHasOptionsMenu(true)

        if (viewModel.publicKey == null) {
            requestPublicKey.launch(Slot.KEY_MANAGEMENT)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.note_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save_note -> {
                var content = contentEditText.text.toString()
                viewModel.publicKey?.let {
                    val cipher = Cipher.getInstance("RSA")
                    cipher.init(Cipher.ENCRYPT_MODE, it)
                    val encrypted = cipher.doFinal(content.encodeToByteArray())
                    content = Base64.encodeToString(encrypted, Base64.DEFAULT)
                }
                viewModel.setNote(titleEditText.text.toString(), content)
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param noteId Parameter 1.
         * @return A new instance of fragment NoteFragment.
         */
        @JvmStatic
        fun newInstance(noteId: String?) =
            NoteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTE_ID, noteId)
                }
            }
    }
}