package com.varsel.firechat.domain.use_case._util

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class AddRecyclerViewSeparator_UseCase {
    operator fun invoke(recyclerView: RecyclerView, fragment: Fragment) {
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                fragment.requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }
}