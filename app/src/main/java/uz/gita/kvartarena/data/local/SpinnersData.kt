package uz.gita.kvartarena.data.local

import uz.gita.kvartarena.R
import uz.gita.kvartarena.model.Type

class SpinnersData {
    companion object {
        val list = listOf(
            Type("Oziq-ovqat", R.drawable.food),
            Type("Futbol", R.drawable.football),
            Type("Kvartplata", R.drawable.house),
            Type("O'zaro qarz", R.drawable.dollar),
            Type("Wi-fi", R.drawable.wifi),
            Type("Kommunal", R.drawable.kommunal),
            Type("Taksi", R.drawable.taxi),
            Type("Boshqalar", R.drawable.boshqalar)
        )

        fun getAll(): List<Type> = listOf(
            Type("Oziq-ovqat", R.drawable.food),
            Type("Futbol", R.drawable.football),
            Type("Kvartplata", R.drawable.house),
            Type("O'zaro qarz", R.drawable.dollar),
            Type("Wi-fi", R.drawable.wifi),
            Type("Kommunal", R.drawable.kommunal),
            Type("Taksi", R.drawable.taxi),
            Type("Boshqalar", R.drawable.boshqalar)
        )
    }
}