package com.refo.cottontails.data

data class DataKawin(
    val id : String,
    val kelinciBetina : String,
    val tanggal : String,
    val kelinciJantan : String?,
    var keterangan : String = ""

){
    constructor() : this ("","","","","")

}
