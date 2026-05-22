package com.marconius.wordbopper

import android.app.Application
import com.humanware.keysoftsdk.contextmenu.WriteCommandsXmlFileToInternalMemoryStorageExecutor

class WordBopperApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WriteCommandsXmlFileToInternalMemoryStorageExecutor(this).execute()
    }
}
