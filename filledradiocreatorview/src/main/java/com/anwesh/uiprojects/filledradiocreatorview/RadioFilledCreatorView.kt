package com.anwesh.uiprojects.filledradiocreatorview

/**
 * Created by anweshmishra on 31/08/20.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#FFC107",
        "#3F51B5",
        "#4CAF50",
        "#2196F3"
).map{Color.parseColor(it)}.toTypedArray()
val parts : Int = 2
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 4.8f
val concFactor : Float = 7.2f
val delay : Long = 20
val backColor : Int = Color.parseColor("#bdbdbd")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
