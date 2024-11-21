package com.cs407.caloriecam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.graphics.Bitmap
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

class MealLoggingFragment : Fragment() {

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
                // Handle the captured photo (e.g., display or save it)
                Toast.makeText(requireContext(), "Photo captured successfully", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data
                // Handle the selected image URI (e.g., display or save it)
                Toast.makeText(requireContext(), "Photo selected successfully", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_meal_logging, container, false)

        // Find UI elements
        val btnTakePhoto: Button = view.findViewById(R.id.btn_take_photo)
        val btnChoosePhoto: Button = view.findViewById(R.id.btn_choose_photo)

        // Set up button click listeners
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
            Toast.makeText(requireContext(), "Device has a camera.", Toast.LENGTH_SHORT).show()
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity = takePictureIntent.resolveActivity(packageManager)

            if (resolvedActivity != null) {
                Toast.makeText(requireContext(), "Camera app found.", Toast.LENGTH_SHORT).show()
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
        galleryActivityLauncher.launch(galleryIntent)
    }
}
