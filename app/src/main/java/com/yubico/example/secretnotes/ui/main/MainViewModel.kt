package com.yubico.example.secretnotes.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val noteStore = linkedMapOf("One" to "lorem ipsum", "Two" to "The brown fox")

    private val _noteList = MutableLiveData(noteStore.keys.toList().sorted())
    val noteList: LiveData<List<String>> = _noteList

    fun getNote(noteId: String) = noteStore[noteId]

    fun setNote(noteId: String, content: String) {
        noteStore[noteId] = content
        _noteList.postValue(noteStore.keys.toList().sorted())
    }
}