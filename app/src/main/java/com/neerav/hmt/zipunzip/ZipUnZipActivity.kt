package com.neerav.hmt.zipunzip

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_zip_unzip.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import org.apache.commons.io.FileUtils
import java.io.*
import java.lang.Exception
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.hzy.lib7z.IExtractCallback
import com.hzy.lib7z.Z7Extractor
import com.hzy.libp7zip.ExitCode
import com.hzy.libp7zip.P7ZipApi


class ZipUnZipActivity : AppCompatActivity() {

    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zip_unzip)

        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)

        if (ContextCompat.checkSelfPermission(this@ZipUnZipActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@ZipUnZipActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1000
            )
        } else {

        }

        builder = AlertDialog.Builder(this@ZipUnZipActivity)
        builder.setView(R.layout.progress_dialog)
        dialog = builder.create()
        dialog.setCancelable(false)


        btnZipFiles.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this@ZipUnZipActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                ActivityCompat.requestPermissions(
                    this@ZipUnZipActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1000
                )

            } else {
                val intent = Intent()
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(intent, 200)
            }


        }


        tvGoToStorage.setOnClickListener {

            val selectedUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/zipUnzip/")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.data = selectedUri
            intent.type = "*/*"

            if (intent.resolveActivityInfo(packageManager, 0) != null) {
                startActivity(intent)
            } else {
                // if you reach this place, it means there is no any file
                // explorer app installed on your device
                Toast.makeText(this@ZipUnZipActivity, "Install Any File Manager!", Toast.LENGTH_LONG).show()
            }


        }


        btnUnZipFile.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this@ZipUnZipActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                ActivityCompat.requestPermissions(
                    this@ZipUnZipActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1000
                )
            } else {
                val intent = Intent()
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/zip"
                startActivityForResult(intent, 400)
            }


        }


        btnExtract7ZipFile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@ZipUnZipActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                ActivityCompat.requestPermissions(
                    this@ZipUnZipActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1000
                )
            } else {
                // extract 7z file here
                val intent = Intent()
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "application/x-7z-compressed"
                startActivityForResult(intent, 800)
            }

        }


        btnExtractRAR.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this@ZipUnZipActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                ActivityCompat.requestPermissions(
                    this@ZipUnZipActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1000
                )
            } else {
                val intent = Intent()
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(intent, 1200)
            }
        }

        tvHelp.text = "Note: File is stored under sdcard/zipUnzip folder"
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val uriList = ArrayList<Uri>()
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {

            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    uriList.add(clipData.getItemAt(i).uri)
                }
            } else {
                if (data?.data != null)
                    uriList.add(data.data!!)
            }
        }

        if (requestCode == 400 && resultCode == Activity.RESULT_OK) {

            if (data != null && data.data != null) {
                UnZipTask(data.data!!).execute()
            }

        }

        // Extract 7z file
        if (requestCode == 800 && resultCode == Activity.RESULT_OK) {

            if (data != null && data.data != null) {
                Extract7ZTask(data.data!!).execute()
            }
        }

        // Extract .rar file
        if (requestCode == 1200 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                ExtractRARTask(uri = data.data!!).execute()
            }

        }

        if (uriList.isNotEmpty()) {

            ZipTask(uriList = uriList).execute()

        }


    }

    /**   Zip file function
     *    @param uriList :ArrayList<Uri>  input files uri
     *    @param outPutPath:String    out put location to store zip files
     */
    fun makeZipFile(uriList: ArrayList<Uri>, outPutPath: String) {


        val zipParam = ZipParameters()
        zipParam.compressionMethod = Zip4jConstants.COMP_DEFLATE
        zipParam.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL

        val tempDir = Environment.getExternalStorageDirectory().absolutePath + "/temp"
        val tempFile = File(tempDir)
        if (tempFile.isDirectory) {
            FileUtils.cleanDirectory(tempFile)
        }
        try {
            val zipFile = ZipFile(outPutPath + "/" + SystemClock.elapsedRealtime().toString() + ".zip")
            for (item in uriList) {
                val mimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(item))
                FileUtils.copyInputStreamToFile(
                    contentResolver.openInputStream(item),
                    File(tempDir, SystemClock.elapsedRealtime().toString() + "." + mimeType)
                )

            }
            zipFile.addFolder(tempDir, zipParam)

        } catch (e: Exception) {
            Log.e("ex", e.toString())
            throw  e
        } finally {

        }


    }


    /**  UnZip file
     *   @param uri for input file
     *   @param outPutPath output location for store extracted files.
     */
    fun unZip(uri: Uri, outPutPath: String) {
        val zipParam = ZipParameters()
        zipParam.compressionMethod = Zip4jConstants.COMP_DEFLATE
        zipParam.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL

        val tempDir = Environment.getExternalStorageDirectory().absolutePath + "/temp"
        val tempFile = File(tempDir)
        if (tempFile.isDirectory) {
            FileUtils.cleanDirectory(tempFile)
        }
        try {
            FileUtils.copyInputStreamToFile(contentResolver.openInputStream(uri), File(tempDir, "demo.zip"))
            val zipFile = ZipFile(File(tempDir, "demo.zip"))
            zipFile.extractAll(outPutPath)
        } catch (e: Exception) {
            Log.e("exc", e.toString())
            throw e
        }


    }


    /**
     *    Asynch task for zip the files
     *    @param uriList :ArrayList<Uri>  list of uri for zip file
     */
    @SuppressLint("StaticFieldLeak")
    inner class ZipTask(private var uriList: ArrayList<Uri>) : AsyncTask<Void, Void, Void>() {

        private var isOk = true

        override fun doInBackground(vararg params: Void?): Void? {
            val backupDBPath = Environment.getExternalStorageDirectory().absolutePath + "/zipUnzip"
            val dir = File(backupDBPath)
            if (!dir.exists()) {
                dir.mkdir()

                val innerDir = File(backupDBPath, "compress")
                if (!innerDir.exists()) {
                    innerDir.mkdir()
                }
            } else {
                val innerDir = File(backupDBPath, "compress")
                if (!innerDir.exists()) {
                    innerDir.mkdir()
                }
            }
            Log.e("path", backupDBPath)
            try {
                makeZipFile(uriList = uriList, outPutPath = "$backupDBPath/compress")
            } catch (e: Exception) {
                isOk = false
            }


            return null
        }


        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            setDialog(false)
            if (isOk)
                Toast.makeText(this@ZipUnZipActivity, "file  zipped", Toast.LENGTH_LONG).show()
        }

        override fun onPreExecute() {
            super.onPreExecute()
            setDialog(true)
        }
    }


    /**
     *    Asynch task for Unzip the files
     *
     */
    @SuppressLint("StaticFieldLeak")
    inner class UnZipTask(private var uri: Uri) : AsyncTask<Void, Void, Void>() {

        private var isOk = true

        override fun onPreExecute() {
            super.onPreExecute()
            setDialog(true)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val backupDBPath = Environment.getExternalStorageDirectory().absolutePath + "/zipUnzip"
            val dir = File(backupDBPath)
            if (!dir.exists()) {
                dir.mkdir()

                val innerDir = File(backupDBPath, "extract")
                if (!innerDir.exists()) {
                    innerDir.mkdir()
                }
            } else {
                val innerDir = File(backupDBPath, "extract")
                if (!innerDir.exists()) {
                    innerDir.mkdir()
                }
            }

            try {
                unZip(uri, "$backupDBPath/extract")
            } catch (e: Exception) {
                isOk = false
            }


            return null
        }


        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            setDialog(false)
            if (isOk)
                Toast.makeText(this@ZipUnZipActivity, "file  Unzipped", Toast.LENGTH_LONG).show()
        }
    }


    private fun setDialog(show: Boolean) {

        if (show)
            dialog.show()
        else
            dialog.dismiss()
    }


    /**  Extract .7z files
     *   @param uri input uri
     *   @param outPutPath output location for store extracted files.
     */
    fun extract7Zip(uri: Uri, outPutPath: String) {


        val tempDir = Environment.getExternalStorageDirectory().absolutePath + "/temp"
        val tempFile = File(tempDir)
        if (tempFile.isDirectory) {
            FileUtils.cleanDirectory(tempFile)
        }
        try {
            FileUtils.copyInputStreamToFile(contentResolver.openInputStream(uri), File(tempDir, "demo.7z"))

            Z7Extractor.extractFile("$tempDir/demo.7z", outPutPath,
                object : IExtractCallback {
                    override fun onSucceed() {
                        Toast.makeText(this@ZipUnZipActivity, "File extracted successfully", Toast.LENGTH_LONG).show()

                    }

                    override fun onGetFileNum(fileNum: Int) {


                    }

                    override fun onProgress(name: String?, size: Long) {


                    }

                    override fun onError(errorCode: Int, message: String?) {
                        Toast.makeText(this@ZipUnZipActivity, "Error occurred!", Toast.LENGTH_LONG).show()


                    }

                    override fun onStart() {


                    }

                }


            )

        } catch (e: Exception) {
            Log.e("exc", e.toString())
            throw  e
        }

    }


    /**
     *    Asynch task for Extract 7Z   files
     *
     */
    @SuppressLint("StaticFieldLeak")
    inner class Extract7ZTask(private var uri: Uri) : AsyncTask<Void, Void, Void>() {


        override fun onPreExecute() {
            super.onPreExecute()
            setDialog(true)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val backupDBPath = Environment.getExternalStorageDirectory().absolutePath + "/zipUnzip"
            val dir = File(backupDBPath)
            if (!dir.exists()) {
                dir.mkdir()

                val innerDir = File(backupDBPath, "extract")
                if (!innerDir.exists()) {
                    innerDir.mkdir()
                }
            }

            extract7Zip(uri, "$backupDBPath/extract")

            return null
        }


        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            setDialog(false)
            Toast.makeText(this@ZipUnZipActivity, "file  Extracted..", Toast.LENGTH_LONG).show()
        }
    }


    /** Extract the rar file
     *   @param uri input file
     *   @param outPutPath output location for storage
     */
    fun extractRAR(uri: Uri, outPutPath: String): Int {

        var result = 0

        val tempDir = Environment.getExternalStorageDirectory().absolutePath + "/temp"
        val tempFile = File(tempDir)
        if (tempFile.isDirectory) {
            FileUtils.cleanDirectory(tempFile)
        }
        try {
            FileUtils.copyInputStreamToFile(contentResolver.openInputStream(uri), File(tempDir, "demo.rar"))

            val command = Command.getExtractCmd("$tempDir/demo.rar", outPutPath)
            result = P7ZipApi.executeCommand(command)

        } catch (e: Exception) {
            Log.e("exc", e.toString())
        }

        return result
    }


    /**
     *    Asynch task for Extract 7Z   files
     *
     */
    @SuppressLint("StaticFieldLeak")
    inner class ExtractRARTask(private var uri: Uri) : AsyncTask<Void, Void, Void>() {

        private var result: Int = -1000
        override fun onPreExecute() {
            super.onPreExecute()
            setDialog(true)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val backupDBPath = Environment.getExternalStorageDirectory().absolutePath + "/zipUnzip"
            val dir = File(backupDBPath)
            if (!dir.exists()) {
                dir.mkdir()

                val innerDir = File(backupDBPath, "extract")
                if (!innerDir.exists()) {
                    innerDir.mkdir()
                }
            }

            this.result = extractRAR(uri, "$backupDBPath/extract")

            return null
        }


        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            setDialog(false)

            when {
                this.result == ExitCode.EXIT_OK -> Toast.makeText(
                    this@ZipUnZipActivity,
                    "file  Extracted..",
                    Toast.LENGTH_LONG
                ).show()
                this.result == ExitCode.EXIT_NOT_SUPPORT -> Toast.makeText(
                    this@ZipUnZipActivity,
                    "File type Not Supported..",
                    Toast.LENGTH_LONG
                ).show()
                this.result == ExitCode.EXIT_CMD_ERROR -> Toast.makeText(
                    this@ZipUnZipActivity,
                    "Error occurred..",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

}
