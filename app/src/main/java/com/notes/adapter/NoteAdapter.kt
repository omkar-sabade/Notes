package com.notes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notes.R
import com.notes.model.Note
import kotlinx.android.synthetic.main.note_item.view.*

class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem
        }
    }

    var listener: OnItemClickListener? = null


    override fun onBindViewHolder(noteHolder: NoteHolder, position: Int) {
        val note = getItem(position)
        noteHolder.textViewDescription.text = note.description
        noteHolder.textViewPriority.text = ""
        noteHolder.textViewTitle.text = note.title
        noteHolder.textViewPriority.setBackgroundResource(if (note.pinToTop) R.drawable.ic_star_white_24dp else R.drawable.ic_star_border_black_24dp)
    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NoteHolder =
        NoteHolder(LayoutInflater.from(p0.context).inflate(R.layout.note_item, p0, false))

    operator fun get(adapterPosition: Int): Note = getItem(adapterPosition)

    inner class NoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    listener?.onItemClick(getItem(adapterPosition))
            }
        }

        var textViewTitle: TextView = itemView.title
        var textViewDescription: TextView = itemView.description
        var textViewPriority: TextView = itemView.priority

    }

    interface OnItemClickListener {
        fun onItemClick(note: Note)
    }
}