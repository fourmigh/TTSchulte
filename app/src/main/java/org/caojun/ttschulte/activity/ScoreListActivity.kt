package org.caojun.ttschulte.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.RadioButton
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import kotlinx.android.synthetic.main.activity_score_list.*
import kotlinx.android.synthetic.main.layout_type.*
import org.caojun.ttschulte.Constant
import org.caojun.ttschulte.R
import org.caojun.ttschulte.adapter.ScoreAdapter
import org.caojun.ttschulte.room.Score
import org.caojun.ttschulte.room.ScoreBmob
import org.caojun.ttschulte.room.TTSDatabase
import org.caojun.ttschulte.utils.Schulte
import org.caojun.utils.TimeUtils
import org.caojun.widget.MultiRadioGroup
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by CaoJun on 2018-1-16.
 */
class ScoreListActivity : BaseActivity() {

    private var layout = 0
    private var type = 0
    private var isLocal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_list)

        tvChinese.visibility = View.GONE

        this.title = intent.getStringExtra(Constant.Key_Title)

        layout = intent.getIntExtra(Constant.Key_Layout, Schulte.Layout_9)
        type = intent.getIntExtra(Constant.Key_Type, Schulte.Type_Natural)
        isLocal = intent.getBooleanExtra(Constant.Key_IsLocal, true)
        readData()

        rgLayout.setOnCheckedChangeListener(object : MultiRadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: MultiRadioGroup, checkedId: Int) {
                layout = group.indexOfChild(group.findViewById(checkedId))
                readData()
            }
        })

//        rgLayout.setOnCheckedChangeListener { radioGroup, i ->
//            layout = radioGroup.indexOfChild(radioGroup.findViewById(i))
//            readData()
//        }

        rgType.setOnCheckedChangeListener(object : MultiRadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: MultiRadioGroup, checkedId: Int) {
                type = group.indexOfChild(group.findViewById(checkedId))
                readData()
            }
        })

//        rgType.setOnCheckedChangeListener { radioGroup, i ->
//            type = radioGroup.indexOfChild(radioGroup.findViewById(i))
//            readData()
//        }
    }

    private fun readData() {
        (rgLayout.getChildAt(layout) as RadioButton).isChecked = true
        (rgType.getChildAt(type) as RadioButton).isChecked = true

        doAsync {
            if (isLocal) {
                val list = TTSDatabase.getDatabase(this@ScoreListActivity).getScore().query(layout, type)
                uiThread {
                    listView.adapter = ScoreAdapter(this@ScoreListActivity, list)
                }
            } else {
                val query = BmobQuery<ScoreBmob>()
                query.addWhereEqualTo("layout", layout)
                query.addWhereEqualTo("type", type)
                query.order("score")
                query.findObjects(object : FindListener<ScoreBmob>() {
                    override fun done(list: MutableList<ScoreBmob>?, e: BmobException?) {
                        uiThread {
                            listView.adapter = ScoreAdapter(this@ScoreListActivity, online2local(list))
                        }
                    }
                })
            }
        }
    }

    private fun online2local(list: MutableList<ScoreBmob>?): ArrayList<Score>? {
        if (list == null || list.isEmpty()) {
            return null
        }

        val filters = ArrayList<ScoreBmob>()

        //保留最好成成绩
        filters.add(list[0])

        //过滤非本月数据
        for (i in 1 until list.size) {
            val time = list[i].time
            val date = TimeUtils.getDate(time)
            if (TimeUtils.isThisMonth(date)) {
                filters.add(list[i])
            }
        }

        val scores = ArrayList<Score>()
        filters.indices.mapTo(scores) { Score(filters[it]) }
        return scores
    }
}