package org.caojun.ttschulte.activity

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_welcome.*
import org.caojun.particle.ParticleView
import org.caojun.ttschulte.Constant
import org.caojun.ttschulte.R
import org.caojun.utils.DataStorageUtils
import org.jetbrains.anko.startActivity


class WelcomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val switchOn = DataStorageUtils.loadBoolean(this, Constant.Key_Switch_Welcome, true)
        if (!switchOn) {
            doGoNext()
            return
        }

        particleView.startAnim()
        particleView.setOnParticleAnimListener(object : ParticleView.ParticleAnimListener {
            override fun onAnimationEnd() {
                doGoNext()
            }
        })

        tvSkip.setOnClickListener {
            particleView.pauseAnim()
            DataStorageUtils.saveBoolean(this@WelcomeActivity, Constant.Key_Switch_Welcome, false)
            doGoNext()
        }
    }

    private fun doGoNext() {
        startActivity<GameActivity>()
        finish()
    }
}