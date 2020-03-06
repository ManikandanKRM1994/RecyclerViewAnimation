package com.krm.recyclerviewanimation

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SwitchCompat

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_GRID = "GRID"
    }

    private var enabledGrid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        findViewById<View>(R.id.btn_animator_sample).setOnClickListener {
            startActivity(Intent(this, AnimatorSampleActivity::class.java).apply {
                putExtra(KEY_GRID, enabledGrid)
            })
        }

        findViewById<View>(R.id.btn_adapter_sample).setOnClickListener {
            startActivity(Intent(this, AdapterSampleActivity::class.java).apply {
                putExtra(KEY_GRID, enabledGrid)
            })
        }

        findViewById<SwitchCompat>(R.id.grid).setOnCheckedChangeListener { _, isChecked -> enabledGrid = isChecked }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
