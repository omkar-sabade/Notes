package com.notes.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.notes.R
import com.notes.adapter.NoteAdapter
import com.notes.model.Note
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var noteViewModel: NoteViewModel
    lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        recyclerView.hasFixedSize()

        adapter = NoteAdapter()
        recyclerView.adapter = adapter

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        noteViewModel.notes.observe(this, Observer {
            adapter.submitList(it!!)
            num_res.text = it.size.toString()
        })

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean =
                false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                noteViewModel.delete(adapter[viewHolder.adapterPosition])
                Toast.makeText(this@MainActivity, "Note deleted", Toast.LENGTH_SHORT).show()
            }
        }).attachToRecyclerView(recyclerView)

        adapter.listener = object : NoteAdapter.OnItemClickListener {
            override fun onItemClick(note: Note) {
                val intent = Intent(baseContext, AddEditNoteActivity::class.java)
                intent.putExtra(AddEditNoteActivity.UPDATE_EXTRA, true)
                intent.putExtra(AddEditNoteActivity.ID_EXTRA, note.id)
                intent.putExtra(AddEditNoteActivity.PRIORITY, note.priority)
                intent.putExtra(AddEditNoteActivity.DESC, note.description)
                intent.putExtra(AddEditNoteActivity.TITLE, note.title)
                intent.putExtra(AddEditNoteActivity.ISCHECKED, note.pinToTop)
                intent.putExtra(AddEditNoteActivity.SRC, note.source)
                startActivity(intent)
            }
        }

        add_note.setOnClickListener {
            startActivity(Intent(baseContext, AddEditNoteActivity::class.java))
        }

        search_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, s.toString())
                noteViewModel.getAllNotes(s.toString())
                noteViewModel.notes.observe(this@MainActivity, Observer {
                    adapter.submitList(it!!)
                    num_res.text = it.size.toString()
                })
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.delete_all_notes -> {
            noteViewModel.deleteAllNotes()
            Toast.makeText(this@MainActivity, "All notes deleted", Toast.LENGTH_SHORT).show()
            true
        }
        else -> false
    }

    companion object {
        val firebaseFirestore = FirebaseFirestore.getInstance()
        const val TAG = "MainActivity"
    }
}
