package com.yubico.example.secretnotes

import android.os.Bundle
import android.util.Base64
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yubico.example.secretnotes.ui.main.MainFragment
import com.yubico.example.secretnotes.ui.main.MainViewModel
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

private const val PREF_NOTES = "NOTES"
private const val PREF_PUBLIC_KEY = "PUBLIC_KEY"

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
        loadPublicKey()
    }

    override fun onPause() {
        saveNotes()
        savePublicKey()
        super.onPause()
    }

    private fun loadNotes() {
        getPreferences(MODE_PRIVATE).getStringSet(PREF_NOTES, null)?.forEach {
            val parts = it.split("\n".toRegex(), 2)
            viewModel.setNote(parts[0], parts[1])
        }
    }

    private fun saveNotes() {
        viewModel.noteList.value?.let { notes ->
            getPreferences(MODE_PRIVATE).edit()
                .putStringSet(
                    PREF_NOTES,
                    notes.map { "$it\n${viewModel.getNote(it)}" }.toSet()
                ).apply()
        }
    }

    private fun loadPublicKey() {
        getPreferences(MODE_PRIVATE).getString(PREF_PUBLIC_KEY, null)?.let {
            val bytes = Base64.decode(it, Base64.DEFAULT)
            val kf = KeyFactory.getInstance("RSA")
            val spec = X509EncodedKeySpec(bytes)
            viewModel.publicKey = kf.generatePublic(spec)
        }
    }

    private fun savePublicKey() {
        viewModel.publicKey?.let {
            getPreferences(MODE_PRIVATE).edit()
                .putString(PREF_PUBLIC_KEY, Base64.encodeToString(it.encoded, Base64.DEFAULT))
                .apply()
        }
    }
}