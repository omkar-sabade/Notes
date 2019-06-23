package com.notes.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.notes.R
import com.notes.model.Note
import kotlinx.android.synthetic.main.activity_add_edit_note.*
import kotlinx.android.synthetic.main.activity_add_edit_note.description

class AddEditNoteActivity : AppCompatActivity() {

    companion object {
        const val UPDATE_EXTRA = "UPDATE"
        const val ID_EXTRA = "ID"
        const val TITLE = "TITLE"
        const val DESC = "DESC"
        const val PRIORITY = "PRIORITY"
        const val ISCHECKED = "ISCHECKED"

    }

    private lateinit var addEditNoteViewModel: AddEditNoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)


        num_picker.minValue = 1
        num_picker.maxValue = 10

        addEditNoteViewModel = ViewModelProviders.of(this).get(AddEditNoteViewModel::class.java)

        addEditNoteViewModel.notes.observe(this, Observer {
            it?.forEach { note ->
                Log.d("Add", note.title)
            }
        })


        if (intent.hasExtra(UPDATE_EXTRA)) {
            title_textview.setText(intent.getStringExtra(TITLE).capitalize())
            description.setText(intent.getStringExtra(DESC))
            num_picker.value = intent.getIntExtra(PRIORITY, 3)
            pin_to_top.isChecked = intent.getBooleanExtra(ISCHECKED, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.save_note -> {
            Log.d("AddEditNoteActivity", "Saving note")
            saveNote()
            true
        }
        else -> false
    }

    private fun saveNote() {

        if (title_textview.text.isEmpty() || description.text.isEmpty()) {
            Toast.makeText(
                this@AddEditNoteActivity,
                "Title and description cannot be empty",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        when {
            intent.hasExtra(UPDATE_EXTRA) -> {
                val id = intent.getIntExtra(ID_EXTRA, -1)
                addEditNoteViewModel.update(
                    Note(
                        title = title_textview.text.toString(),
                        description = description.text.toString(),
                        priority = if (pin_to_top.isChecked) 10 else 0,
                        pinToTop = pin_to_top.isChecked
                    ).apply {
                        this.id = id
                    }
                )
            }
            else -> addEditNoteViewModel.insert(
                Note(
                    title = title_textview.text.toString(),
                    description = description.text.toString(),
                    priority = num_picker.value,
                    pinToTop = pin_to_top.isChecked
                )
            )
        }
        finish()
    }
}
