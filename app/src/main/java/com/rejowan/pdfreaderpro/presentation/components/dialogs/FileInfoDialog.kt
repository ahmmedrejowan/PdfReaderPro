package com.rejowan.pdfreaderpro.presentation.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.util.FormattingUtils

@Composable
fun FileInfoDialog(
    pdfFile: PdfFile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File Information") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoRow(label = "Name", value = pdfFile.name)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                InfoRow(label = "Size", value = FormattingUtils.formattedFileSize(pdfFile.size))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                InfoRow(label = "Pages", value = "${pdfFile.pageCount} pages")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                InfoRow(label = "Modified", value = FormattingUtils.formattedDate(pdfFile.dateModified))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                InfoRow(label = "Location", value = pdfFile.parentFolder)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false).padding(start = 16.dp)
        )
    }
}
