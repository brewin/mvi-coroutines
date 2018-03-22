package com.github.brewin.mvicoroutines.view.base

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

@Suppress("UNCHECKED_CAST")
class GenericListAdapter<V : View, T>(
    @LayoutRes private val layoutResId: Int,
    items: List<T> = emptyList(),
    private val init: (V, T) -> Unit
) : RecyclerView.Adapter<GenericListAdapter.ViewHolder<V, T>>() {

    var items = items
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<V, T> {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false) as V
        return ViewHolder(view, init)
    }

    override fun onBindViewHolder(holder: ViewHolder<V, T>, position: Int) {
        holder.bindData(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder<V : View, in T>(
        view: V,
        private val init: (V, T) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bindData(item: T) {
            init(itemView as V, item)
        }
    }
}