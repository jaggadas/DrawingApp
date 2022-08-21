package com.example.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_sizee.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.provider.MediaStore
import android.widget.Gallery
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

//bitmaps are images basically
class MainActivity : AppCompatActivity() {
    private var mImageBurronCurrentPaint: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)
        mImageBurronCurrentPaint = ll_paint_colors[1] as ImageButton
        mImageBurronCurrentPaint!!.setImageDrawable(//to change color of background of color selection once it is pressed
                ContextCompat.getDrawable(this,R.drawable.palette_pressed))
        drawing_view.setSizeForBrush(20.toFloat())
        ib_brush.setOnClickListener{
            showBrushSizeChooseDialog()
        }
        ib_save.setOnClickListener {
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(fl_drawing_view_container)).execute()
            }else{
                requestStoragePermission()
            }
        }
        ib_gallery.setOnClickListener{
            if (isReadStorageAllowed()){
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)//for getting image from gallery
                startActivityForResult(pickPhotoIntent,GALLERY)
            }
            else{
                requestStoragePermission()
            }
        }
        ib_undo.setOnClickListener{
            drawing_view.onClickUndo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if (requestCode== GALLERY){
                try{
                    if(data!!.data!=null){//user has selected photo
                        iv_background.visibility=View.VISIBLE
                        iv_background.setImageURI(data.data)//data is the image
                    }
                    else{
                        Toast.makeText(this@MainActivity,
                        "Error in parsing the image or its corrupted",Toast.LENGTH_SHORT)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }
    private fun showBrushSizeChooseDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_sizee)//for dialog boxessss
        brushDialog.setTitle("Brush size:")
        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener {drawing_view.setSizeForBrush(10.toFloat())
        brushDialog.dismiss()}//toquit the dialog
        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener {drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()}
        val largeBtn = brushDialog.ib_large_brush
        largeBtn.setOnClickListener {drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()}
        brushDialog.show()

    }
    fun paintClicked(view:View){
        if(view!= mImageBurronCurrentPaint){
            val imageButton = view as ImageButton// as to convert to imagebutton
            val colorTag =imageButton.tag.toString()
            drawing_view.setColor(colorTag)
            imageButton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.palette_pressed))
            mImageBurronCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.palette_normal))//to make normal the previously selected button
        mImageBurronCurrentPaint = view
        }
    }
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).toString()))
        {
            Toast.makeText(this,"Need permission to add a Background",Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity,"Permission granted now you can read the storage",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity,"You have denied the permission",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun isReadStorageAllowed():Boolean{
        val result =ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result==PackageManager.PERMISSION_GRANTED
    }
    private fun getBitmapFromView(view: View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable!= null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor((Color.WHITE))
        }
        view.draw(canvas)
        return returnedBitmap
    }
    private inner class BitmapAsyncTask(val mBitmap: Bitmap):AsyncTask<Any, Void, String>()//for doing saving processs int the background
    {
        private lateinit var mProgressDialog :Dialog
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }
        override fun doInBackground(vararg params: Any?): String {
            var result = ""
            if(mBitmap!= null){
                try{
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,100,bytes)
                    val f = File(externalCacheDir!!.absoluteFile.toString()
                            +File.separator +"KidDrawingApp_"
                            +System.currentTimeMillis()/1000
                            +".png")//file name for unique name
                    val fos=FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath
                }catch (e: Exception){
                    result=""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            cancelProgressDialog()
            super.onPostExecute(result)
            if (!result!!.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
        }
            MediaScannerConnection.scanFile(this@MainActivity,arrayOf(result),null){//for sharing the file
                path, uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                startActivity(
                    Intent.createChooser(
                        shareIntent,"Share"
                    )
                )
            }

    }private fun showProgressDialog(){
        mProgressDialog=Dialog(this@MainActivity)
        mProgressDialog.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog.show()
    }
        private fun cancelProgressDialog(){
            mProgressDialog.dismiss()
        }
    }
    companion object{
        private const val STORAGE_PERMISSION_CODE =1
        private const val GALLERY =2
    }
}