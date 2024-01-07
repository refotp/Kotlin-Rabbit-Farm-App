package com.refo.cottontails.data

data class DataKelinciPeriksa(
    val namaKelinci: String = "",
    val fotoKelinci: String?= "",
    val tanggalPeriksa: String = "",
    val keterangan : String = ""
) {
    constructor() : this("","","","")
}
