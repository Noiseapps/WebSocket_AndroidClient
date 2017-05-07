package com.noiseapps.websockets

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ListAdapter(val context: Context) : RecyclerView.Adapter<ListAdapter.Companion.ViewHolder>() {

    val items: MutableList<Message> = ArrayList()

    fun addItem(message: Message) {
        items.add(message)
        this.notifyItemInserted(items.count() - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = inflater.inflate(R.layout.item_cell, parent, false)
        return ViewHolder(context, binding)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    companion object {
        class ViewHolder(val context: Context, rootView: View) : RecyclerView.ViewHolder(rootView) {
            val label: TextView = rootView.findViewById(R.id.cellTitle) as TextView

            fun bind(message: Message) {
                label.text = String.format("%s - %s", message.title, message.message)
                if (message.title?.equals("status", true) ?: false) {
                    this.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen))
                } else {
                    this.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBlue))
                }
            }
        }
    }
}