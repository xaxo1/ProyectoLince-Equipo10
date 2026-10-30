package com.equipo10.lince.model

/** Filtros del Calendario de servicios. */
enum class FiltroServicios(val etiqueta: String) {
    PROXIMOS("Próximos"),
    EN_CURSO("En curso"),
    FINALIZADOS("Finalizados"),
    CANCELADOS("Cancelados"),
    TODOS("Todos")
}

/** Estado completo de la pantalla "Calendario de servicios". */
data class ServiciosUiState(
    val cargando: Boolean = true,
    val porFecha: Map<String, List<Servicio>> = emptyMap(),   // MAP: fecha -> servicios de ese día
    val filtro: FiltroServicios = FiltroServicios.PROXIMOS,
    val busqueda: String = "",                                 // texto del buscador
    val cantidad: Int = 0,
    val total: Int = 0,
    val error: String? = null
)
