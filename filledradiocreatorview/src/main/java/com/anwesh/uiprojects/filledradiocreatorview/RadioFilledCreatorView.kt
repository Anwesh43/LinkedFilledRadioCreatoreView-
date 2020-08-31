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
val deg : Float = 180f
val start : Float = -90f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawRadioFilledCreator(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts + 1)
    val sf2 : Float = sf.divideScale(1, parts + 1)
    val r : Float = Math.min(w, h) / sizeFactor
    val concR : Float = Math.min(w, h) / concFactor
    save()
    translate(w / 2, h / 2)
    paint.style = Paint.Style.STROKE
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        drawArc(RectF(-r, -r, r, r), start, deg * sf1, false, paint)
        restore()
    }
    paint.style = Paint.Style.FILL
    drawCircle(0f, 0f, r * sf2, paint)
    restore()
}

fun Canvas.drawRFCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(w / 2, h / 2)
    drawRadioFilledCreator(scale, w, h, paint)
    restore()
}

class RadioFilledCreatorView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RFCNode(var i : Int, val state : State = State()) {

        private var next : RFCNode? = null
        private var prev : RFCNode? = null

        init {

        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = RFCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRFCNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RFCNode {
            var curr : RFCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RadioFilledCreator(var i : Int) {

        private var curr : RFCNode = RFCNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RadioFilledCreatorView) {

        private val animator : Animator = Animator(view)
        private val rfc : RadioFilledCreator = RadioFilledCreator(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            rfc.draw(canvas, paint)
            animator.animate {
                rfc.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rfc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : RadioFilledCreatorView {
            val view : RadioFilledCreatorView = RadioFilledCreatorView(activity)
            activity.setContentView(view)
            return view
        }
    }
}