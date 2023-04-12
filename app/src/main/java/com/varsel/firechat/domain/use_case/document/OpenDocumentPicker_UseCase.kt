package com.varsel.firechat.domain.use_case.document

import android.content.Intent
import androidx.fragment.app.Fragment
import javax.inject.Inject

const val PICK_DOCUMENT_REQUEST_CODE = 123
val mimeTypes = arrayListOf(
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
    "text/plain",
    "application/pdf",
    "application/zip"
)
class OpenDocumentPicker_UseCase @Inject constructor(

) {
    operator fun invoke(fragment: Fragment) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        fragment.startActivityForResult(intent, PICK_DOCUMENT_REQUEST_CODE)
    }
}