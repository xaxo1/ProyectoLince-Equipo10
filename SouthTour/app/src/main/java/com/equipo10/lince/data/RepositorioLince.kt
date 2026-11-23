package com.equipo10.lince.data

import android.graphics.Bitmap
import com.equipo10.lince.data.local.FotoIncidenciaDao
import com.equipo10.lince.data.local.FotoIncidenciaEntity
import com.equipo10.lince.data.local.HitoPendienteDao
import com.equipo10.lince.data.local.HitoPendienteEntity
import com.equipo10.lince.data.local.ServicioCacheDao
import com.equipo10.lince.data.local.ServicioCacheEntity
import com.equipo10.lince.data.remoto.ApiExcepcion
import com.equipo10.lince.data.remoto.BackendApi
import com.equipo10.lince.data.remoto.BloqueoSolicitud
import com.equipo10.lince.data.remoto.ConfirmacionDto
import com.equipo10.lince.data.remoto.ConfirmacionSolicitud
import com.equipo10.lince.data.remoto.GeocercaDto
import com.equipo10.lince.data.remoto.GeocercaSolicitud
import com.equipo10.lince.data.remoto.HitoDto
import com.equipo10.lince.data.remoto.HitoSolicitud
import com.equipo10.lince.data.remoto.IncidenciaDto
import com.equipo10.lince.data.remoto.IncidenciaSolicitud
import com.equipo10.lince.data.remoto.LinceApi
import com.equipo10.lince.data.remoto.LoginSolicitud
import com.equipo10.lince.data.remoto.RecursoLinceDto
import com.equipo10.lince.data.remoto.ServicioLinceDto
import com.equipo10.lince.data.remoto.UsuarioDto
import com.equipo10.lince.data.remoto.comoApiExcepcion
import com.equipo10.lince.model.Catalogos
import com.equipo10.lince.model.DiaDisponibilidad
import com.equipo10.lince.model.EstadoDia
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.GeocercaInfo
import com.equipo10.lince.model.HitoPanel
import com.equipo10.lince.model.Incidencia
import com.equipo10.lince.model.ListaServicios
import com.equipo10.lince.model.Pagina
import com.equipo10.lince.model.Perfil
import com.equipo10.lince.model.RecursoSouthbound
import com.equipo10.lince.model.RegistroHito
import com.equipo10.lince.model.Reglas
import com.equipo10.lince.model.Reserva
import com.equipo10.lince.model.ActividadItinerario
import com.equipo10.lince.model.ResumenMensual
import com.equipo10.lince.model.Rol
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.SesionNoIniciadaException
import com.equipo10.lince.model.TipoHito
import com.equipo10.lince.model.TipoIncidencia
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.model.Ubicacion
import com.equipo10.lince.model.Usuario
import com.equipo10.lince.util.Formatos
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.util.UUID

/** Guarda la foto de una incidencia en el teléfono y devuelve su ruta (interfaz: se reemplaza en las pruebas). */
fun interface AlmacenFotos {
    suspend fun guardar(foto: Bitmap): String
}

/**
 * REPOSITORY: único punto de acceso a los datos para los ViewModels.
 * - Lince API (Retrofit, SOLO LECTURA): servicios, reservas, guías, transportistas, catálogos y resumen.
 * - Backend propio (Retrofit + JWT): login, hitos, incidencias, disponibilidad y confirmaciones.
 * - Room: caché de servicios y cola de hitos pendientes cuando no hay internet (los envía WorkManager).
 * Todas las funciones devuelven Result: éxito o una ApiExcepcion con mensaje y requestId.
 */
