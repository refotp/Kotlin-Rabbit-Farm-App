package com.refo.cottontails.data

data class DataKelinciKawin(
    val id : String,
    val namaKelinci: String?,
    val kelinciJantan :String? = "",
    val fotoKelinci: String?,
    val tanggalKawin: String?,
    val perbedaanTanggal : Long = 0
) {
    constructor() :this("","","","","",0)
}
