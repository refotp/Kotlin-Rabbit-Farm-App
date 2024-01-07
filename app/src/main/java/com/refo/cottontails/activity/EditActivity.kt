package com.refo.cottontails.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.refo.cottontails.R
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.databinding.ActivityEditBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private lateinit var selectedPhoto: String
    private var bobotKelinci: Double? = null
    private var gender: String? = null
    private val nameJantanList = mutableListOf<String>()
    private val nameBetinaList = mutableListOf<String>()
    private lateinit var selectedImageUri: Uri
    private lateinit var imageCaptureLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var kelID: String
    private lateinit var status : String
    private lateinit var tipe : String
    private lateinit var imageCaptureUri: Uri
    private lateinit var foto : String
    private var useExistingPhoto = true
    private var indukJantan : String? = null
    private var indukBetina : String? = null


    companion object {
        const val CAMERA_PERMISSION_CODE = 101
        const val GALLERY_PERMISSION_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        selectedImageUri = Uri.EMPTY
        val rootView = binding.rootView
        kelID = intent.getStringExtra("Id") ?: ""
        val test = kelID
        val test2 = test
        status = intent.getStringExtra("Status") ?: ""
        tipe = intent.getStringExtra("Tipe") ?: ""
        foto = intent.getStringExtra("Foto Kelinci") ?: ""
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        binding.imageViewBackButton.setOnClickListener{
            onBackPressed()
        }
        // Inisialisasi Firebase Storage dan Database
        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci").child(tipe)
        getInduk(userId)
        // Inisialisasi launcher untuk kamera dan galeri
        imageCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Gunakan hasil dari kamera
                val imageUri = imageCaptureUri // Cobalah mengambil Uri dari hasil
                if (imageUri != null) {
                    selectedImageUri = imageUri
                    binding.iamgeViewFotoKelinci.setImageURI(imageUri)
                } else {
                    Log.e("TAG", "Uri gambar dari kamera null")
                }
            } else {
                Log.e("TAG", "Pengambilan gambar dari kamera gagal")
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            binding.iamgeViewFotoKelinci.setImageBitmap(imageBitmap)
            selectedImageUri = uri!!
        }

        // Mengisi formulir dengan data kelinci yang ada
        setForm(kelID,userId)
        binding.checkboxIndukJantan.setOnCheckedChangeListener{
            buttonView, isChecked ->
                if (isChecked){
                    binding.textViewTitleIndukJantan.visibility = View.VISIBLE
                    binding.spinnerIndukJantan.visibility = View.VISIBLE
                    setSpinnerJantan()
                }
                else{
                    binding.textViewTitleIndukBetina.visibility = View.GONE
                    binding.spinnerIndukBetina.visibility = View.GONE
                }

        }
        binding.checkboxIndukBetina.setOnCheckedChangeListener{
            buttonView, isChecked ->
            if (isChecked){
                binding.textViewTitleIndukBetina.visibility = View.VISIBLE
                binding.spinnerIndukBetina.visibility = View.VISIBLE
                setSpinnerBetina()
            }
            else{
                binding.textViewTitleIndukBetina.visibility = View.GONE
                binding.spinnerIndukBetina.visibility = View.GONE
            }
        }
        // Mengatur aksi tombol
        binding.buttonPickDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.buttonCamera.setOnClickListener {
            checkCameraPermissionAndCaptureImage()
        }

        binding.buttonGallery.setOnClickListener {
            checkGalleryPermissionAndPickImage()
        }

        binding.buttonSumbitForm.setOnClickListener {
            val cekTanggal = cekDate()
            when {
                !cekTanggal -> {
                    rootView.ShowSnackBar("Harap masukan tanggal dengan benar!",100,rootView)
                }
                binding.editTextNamaKelinci.editText?.text.toString().trim().isEmpty() -> {
                    rootView.ShowSnackBar("Nama Kelinci masih kosong, harap isi dahulu", 100, rootView)
                }
                binding.editTextTanggalLahir.editText?.text!!.isEmpty() -> {
                    rootView.ShowSnackBar("Tanggal lahir masih kosong, harap isi tanggal lahir terlebih dahulu !", 100, rootView)
                }
                gender == null -> {
                    rootView.ShowSnackBar("Jenis Kelamin masih kosong, harap isi jenis kelamin terlebih dahulu !", 100, rootView)
                }
                else -> {
                    // Upload gambar terlebih dahulu
                    val dialog2 = Dialog(this)
                    dialog2.setContentView(R.layout.progress_layout)
                    if (dialog2.window != null) {
                        dialog2.window!!.setBackgroundDrawable(ColorDrawable(0))
                    }
                    dialog2.show()
                    uploadImageToFirebaseStorage(kelID,userId)
                }
            }
        }

        binding.radioGroupKelamin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            val selectedValue = selectedRadioButton.text.toString()
            gender = selectedValue
        }
    }
    private fun getInduk(userId: String) {
        getIndukJantan(userId)
        getIndukBetina(userId)
    }

    private fun getIndukJantan(userId: String) {
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci")
            .addListenerForSingleValueEvent(
                object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            for (dataSnapshot in snapshot.children){
                                val item = dataSnapshot.getValue(DataKelinci::class.java)
                                val jenisKelamin = item?.jenisKelaminKelinci
                                if (jenisKelamin == "Jantan"){
                                    nameJantanList.add(item.namaKelinci.toString())
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                }
            )
    }
    private fun getIndukBetina(userId: String) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
            .addListenerForSingleValueEvent(
                object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            for (dataSnapshot in snapshot.children){
                                val item = dataSnapshot.getValue(DataKelinci::class.java)
                                val jenisKelamin = item?.jenisKelaminKelinci
                                if (jenisKelamin == "Betina"){
                                    nameBetinaList.add(item.namaKelinci.toString())
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                }
            )
    }
    private fun setSpinnerJantan() {
        val adapterJantan = ArrayAdapter(
            this@EditActivity,
            android.R.layout.simple_spinner_dropdown_item,
            nameJantanList
        )
        binding.spinnerIndukJantan.adapter = adapterJantan
    }
    private fun setSpinnerBetina() {
        val adapterBetina = ArrayAdapter(
            this@EditActivity,
            android.R.layout.simple_spinner_dropdown_item,
            nameBetinaList
        )
        binding.spinnerIndukBetina.adapter = adapterBetina
    }
    private fun cekDate(): Boolean {
        val cal = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
        val inputDateStr = binding.editTextTanggalLahir.editText?.text.toString()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val inputDate = dateFormat.parse(inputDateStr)
        val tanggalSaatIni = dateFormat.parse(currentDate)

        // Bandingkan tanggal yang diinputkan dengan tanggal saat ini
        return !(inputDate != null && inputDate.after(tanggalSaatIni))
    }

    private fun setForm(kelID: String, userId: String) {
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("kelinci").child(kelID).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(DataKelinci::class.java)
                    bindForm(item)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Penanganan kesalahan pembatalan (optional)
                }
            }
        )
    }

    private fun bindForm(item: DataKelinci?) {
        val namaKelinci = item?.namaKelinci ?: ""
        val jenis = item?.jenisKelinci ?: ""
        val tanggalLahir = item?.tanggalLahir ?: ""
        val bobotKelinci = item?.bobotKelinci ?: 0.0
        val fotoKelinci = if (useExistingPhoto) item?.fotoKelinci ?: "" else selectedPhoto
        binding.editTextNamaKelinci.editText?.setText(namaKelinci)
        binding.editTextJenisKelinci.editText?.setText(jenis)
        binding.editTextTanggalLahir.editText?.setText(tanggalLahir)
        binding.editTextBobotKelinci.editText?.setText(bobotKelinci.toString())

        if (item?.jenisKelaminKelinci == "Jantan") {
            binding.radioButtonMale.isChecked = true
        } else if (item?.jenisKelaminKelinci == "Betina") {
            binding.radioButtonFemale.isChecked = true
        }
        if (useExistingPhoto) {
            // Gunakan foto yang ada (item?.fotoKelinci)
            Glide.with(this).load(item?.fotoKelinci).into(binding.iamgeViewFotoKelinci)
        } else {
            // Gunakan foto yang baru (selectedImageUri)
            Glide.with(this).load(selectedImageUri).into(binding.iamgeViewFotoKelinci)
        }
    }
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        if (path != null) {
            return Uri.parse(path)
        } else {
            // Jika path null, Anda dapat mengembalikan Uri.EMPTY atau mengambil tindakan lain sesuai kebutuhan Anda.
            return Uri.EMPTY // Atau Anda bisa memilih tindakan lainnya
        }
    }

    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val cal = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view: DatePicker, mYear: Int, mMonth: Int, mDay: Int ->
            cal.set(Calendar.YEAR, mYear)
            cal.set(Calendar.MONTH, mMonth)
            cal.set(Calendar.DAY_OF_MONTH, mDay)
            val dateFormat = "dd-MM-yyyy"
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            binding.editTextTanggalLahir.editText?.setText(simpleDateFormat.format(cal.time))
        }, year, month, day)

        dpd.show()
    }

    private fun checkCameraPermissionAndCaptureImage() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val imageFileName = "${UUID.randomUUID()}.jpg"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File(storageDir, imageFileName)
            imageCaptureUri = FileProvider.getUriForFile(this, "com.refo.cottontails.fileprovider", imageFile)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri)
            imageCaptureLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun checkGalleryPermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_PERMISSION_CODE)
        }
    }

    private fun uploadImageToFirebaseStorage(kelID: String, userId: String) {
        if (selectedImageUri != Uri.EMPTY) {
            // Pengguna memilih untuk mengganti foto
            val imageFileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageReference.child("images/$imageFileName")
            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Gambar berhasil diunggah
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val newPhotoUrl = uri.toString()
                        updateDataTernak(kelID, userId, newPhotoUrl) // Memperbarui data dengan URL foto baru
                    }.addOnFailureListener { exception ->
                        // Penanganan kesalahan saat mendapatkan URL gambar
                        exception.printStackTrace()
                    }
                }
                .addOnFailureListener { exception ->
                    // Penanganan kesalahan saat mengunggah gambar
                    exception.printStackTrace()
                }
        } else {
            // Pengguna memilih untuk tetap menggunakan foto yang ada
            updateDataTernak(kelID, userId, foto) // Tetap menggunakan foto yang ada
        }

        }
    private fun updateImageViewWithUri(uri: Uri) {
        // Load gambar dengan Glide ke ImageView
        Glide.with(this).load(uri).into(binding.iamgeViewFotoKelinci)
    }
    private fun updateDataTernak(kelID: String,userId : String?, newPhotoUrl: String) {

        val namaKelinci = binding.editTextNamaKelinci.editText?.text.toString().trim()
        val jenisKelinci = binding.editTextJenisKelinci.editText?.text.toString().trim()
        indukBetina = binding.spinnerIndukBetina.selectedItem?.toString()
        indukJantan = binding.spinnerIndukJantan.selectedItem?.toString()
        val tanggalLahir = binding.editTextTanggalLahir.editText?.text.toString()
        val bobotKelinci = binding.editTextBobotKelinci.editText?.text.toString().toDoubleOrNull() ?: 0.0
        val jenisKelaminKelinci = gender ?: ""

        // Membuat objek DataKelinci dengan data yang diperbarui
        val updatedKelinci = if (indukJantan != null && indukBetina != null) {
            DataKelinci(
                kelID, namaKelinci, jenisKelinci, tanggalLahir, bobotKelinci,
                jenisKelaminKelinci, newPhotoUrl, indukJantan!!, indukBetina!!, status, tipe
            )
        } else {
            DataKelinci(
                kelID, namaKelinci, jenisKelinci, tanggalLahir, bobotKelinci, jenisKelaminKelinci,
                newPhotoUrl, indukJantan = "", indukBetina = "", status, tipe
            )
        }

        // Memperbarui data kelinci di Firebase Database
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci").child(kelID).setValue(updatedKelinci)
            .addOnSuccessListener {

                finish()
            }
            .addOnFailureListener { exception ->
                // Penanganan kesalahan saat memperbarui data
                exception.printStackTrace()
                // Tampilkan pesan kesalahan kepada pengguna jika perlu
            }
    }
}