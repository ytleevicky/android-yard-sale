package com.vickylee.yardsale.fragments

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vickylee.yardsale.R
import com.vickylee.yardsale.databinding.FragmentCameraPreviewBinding
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class CameraPreviewFragment : Fragment(R.layout.fragment_camera_preview) {

    //region Properties
    private val TAG = this.toString()
    private var _binding: FragmentCameraPreviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomNav: BottomNavigationView

    // request code
    private val REQUEST_PERMISSION_CODE = 1234

    // List of permissions that app requires
    private val REQUIRED_PERMISSIONS_LIST =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    // camera properties
    private lateinit var imageCapture: ImageCapture
    //endregion

    //region Lifecycle Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.visibility = View.GONE

        // setup view binding
        _binding = FragmentCameraPreviewBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startCamera()

        binding.btnCameraCapture.setOnClickListener {
            Log.d(TAG, "Camera Button Pressed: Taking a photo...")
            takePhoto()
        }
    }

    override fun onPause() {
        super.onPause()

        bottomNav.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        bottomNav.visibility = View.GONE
    }
    //endregion

    //region Helper functions
    private fun takePhoto() {
        Log.e(TAG, "Camera unavailable. Cannot Click Pictures.")

        if (imageCapture != null) {
            val outputDirectory = getOutputDirectory()      // image will be saved in this location

            val fileName = "C435_${
                SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.CANADA)
                    .format(System.currentTimeMillis())
            }.jpeg"

            val pictureFile = File(outputDirectory, fileName)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(pictureFile).build()

            imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "onError: Image couldn't be saved: $exception")
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.e(TAG, "onImageSaved: Image saved successfully.")

                        // show the image on ImageView

                        // save the picture to external storage
                        Log.e(TAG, "onImageSaved: Image saved at ${Uri.fromFile(pictureFile)}.")
                        saveToExternalStorage(Uri.fromFile(pictureFile))
                    }
                }
            )

        } else {
            Log.e(TAG, "takePhoto: Image Capture use-case cannot be created. ")
        }
    }

    private fun startCamera() {
        Log.d(TAG, "startCamera(): Starting camera...")

        this.imageCapture = ImageCapture.Builder().build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // bind the preview using the cameraProvider
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also { preview ->
            preview.setSurfaceProvider(binding.previewViewContainer.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

        } catch (ex: Exception) {
            Log.e(TAG, "bindPreview: Use-case binding failed $ex")
        }
    }

    //-------------------------Boilerplate code for obtaining directory location to save picture---------------------------//
    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireContext().filesDir
    }


    //-------------------------Boilerplate code for to save picture on external storage---------------------------//
    private fun saveToExternalStorage(pictureURI: Uri) {

        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {

            val resolver: ContentResolver = this.requireContext().contentResolver
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val contentValues = ContentValues()

            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, pictureURI.lastPathSegment)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/CameraX_Kotlin")

            val uri = resolver.insert(contentUri, contentValues)

            if (uri == null) {
                Toast.makeText(
                    requireContext(),
                    "Failed to create media on external storage.",
                    Toast.LENGTH_SHORT
                ).show()
                throw IOException("Failed to create media on external storage.")
            } else {
                Toast.makeText(
                    requireContext(),
                    "Media successfully saved to external storage.",
                    Toast.LENGTH_SHORT
                ).show()

                Log.e(TAG, "Media successfully saved to external storage.")
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Unable to access external storage.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e(TAG, "Unable to access external storage.")
        }
    }
    //endregion

}