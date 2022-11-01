package uz.gita.kvartarena.model

data class Expense(
    val timeStamp: String,
    val comment: String,
    val amount: Int,
    val investor: String,
    val investorName: String,
    val type: String
)