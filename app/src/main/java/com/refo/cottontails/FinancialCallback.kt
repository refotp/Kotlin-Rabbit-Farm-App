package com.refo.cottontails

interface FinancialCallback {
    fun onPemasukanUpdated(pemasukan: Int)
    fun onPengeluaranUpdated(pengeluaran: Int)
}