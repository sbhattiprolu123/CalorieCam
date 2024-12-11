package com.cs407.caloriecam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

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
                if (photo != null) {
                    handleCapturedPhoto(photo)
                }
            }
        }

    private val galleryActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    handleSelectedPhoto(imageUri)
                }
            }
        }

    private lateinit var spinnerSelection: Spinner
    private var selectedPhotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meal_logging, container, false)

        val btnTakePhoto: Button = view.findViewById(R.id.btn_take_photo)
        val btnChoosePhoto: Button = view.findViewById(R.id.btn_choose_photo)
        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        val btnUpload: Button = Button(requireContext()).apply {
            text = "Upload"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        (view as LinearLayout).addView(btnUpload)

        spinnerSelection = view.findViewById(R.id.spinner_selection)

        // Populate Spinner with foodList from CalorieTracking
        val calorieTracking = CalorieTracking()
        val foodList = calorieTracking.getFoodList()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, foodList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSelection.adapter = adapter

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

        btnUpload.setOnClickListener {
            uploadData()
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
        galleryActivityLauncher.launch(galleryIntent)
    }

    private fun handleCapturedPhoto(photo: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val photoBytes = outputStream.toByteArray()
        val photoUri = Uri.parse(MediaStore.Images.Media.insertImage(requireContext().contentResolver, photo, null, null))
        selectedPhotoUri = photoUri
        Toast.makeText(requireContext(), "Photo captured and ready to upload.", Toast.LENGTH_SHORT).show()
    }

    private fun handleSelectedPhoto(uri: Uri) {
        selectedPhotoUri = uri
        Toast.makeText(requireContext(), "Photo selected and ready to upload.", Toast.LENGTH_SHORT).show()
    }

    private fun uploadData() {
        val selectedFood = spinnerSelection.selectedItem?.toString() ?: "No food selected"

        if (selectedPhotoUri == null) {
            Toast.makeText(requireContext(), "No photo selected to upload.", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload the photo to Firebase Storage
        val storageReference = FirebaseStorage.getInstance().reference
        val photoRef = storageReference.child("meal_photos/${UUID.randomUUID()}.jpg")
        val uploadTask = photoRef.putFile(selectedPhotoUri!!)

        uploadTask.addOnSuccessListener {
            photoRef.downloadUrl.addOnSuccessListener { uri ->
                // Upload the food and photo URL to Firebase Database
                val databaseReference = FirebaseDatabase.getInstance().reference.child("meals")
                val mealId = databaseReference.push().key

                val mealData = mapOf(
                    "food" to selectedFood,
                    "photoUrl" to uri.toString()
                )

                databaseReference.child(mealId!!).setValue(mealData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Data uploaded successfully.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to upload data.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to upload photo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCalorieTracking() {
        val intent = Intent(requireContext(), CalorieTracking::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}