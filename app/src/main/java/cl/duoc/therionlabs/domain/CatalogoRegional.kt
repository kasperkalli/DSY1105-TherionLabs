package cl.duoc.therionlabs.domain

import java.text.Normalizer
import java.util.Locale

object CatalogoRegional {
    const val region = "Región de Valparaíso"
    val sedes = listOf("Valparaíso", "Viña del Mar")
    val comunas =
        listOf(
            "Algarrobo",
            "Cabildo",
            "Calera",
            "Calle Larga",
            "Cartagena",
            "Casablanca",
            "Catemu",
            "Concón",
            "El Quisco",
            "El Tabo",
            "Hijuelas",
            "Isla de Pascua",
            "Juan Fernández",
            "La Cruz",
            "La Ligua",
            "Limache",
            "Llaillay",
            "Los Andes",
            "Nogales",
            "Olmué",
            "Panquehue",
            "Papudo",
            "Petorca",
            "Puchuncaví",
            "Putaendo",
            "Quillota",
            "Quilpué",
            "Quintero",
            "Rinconada",
            "San Antonio",
            "San Esteban",
            "San Felipe",
            "Santa María",
            "Santo Domingo",
            "Valparaíso",
            "Villa Alemana",
            "Viña del Mar",
            "Zapallar",
        )

    private val informatica = listOf("Ingeniería en Informática", "Analista Programador")
    private val administracion =
        listOf(
            "Ingeniería en Marketing Digital",
            "Ingeniería en Gestión Logística",
            "Ingeniería en Comercio Exterior",
            "Técnico en Administración",
            "Técnico en Gestión Logística",
        )

    private val valparaiso =
        linkedMapOf(
            "Administración y Negocios" to administracion,
            "Construcción" to
                listOf(
                    "Técnico Modelador Proyectista",
                    "Ingeniería en Construcción",
                    "Ingeniería en Prevención de Riesgos",
                    "Técnico en Construcción",
                    "Técnico Topógrafo Geomático",
                ),
            "Gastronomía" to listOf("Gastronomía Internacional"),
            "Informática y Telecomunicaciones" to informatica,
            "Ingeniería y Recursos Naturales" to
                listOf(
                    "Técnico en Electricidad y Automatización Industrial",
                    "Técnico en Mantenimiento Industrial",
                    "Ingeniería en Maquinaria y Vehículos Pesados",
                    "Ingeniería en Mantenimiento Industrial",
                    "Ingeniería en Electricidad y Automatización Industrial",
                    "Ingeniería Agrícola",
                    "Ingeniería en Mecánica Automotriz y Autotrónica",
                    "Técnico Veterinario y Pecuario",
                    "Técnico en Operación y Supervisión de Procesos Mineros",
                    "Técnico en Mecánica Automotriz y Autotrónica",
                    "Técnico en Maquinaria y Vehículos Pesados",
                ),
            "Salud y Bienestar" to
                listOf(
                    "Técnico en Laboratorio Clínico, Banco de Sangre e Imagenología",
                    "Informática Biomédica",
                    "Técnico en Enfermería",
                    "Técnico en Química y Farmacia",
                    "Técnico en Odontología",
                ),
            "Turismo y Hospitalidad" to listOf("Administración en Turismo y Hospitalidad"),
        )
    private val vina =
        linkedMapOf(
            "Administración y Negocios" to
                (administracion +
                    listOf(
                        "Contabilidad General Mención Legislación Tributaria",
                        "Auditoría",
                        "Comercio Exterior",
                        "Ingeniería en Administración Mención Gestión de Personas",
                        "Ingeniería en Administración Mención Innovación y Emprendimiento",
                        "Ingeniería en Administración Mención Finanzas",
                    )),
            "Comunicación" to
                listOf(
                    "Comunicación Audiovisual",
                    "Relaciones Públicas y Comunicación Organizacional",
                    "Publicidad",
                    "Animación Digital",
                    "Ingeniería en Sonido",
                    "Tecnología en Sonido e Iluminación",
                ),
            "Diseño" to
                listOf(
                    "Ilustración para Contextos Globales",
                    "Diseño Industrial e Innovación en Productos",
                    "Diseño Gráfico",
                    "Diseño de Vestuario",
                    "Diseño de Ambientes",
                ),
            "Informática y Telecomunicaciones" to
                (informatica + "Ingeniería en Redes y Telecomunicaciones"),
            "Salud y Bienestar" to listOf("Preparador Físico"),
        )

    fun porEscuela(sede: String): Map<String, List<String>> =
        when (sede) {
            "Valparaíso" -> valparaiso
            "Viña del Mar" -> vina
            else -> emptyMap()
        }

    fun carreras(sede: String) = porEscuela(sede).values.flatten().distinct()

    fun normalizar(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .trim()
            .lowercase(Locale.ROOT)
}
