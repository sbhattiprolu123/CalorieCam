package com.cs407.caloriecam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class MealLoggingFragment : Fragment(R.layout.fragment_meal_logging) {

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePhoto()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val readMediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                choosePhotoFromGallery()
            } else {
                Toast.makeText(requireContext(), "Media permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val photo = result.data?.extras?.get("data") as? Bitmap
                if(photo != null){
                    val savedPath = saveImageToInternalStorage(photo)
                    if (savedPath != null) {
                        // Store the path in SharedPreferences
                        val prefs = requireContext().getSharedPreferences("MY_PREFS", AppCompatActivity.MODE_PRIVATE)
                        prefs.edit()
                            .putString("latest_image_path", savedPath)
                            .apply()
                    }
                }
                Toast.makeText(requireContext(), "Photo captured successfully", Toast.LENGTH_SHORT).show()
                navigateToCalorieTracking()
            }
        }

    private val galleryActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data
                val prefs = requireContext().getSharedPreferences("MY_PREFS", AppCompatActivity.MODE_PRIVATE)
                prefs.edit()
                    .putString("latest_image_uri", imageUri.toString())
                    .apply()
                Toast.makeText(requireContext(), "Photo selected successfully", Toast.LENGTH_SHORT).show()
                navigateToCalorieTracking()
            }
        }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String? {
        val filename = "captured_image_${System.currentTimeMillis()}.png"
        return try {
            val file = File(requireContext().filesDir, filename)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meal_logging, container, false)

        val btnTakePhoto: Button = view.findViewById(R.id.btn_take_photo)
        val btnChoosePhoto: Button = view.findViewById(R.id.btn_choose_photo)
        val btnLogout: Button = view.findViewById(R.id.btnLogout) // Add this button to the layout

        btnTakePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                takePhoto()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        btnChoosePhoto.setOnClickListener {
            if (checkReadMediaPermission()) {
                choosePhotoFromGallery()
            } else {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                readMediaPermissionLauncher.launch(permission)
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        return view
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkReadMediaPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun takePhoto() {
        val packageManager = requireActivity().packageManager

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity = takePictureIntent.resolveActivity(packageManager)

            if (resolvedActivity != null) {
                cameraActivityLauncher.launch(takePictureIntent)
            } else {
                Toast.makeText(requireContext(), "No camera app found.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No camera hardware found on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        galleryActivityLauncher.launch(galleryIntent)
    }

    private fun navigateToCalorieTracking() {
        val intent = Intent(requireContext(), CalorieTracking::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
