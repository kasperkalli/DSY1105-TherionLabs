package cl.duoc.therionlabs.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

@Database(
    entities =
        [
            ProspectoEntity::class,
            UsuarioEntity::class,
            EventoEntity::class,
            SeguimientoEntity::class,
        ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun prospectoDao(): ProspectoDAO

    abstract fun operacionDao(): OperacionDAO

    companion object {

        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN modalidad TEXT NOT NULL DEFAULT ''"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN canalContacto TEXT NOT NULL DEFAULT ''"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN observaciones TEXT NOT NULL DEFAULT ''"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN autorizaContacto INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN fechaCaptura INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 0"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN identificador TEXT NOT NULL DEFAULT ''"
                    )

                    // Los registros que ya existían reciben su propio identificador.
                    val idsExistentes = mutableListOf<Int>()
                    db.query("SELECT id FROM prospectos WHERE identificador = ''").use { cursor ->
                        val indiceId = cursor.getColumnIndexOrThrow("id")
                        while (cursor.moveToNext()) {
                            idsExistentes.add(cursor.getInt(indiceId))
                        }
                    }

                    idsExistentes.forEach { id ->
                        db.execSQL(
                            "UPDATE prospectos SET identificador = ? WHERE id = ?",
                            arrayOf<Any>(UUID.randomUUID().toString(), id),
                        )
                    }

                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS index_prospectos_identificador ON prospectos(identificador)"
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE prospectos ADD COLUMN fotoRuta TEXT")
                    db.execSQL("ALTER TABLE prospectos ADD COLUMN latitud REAL")
                    db.execSQL("ALTER TABLE prospectos ADD COLUMN longitud REAL")
                    db.execSQL("ALTER TABLE prospectos ADD COLUMN precisionMetros REAL")
                    db.execSQL("ALTER TABLE prospectos ADD COLUMN fechaUbicacion INTEGER")
                }
            }

        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE prospectos ADD COLUMN direccion TEXT")
                }
            }

        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN eventoId TEXT NOT NULL DEFAULT ''"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN eventoNombre TEXT NOT NULL DEFAULT ''"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN captadorId TEXT NOT NULL DEFAULT ''"
                    )
                    db.execSQL(
                        "ALTER TABLE prospectos ADD COLUMN captador TEXT NOT NULL DEFAULT ''"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_prospectos_eventoId ON prospectos(eventoId)"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS usuarios (id TEXT NOT NULL, usuario TEXT NOT NULL, nombre TEXT NOT NULL, rol TEXT NOT NULL, salt TEXT NOT NULL, passwordHash TEXT NOT NULL, PRIMARY KEY(id))"
                    )
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS index_usuarios_usuario ON usuarios(usuario)"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS eventos (id TEXT NOT NULL, nombre TEXT NOT NULL, colegio TEXT NOT NULL, fecha TEXT NOT NULL, actividad TEXT NOT NULL, activo INTEGER NOT NULL, comuna TEXT NOT NULL DEFAULT '', PRIMARY KEY(id))"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS seguimientos (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, prospectoId INTEGER NOT NULL, administrativoId TEXT NOT NULL, administrativoNombre TEXT NOT NULL, estado TEXT NOT NULL, canal TEXT NOT NULL, observacion TEXT NOT NULL, fecha INTEGER NOT NULL, estadoEnvio TEXT NOT NULL, FOREIGN KEY(prospectoId) REFERENCES prospectos(id) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(administrativoId) REFERENCES usuarios(id) ON UPDATE NO ACTION ON DELETE RESTRICT)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_seguimientos_prospectoId ON seguimientos(prospectoId)"
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_seguimientos_administrativoId ON seguimientos(administrativoId)"
                    )
                }
            }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "therionlabs_database",
                            )
                            .addMigrations(
                                MIGRATION_1_2,
                                MIGRATION_2_3,
                                MIGRATION_3_4,
                                MIGRATION_4_5,
                            )
                            .build()
                            .also { INSTANCE = it }
                }
        }
    }
}
