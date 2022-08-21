package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.jar.Attributes

class DrawingView(context: Context, attrs:AttributeSet): View(context,attrs){
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null//everything drawn in android is bitmap
    private  var mDrawPaint: Paint? =null//control click class to learn about class stuff after colon is class
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float =0.toFloat()
    private var color= Color.BLACK
    private var canvas: Canvas?= null
    private val mPaths=ArrayList<CustomPath>()//to store the previous paths
    private val mUndoPaths = ArrayList<CustomPath>()
    init{
        setUpDrawing()
    }
    private fun setUpDrawing(){
        //setting up attributes for drawing
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = color //!! for asserting that it is not empty
        mDrawPaint!!.style=Paint.Style.STROKE
        mDrawPaint!!.strokeJoin=Paint.Join.ROUND
        mDrawPaint!!.strokeCap=Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)//see what it is
        mBrushSize = 20.toFloat()

    }
    fun onClickUndo(){//to undo
        if (mPaths.size>0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size -1))//removeat removes the val form original and add adds it to mundopaths
            invalidate()//redraws
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)//canvas thingy
        canvas = Canvas(mCanvasBitmap!!)
    }
    //change Canvas to Canvas? if fails
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)//what to do when draw
        for (path in mPaths){//for keeping the lines persistent after thumb up go through a list of paths and draw
            mDrawPaint!!.strokeWidth=path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path!!, mDrawPaint!!)
        }
        if (!mDrawPath!!.isEmpty) {//check if you have drawn or not and then execute
            mDrawPaint!!.strokeWidth=mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {//for touching coordinates and stuff
        val touchX = event?.x
        val touchY= event?.y
        when(event?.action){//for motion of finger and stuff
            MotionEvent.ACTION_DOWN ->{mDrawPath!!.color=color
            mDrawPath!!.brushThickness=mBrushSize
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mDrawPath=CustomPath(color,mBrushSize)
            }
            else->return false

        }
        invalidate()
        return true
    }
    fun setSizeForBrush(newSize:Float){
        mBrushSize= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)//for keeping the brush size same across different screen sizes
        mDrawPaint!!.strokeWidth=mBrushSize
    }
    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color=color
    }
    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {


    }


}