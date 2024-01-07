package com.refo.cottontails.data

data class DataPengeluaran(
    val idTransaksi: String,
    val jenisPengeluaran : String,
    val jumlahUang : Int,
    val keterangan : String?,
    val waktuTransaksi : String,

){
    constructor() : this("","",0,"","")
}
