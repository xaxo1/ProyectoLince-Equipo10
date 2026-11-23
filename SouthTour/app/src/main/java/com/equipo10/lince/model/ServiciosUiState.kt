package com.equipo10.lince.model

/** Filtros por estado del Calendario de servicios. */
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
    val filtroTipo: TipoServicio? = null,                      // null = todos los tipos
    val fechaSeleccionada: String? = null,                    // día elegido en la cuadrícula del calendario
    val diasConServicio: Set<String> = emptySet(),             // para marcar la cuadrícula
    val busqueda: String = "",
    val cantidad: Int = 0,
    val total: Int = 0,
    val error: String? = null
)
