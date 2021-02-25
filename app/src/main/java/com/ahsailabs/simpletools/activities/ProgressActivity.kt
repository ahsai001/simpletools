package com.ahsailabs.simpletools.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ahsailabs.simpletools.R
import com.ahsailabs.simpletools.models.ReadQuranLogModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.zaitunlabs.zlcore.utils.CommonUtil
import com.zaitunlabs.zlcore.utils.ViewBindingUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProgressActivity : AppCompatActivity() {
    lateinit var viewBindingUtils: ViewBindingUtil<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        val logList = intent.getParcelableArrayListExtra<ReadQuranLogModel>("logList")
        val ayatList = intent.getIntegerArrayListExtra("ayatList")

        viewBindingUtils = ViewBindingUtil.initWithParentView(findViewById<View>(R.id.root))

        val entries: MutableList<Entry> = ArrayList()

        val dataPair = getProgressList(logList, ayatList)

        for (i in dataPair.first.indices){
            entries.add(Entry(i.toFloat(), dataPair.first[i].toFloat()));
        }

        val dataSet = LineDataSet(entries,"Jumlah ayat dibaca akumulasi setiap hari")
        val lineData = LineData(dataSet)

        val lineChart: LineChart = viewBindingUtils.getViewWithId(R.id.lcProgress) as LineChart

        lineChart.data = lineData

        lineChart.xAxis.labelRotationAngle = 60F
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChart.xAxis.valueFormatter = IAxisValueFormatter {
            value, axis ->  dataPair.second[value.toInt()]

        }

        lineChart.description.isEnabled = false

        lineChart.setVisibleXRangeMaximum(10F)

        if(dataPair.second.size - 10F >= 0)
            lineChart.moveViewToX(dataPair.second.size - 10F)

        lineChart.invalidate()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Progress Chart")
    }



    fun getProgressList(logList: List<ReadQuranLogModel>?, ayatList: ArrayList<Int>?): Pair<List<Int>,List<String>> {
        logList?.let {
            if(it.size > 1){
                //reversed list
                val reversedlogList = it.reversed()

                val progressList = ArrayList<Int>()
                val progressDateList = ArrayList<Date>()

                for (i in reversedlogList.indices){
                    if(i == 0) continue
                    if(reversedlogList[i].nomor == reversedlogList[i-1].nomor){
                        //case 1 : kalo dalam 1 surat ==> ayat2-ayat1
                        progressDateList.add(reversedlogList[i]._created_at)
                        progressList.add(reversedlogList[i].ayat - reversedlogList[i-1].ayat)
                    } else {
                        /*
                            case 2 : kalo beda surat {
                                total ayat 1 - ayat1 + 1
                                for (surat1+1 to surat2-1){
                                    +totalayat
                                }
                                +ayat2
                            }
                         */

                        var totalAyat = 0

                        ayatList?.let {
                            var totalAyat = ayatList[reversedlogList[i-1].nomor-1] - reversedlogList[i-1].ayat + 1
                            for (x in (reversedlogList[i-1].nomor+1) until (reversedlogList[i].nomor-1)){
                                totalAyat += ayatList[x-1]
                            }
                        }

                        totalAyat += reversedlogList[i].ayat

                        progressDateList.add(reversedlogList[i]._created_at)
                        progressList.add(totalAyat)
                    }
                }


                //grouping by same date
                val groupedValueList = ArrayList<Int>()
                val groupedDateList = ArrayList<String>()

                var prevDateString: String = ""
                var lastIndex = -1

                //Log.e("ahmad","progressList : ${progressList.size}")
                //Log.e("ahmad","progressDateList : ${progressDateList.size}")

                for (ix in progressList.indices){
                    val df = SimpleDateFormat("MMM dd, yyyy", CommonUtil.getCurrentDeviceLocale(this))
                    val dateString = df.format(progressDateList[ix])

                    //Log.e("ahmad","date $ix : $dateString")
                    if(dateString == prevDateString){
                        groupedValueList[lastIndex] = groupedValueList[lastIndex]+progressList[ix]
                    } else {
                        groupedValueList.add(progressList[ix])
                        groupedDateList.add(dateString)
                        lastIndex = groupedValueList.size-1
                        prevDateString = dateString
                    }
                }


                return Pair(groupedValueList, groupedDateList)
            }
        }

        return Pair(emptyList(), emptyList())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        fun start(context: Context, logModelList: ArrayList<ReadQuranLogModel>, ayatList: ArrayList<Int>){
            val intent = Intent(context,ProgressActivity::class.java)
            intent.putParcelableArrayListExtra("logList", logModelList)
            intent.putExtra("ayatList",ayatList)
            context.startActivity(intent)
        }

    }




}
