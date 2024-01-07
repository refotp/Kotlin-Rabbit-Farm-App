package com.refo.cottontails

import android.widget.ImageView

data class CardItemKeuangan(
    val title : String,
    val uang : String,
    val gambar : Int,
    val cardBackgroundResId : Int,
){
    constructor() : this("","Rp 0",0,0)
}
