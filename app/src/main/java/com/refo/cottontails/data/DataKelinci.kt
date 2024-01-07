package com.refo.cottontails.data

import android.media.Image
import android.net.Uri

data class DataKelinci(
        val kelId: String,
        val namaKelinci: String?,
        val jenisKelinci: String?,
        val tanggalLahir : String?,
        val bobotKelinci: Double?,
        val jenisKelaminKelinci: String?,
        val fotoKelinci: String?,
        val indukJantan : String = "",
        val indukBetina : String = "",
        var status : String = "Hidup",
        var tipe : String = "aktif",

){

        constructor() : this("","","","",0.0,"","","","")
}
