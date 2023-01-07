package com.varsel.firechat.domain.use_case._util.animation

import com.google.android.material.bottomsheet.BottomSheetDialog

class ChangeDialogDimAmountUseCase {
    operator fun invoke(dialog: BottomSheetDialog, amount: Float){
        if(dialog.window != null){
            dialog.window!!.setDimAmount(amount)
        }
    }
}