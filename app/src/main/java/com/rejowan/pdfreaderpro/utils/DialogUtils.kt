package com.rejowan.pdfreaderpro.utils

import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import com.rejowan.pdfreaderpro.dataClasses.PdfFile
import com.rejowan.pdfreaderpro.dataClasses.RecentModel
import com.rejowan.pdfreaderpro.databinding.DialogDeleteFilesBinding
import com.rejowan.pdfreaderpro.databinding.DialogInfoFilesBinding
import com.rejowan.pdfreaderpro.interfaces.OnPdfFileClicked
import com.rejowan.pdfreaderpro.interfaces.OnRecentClicked
import java.io.File

class DialogUtils {


    companion object {
        fun showInfoDialog(context: Context, pdfFile: PdfFile) {
            val dialog = Dialog(context)
            val dialogBinding: DialogInfoFilesBinding = DialogInfoFilesBinding.inflate(
                LayoutInflater.from(context)
            )
            dialog.setContentView(dialogBinding.root)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )

            dialogBinding.fileName.text = pdfFile.name
            dialogBinding.fileSize.text = FormattingUtils.formattedFileSize(pdfFile.size)
            dialogBinding.filePath.text = pdfFile.path.substringBeforeLast("/")
            dialogBinding.lastModified.text = FormattingUtils.formattedDate(pdfFile.dateModified)
            dialogBinding.pages.text = FormattingUtils.getPdfPageCount(pdfFile.path).toString()


            dialogBinding.dismiss.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }


        fun sharePDF(context: Context, pdfFile: PdfFile) {

            try {
                val file = File(pdfFile.path)
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "application/pdf"
                val fileUri = FileProvider.getUriForFile(
                    context, context.packageName + ".provider", file
                )
                shareIntent.clipData = ClipData.newRawUri("", fileUri)
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(shareIntent, "Share File"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


        fun sharePDF(context: Context, recentModel: RecentModel) {

            try {
                val file = File(recentModel.path)
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "application/pdf"
                val fileUri = FileProvider.getUriForFile(
                    context, context.packageName + ".provider", file
                )
                shareIntent.clipData = ClipData.newRawUri("", fileUri)
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(shareIntent, "Share File"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


        fun showDeleteDialog(
            context: Context,
            pdfFile: PdfFile,
            onPdfFileClicked: OnPdfFileClicked
        ) {

            val dialog = Dialog(context)
            val dialogBinding: DialogDeleteFilesBinding = DialogDeleteFilesBinding.inflate(
                LayoutInflater.from(context)
            )
            dialog.setContentView(dialogBinding.root)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )

            dialogBinding.fileName.text = pdfFile.name
            dialogBinding.fileSize.text = FormattingUtils.formattedFileSize(pdfFile.size)
            dialogBinding.filePath.text = pdfFile.path.substringBeforeLast("/")

            dialogBinding.cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.delete.setOnClickListener {
                dialog.dismiss()
                onPdfFileClicked.onPdfFileDeleted(pdfFile)
            }


            dialog.show()

        }


        fun showDeleteDialog(
            context: Context,
            recentModel: RecentModel,
            onRecentClicked: OnRecentClicked
        ) {

            val dialog = Dialog(context)
            val dialogBinding: DialogDeleteFilesBinding = DialogDeleteFilesBinding.inflate(
                LayoutInflater.from(context)
            )
            dialog.setContentView(dialogBinding.root)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setLayout(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )

            dialogBinding.fileName.text = recentModel.name
            dialogBinding.fileSize.text = FormattingUtils.formattedFileSize(recentModel.size)
            dialogBinding.filePath.text = recentModel.path.substringBeforeLast("/")

            dialogBinding.cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.delete.setOnClickListener {
                dialog.dismiss()
                onRecentClicked.onDeleted(recentModel)
            }


            dialog.show()

        }


    }


}