class RepositorioLince(
    private val backend: BackendApi,
    private val lince: LinceApi,
    private val sesion: Sesion,
    private val pendientes: HitoPendienteDao,
    private val cache: ServicioCacheDao,
    private val fotos: FotoIncidenciaDao,
    private val almacenFotos: AlmacenFotos,
    private val programarSincronizacion: () -> Unit,
    private val nuevoId: () -> String = { UUID.randomUUID().toString() }
) {
    private val gson = Gson()
    private var reglasCache: Reglas? = null

    val usuarioActual: Usuario? get() = sesion.usuario

    /** Ejecuta una llamada y convierte cualquier error en Result.failure(ApiExcepcion). */
    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e.comoApiExcepcion())
        }

    private fun usuario(): Usuario = sesion.usuario ?: throw SesionNoIniciadaException()

    // ======================= SESIÓN =======================

    suspend fun login(identificador: String, clave: String): Result<Usuario> = llamar {
        val respuesta = backend.login(LoginSolicitud(identificador.trim(), clave))
        val token = respuesta.token ?: throw ApiExcepcion(500, "SIN_TOKEN", "El servidor no entregó el token")
        val usuario = respuesta.usuario?.aModelo() ?: throw ApiExcepcion(500, "SIN_USUARIO", "Respuesta incompleta")
        sesion.guardar(token, usuario)
        usuario
    }

    suspend fun cerrarSesion() = sesion.cerrar()

    suspend fun reglas(): Reglas {
        reglasCache?.let { return it }
        val reglas = llamar { backend.reglas() }.map {
            Reglas(it.radioDefecto, it.precisionMaxima, it.minutosAntes, it.diasAnticipacion)
        }.getOrDefault(Reglas())
        reglasCache = reglas
        return reglas
    }

    // ======================= SERVICIOS (Lince API + hitos del backend) =======================

    /**
     * Servicios del usuario entre dos fechas. Lee TODAS las páginas de la Lince API filtrando por
     * su guiaId / transportistaId. Sin internet, usa la caché de Room.
     */
    suspend fun serviciosEntre(desde: String, hasta: String): Result<ListaServicios> = llamar {
        val u = usuario()
        val (dtos, desdeCache) = try {
            descargarServicios(u, desde, hasta) to false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val error = e.comoApiExcepcion()
            if (!error.sinConexion) throw error
            cache.entre(desde, hasta).map { gson.fromJson(it.json, ServicioLinceDto::class.java) } to true
        }
        val hitos = if (desdeCache) emptyList() else llamar { backend.misHitos() }.getOrDefault(emptyList())
        val confirmaciones = if (desdeCache) emptyList() else llamar { backend.confirmaciones() }.getOrDefault(emptyList())
        val enCola = pendientes.todos()
        val servicios = dtos.map { it.aServicio(hitos, confirmaciones, enCola) }
            .sortedBy { it.fecha + it.horaPickup }
        ListaServicios(servicios, desdeCache)
    }

    private suspend fun descargarServicios(u: Usuario, desde: String, hasta: String): List<ServicioLinceDto> {
        val resultado = mutableListOf<ServicioLinceDto>()
        var pagina = 1
        do {
            val respuesta = lince.servicios(
                page = pagina, pageSize = 100, fechaDesde = desde, fechaHasta = hasta,
                guiaId = u.recursoId.takeIf { u.rol == Rol.GUIA },
                transportistaId = u.recursoId.takeIf { u.rol == Rol.TRANSPORTISTA }
            )
            resultado += respuesta.data.orEmpty()
            val totalPaginas = respuesta.meta?.totalPages ?: 1
            pagina++
        } while (pagina <= totalPaginas && pagina <= 10)
        cache.guardar(resultado.map { ServicioCacheEntity(it.id, it.fecha.orEmpty(), gson.toJson(it)) })
        return resultado
    }

    /** Detalle de un servicio: Lince API (o caché) + sus hitos y la confirmación en el backend. */
    suspend fun servicio(id: Int): Result<Servicio> = llamar {
        val dto = try {
            lince.servicio(id).data?.also { cache.guardar(listOf(ServicioCacheEntity(it.id, it.fecha.orEmpty(), gson.toJson(it)))) }
                ?: throw ApiExcepcion(404, "NOT_FOUND", "No existe el servicio $id")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val error = e.comoApiExcepcion()
            if (!error.sinConexion) throw error
            cache.porId(id)?.let { gson.fromJson(it.json, ServicioLinceDto::class.java) } ?: throw error
        }
        val hitos = llamar { backend.hitosDelServicio(id) }.getOrDefault(emptyList())
        val confirmaciones = llamar { backend.confirmaciones() }.getOrDefault(emptyList())
        dto.aServicio(hitos, confirmaciones, pendientes.delServicio(id))
    }

    /** Último resultado de check-in / check-out de un servicio (para mostrar distancia, radio y precisión). */
    suspend fun registrosDelServicio(id: Int): Result<List<RegistroHito>> = llamar {
        val enviados = llamar { backend.hitosDelServicio(id) }.getOrDefault(emptyList()).map { it.aRegistro() }
        val enCola = pendientes.delServicio(id).map { it.aRegistroPendiente() }
        enviados + enCola
    }

    suspend fun geocercaDe(lugar: String): Result<GeocercaInfo> = llamar { backend.geocerca(lugar).aModelo() }

    /** Confirmar o rechazar la asignación (se guarda en el backend; la Lince API es de solo lectura). */
    suspend fun responderAsignacion(servicioId: Int, confirmar: Boolean, motivo: String?): Result<String> = llamar {
        backend.confirmar(servicioId, ConfirmacionSolicitud(if (confirmar) "CONFIRMADA" else "RECHAZADA", motivo)).estado.orEmpty()
    }

    // ======================= HITOS (check-in / check-out) =======================

    /**
     * Registra un hito en el backend. Si NO hay conexión, lo guarda en Room y programa WorkManager:
     * se enviará solo al volver internet, con la misma clave de idempotencia (sin doble registro).
     */
    suspend fun registrarHito(servicioId: Int, tipo: TipoHito, ubicacion: Ubicacion): Result<RegistroHito> {
        val solicitud = HitoSolicitud(
            idempotencia = nuevoId(),
            servicioId = servicioId,
            tipo = tipo.name,
            latitud = ubicacion.latitud,
            longitud = ubicacion.longitud,
            precisionMetros = ubicacion.precisionMetros,
            fechaHoraDispositivo = Formatos.fechaHoraIso()
        )
        return try {
            Result.success(backend.registrarHito(solicitud).aRegistro())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            val error = e.comoApiExcepcion()
            if (!error.sinConexion) return Result.failure(error)
            pendientes.guardar(solicitud.aPendiente())
            programarSincronizacion()
            Result.success(solicitud.aPendiente().aRegistroPendiente())
        }
    }

    suspend fun cantidadPendientes(): Int = pendientes.cantidad()

    /**
     * Envía la cola de hitos pendientes (lo llama el Worker). Si se corta la conexión de nuevo,
     * se detiene y devuelve failure para que WorkManager reintente más tarde.
     */
    suspend fun sincronizarPendientes(): Result<Int> {
        var enviados = 0
        for (p in pendientes.todos()) {
            try {
                backend.registrarHito(p.aSolicitud())
                pendientes.eliminar(p.idempotencia)
                enviados++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val error = e.comoApiExcepcion()
                if (error.sinConexion) return Result.failure(error)
                // El servidor lo rechazó por una regla (ej. ya registrado): no tiene sentido reintentar
                pendientes.eliminar(p.idempotencia)
            }
        }
        return Result.success(enviados)
    }

    // ======================= INCIDENCIAS =======================

    suspend fun incidencias(servicioId: Int): Result<List<Incidencia>> = llamar {
        val rutas = fotos.todas().associate { it.incidenciaId to it.rutaFoto }
        backend.incidenciasDelServicio(servicioId).map { it.aModelo(rutas[it.id]) }
    }

    suspend fun todasLasIncidencias(): Result<List<Incidencia>> = llamar {
        val rutas = fotos.todas().associate { it.incidenciaId to it.rutaFoto }
        backend.incidencias().map { it.aModelo(rutas[it.id]) }
    }

    suspend fun registrarIncidencia(
        servicioId: Int,
        tipo: TipoIncidencia,
        descripcion: String,
        pasajerosPresentes: Int?,
        foto: Bitmap?
    ): Result<Incidencia> = llamar {
        val dto = backend.crearIncidencia(IncidenciaSolicitud(servicioId, tipo.name, descripcion.trim(), pasajerosPresentes))
        val ruta = foto?.let { almacenFotos.guardar(it) }
        ruta?.let { fotos.guardar(FotoIncidenciaEntity(dto.id, it)) }
        dto.aModelo(ruta)
    }

    // ======================= DISPONIBILIDAD =======================

    /** Próximos [dias] días: con servicio (Lince API), bloqueado (backend) o libre. */
    suspend fun disponibilidad(dias: Int = 45): Result<List<DiaDisponibilidad>> = llamar {
        val servicios = serviciosEntre(Formatos.hoy(), Formatos.fechaEnDias(dias - 1)).getOrThrow().servicios
            .filter { it.estado != EstadoServicio.CANCELADO }
        val bloqueos = backend.bloqueos()
        (0 until dias).map { n ->
            val fecha = Formatos.fechaEnDias(n)
            val servicio = servicios.find { it.fecha == fecha }
            val bloqueo = bloqueos.find { it.fecha == fecha }
            DiaDisponibilidad(
                fecha = fecha,
                diasDeAnticipacion = n,
                estado = when {
                    servicio != null -> EstadoDia.CON_SERVICIO
                    bloqueo != null -> EstadoDia.BLOQUEADO
                    else -> EstadoDia.LIBRE
                },
                servicio = servicio?.excursion,
                bloqueoId = bloqueo?.id
            )
        }
    }

    suspend fun bloquearDia(fecha: String, motivo: String? = null): Result<Unit> = llamar {
        backend.bloquear(BloqueoSolicitud(fecha, motivo))
        Unit
    }

    suspend fun desbloquearDia(idBloqueo: Long): Result<Unit> = llamar {
        val respuesta = backend.desbloquear(idBloqueo)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
    }

    // ======================= RESERVA, RESUMEN Y PERFIL =======================

    /** Reserva completa del servicio, con los datos que entrega la Lince API. */
    suspend fun reserva(servicioId: Int): Result<Reserva> = servicio(servicioId).map { s ->
        Reserva(
            numero = s.numeroReserva,
            agencia = s.agencia,
            titular = s.titularReserva,
            nacionalidad = s.oficina,
            totalPasajeros = s.pasajeros,
            contactoAgencia = "Estado de la reserva: ${s.estadoLince} · Asignación: ${s.statusPL ?: "sin asignar"}",
            observaciones = listOfNotNull(s.notas, s.idiomaRequerido?.let { "Idioma requerido: $it" }, s.vehiculo?.let { "Vehículo: $it" }),
            itinerario = listOf(ActividadItinerario(s.fecha, s.horaPickup, "${s.excursion} (${s.puntoPickup} → ${s.dropOff})"))
        )
    }

    suspend fun resumen(desplazamientoMes: Int): Result<ResumenMensual> = llamar {
        val (desde, hasta) = Formatos.rangoMes(desplazamientoMes)
        val servicios = serviciosEntre(desde, hasta).getOrThrow().servicios
        val global = llamar { lince.resumen(desde, hasta).data }.getOrNull()
        val conCheckIn = servicios.filter { it.resultadoCheckIn != null }
        ResumenMensual(
            mes = Formatos.nombreMes(desplazamientoMes),
            esMesActual = desplazamientoMes == 0,
            misServicios = servicios.size,
            completados = servicios.count { it.estado == EstadoServicio.FINALIZADO },
            cancelados = servicios.count { it.estado == EstadoServicio.CANCELADO },
            puntualidad = if (conCheckIn.isEmpty()) null
                else conCheckIn.count { it.resultadoCheckIn == "VERIFICADA" } * 100 / conCheckIn.size,
            porTipo = servicios.groupingBy { it.tipo }.eachCount(),
            totalSouthbound = global?.totalServicios ?: 0,
            porEstadoSouthbound = global?.porEstadoServicio.orEmpty().associate { it.clave.orEmpty() to it.servicios },
            detalle = servicios.sortedByDescending { it.fecha + it.horaPickup }
        )
    }

    suspend fun perfil(): Result<Perfil> = llamar {
        val u = usuario()
        val (desde, hasta) = Formatos.rangoMes(0)
        val delMes = if (u.rol == Rol.COORDINADOR) 0
            else serviciosEntre(desde, hasta).getOrNull()?.servicios?.size ?: 0
        val hitos = if (u.rol == Rol.COORDINADOR) 0 else llamar { backend.misHitos() }.getOrNull()?.size ?: 0
        Perfil(usuario = u, serviciosDelMes = delMes, hitosRegistrados = hitos, pendientesSincronizar = pendientes.cantidad())
    }

    // ======================= COORDINACIÓN =======================

    suspend fun panelDelDia(fecha: String? = null): Result<List<HitoPanel>> = llamar {
        backend.hitosDelDia(fecha).map { it.aPanel() }
    }

    suspend fun ultimasPosiciones(): Result<List<HitoPanel>> = llamar { backend.posiciones().map { it.aPanel() } }

    suspend fun geocercas(): Result<List<GeocercaInfo>> = llamar { backend.geocercas().map { it.aModelo() } }

    suspend fun cambiarRadio(geocerca: GeocercaInfo, radioMetros: Int): Result<GeocercaInfo> = llamar {
        backend.actualizarGeocerca(
            geocerca.id,
            GeocercaSolicitud(geocerca.nombre, geocerca.oficina, geocerca.latitud, geocerca.longitud, radioMetros)
        ).aModelo()
    }

    // ======================= CATÁLOGO SOUTHBOUND (Lince API) =======================

    suspend fun serviciosSouthbound(pagina: Int, tipo: TipoServicio?): Result<Pagina<Servicio>> = llamar {
        val r = lince.servicios(page = pagina, pageSize = 20, tipo = tipo?.name)
        Pagina(
            items = r.data.orEmpty().map { it.aServicio(emptyList(), emptyList(), emptyList()) },
            pagina = r.meta?.page ?: pagina,
            totalPaginas = r.meta?.totalPages ?: pagina,
            total = r.meta?.total ?: 0
        )
    }

    suspend fun recursosSouthbound(guias: Boolean, pagina: Int): Result<Pagina<RecursoSouthbound>> = llamar {
        val r = if (guias) lince.guias(pagina, 20) else lince.transportistas(pagina, 20)
        Pagina(r.data.orEmpty().map { it.aRecurso() }, r.meta?.page ?: pagina, r.meta?.totalPages ?: pagina, r.meta?.total ?: 0)
    }

    suspend fun catalogos(): Result<Catalogos> = llamar {
        val c = lince.catalogos().data ?: throw ApiExcepcion(404, "NOT_FOUND", "Catálogos no disponibles")
        Catalogos(
            oficinas = c.oficinas.orEmpty().map { "${it.codigo} · ${it.nombre}" },
            tiposServicio = c.tiposServicio.orEmpty().map { "${it.tipo} (${it.srv})" },
            estadosServicio = c.estadosServicio.orEmpty().map { it.codigo.orEmpty() to it.descripcion.orEmpty() },
            statusPL = c.statusPL.orEmpty().map { it.codigo.orEmpty() to it.descripcion.orEmpty() }
        )
    }

    // ======================= CONVERSIONES DTO -> MODELO =======================

    private fun UsuarioDto.aModelo() = Usuario(
        id = id,
        nombre = nombre.orEmpty(),
        correo = correo.orEmpty(),
        rut = rut.orEmpty(),
        rol = runCatching { Rol.valueOf(rol.orEmpty()) }.getOrDefault(Rol.TRANSPORTISTA),
        recursoId = linceRecursoId
    )

    private fun ServicioLinceDto.aServicio(
        hitos: List<HitoDto>,
        confirmaciones: List<ConfirmacionDto>,
        enCola: List<HitoPendienteEntity>
    ): Servicio {
        val propios = hitos.filter { it.servicioId == id }
        val checkIn = propios.lastOrNull { it.tipo == "CHECK_IN" && it.resultado == "VERIFICADA" }
            ?: propios.lastOrNull { it.tipo == "CHECK_IN" }
        val checkOut = propios.lastOrNull { it.tipo == "CHECK_OUT" }
        // El servicio empieza con una llegada verificada o atrasada (estaba en el hotel, pero tarde).
        // Fuera de zona o GPS impreciso NO lo inician: el recurso puede volver a intentar el check-in.
        val inicio = propios.any { it.tipo == "CHECK_IN" && it.resultado in setOf("VERIFICADA", "ATRASADA") }
        val colaPropia = enCola.filter { it.servicioId == id }
        val cancelado = estadoServicio == "XX" || asignacion?.statusPL in setOf("CX", "DL")
        val estado = when {
            cancelado -> EstadoServicio.CANCELADO
            checkOut != null || colaPropia.any { it.tipo == "CHECK_OUT" } -> EstadoServicio.FINALIZADO
            inicio || colaPropia.any { it.tipo == "CHECK_IN" } -> EstadoServicio.EN_CURSO
            else -> EstadoServicio.CONFIRMADO
        }
        return Servicio(
            id = id,
            codigo = codigo.orEmpty(),
            tipo = TipoServicio.desde(tipo),
            fecha = fecha.orEmpty(),
            horaPickup = horaPickup.orEmpty(),
            excursion = descripcion.orEmpty(),
            agencia = cliente?.nombre.orEmpty(),
            numeroReserva = reserva?.referencia.orEmpty(),
            titularReserva = reserva?.nombre.orEmpty(),
            puntoPickup = pickup.orEmpty(),
            dropOff = dropoff.orEmpty(),
            pasajeros = pax?.total ?: 0,
            paxTexto = pax?.texto.orEmpty(),
            estadoLince = estadoServicio.orEmpty(),
            statusPL = asignacion?.statusPL,
            oficina = oficina?.nombre.orEmpty(),
            idiomaRequerido = idiomaRequerido?.nombre,
            notas = notas,
            guia = guia?.nombre,
            transportista = transportista?.nombre,
            vehiculo = vehiculo?.let { "${it.tipo} · ${it.patente}" },
            estado = estado,
            horaCheckIn = checkIn?.let { Formatos.horaDe(it.fechaHoraServidor) }
                ?: colaPropia.firstOrNull { it.tipo == "CHECK_IN" }?.let { Formatos.horaDe(it.fechaHoraDispositivo) },
            horaCheckOut = checkOut?.let { Formatos.horaDe(it.fechaHoraServidor) }
                ?: colaPropia.firstOrNull { it.tipo == "CHECK_OUT" }?.let { Formatos.horaDe(it.fechaHoraDispositivo) },
            resultadoCheckIn = checkIn?.resultado,
            conAtraso = checkIn != null && checkIn.resultado != "VERIFICADA",
            confirmacion = confirmaciones.find { it.servicioId == id }?.estado,
            sincronizacionPendiente = colaPropia.isNotEmpty()
        )
    }

    private fun HitoDto.aRegistro() = RegistroHito(
        tipo = runCatching { TipoHito.valueOf(tipo.orEmpty()) }.getOrDefault(TipoHito.CHECK_IN),
        resultado = resultado.orEmpty(),
        mensaje = mensaje.orEmpty(),
        distanciaMetros = distanciaMetros,
        radioMetros = radioMetros,
        precisionMetros = precisionMetros,
        hora = Formatos.horaDe(fechaHoraServidor)
    )

    private fun HitoDto.aPanel() = HitoPanel(
        servicioId = servicioId,
        usuario = usuarioNombre.orEmpty(),
        tipo = tipo.orEmpty(),
        resultado = resultado.orEmpty(),
        mensaje = mensaje.orEmpty(),
        lugar = lugar,
        distanciaMetros = distanciaMetros,
        latitud = latitud,
        longitud = longitud,
        hora = Formatos.horaDe(fechaHoraServidor)
    )

    private fun HitoSolicitud.aPendiente() = HitoPendienteEntity(
        idempotencia, servicioId, tipo, latitud, longitud, precisionMetros, fechaHoraDispositivo
    )

    private fun HitoPendienteEntity.aSolicitud() = HitoSolicitud(
        idempotencia, servicioId, tipo, latitud, longitud, precisionMetros, fechaHoraDispositivo
    )

    private fun HitoPendienteEntity.aRegistroPendiente() = RegistroHito(
        tipo = runCatching { TipoHito.valueOf(tipo) }.getOrDefault(TipoHito.CHECK_IN),
        resultado = "PENDIENTE",
        mensaje = "Sin conexión: quedó guardado en el teléfono y se enviará al recuperar internet",
        distanciaMetros = null,
        radioMetros = null,
        precisionMetros = precisionMetros,
        hora = Formatos.horaDe(fechaHoraDispositivo),
        pendiente = true
    )

    private fun GeocercaDto.aModelo() = GeocercaInfo(id, nombre.orEmpty(), oficina.orEmpty(), latitud, longitud, radioMetros)

    private fun IncidenciaDto.aModelo(rutaFoto: String?) = Incidencia(
        id = id,
        servicioId = servicioId,
        tipo = TipoIncidencia.desde(tipo),
        descripcion = descripcion.orEmpty(),
        pasajerosPresentes = pasajerosPresentes,
        automatica = automatica,
        estado = estado.orEmpty(),
        registradaEn = fechaHora.orEmpty().replace('T', ' ').take(16),
        autor = usuarioNombre.orEmpty(),
        rutaFoto = rutaFoto
    )

    private fun RecursoLinceDto.aRecurso() = RecursoSouthbound(
        id = id,
        codigo = codigo.orEmpty(),
        nombre = nombre.orEmpty(),
        oficina = oficina?.nombre.orEmpty(),
        detalle = idiomas?.joinToString { it.nombre.orEmpty() }
            ?: vehiculos?.joinToString { "${it.tipo} ${it.patente}" }.orEmpty(),
        evaluacion = evaluacionPromedio
    )
}
