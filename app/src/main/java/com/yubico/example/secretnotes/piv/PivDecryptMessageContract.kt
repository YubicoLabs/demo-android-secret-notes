package com.yubico.example.secretnotes.piv

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.yubico.yubikit.android.ui.YubiKeyPromptActivity
import com.yubico.yubikit.android.ui.YubiKeyPromptConnectionAction
import com.yubico.yubikit.core.application.CommandState
import com.yubico.yubikit.core.smartcard.SmartCardConnection
import com.yubico.yubikit.core.util.Pair
import com.yubico.yubikit.piv.PivSession
import com.yubico.yubikit.piv.Slot
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

private const val EXTRA_SLOT = "SLOT"
private const val EXTRA_ENCRYPTED = "ENCRYPTED"
private const val EXTRA_DECRYPTED = "DECRYPTED"

class PivDecryptMessageContract : ActivityResultContract<Pair<Slot, ByteArray>, ByteArray?>() {
    override fun createIntent(context: Context, input: Pair<Slot, ByteArray>): Intent =
        YubiKeyPromptActivity.createIntent(context, PivDecryptMessageAction::class.java).apply {
            putExtra(EXTRA_SLOT, input.first)
            putExtra(EXTRA_ENCRYPTED, input.second)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): ByteArray? {
        return when(resultCode) {
            Activity.RESULT_OK -> intent!!.getByteArrayExtra(EXTRA_DECRYPTED)
            else -> null
        }
    }
}

class PivDecryptMessageAction :
    YubiKeyPromptConnectionAction<SmartCardConnection>(SmartCardConnection::class.java) {
    override fun onYubiKeyConnection(
        connection: SmartCardConnection,
        extras: Bundle,
        commandState: CommandState?
    ): Pair<Int, Intent>? {
        val slot = extras.getSerializable(EXTRA_SLOT) as Slot
        val encrypted = extras.getByteArray(EXTRA_ENCRYPTED)

        val piv = PivSession(connection)
        piv.verifyPin("123456".toCharArray())
        val decrypted = piv.decrypt(slot, encrypted, Cipher.getInstance("RSA"))

        return Pair(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_DECRYPTED, decrypted)
        })
    }

}