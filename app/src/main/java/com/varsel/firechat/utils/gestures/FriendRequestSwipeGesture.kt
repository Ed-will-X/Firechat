package com.varsel.firechat.utils.gestures

import android.graphics.Canvas
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.varsel.firechat.R
import com.varsel.firechat.view.signedIn.SignedinActivity
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


abstract class FriendRequestSwipeGesture(val activity: SignedinActivity): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {


        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addSwipeLeftBackgroundColor(activity.resources.getColor(R.color.red_accent))
            .addSwipeLeftActionIcon(R.drawable.ic_trash)
            .addSwipeLeftLabel(activity.getString(R.string.reject))
            .addSwipeRightBackgroundColor(activity.resources.getColor(R.color.light_green))
            .addSwipeRightActionIcon(R.drawable.ic_baseline_check_24)
            .addSwipeRightLabel(activity.getString(R.string.accept))
            .create()
            .decorate()


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}