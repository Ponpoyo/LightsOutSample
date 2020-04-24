package com.example.firstapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myView = MyView(this)
        setContentView(myView)
    }


    internal inner class MyView(context: Context) : View(context) {
        private var paint: Paint = Paint()
//        private var debugText = "debug"


        private var blockNumOfX = 4 //ブロック個数x
        private var blockNumOfY = 4 //ブロック個数y
        private val margin = 50f //余白
        private var startY = height * 1 / 3f
        private var xWidth = width - margin * 2
        private var sqSize: Float = 0f

        private var level: Int = 1;

        val myHandler = Handler()
        var time = 30
        val addTime = 2  //ボーナスのタイム
        var reverseNumber = 3 //ひっくり返す回数
        var slipNumber = 1 //スキップ可能回数

        //状態
        var state: String = "Title"


        //フィールド状態リスト
        //-1:白 1:赤
        val fieldList: MutableList<MutableList<Int>> = mutableListOf()

        val runnable = object : Runnable {
            override fun run() {
                if (state == "Main") {
                    time--
//                println("tt $time")
                    invalidate()
                }
                myHandler.postDelayed(this, 1000)
            }
        }

        //初期化
        init {
            resetField()
            createField()
            myHandler.post(runnable)
        }

        private fun resetField() {
            fieldList.clear()
            for (i in 1..blockNumOfY) {
                val temp: MutableList<Int> = mutableListOf()
                for (j in 1..blockNumOfX) {
                    temp.add(1)
                }
                fieldList.add(temp)
            }
            invalidate()
        }

        @SuppressLint("ServiceCast", "DrawAllocation")
        override fun onDraw(canvas: Canvas) {
            val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val disp = wm.getDefaultDisplay()
            val size = Point()
            disp.getSize(size)

            if (state == "Title") {
                //テキスト
//                paint.style = Paint.Style.STROKE
//                paint.color = Color.argb(255, 60, 80, 80) //カラー
                paint.color = Color.argb(255, 180, 235, 190) //カラー
                paint.strokeWidth = 1f //線幅
                paint.textSize = 60f
                paint.textAlign = Paint.Align.CENTER
                //レベル
                canvas.drawText("Lights!", width / 2f, height / 2f - 50, paint)
                paint.textSize = 30f
                canvas.drawText("β-version", width / 2f+50f, height / 2f, paint)
                paint.textSize = 30f
                canvas.drawText("Tap to play!", width / 2f, height / 2f + 80, paint)
            }
            if (state == "Main") {

                startY = height * 1 / 3f
                xWidth = width - margin * 2
                sqSize = xWidth / blockNumOfX //ブロック幅高


                //四角形表示
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.color = Color.argb(255, 190, 200, 255) //カラー
                paint.strokeWidth = 5f //線幅
                for (j in 1..blockNumOfY) {
                    for (i in 1..blockNumOfX) {
                        //四角形表示
                        //外周
                        paint.style = Paint.Style.STROKE
                        paint.color = Color.argb(255, 190, 255, 200) //カラー
                        canvas.drawRect(
                            margin + sqSize * (i - 1), startY + sqSize * (j - 1),
                            margin + sqSize * i, startY + sqSize * j,
                            paint
                        )
                        //内部
                        paint.style = Paint.Style.FILL

                        //赤
                        if (fieldList[j - 1][i - 1] == 1)
                            paint.color = Color.argb(150, 255, 0, 0) //カラー
                        //白
                        if (fieldList[j - 1][i - 1] == -1)
                            paint.color = Color.argb(150, 255, 255, 255) //カラー
                        canvas.drawRect(
                            margin + sqSize * (i - 1), startY + sqSize * (j - 1),
                            margin + sqSize * i, startY + sqSize * j,
                            paint
                        )
                    }
                }

                //テキスト
                paint.color = Color.argb(255, 0, 0, 0) //カラー
                paint.strokeWidth = 1f //線幅
                paint.textSize = 40f
                paint.textAlign = Paint.Align.LEFT
                //レベル
                canvas.drawText("Level :  $level", 50f, 70f, paint)

                //タイマー
                val timer = Timer()
                when {
                    time < 20 -> paint.color = Color.argb(255, 255, 0, 0) //赤
                    20 <= time -> paint.color = Color.argb(255, 0, 0, 0) //黒
                }
                paint.strokeWidth = 2f //線幅
                paint.textSize = 60f
                paint.textAlign = Paint.Align.CENTER
                //レベル
                canvas.drawText("$time", width / 2f, 200f, paint)


                //クリアー
                if (isClear()) {
                    println("LEVEL CLEAR!! $reverseNumber")
                    time += addTime * level //時間加える
                    level++ //レベルアップ

                    setting()

                    resetField() //すべて赤に戻す
                    createField() //問題を作る
                }

                //ゲームオーバー
                if (time <= 0) {
                    println("GAME OVER")
                    state = "GameOver"
                    invalidate()
                }
            }
            if (state == "GameOver") {
                //テキスト
//                paint.style = Paint.Style.STROKE
//                paint.color = Color.argb(255, 60, 80, 80) //カラー
                paint.color = Color.argb(255, 180, 235, 190) //カラー
                paint.strokeWidth = 1f //線幅
                paint.textSize = 60f
                paint.textAlign = Paint.Align.CENTER
                //レベル
                canvas.drawText("GAME OVER", width / 2f, height / 2f - 50, paint)
                paint.textSize = 50f
                canvas.drawText("Level :  $level", width / 2f, height / 2f, paint)
                paint.textSize = 30f
                canvas.drawText("Tap to restart!", width / 2f, height / 2f + 80, paint)
            }
        }

        var te = 0
        //問題難易度設定
        private fun setting() {

            when {
                level <= 3 -> {
                    blockNumOfX = 4
                    blockNumOfY = 4
                    reverseNumber++
                }
                level == 4 -> {
                    blockNumOfX = 5
                    reverseNumber = 3
                }
                level <= 6 -> {
                    reverseNumber++
                }
                level == 7 -> {
                    blockNumOfY = 5
                    reverseNumber = 3
                }
                level <= 9 -> {
                    reverseNumber++
                }
                level == 10 -> {
                    blockNumOfX = 5
                    reverseNumber = 3
                }
                level <= 12 -> {
                    reverseNumber++
                }
                level == 13 -> {
                    blockNumOfY = 5
                    reverseNumber = 4
                }
                level <= 15 -> {
                    reverseNumber++
                }
                level == 16 -> {
                    blockNumOfX = 6
                    reverseNumber = 4
                }
                level <= 18 -> {
                    reverseNumber++
                }
                level == 19 -> {
                    blockNumOfY = 6
                    reverseNumber = 5
                }
                level <= 21 -> {
                    reverseNumber++
                }
                level == 22 -> {
                    blockNumOfX = 7
                    reverseNumber = 5
                }
                level <= 25 -> {
                    reverseNumber++
                }
                level == 26 -> {
                    blockNumOfY = 7
                    reverseNumber = 5
                }
                level <= 28 -> {
                    reverseNumber++
                }
                level == 29 -> {
                    blockNumOfX = 8
                    reverseNumber = 5
                }
                level <= 31 -> {
                    reverseNumber++
                }
                level == 32 -> {
                    blockNumOfY = 8
                    reverseNumber = 6
                }
                level <= 34 -> {
                    reverseNumber++
                }
                level==35 ->{
                    reverseNumber = 10
                }
                else ->{
                    te++
                    if(te%2==0){
                        reverseNumber++
                    }
                    if(te==5){
                        if(blockNumOfX<=8){
                            blockNumOfX++
                        }else{
                            blockNumOfX=4
                        }
                    }
                    if(te==10){
                        if(blockNumOfY<=8){
                            blockNumOfY++
                        }else{
                            blockNumOfY=4
                        }
                        te=0
                    }
                }
            }
        }


        //クリアーしたかどうか
        private fun isClear(): Boolean {
            fieldList.forEach { idx ->
                idx.forEach { idx ->
                    if (idx == -1) return false
                }
            }
            return true
        }

        //問題を作る
        private fun createField() {
            for (i in 1..reverseNumber) {
                val rx = Random.nextInt(blockNumOfX - 1)
                val ry = Random.nextInt(blockNumOfY - 1)
                println("rx $rx, ry $ry")
                reverse(rx, ry)
            }
        }

        //クリックしたときにひっくり返す
        private fun clickAtPoint(event: MotionEvent) {
//            debugText = "${event.x} ${event.y}"
            var clickX: Int = -1
            var clickY: Int = -1

            for (i in 1..blockNumOfX) {
                println("aaaaa ${margin + sqSize * (i - 1)} , ${margin + sqSize * i}")
                if (margin + sqSize * (i - 1) <= event.x && event.x <= margin + sqSize * i) {
                    clickX = i - 1
                    break
                }
            }
            for (i in 1..blockNumOfY) {
//                        println("cccccccc ${startY*(i-1)}, ${startY*i}")
                if (startY + sqSize * (i - 1) <= event.y && event.y <= startY + sqSize * i) {
                    clickY = i - 1
                    break
                }
            }

            //ひっくり返す
            reverse(clickX, clickY)



            println("click $clickX $clickY")
        }

        //ひっくり返す
        private fun reverse(clickX: Int, clickY: Int) {
            if (clickX != -1 && clickY != -1) { //枠内をタップしたとき
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (clickX + i in 0..blockNumOfX - 1 && clickY + j in 0..blockNumOfY - 1) {
                            println("x: ${clickX + i}  y: ${clickY + j}")
                            fieldList[clickY + j][clickX + i] *= -1


                            fieldList.forEach { idx ->
                                idx.forEach { idx ->
                                    print("$idx ")
                                }
                                print("\n")
                            }
                        }
                    }
                }
            }
        }


        //タップ時
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (state == "Title") {

                //状態
                state = "Main"
            }
            if (state == "Main") {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> clickAtPoint(event)
                }
                println("タップしたお!")
                for (i in 0..blockNumOfY - 1) {
                    for (j in 0..blockNumOfX - 1) {
                        print("${fieldList[i][j]} ")
                    }
                    print("\n")
                }

                invalidate() //再描画
            }
            if (state == "GameOver") {
                blockNumOfX = 4 //ブロック個数x
                blockNumOfY = 4 //ブロック個数y
                startY = height * 1 / 3f
                xWidth = width - margin * 2
                sqSize = 0f

                level = 1;

                time = 60
                reverseNumber = 3 //ひっくり返す回数
                slipNumber = 1 //スキップ可能回数

                state = "Main"
            }
            return super.onTouchEvent(event)
        }
    }


}

