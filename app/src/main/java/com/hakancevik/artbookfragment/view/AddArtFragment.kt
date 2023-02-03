package com.hakancevik.artbookfragment.view

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.hakancevik.artbookfragment.R

import com.hakancevik.artbookfragment.database.ArtDao
import com.hakancevik.artbookfragment.database.ArtDatabase
import com.hakancevik.artbookfragment.databinding.FragmentAddArtBinding
import com.hakancevik.artbookfragment.model.Art
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Base64


class AddArtFragment : Fragment() {

    private var _binding: FragmentAddArtBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedBitmap: Bitmap? = null


    private lateinit var db: ArtDatabase
    private lateinit var artDao: ArtDao

    val compositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddArtBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Room.databaseBuilder(requireActivity().applicationContext, ArtDatabase::class.java, "Arts").build()
        artDao = db.artDao()


        arguments?.let {
            val getInfo = AddArtFragmentArgs.fromBundle(it).info

            if (getInfo.equals("new")) {

                binding.saveButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.GONE
                binding.selectImage.setImageResource(R.drawable.select_image)



                registerLauncher()

                binding.selectImage.setOnClickListener(View.OnClickListener {
                    if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // rationale
                            Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                                // request permission
                                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()


                        } else {
                            // request permission
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }


                    } else {
                        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncher.launch(intentToGallery)
                    }

                })


                binding.saveButton.setOnClickListener(View.OnClickListener {

                    selectedBitmap?.let {

                        val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
                        val outputStream = ByteArrayOutputStream()
                        smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                        val byteArray = outputStream.toByteArray()

                        val art = Art(binding.artNameText.text.toString(), binding.artistNameText.text.toString(), binding.yearText.text.toString(), byteArray)

                        compositeDisposable.add(
                            artDao.insert(art)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::handleResponse)
                        )

                    }


                })


            } else {
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE

                arguments?.let {
                    val getArt = AddArtFragmentArgs.fromBundle(it).art
                    binding.artNameText.setText(getArt.artName)
                    binding.artistNameText.setText(getArt.artistName)
                    binding.yearText.setText(getArt.year)

                    val byteArray = getArt.image
                    val imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
                    binding.selectImage.setImageBitmap(imageBitmap)

                    binding.deleteButton.setOnClickListener(View.OnClickListener {

                        compositeDisposable.add(
                            artDao.delete(getArt)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(this::handleResponse)
                        )

                    })


                }


            }

        }


    }

    private fun handleResponse() {
        val action = AddArtFragmentDirections.actionAddArtFragmentToHomeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }


    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)

                    // uri to --> Bitmap
                    if (imageData != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(requireActivity().contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.selectImage.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageData)
                                binding.selectImage.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                }
            }

        }


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(requireContext(), "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            // landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()

        } else {
            // portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()

        }

        return Bitmap.createScaledBitmap(image, width, height, true)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.clear()
    }


}