package com.cyberqbit.ceptekabin.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS takvim_girisi (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                tarih_gun INTEGER NOT NULL,
                slot INTEGER NOT NULL DEFAULT 0,
                kombin_id INTEGER NOT NULL,
                kombin_ad TEXT NOT NULL,
                ust_giyim_ad TEXT,
                alt_giyim_ad TEXT,
                dis_giyim_ad TEXT,
                ayakkabi_ad TEXT,
                aksesuar_ad TEXT,
                ust_giyim_resim TEXT,
                alt_giyim_resim TEXT,
                dis_giyim_resim TEXT,
                ayakkabi_resim TEXT,
                aksesuar_resim TEXT,
                ekleme_zamani INTEGER NOT NULL DEFAULT 0,
                kombin_silinmis INTEGER NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_takvim_girisi_tarih_gun_slot ON takvim_girisi (tarih_gun, slot)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_takvim_girisi_kombin_id ON takvim_girisi (kombin_id)")
    }
}
