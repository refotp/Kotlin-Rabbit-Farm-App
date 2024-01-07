package com.refo.cottontails.activity
import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.refo.cottontails.ShowSnackBar
import com.refo.cottontails.data.DataKelinci
import com.refo.cottontails.databinding.ActivityInputTambahTernakBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class InputTernakActivity : AppCompatActivity() {
    private lateinit var binding : ActivityInputTambahTernakBinding
    private lateinit var selectedPhoto: String
    private var selectedImageUri : Uri? = null
    private var bobotKelinci : Double? = null
    private var gender : String? = null
    val nameJantanAktif = mutableListOf<String>()
    val nameJantanNonAktif = mutableListOf<String>()
    val nameBetinaAktif = mutableListOf<String>()
    val nameBetinaNonAktif = mutableListOf<String>()
    val nameBetinaList = mutableListOf<String>()
    val nameJantanList = mutableListOf<String>()
    private lateinit var imageCaptureLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var storageReference: StorageReference
    private var indukJantan : String = ""
    private var indukBetina : String = ""
    private lateinit var updatedKelinci : DataKelinci
    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
        private const val GALLERY_PERMISSION_CODE = 102
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputTambahTernakBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
//        val sharedPref = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
//        val userId = sharedPref.getString("userId", null)
        val rootView = binding.rootView
        supportActionBar?.hide()
        storageReference = FirebaseStorage.getInstance().reference
        activityLauncher()
        buttonAction(rootView,userId)
        getInduk(userId)
    }



    private fun getInduk(userId: String?) {
        getIndukJantan(userId)
        getIndukBetina(userId)
    }

    private fun getIndukJantan(userId: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("kelinci")
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
    private fun getIndukBetina(userId: String?) {
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
            this@InputTernakActivity,
            android.R.layout.simple_spinner_dropdown_item,
            nameJantanList
        )
        binding.spinnerIndukJantan.adapter = adapterJantan
    }

    private fun setSpinnerBetina() {
        val adapterBetina = ArrayAdapter(
            this@InputTernakActivity,
            android.R.layout.simple_spinner_dropdown_item,
            nameBetinaList
        )
        binding.spinnerIndukBetina.adapter = adapterBetina
    }

    private fun activityLauncher() {
        imageCaptureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val imageBitmap = data.extras?.get("data") as Bitmap
                        binding.iamgeViewFotoKelinci.setImageBitmap(imageBitmap)
                        selectedImageUri = getImageUriFromBitmap(imageBitmap)
                    } else {
                        // Handle the case where data is null
                    }
                }
            }
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            binding.iamgeViewFotoKelinci.setImageBitmap(imageBitmap)
            selectedImageUri = uri

        }
    }

    private fun InputTernakActivity.buttonAction(rootView: View, userId: String?) {
        binding.imageViewBackButton.setOnClickListener {
            onBackPressed()
        }

        binding.checkboxIndukJantan.setOnCheckedChangeListener{
                buttonView, isChecked ->
            if (isChecked){
                binding.textViewTitleIndukJantan.visibility = View.VISIBLE
                binding.spinnerIndukJantan.visibility = View.VISIBLE
                setSpinnerJantan()
            }
            else{
                binding.textViewTitleIndukJantan.visibility = View.GONE
                binding.spinnerIndukJantan.visibility = View.GONE
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

        binding.buttonPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            val cal = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view: DatePicker, mYear: Int, mMonth: Int, mDay: Int ->
                    cal.set(Calendar.YEAR, mYear)
                    cal.set(Calendar.MONTH, mMonth)
                    cal.set(Calendar.DAY_OF_MONTH, mDay)
                    val dateFormat = "dd-MM-yyyy"
                    val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
                    binding.editTextTanggalLahir.editText?.setText(simpleDateFormat.format(cal.time))

                },
                year,
                month,
                day)

            dpd.show()
        }


        binding.buttonCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                imageCaptureLauncher.launch(intent)
            } else {
                // Permintaan izin kamera kepada pengguna
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE)
            }
        }
        binding.buttonGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {
                galleryLauncher.launch("image/*")
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    GALLERY_PERMISSION_CODE)
            }

        }
        binding.buttonSumbitForm.setOnClickListener {
            val cekTanggal = checkDate()
            when {
                !cekTanggal ->{
                    rootView.ShowSnackBar("Harap masukan tanggal dengan benar!",100,rootView)
                }
                binding.editTextNamaKelinci.editText?.text.toString().trim().isEmpty() -> {
                    rootView.ShowSnackBar("Nama Kelinci masih kosong, harap isi dahulu",
                        100,
                        rootView)
                }
                binding.editTextTanggalLahir.editText?.text!!.isEmpty() -> {
                    rootView.ShowSnackBar("Tanggal lahir masih kosong, harap isi tanggal lahir terlebih dahulu !",
                        100,
                        rootView)
                }
                selectedImageUri == null -> {
                    rootView.ShowSnackBar("Foto Masih Kosong, harap isi foto terlebih dahulu !",
                        100,
                        rootView)
                }
                gender == null -> {
                    rootView.ShowSnackBar("Jenis Kelamin masih kosong, harap isi jenis kelamin terlebih dahulu !",
                        100,
                        rootView)
                }
                else -> {
                    uploadImageToFirebaseStorage(selectedImageUri!!,userId)
                }
            }


        }
        binding.radioGroupKelamin.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            val selectedValue = selectedRadioButton.text.toString()
            gender = selectedValue

        }
    }

    private fun checkDate() : Boolean {
        val cal = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
        val inputDateStr = binding.editTextTanggalLahir.editText?.text.toString()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

            val inputDate = dateFormat.parse(inputDateStr)
            val tanggalSaatIni = dateFormat.parse(currentDate)

            // Bandingkan tanggal yang diinputkan dengan tanggal saat ini
        return !(inputDate != null && inputDate.after(tanggalSaatIni))

    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            val outputStream = contentResolver.openOutputStream(uri)
            outputStream?.write(baos.toByteArray())
            outputStream?.close()
        }

        return uri ?: Uri.parse("") // Return an empty Uri if uri is null
    }


    private fun uploadImageToFirebaseStorage(imageUri: Uri, userId: String?) {
        val imageFileName = "${UUID.randomUUID()}.jpg"
        val imageRef = storageReference.child("images/$imageFileName")
        val dialog2 = Dialog(this)
        dialog2.setContentView(com.refo.cottontails.R.layout.progress_layout)
        if (dialog2.window != null){
            dialog2.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        dialog2.show()
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Gambar berhasil diunggah
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    selectedPhoto = uri.toString()
                    tambahTernak()
                    // Anda dapat menggunakan imageUrl sesuai kebutuhan Anda
                }.addOnFailureListener { exception ->
                    // Penanganan kesalahan saat mendapatkan URL gambar
                    exception.printStackTrace()
                }
            }
            .addOnFailureListener { exception ->
                // Penanganan kesalahan saat mengunggah gambar
                exception.printStackTrace()
            }
    }

    private fun tambahTernak() {
        val dbref = FirebaseDatabase.getInstance().reference
        val namaKelinci = binding.editTextNamaKelinci.editText?.text.toString().trim()
        val jenisKelinci = binding.editTextJenisKelinci.editText?.text?.toString()?.trim()
        val bobotKelinciText = binding.editTextBobotKelinci.editText?.text.toString()
        bobotKelinci = if (bobotKelinciText.isNotEmpty()) {
            bobotKelinciText.toDouble()
        } else {
            0.0
        }
        val jenisKelamin = gender
        val tanggalLahir = binding.editTextTanggalLahir.editText?.text.toString()
        indukJantan = if (binding.checkboxIndukJantan.isChecked) {
            binding.spinnerIndukJantan.selectedItem.toString()
        } else {
            ""
        }
        indukBetina = if (binding.checkboxIndukBetina.isChecked) {
            binding.spinnerIndukBetina.selectedItem.toString()
        } else {
            ""
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userID = currentUser.uid

            val userRef = dbref.child("users").child(userID)
            val kelId = userRef.child("kelinci").push().key
            if (kelId != null) {
                val dataKelinci =
                    DataKelinci(kelId,
                        namaKelinci,
                        jenisKelinci,
                        tanggalLahir,
                        bobotKelinci,
                        jenisKelamin,
                        selectedPhoto,
                        indukJantan,
                        indukBetina)
                userRef.child("kelinci").child(kelId)
                    .setValue(dataKelinci)
                    .addOnCompleteListener { kelinci ->
                        if (kelinci.isSuccessful) {
                            finish()
                        } else {

                        }
                    }
            }




        }


    }

}