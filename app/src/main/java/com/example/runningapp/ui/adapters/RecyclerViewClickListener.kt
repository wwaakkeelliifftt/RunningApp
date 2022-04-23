package com.example.runningapp.ui.adapters

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 *  Look is like very cool solution, but...
 *  two inline fun can't work together - always fail invoke at onLongItemClick(), triggered when we
 *  have just 1 touch for invoke onItemClick()..
 *  But.. longClick works fine, when we use only this fun in fragment
 *
 * */

inline fun RecyclerView.onItemClick(crossinline click: (position: Int) -> Unit) =
    setOnItemClickListener(onClick = click)

inline fun RecyclerView.onLongItemClick(crossinline click: (position: Int) -> Unit) =
    setOnItemClickListener(onLongClick = click)

inline fun RecyclerView.setOnItemClickListener(
    crossinline onClick: (position: Int) -> Unit = { },
    crossinline onLongClick: (position: Int) -> Unit = { }
) {
    addOnItemTouchListener(
        RecyclerItemClickListener(
            this,
            object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    onClick.invoke(position)
                }
                override fun onLongItemClick(child: View, position: Int) {
                    onLongClick.invoke(position)
                }
            }
        )
    )
}

class RecyclerItemClickListener(
    recyclerView: RecyclerView,
    private val mListener: OnItemClickListener?
) : RecyclerView.OnItemTouchListener {

    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int)
        fun onLongItemClick(child: View, position: Int)
    }

    private var mGestureDetector: GestureDetector = GestureDetector(
        recyclerView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
            override fun onLongPress(event: MotionEvent) {
                val child = recyclerView.findChildViewUnder(event.x, event.y)
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        }
    )

    override fun onInterceptTouchEvent(recycler: RecyclerView, event: MotionEvent): Boolean {
        val childView = recycler.findChildViewUnder(event.x, event.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(event)) {
            mListener.onItemClick(childView, recycler.getChildAdapterPosition(childView))
            return true
        }
        return false
    }


    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) { }
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) { }

}


