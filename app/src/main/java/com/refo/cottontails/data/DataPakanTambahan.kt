package com.refo.cottontails.data

data class DataPakanTambahan(
    val tanggalPemberian: String?,
    val keterangan : String?
){
    constructor() : this("","")
}