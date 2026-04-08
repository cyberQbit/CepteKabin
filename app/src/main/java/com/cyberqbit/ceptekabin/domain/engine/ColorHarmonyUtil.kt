package com.cyberqbit.ceptekabin.domain.engine

object ColorHarmonyUtil {

    enum class RenkGrubu {
        NOTR, LACIVERT, MAVI, KIRMIZI, YESIL, SARI, TURUNCU, PEMBE, MOR, KAHVERENGI, DESENLI, DIGER
    }

    private val renkHaritasi = mapOf(
        "Siyah" to RenkGrubu.NOTR, "Beyaz" to RenkGrubu.NOTR, "Gri" to RenkGrubu.NOTR,
        "Bej" to RenkGrubu.NOTR, "Krem" to RenkGrubu.NOTR, "Ekru" to RenkGrubu.NOTR,
        "Lacivert" to RenkGrubu.LACIVERT, "Mavi" to RenkGrubu.MAVI, "Petrol" to RenkGrubu.MAVI,
        "Kırmızı" to RenkGrubu.KIRMIZI, "Bordo" to RenkGrubu.KIRMIZI,
        "Yeşil" to RenkGrubu.YESIL, "Haki" to RenkGrubu.YESIL,
        "Sarı" to RenkGrubu.SARI, "Hardal" to RenkGrubu.SARI,
        "Turuncu" to RenkGrubu.TURUNCU, "Pembe" to RenkGrubu.PEMBE, "Mor" to RenkGrubu.MOR,
        "Kahverengi" to RenkGrubu.KAHVERENGI,
        "Çoklu / Desenli" to RenkGrubu.DESENLI, "Diğer" to RenkGrubu.DIGER
    )

    fun getRenkGrubu(renk: String): RenkGrubu = renkHaritasi[renk] ?: RenkGrubu.DIGER

    fun uyumPuani(renk1: String, renk2: String): Int {
        val g1 = getRenkGrubu(renk1)
        val g2 = getRenkGrubu(renk2)
        if (g1 == g2) return 85
        if (g1 == RenkGrubu.NOTR || g2 == RenkGrubu.NOTR) return 95
        if (g1 == RenkGrubu.LACIVERT || g2 == RenkGrubu.LACIVERT) return 90
        if (g1 == RenkGrubu.DESENLI || g2 == RenkGrubu.DESENLI) return 60
        if (g1 == RenkGrubu.KAHVERENGI || g2 == RenkGrubu.KAHVERENGI) {
            val diger = if (g1 == RenkGrubu.KAHVERENGI) g2 else g1
            return when (diger) {
                RenkGrubu.YESIL, RenkGrubu.SARI -> 85
                RenkGrubu.TURUNCU -> 80
                RenkGrubu.MAVI -> 75
                RenkGrubu.KIRMIZI -> 65
                else -> 60
            }
        }
        val komplementer = setOf(
            setOf(RenkGrubu.MAVI, RenkGrubu.TURUNCU), setOf(RenkGrubu.KIRMIZI, RenkGrubu.YESIL),
            setOf(RenkGrubu.SARI, RenkGrubu.MOR), setOf(RenkGrubu.PEMBE, RenkGrubu.YESIL)
        )
        if (setOf(g1, g2) in komplementer) return 80
        val analog = setOf(
            setOf(RenkGrubu.MAVI, RenkGrubu.MOR), setOf(RenkGrubu.KIRMIZI, RenkGrubu.TURUNCU),
            setOf(RenkGrubu.SARI, RenkGrubu.TURUNCU), setOf(RenkGrubu.PEMBE, RenkGrubu.MOR),
            setOf(RenkGrubu.PEMBE, RenkGrubu.KIRMIZI), setOf(RenkGrubu.YESIL, RenkGrubu.MAVI)
        )
        if (setOf(g1, g2) in analog) return 75
        val cakisan = setOf(
            setOf(RenkGrubu.KIRMIZI, RenkGrubu.TURUNCU), setOf(RenkGrubu.KIRMIZI, RenkGrubu.PEMBE)
        )
        if (setOf(g1, g2) in cakisan) return 45
        return 55
    }

    fun toplamUyumPuani(renkler: List<String>): Int {
        if (renkler.size < 2) return 100
        val ciftler = renkler.flatMapIndexed { i, r1 ->
            renkler.drop(i + 1).map { r2 -> uyumPuani(r1, r2) }
        }
        return if (ciftler.isEmpty()) 100 else ciftler.average().toInt()
    }
}
