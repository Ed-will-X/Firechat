package com.varsel.firechat.utils.gestures

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.presentation.signedIn.SignedinActivity
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

abstract class FriendsSwipeGesture(val activity: SignedinActivity): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

        return false
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addSwipeLeftBackgroundColor(activity.resources.getColor(R.color.red_accent))
            .addSwipeLeftActionIcon(R.drawable.ic_trash_white)
            .setSwipeLeftLabelColor(activity.resources.getColor(R.color.white))
            .addSwipeLeftLabel(activity.getString(R.string.unfriend))
            .create()
            .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}