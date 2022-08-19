package com.example.hungryheadzbusiness.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.*
import android.webkit.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.hungryheadzbusiness.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity2 : AppCompatActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        sellerbottomNavigationView = findViewById(R.id.sellerbottomNavigationView)
        progressbar = findViewById(R.id.progressBar)
        root = findViewById(R.id.rootView)

        webView = findViewById(R.id.webView)
        webView.loadUrl("https://tastee.inspeero.com")
        webView.webViewClient = webViewClient()
        webView.webChromeClient = webViewChromeClient()
        webView.settings.javaScriptEnabled = true
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true


        if (Build.VERSION.SDK_INT >= 21) {
            webView.settings.mixedContentMode = 0
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

    }

    private fun showNoInternetDialogue() {
        val dialog= Dialog(this)
        dialog.setContentView(R.layout.dialogue_box_no_internet)
        dialog.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_bg))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.show()

        val btn_ok=dialog.findViewById<RelativeLayout>(R.id.btn_ok)
        btn_ok.setOnClickListener{
            dialog.dismiss()
            finish()
        }
    }

    private fun showFailedToLoadDialogue() {
        val dialog= Dialog(this)
        dialog.setContentView(R.layout.dialogue_box_failed_to_load)
        dialog.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_bg))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.show()

        val btn_ok=dialog.findViewById<RelativeLayout>(R.id.btn_ok)
        btn_ok.setOnClickListener{
            dialog.dismiss()
            finish()
        }
    }

    private fun showAllowCameraDialogue() {
        cameradialog= Dialog(this)
        cameradialog!!.setContentView(R.layout.dialogue_box_access_camera)
        cameradialog!!.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_bg))
        cameradialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cameradialog!!.setCancelable(false)
        cameradialog!!.show()

        val btn_allow=cameradialog!!.findViewById<RelativeLayout>(R.id.btn_allow)
        btn_allow.setOnClickListener{
            cameradialog!!.dismiss()
            requestCameraPermission()
        }
    }

    private fun showAllowStoragePermission() {
        storagedialog= Dialog(this)
        storagedialog!!.setContentView(R.layout.dialogue_box_access_storage)
        storagedialog!!.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_bg))
        storagedialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        storagedialog!!.setCancelable(false)
        storagedialog!!.show()

        val btn_allow=storagedialog!!.findViewById<RelativeLayout>(R.id.btn_allow)
        btn_allow.setOnClickListener{
            storagedialog!!.dismiss()
            requestReadWriteExternalStoragePermission()
        }
    }



    private fun isConnectedToInternet(mainActivity: MainActivity2): Boolean {
        val connectivityManager =
            mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wificon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobilecon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return wificon != null && wificon.isConnected || mobilecon != null && mobilecon.isConnected
    }

    companion object {
        var sellerbottomNavigationView: BottomNavigationView? = null
        var progressbar: RelativeLayout? = null
        private var mUploadMessage: ValueCallback<Uri>? = null
        private var mCapturedImageURI: Uri? = null
        private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
        private var mCameraPhotoPath: String? = null
        private const val INPUT_FILE_REQUEST_CODE = 1
        private const val READ_WRITE_STORAGE_ACCESS_LOCATION = 2
        private const val WRITE_STORAGE_ACCESS_LOCATION = 3
        private const val CAMERA_ACCESS_LOCATION = 4

        var currentUrl: String? = null
        var root: ConstraintLayout? = null

        var storagedialog:Dialog?=null
        var cameradialog:Dialog?=null

    }

    inner class webViewChromeClient : WebChromeClient() {


        // For Android 5.0
        @SuppressLint("QueryPermissionsNeeded")
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onShowFileChooser(
            view: WebView?,
            filePath: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {

            if (hasPermission(Manifest.permission.CAMERA)
            ) {
                if(hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if(currentUrl!="https://tastee.inspeero.com/bussiness_register") {
                        sellerbottomNavigationView!!.visibility = View.VISIBLE
                    }
                    else{
                        sellerbottomNavigationView!!.visibility = View.GONE
                    }
                    Log.d("tag", "showFileChooser")
                    mFilePathCallback?.onReceiveValue(null)
                    mFilePathCallback = filePath
                    var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                            var photoFile: File? = null

                            photoFile = createImageFile()
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)


                            if (photoFile != null) {
                                mCameraPhotoPath = "file:" + photoFile.absolutePath
                                takePictureIntent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile)
                                )
                            } else {
                                takePictureIntent = null
                            }
                        }
                        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        contentSelectionIntent.type = "image/*"
                        val intentArray: Array<Intent?> =
                            takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)

                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Continue action using")
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                        startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
                    } catch (e: IOException) {
                        //Log.d("tag", "Unable to create Image File", e)
                    }
                }
                else{
                    showAllowStoragePermission()
                }
            }
            else{
                showAllowCameraDialogue()
            }
            return true
        }


        // openFileChooser for Android 3.0+
        fun openFileChooser(uploadMsg: ValueCallback<Uri>?, acceptType: String?) {

            if (hasPermission(Manifest.permission.CAMERA)
            ) {
                if(hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.d("tag", "openFileChooser")
                    if(currentUrl!="https://tastee.inspeero.com/bussiness_register") {
                        sellerbottomNavigationView!!.visibility = View.VISIBLE
                    }
                    else{
                        sellerbottomNavigationView!!.visibility = View.GONE
                    }
                    mUploadMessage = uploadMsg
                    val imageStorageDir = File(
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                        ), "AndroidExampleFolder"
                    )
                    if (!imageStorageDir.exists()) {
                        imageStorageDir.mkdirs()
                    }

                    val file = File(
                        imageStorageDir.toString() + File.separator + "IMG_"
                                + System.currentTimeMillis().toString() + ".jpg"
                    )
                    mCapturedImageURI = Uri.fromFile(file)

                    val captureIntent = Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE
                    )
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI)
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)
                    i.type = "image/*"

                    val chooserIntent = Intent.createChooser(i, "Continue action using")

                    chooserIntent.putExtra(
                        Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent)
                    )

                    startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
                }
                else{
                    showAllowStoragePermission()
                }
            }
            else{
                showAllowCameraDialogue()
            }
        }

        // openFileChooser for Android < 3.0
        fun openFileChooser(uploadMsg: ValueCallback<Uri>?) {


            openFileChooser(uploadMsg, "")

        }

        //openFileChooser for other Android versions
        fun openFileChooser(
            uploadMsg: ValueCallback<Uri>?,
            acceptType: String?,
            capture: String?
        ) {

            openFileChooser(uploadMsg, acceptType)

        }

    }


    private fun requestReadWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),READ_WRITE_STORAGE_ACCESS_LOCATION)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),CAMERA_ACCESS_LOCATION)
    }

    private fun hasPermission(id: String): Boolean {
        return ActivityCompat.checkSelfPermission(this@MainActivity2,id)== PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName, ".jpg", storageDir
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            CAMERA_ACCESS_LOCATION -> {
                if(wasPermissionGranted(grantResults)){
                    Log.d("tag", "Permission Granted")
                }
                else{
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity2,
                            Manifest.permission.CAMERA
                        )
                    ){

                            showAllowCameraDialogue()

                    }
                    else {
                        val i = Intent()
                        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        i.addCategory(Intent.CATEGORY_DEFAULT)
                        i.data = Uri.parse("package:" + applicationContext.packageName)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        applicationContext.startActivity(i)
                    }
                }
            }

            READ_WRITE_STORAGE_ACCESS_LOCATION -> {
                if(wasPermissionGranted(grantResults)){
                    Log.d("tag", "Permission Granted")
                }
                else{
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity2,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity2,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ){

                            showAllowStoragePermission()

                    }
                    else {
                        val i = Intent()
                        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        i.addCategory(Intent.CATEGORY_DEFAULT)
                        i.data = Uri.parse("package:" + applicationContext.packageName)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        applicationContext.startActivity(i)
                    }
                }
            }

            /*WRITE_STORAGE_ACCESS_LOCATION -> {
                if(wasPermissionGranted(grantResults)){
                    Log.d("tag", "Permission Granted")
                }
                else{
                    AlertDialog.Builder(
                        ContextThemeWrapper(
                            this@MainActivity2,
                            androidx.appcompat.R.style.Theme_AppCompat
                        )
                    ).setTitle("Access Write External Storage Permission")
                        .setIcon(R.drawable.ic_baseline_error_outline_24)
                        .setMessage("External storage access is required to continue this action. Please allow this application to access this device's external storage. May require app to be restart!!.")
                        .setPositiveButton(
                            "Allow"
                        ) { dialog: DialogInterface?, which: Int ->
                            val i = Intent()
                            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            i.addCategory(Intent.CATEGORY_DEFAULT)
                            i.data = Uri.parse("package:" + applicationContext.packageName)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            applicationContext.startActivity(i)
                            dialog!!.dismiss()

                            AlertDialog.Builder(
                                ContextThemeWrapper(
                                    this@MainActivity2,
                                    androidx.appcompat.R.style.Theme_AppCompat
                                )
                            ).setTitle("App restart required")
                                .setIcon(R.drawable.ic_baseline_error_outline_24)
                                .setMessage("App restart required to apply changes.")
                                .setPositiveButton(
                                    "Ok"
                                ) { dialog: DialogInterface?, which: Int ->
                                    finish()
                                }.setCancelable(false)
                                .show()

                        }.setCancelable(false)
                        .show()
                }
            }*/
        }
    }

    private fun wasPermissionGranted(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode !== INPUT_FILE_REQUEST_CODE
            || mFilePathCallback == null
        ) {
            super.onActivityResult(requestCode, resultCode, data)
        }

        var results: Array<Uri>?=null
        if (resultCode === RESULT_OK) {
            if (data == null) {
                if (mCameraPhotoPath != null) { //if there is not data here, then we may have taken a photo/video
                    results = arrayOf(Uri.parse(mCameraPhotoPath))
                }
            } else {
                val dataString: String = data.dataString!!
                results = arrayOf(Uri.parse(dataString))
            }
        }
        mFilePathCallback!!.onReceiveValue(results)
        mFilePathCallback = null

    }

    inner class webViewClient : WebViewClient() {
        @SuppressLint("JavascriptInterface")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            return if (url.contains("mailto:")) {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                true
            } else {
                view.loadUrl(url)
                true
            }

        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (!isConnectedToInternet(this@MainActivity2)) {
                progressbar!!.visibility = View.GONE
                showNoInternetDialogue()
            }
            else{
                progressbar!!.visibility = View.VISIBLE

            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressbar!!.visibility = View.GONE
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            showFailedToLoadDialogue()
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            if (url != null) {
                Log.d("tag",url)
            }
            when (url!!) {
                //ForAll
                "https://tastee.inspeero.com/choose" -> {
                    currentUrl="https://tastee.inspeero.com/choose"
                    webView.visibility=View.GONE
                    sellerbottomNavigationView!!.visibility = View.GONE
                    webView.loadUrl("https://tastee.inspeero.com/business_signup")
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:
                        KeyboardVisibilityEventListener {
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                        }

                    })
                }

                "https://tastee.inspeero.com/business_signup" -> {
                    currentUrl="https://tastee.inspeero.com/business_signup"
                    webView.visibility= View.VISIBLE
                    sellerbottomNavigationView!!.visibility = View.GONE
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:
                        KeyboardVisibilityEventListener {
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                        }

                    })
                }

                "https://tastee.inspeero.com/bussiness_register" ->{
                    currentUrl="https://tastee.inspeero.com/bussiness_register"
                    webView.visibility= View.VISIBLE
                    sellerbottomNavigationView!!.visibility = View.GONE
                    if (!hasPermission(Manifest.permission.CAMERA))
                    {

                            showAllowCameraDialogue()

                    }
                    if(!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) || !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                            showAllowStoragePermission()

                    }
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:
                        KeyboardVisibilityEventListener {
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                        }

                    })
                }


                //ForSeller
                "https://tastee.inspeero.com/feed" -> {
                    currentUrl="https://tastee.inspeero.com/feed"
                    webView.visibility= View.VISIBLE
                    sellerbottomNavigationView!!.visibility = View.VISIBLE
                    if (sellerbottomNavigationView!!.selectedItemId != R.id.ic_feed) {
                        sellerbottomNavigationView!!.selectedItemId = R.id.ic_feed
                    }
                    sellerbottomNavigationView!!.setOnItemSelectedListener { item ->
                        when (item.itemId) {
                            R.id.ic_feed -> {
                                webView.loadUrl("https://tastee.inspeero.com/feed")
                                true
                            }
                            R.id.ic_disc -> {
                                webView.loadUrl("https://tastee.inspeero.com/promotions/Promotions")
                                true
                            }
                            R.id.ic_gallery -> {
                                webView.loadUrl("https://tastee.inspeero.com/gallery/Food")
                                true
                            }
                            R.id.ic_details -> {
                                //https://tastee.inspeero.com/vendor_details/details
                                webView.loadUrl("https://tastee.inspeero.com/vendor_details/details")
                                true
                            }
                            else -> {
                                false
                            }
                        }
                    }

                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:KeyboardVisibilityEventListener{
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.VISIBLE
                            }
                        }

                    })

                    if (!hasPermission(Manifest.permission.CAMERA))
                    {

                        showAllowCameraDialogue()

                    }
                    if(!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) || !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                        showAllowStoragePermission()

                    }


                }

                "https://tastee.inspeero.com/promotions/Promotions" -> {
                    currentUrl="https://tastee.inspeero.com/promotions/Promotions"
                    webView.visibility= View.VISIBLE
                    if (sellerbottomNavigationView!!.selectedItemId != R.id.ic_disc) {
                        sellerbottomNavigationView!!.selectedItemId = R.id.ic_disc
                    }
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:KeyboardVisibilityEventListener{
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.VISIBLE
                            }
                        }

                    })
                }

                "https://tastee.inspeero.com/gallery/Food" -> {
                    currentUrl="https://tastee.inspeero.com/gallery/Food"
                    webView.visibility= View.VISIBLE
                    if (sellerbottomNavigationView!!.selectedItemId != R.id.ic_gallery) {
                        sellerbottomNavigationView!!.selectedItemId = R.id.ic_gallery
                    }
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:KeyboardVisibilityEventListener{
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.VISIBLE
                            }
                        }

                    })
                }

                "https://tastee.inspeero.com/vendor_details/details" -> {
                    //https://tastee.inspeero.com/vendor_details/details
                    currentUrl="https://tastee.inspeero.com/vendor_details/details"
                    webView.visibility= View.VISIBLE
                    if (sellerbottomNavigationView!!.selectedItemId != R.id.ic_details) {
                        sellerbottomNavigationView!!.selectedItemId = R.id.ic_details
                    }
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:KeyboardVisibilityEventListener{
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.VISIBLE
                            }
                        }

                    })
                }

                "https://tastee.inspeero.com/regulars" ->{
                    currentUrl="https://tastee.inspeero.com/regulars"
                    webView.visibility= View.VISIBLE
                    sellerbottomNavigationView!!.selectedItemId = R.id.invisible
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:KeyboardVisibilityEventListener{
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.VISIBLE
                            }
                        }

                    })
                }

                "https://tastee.inspeero.com/messages/null" ->{
                    currentUrl="https://tastee.inspeero.com/messages/null"
                    webView.visibility= View.VISIBLE
                    sellerbottomNavigationView!!.selectedItemId = R.id.invisible
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:KeyboardVisibilityEventListener{
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.VISIBLE
                            }
                        }

                    })
                }

                "https://tastee.inspeero.com/reviews/all" ->{
                    currentUrl="https://tastee.inspeero.com/reviews/all"
                    webView.visibility= View.VISIBLE
                    sellerbottomNavigationView!!.selectedItemId = R.id.invisible
                    KeyboardVisibilityEvent.setEventListener(this@MainActivity2,object:KeyboardVisibilityEventListener{
                        override fun onVisibilityChanged(isOpen: Boolean) {
                            if(isOpen){
                                sellerbottomNavigationView!!.visibility=View.GONE
                            }
                            else{
                                sellerbottomNavigationView!!.visibility=View.VISIBLE
                            }
                        }

                    })
                }
            }
            super.doUpdateVisitedHistory(view, url, isReload)
        }

    }


    override fun onRestart() {
        super.onRestart()
        Log.d("tag", "Restart")
        if(currentUrl=="https://tastee.inspeero.com/bussiness_register" || currentUrl=="https://tastee.inspeero.com/feed"){
            if (!hasPermission(Manifest.permission.CAMERA))
            {
                if(cameradialog!=null) {
                    if (!cameradialog!!.isShowing) {
                        showAllowCameraDialogue()
                    }
                }
            }
            if(!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) || !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                if(storagedialog!=null) {
                    if (!storagedialog!!.isShowing) {
                        showAllowStoragePermission()
                    }
                }

            }
        }
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            if (currentUrl == "https://tastee.inspeero.com/business_signup" ||  currentUrl == "https://tastee.inspeero.com/feed") {
                onBackPressed()
            } else {
                webView.goBack()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}