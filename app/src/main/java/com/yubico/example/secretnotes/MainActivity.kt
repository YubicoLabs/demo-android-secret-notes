package com.yubico.example.secretnotes

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yubico.example.secretnotes.ui.main.MainFragment
import com.yubico.example.secretnotes.ui.main.MainViewModel

private const val PREF_NOTES = "NOTES"

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
    }

    override fun onPause() {
        saveNotes()
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
}