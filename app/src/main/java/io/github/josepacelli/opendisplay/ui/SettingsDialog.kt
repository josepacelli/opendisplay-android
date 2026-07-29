package io.github.josepacelli.opendisplay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.josepacelli.opendisplay.net.PhoneReceiver

/**
 * The only "settings" this receiver has: the mDNS name the Mac's WiFi picker
 * shows, and — for when mDNS doesn't reach it (some networks block
 * multicast) — this device's own IP address to type into the Mac app's host
 * override by hand. Shown only while disconnected; once video is flowing
 * this app has no chrome at all.
 */
@Composable
fun SettingsDialog(receiver: PhoneReceiver, onDismiss: () -> Unit) {
    val currentName by receiver.serviceName.collectAsState()
    var draftName by remember { mutableStateOf(currentName) }
    val addressHint = remember { receiver.localAddressHint() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurações") },
        text = {
            Column {
                Text(
                    text = "Nome anunciado na rede (aparece no menu de conexão do Mac):",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                )
                Text(
                    text = "Se o Mac não encontrar este aparelho automaticamente por WiFi, " +
                        "informe o endereço abaixo manualmente no app do Mac:",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = addressHint ?: "Endereço IP indisponível — verifique a conexão WiFi.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                receiver.setServiceName(draftName)
                onDismiss()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
