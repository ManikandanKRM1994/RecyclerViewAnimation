package com.krm.recyclerviewanimation

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.krm.recyclerviewanimation.animators.*

class AnimatorSampleActivity : AppCompatActivity() {

    internal enum class Type(val animator: BaseItemAnimator) {
        FadeIn(FadeInAnimator()),
        FadeInDown(FadeInDownAnimator()),
        FadeInUp(FadeInUpAnimator()),
        FadeInLeft(FadeInLeftAnimator()),
        FadeInRight(FadeInRightAnimator()),
        Landing(LandingAnimator()),
        ScaleIn(ScaleInAnimator()),
        ScaleInTop(ScaleInTopAnimator()),
        ScaleInBottom(ScaleInBottomAnimator()),
        ScaleInLeft(ScaleInLeftAnimator()),
        ScaleInRight(ScaleInRightAnimator()),
        FlipInTopX(FlipInTopXAnimator()),
        FlipInBottomX(FlipInBottomXAnimator()),
        FlipInLeftY(FlipInLeftYAnimator()),
        FlipInRightY(FlipInRightYAnimator()),
        SlideInLeft(SlideInLeftAnimator()),
        SlideInRight(SlideInRightAnimator()),
        SlideInDown(SlideInDownAnimator()),
        SlideInUp(SlideInUpAnimator()),
        OvershootInRight(OvershootInRightAnimator(1.0f)),
        OvershootInLeft(OvershootInLeftAnimator(1.0f))
    }

    private val adapter = MainAdapter(this, SampleData.LIST.toMutableList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animator_sample)

        setSupportActionBar(findViewById(R.id.tool_bar))
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val recyclerView = findViewById<RecyclerView>(R.id.list)
        recyclerView.apply {
            itemAnimator = SlideInLeftAnimator()
            adapter = this@AnimatorSampleActivity.adapter

            layoutManager = if (intent.getBooleanExtra(MainActivity.KEY_GRID, true)) {
                GridLayoutManager(context, 2)
            } else {
                LinearLayoutManager(context)
            }
        }


        val spinner = findViewById<Spinner>(R.id.spinner)
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        for (type in Type.values()) {
            spinnerAdapter.add(type.name)
        }
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                recyclerView.itemAnimator = Type.values()[position].animator
                recyclerView.itemAnimator!!.addDuration = 500
                recyclerView.itemAnimator!!.removeDuration = 500
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // no-op
            }
        }

        findViewById<View>(R.id.add).setOnClickListener { adapter.add("newly added item", 1) }

        findViewById<View>(R.id.del).setOnClickListener { adapter.remove(1) }
    }

}
