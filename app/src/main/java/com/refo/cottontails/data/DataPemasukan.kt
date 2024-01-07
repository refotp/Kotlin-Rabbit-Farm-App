package com.refo.cottontails.data

import java.sql.Time

data class DataPemasukan(
    val idTransaksi : String,
    val jumlahUang : Int,
    val namaKelinci : String,
    val waktuTransaksi : String,

){
    constructor() : this("",0,"","")
}
