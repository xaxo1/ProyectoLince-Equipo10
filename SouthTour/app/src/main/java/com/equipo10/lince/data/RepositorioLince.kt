package com.equipo10.lince.data

import android.content.Context
import android.graphics.Bitmap
import com.equipo10.lince.data.local.BloqueoEntity
import com.equipo10.lince.data.local.IncidenciaEntity
import com.equipo10.lince.data.local.LinceDatabase
import com.equipo10.lince.data.local.RegistroServicioEntity
import com.equipo10.lince.model.AccionNoPermitidaException
import com.equipo10.lince.model.ActividadItinerario
import com.equipo10.lince.model.Asignacion
import com.equipo10.lince.model.BloqueoNoPermitidoException
import com.equipo10.lince.model.CredencialesInvalidasException
import com.equipo10.lince.model.DiaDisponibilidad
import com.equipo10.lince.model.EstadoDia
import com.equipo10.lince.model.EstadoServicio
import com.equipo10.lince.model.Guia
import com.equipo10.lince.model.Incidencia
import com.equipo10.lince.model.Perfil
import com.equipo10.lince.model.Reserva
import com.equipo10.lince.model.ReservaNoEncontradaException
import com.equipo10.lince.model.ResumenMensual
import com.equipo10.lince.model.Servicio
import com.equipo10.lince.model.TipoIncidencia
import com.equipo10.lince.model.TipoServicio
import com.equipo10.lince.model.ServicioNoEncontradoException
import com.equipo10.lince.model.SesionNoIniciadaException
import com.equipo10.lince.model.Transportista
import com.equipo10.lince.model.Ubicacion
import com.equipo10.lince.model.Usuario
import com.equipo10.lince.model.montoTotal
import com.equipo10.lince.util.Formatos
import com.equipo10.lince.util.Formatos.fechaEnDias
import com.equipo10.lince.util.Formatos.fechaPasadaDelMes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

/**
 * REPOSITORY: único punto de acceso a los datos para los ViewModels.
 * - Datos FICTICIOS en memoria: usuarios, servicios asignados y reservas ("API REST simulada").
 *   Las funciones son "suspend" y usan delay() para simular la espera de la red.
 * - Datos PROPIOS del usuario guardados con ROOM (SQLite), que se conservan al cerrar la app:
 *   check-in / check-out con ubicación, días bloqueados e incidencias.
 *
 * Las fechas se calculan desde HOY, así "Servicios del día" siempre tiene datos.
 */
object RepositorioLince {

    // ======================= ROOM =======================
    private lateinit var db: LinceDatabase
    private lateinit var carpetaFotos: File

    /** Se llama una vez desde LinceApplication (Room necesita el Context para abrir el archivo .db). */
    fun inicializar(context: Context) {
        db = LinceDatabase.obtener(context)
        carpetaFotos = File(context.filesDir, "incidencias").apply { mkdirs() }
    }

    /** Días hacia adelante que muestra la pantalla de Disponibilidad. */
    private const val DIAS_DISPONIBILIDAD = 45

    /** Regla del README: solo se bloquean días con MÁS de una semana de anticipación. */
    private const val DIAS_MINIMOS_BLOQUEO = 7

    // ======================= USUARIOS =======================
    private val usuarios: List<Usuario> = listOf(
        Guia(1, "Camila Soto", "15.482.910-K", "guia@southbound.cl", "1234",
            idiomas = listOf("Español", "Inglés", "Portugués")),
        Transportista(2, "Jorge Muñoz", "12.345.678-5", "transportista@southbound.cl", "1234",
            licencia = "A3", vehiculo = "Mercedes Sprinter · KXTR-21")
    )

    // MAP: id de usuario -> teléfono (el transportista no tiene, para practicar null safety)
    private val telefonos: Map<Int, String> = mapOf(1 to "+56 9 8765 4321")

    // ======================= ASIGNACIONES (hechas por operaciones) =======================
    // Colección MUTABLE creada con la función de ámbito APPLY
    private val asignaciones: MutableList<Asignacion> = mutableListOf<Asignacion>().apply {
        var correlativo = 1
        fun asignar(usuarioId: Int, servicio: Servicio) {
            add(Asignacion(correlativo++, servicio, usuarioId))
        }

        // ----- Guía: Camila Soto (usuario 1) -----
        asignar(1, Servicio(
            id = 101, excursion = "Full Day Valparaíso y Viña del Mar", agencia = "Andes Travel",
            numeroReserva = "SB-24031", fecha = fechaEnDias(0), horaPickup = "08:00",
            puntoPickup = "Hotel Plaza San Francisco", dropOff = "Hotel Plaza San Francisco",
            pasajeros = 12, contraparte = "Transportista: Jorge Muñoz · Sprinter KXTR-21",
            requisitos = listOf("Guion en inglés y portugués", "Almuerzo incluido en Viña del Mar",
                "Cartel con el logo de la agencia"),
            monto = 55000,
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(1, Servicio(
            id = 102, excursion = "Santiago de Noche y Barrio Lastarria", agencia = "Southbound Receptivo",
            numeroReserva = "SB-24044", fecha = fechaEnDias(0), horaPickup = "19:00",
            puntoPickup = "Hotel Cumbres Lastarria", dropOff = "Hotel Cumbres Lastarria",
            pasajeros = 6, contraparte = "Transportista: Jorge Muñoz · Sprinter KXTR-21",
            requisitos = listOf("Cena incluida (2 pasajeros vegetarianos)"),
            monto = 35000,
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(1, Servicio(
            id = 103, excursion = "Cajón del Maipo y Embalse El Yeso", agencia = "Patagonia Experiences",
            numeroReserva = "SB-24102", fecha = fechaEnDias(2), horaPickup = "07:30",
            puntoPickup = "Hotel Mandarin Oriental", dropOff = "Hotel Mandarin Oriental",
            pasajeros = 8, contraparte = "Transportista: Jorge Muñoz · Sprinter KXTR-21",
            requisitos = listOf("Ropa de abrigo", "Pasaporte para control fronterizo"),
            monto = 50000,
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(1, Servicio(
            id = 104, excursion = "Ruta del Vino Valle de Colchagua", agencia = "Wine Tours Chile",
            numeroReserva = "SB-24177", fecha = fechaEnDias(9), horaPickup = "08:30",
            puntoPickup = "Hotel The Singular", dropOff = "Hotel The Singular",
            pasajeros = 10, contraparte = null,
            requisitos = listOf("Pasajeros mayores de 18 años", "Degustación en 2 viñas"),
            monto = 60000,
            vehiculo = "Van Hyundai H350"
        ))
        asignar(1, Servicio(
            id = 105, excursion = "Isla Negra y Pomaire", agencia = "Andes Travel",
            numeroReserva = "SB-24290", fecha = fechaEnDias(32), horaPickup = "09:00",
            puntoPickup = "Hotel Plaza San Francisco", dropOff = "Hotel Plaza San Francisco",
            pasajeros = 15, monto = 50000,
            vehiculo = "Minibús Hyundai County"
        ))
        asignar(1, Servicio(
            id = 106, excursion = "City Tour Santiago", agencia = "Southbound Receptivo",
            numeroReserva = "SB-23950", fecha = fechaPasadaDelMes(3), horaPickup = "10:00",
            puntoPickup = "Hotel The Singular", dropOff = "Cerro San Cristóbal",
            pasajeros = 2, contraparte = "Transportista: Jorge Muñoz · Sprinter KXTR-21",
            monto = 35000, estado = EstadoServicio.FINALIZADO, horaCheckIn = "09:40", horaCheckOut = "14:10",
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(1, Servicio(
            id = 107, excursion = "Portillo y Laguna del Inca", agencia = "Andes Travel",
            numeroReserva = "SB-23911", fecha = fechaPasadaDelMes(6), horaPickup = "07:00",
            puntoPickup = "Hotel Plaza San Francisco", dropOff = "Hotel Plaza San Francisco",
            pasajeros = 9, contraparte = "Transportista: Luis Pérez · Van JKLP-44",
            monto = 60000, estado = EstadoServicio.CANCELADO,
            vehiculo = "Van Hyundai H350", motivoCancelacion = "Cierre del Paso Los Libertadores"
        ))
        asignar(1, Servicio(
            id = 108, excursion = "Valle de Casablanca", agencia = "Wine Tours Chile",
            numeroReserva = "SB-23870", fecha = fechaPasadaDelMes(10), horaPickup = "09:30",
            puntoPickup = "Hotel Cumbres Vitacura", dropOff = "Hotel Cumbres Vitacura",
            pasajeros = 7, monto = 55000, estado = EstadoServicio.FINALIZADO,
            horaCheckIn = "09:20", horaCheckOut = "17:45", conAtraso = true,
            vehiculo = "Van Hyundai H350"
        ))

        // ----- Transportista: Jorge Muñoz (usuario 2) -----
        asignar(2, Servicio(
            id = 201, excursion = "Traslado Aeropuerto → Hotel", agencia = "Southbound Receptivo",
            numeroReserva = "SB-24030", fecha = fechaEnDias(0), horaPickup = "06:30",
            puntoPickup = "Aeropuerto AMB · Puerta 3", dropOff = "Hotel Plaza San Francisco",
            pasajeros = 4, contraparte = null,
            requisitos = listOf("Cartel con el apellido JOHNSON", "4 maletas grandes"),
            monto = 25000,
            tipo = TipoServicio.TRASLADO, vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(2, Servicio(
            id = 202, excursion = "Full Day Valparaíso y Viña del Mar", agencia = "Andes Travel",
            numeroReserva = "SB-24031", fecha = fechaEnDias(0), horaPickup = "08:00",
            puntoPickup = "Hotel Plaza San Francisco", dropOff = "Hotel Plaza San Francisco",
            pasajeros = 12, contraparte = "Guía: Camila Soto · +56 9 8765 4321",
            requisitos = listOf("Vehículo con capacidad para 12 pasajeros"),
            monto = 70000,
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(2, Servicio(
            id = 207, excursion = "Santiago de Noche y Barrio Lastarria", agencia = "Southbound Receptivo",
            numeroReserva = "SB-24044", fecha = fechaEnDias(0), horaPickup = "19:00",
            puntoPickup = "Hotel Cumbres Lastarria", dropOff = "Hotel Cumbres Lastarria",
            pasajeros = 6, contraparte = "Guía: Camila Soto · +56 9 8765 4321",
            monto = 40000,
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(2, Servicio(
            id = 203, excursion = "Cajón del Maipo y Embalse El Yeso", agencia = "Patagonia Experiences",
            numeroReserva = "SB-24102", fecha = fechaEnDias(2), horaPickup = "07:30",
            puntoPickup = "Hotel Mandarin Oriental", dropOff = "Hotel Mandarin Oriental",
            pasajeros = 8, contraparte = "Guía: Camila Soto · +56 9 8765 4321",
            requisitos = listOf("Llevar cadenas para nieve"),
            monto = 65000,
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(2, Servicio(
            id = 204, excursion = "City Tour Santiago", agencia = "Southbound Receptivo",
            numeroReserva = "SB-23950", fecha = fechaPasadaDelMes(3), horaPickup = "10:00",
            puntoPickup = "Hotel The Singular", dropOff = "Cerro San Cristóbal",
            pasajeros = 2, contraparte = "Guía: Camila Soto · +56 9 8765 4321",
            monto = 40000, estado = EstadoServicio.FINALIZADO, horaCheckIn = "09:35", horaCheckOut = "14:15",
            vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
        asignar(2, Servicio(
            id = 205, excursion = "Portillo y Laguna del Inca", agencia = "Andes Travel",
            numeroReserva = "SB-23911", fecha = fechaPasadaDelMes(6), horaPickup = "07:00",
            puntoPickup = "Hotel Plaza San Francisco", dropOff = "Hotel Plaza San Francisco",
            pasajeros = 9, monto = 75000, estado = EstadoServicio.CANCELADO,
            vehiculo = "Mercedes Sprinter · KXTR-21", motivoCancelacion = "Cancelado por la agencia"
        ))
        asignar(2, Servicio(
            id = 206, excursion = "Traslado Hotel → Aeropuerto", agencia = "Wine Tours Chile",
            numeroReserva = "SB-23870", fecha = fechaPasadaDelMes(8), horaPickup = "05:00",
            puntoPickup = "Hotel Cumbres Vitacura", dropOff = "Aeropuerto AMB",
            pasajeros = 7, monto = 25000, estado = EstadoServicio.FINALIZADO,
            horaCheckIn = "04:40", horaCheckOut = "05:55",
            tipo = TipoServicio.TRASLADO, vehiculo = "Mercedes Sprinter · KXTR-21"
        ))
    }

    // ======================= RESERVAS =======================
    // associateBy: convierte la lista en un MAP número de reserva -> Reserva
    private val reservas: Map<String, Reserva> = listOf(
        Reserva(
            numero = "SB-24031", agencia = "Andes Travel", titular = "Carlos Mendes",
            nacionalidad = "Brasil", totalPasajeros = 12,
            contactoAgencia = "operaciones@andestravel.cl · +56 2 2345 6789",
            observaciones = listOf("Grupo de 12 adultos", "1 pasajero celíaco (avisar en almuerzo)"),
            itinerario = listOf(
                ActividadItinerario(fechaEnDias(-1), "15:00", "Llegada y traslado al Hotel Plaza San Francisco"),
                ActividadItinerario(fechaEnDias(0), "08:00", "Full Day Valparaíso y Viña del Mar"),
                ActividadItinerario(fechaEnDias(1), "Libre", "Día libre en Santiago"),
                ActividadItinerario(fechaEnDias(2), "06:00", "Traslado Hotel → Aeropuerto")
            )
        ),
        Reserva(
            numero = "SB-24030", agencia = "Southbound Receptivo", titular = "Michael Johnson",
            nacionalidad = "Estados Unidos", totalPasajeros = 4,
            contactoAgencia = "receptivo@southbound.cl · +56 2 2987 6543",
            observaciones = listOf("Familia con 2 niños", "Vuelo AA 945 desde Miami"),
            itinerario = listOf(
                ActividadItinerario(fechaEnDias(0), "06:30", "Traslado Aeropuerto → Hotel Plaza San Francisco"),
                ActividadItinerario(fechaEnDias(1), "09:00", "City Tour Santiago"),
                ActividadItinerario(fechaEnDias(4), "07:00", "Traslado Hotel → Aeropuerto")
            )
        ),
        Reserva(
            numero = "SB-24044", agencia = "Southbound Receptivo", titular = "Sophie Laurent",
            nacionalidad = "Francia", totalPasajeros = 6,
            contactoAgencia = "receptivo@southbound.cl · +56 2 2987 6543",
            observaciones = listOf("2 pasajeros vegetarianos"),
            itinerario = listOf(
                ActividadItinerario(fechaEnDias(0), "19:00", "Santiago de Noche y Barrio Lastarria"),
                ActividadItinerario(fechaEnDias(1), "08:30", "Viña Concha y Toro (otra agencia)")
            )
        ),
        Reserva(
            numero = "SB-24102", agencia = "Patagonia Experiences", titular = "Hans Müller",
            nacionalidad = "Alemania", totalPasajeros = 8,
            contactoAgencia = "ops@patagoniaexp.cl · +56 9 6123 4567",
            observaciones = listOf("Llevar ropa de abrigo", "Control fronterizo en el camino"),
            itinerario = listOf(
                ActividadItinerario(fechaEnDias(2), "07:30", "Cajón del Maipo y Embalse El Yeso"),
                ActividadItinerario(fechaEnDias(3), "Libre", "Día libre"),
                ActividadItinerario(fechaEnDias(4), "10:00", "Traslado a Valparaíso")
            )
        ),
        Reserva(
            numero = "SB-24177", agencia = "Wine Tours Chile", titular = "Emily Carter",
            nacionalidad = "Reino Unido", totalPasajeros = 10,
            contactoAgencia = "reservas@winetours.cl · +56 2 2444 5566",
            observaciones = listOf("Todos mayores de 18 años"),
            itinerario = listOf(
                ActividadItinerario(fechaEnDias(9), "08:30", "Ruta del Vino Valle de Colchagua"),
                ActividadItinerario(fechaEnDias(10), "Libre", "Día libre en Santa Cruz")
            )
        ),
        Reserva(
            numero = "SB-24290", agencia = "Andes Travel", titular = "Lucía Fernández",
            nacionalidad = "Argentina", totalPasajeros = 15,
            contactoAgencia = "operaciones@andestravel.cl · +56 2 2345 6789",
            itinerario = listOf(
                ActividadItinerario(fechaEnDias(32), "09:00", "Isla Negra y Pomaire")
            )
        ),
        Reserva(
            numero = "SB-23950", agencia = "Southbound Receptivo", titular = "Paul Weber",
            nacionalidad = "Suiza", totalPasajeros = 2,
            contactoAgencia = "receptivo@southbound.cl · +56 2 2987 6543",
            itinerario = listOf(
                ActividadItinerario(fechaPasadaDelMes(3), "10:00", "City Tour Santiago")
            )
        ),
        Reserva(
            numero = "SB-23911", agencia = "Andes Travel", titular = "Grupo Andes Ski",
            nacionalidad = "Chile", totalPasajeros = 9,
            contactoAgencia = "operaciones@andestravel.cl · +56 2 2345 6789",
            observaciones = listOf("Cancelado por cierre del Paso Los Libertadores"),
            itinerario = listOf(
                ActividadItinerario(fechaPasadaDelMes(6), "07:00", "Portillo y Laguna del Inca")
            )
        ),
        Reserva(
            numero = "SB-23870", agencia = "Wine Tours Chile", titular = "Ana Costa",
            nacionalidad = "Portugal", totalPasajeros = 7,
            contactoAgencia = "reservas@winetours.cl · +56 2 2444 5566",
            itinerario = listOf(
                ActividadItinerario(fechaPasadaDelMes(10), "09:30", "Valle de Casablanca")
            )
        )
    ).associateBy { it.numero }

    // ======================= SESIÓN =======================
    /** Usuario con sesión iniciada. Solo el repositorio puede cambiarlo (private set). */
    var usuarioActual: Usuario? = null
        private set

    /** Control de errores FUNCIONAL: devuelve Result en vez de lanzar la excepción. */
    suspend fun login(identificador: String, clave: String): Result<Usuario> {
        delay(800) // simula la llamada a la API
        return runCatching {
            require(identificador.isNotBlank() && clave.isNotBlank()) { "Completa tu RUT y tu contraseña" }

            // find + coincideCon: acepta RUT (con o sin puntos) o correo
            val usuario = usuarios.find { it.coincideCon(identificador) }
                ?: throw CredencialesInvalidasException()          // Elvis + throw

            if (!usuario.validarClave(clave)) throw CredencialesInvalidasException()

            usuario.also { usuarioActual = it }                    // ALSO: efecto secundario
        }
    }

    fun cerrarSesion() {
        usuarioActual = null
    }

    // ======================= SERVICIOS =======================

    /** Servicios asignados para HOY, ordenados por hora de pickup. */
    suspend fun obtenerServiciosDelDia(): Result<List<Servicio>> {
        delay(500)
        return runCatching {
            val hoy = Formatos.hoy()
            serviciosDelUsuario().filter { it.fecha == hoy }
        }
    }

    /** El siguiente servicio confirmado DESPUÉS de hoy (puede no existir -> null). */
    suspend fun obtenerProximoServicio(): Result<Servicio?> {
        delay(400)
        return runCatching {
            val hoy = Formatos.hoy()
            serviciosDelUsuario()
                .filter { it.fecha > hoy && it.estado == EstadoServicio.CONFIRMADO }
                .minByOrNull { it.fecha + it.horaPickup }
        }
    }

    /** Todos los servicios del usuario (para el Calendario). */
    suspend fun obtenerServicios(): Result<List<Servicio>> {
        delay(600)
        return runCatching { serviciosDelUsuario() }
    }

    /** Un servicio por id (para el Detalle). */
    suspend fun obtenerServicio(id: Int): Result<Servicio> {
        delay(300)
        return runCatching { servicioDelUsuario(id) }
    }

    /**
     * Avanza el estado del servicio, igual que avanzarEstado() de la Clase 6:
     * CONFIRMADO --check-in--> EN_CURSO --check-out--> FINALIZADO
     * El registro (hora + ubicación GPS) se guarda en Room: no se pierde al cerrar la app.
     */
    suspend fun avanzarEstado(id: Int, ubicacion: Ubicacion?): Result<Servicio> {
        delay(500) // simula el envío a la API
        return runCatching {
            val usuario = usuarioActual ?: throw SesionNoIniciadaException()
            val actual = servicioDelUsuario(id)
            val ahora = Formatos.horaActual()

            val actualizado = when (actual.estado) {
                EstadoServicio.CONFIRMADO -> {
                    if (actual.fecha != Formatos.hoy()) {
                        throw AccionNoPermitidaException("Solo puedes iniciar servicios programados para hoy")
                    }
                    actual.copy(
                        estado = EstadoServicio.EN_CURSO,
                        horaCheckIn = ahora,
                        conAtraso = Formatos.llegaConAtraso(ahora, actual.horaPickup),
                        ubicacionCheckIn = ubicacion?.texto()
                    )
                }
                EstadoServicio.EN_CURSO -> actual.copy(
                    estado = EstadoServicio.FINALIZADO,
                    horaCheckOut = ahora,
                    ubicacionCheckOut = ubicacion?.texto()
                )
                EstadoServicio.FINALIZADO -> throw AccionNoPermitidaException("El servicio ya está finalizado")
                EstadoServicio.CANCELADO -> throw AccionNoPermitidaException("No se puede iniciar un servicio cancelado")
            }

            db.registroServicioDao().guardar(actualizado.aRegistro(usuario.id))
            actualizado
        }
    }

    /** Devuelve el servicio a como lo asignó operaciones (como reiniciar() de la Clase 6): borra su registro en Room. */
    suspend fun reiniciarServicio(id: Int): Result<Servicio> {
        delay(300)
        return runCatching {
            val original = asignacionDelUsuario(id).servicio
            db.registroServicioDao().eliminar(id)
            original
        }
    }

    // ======================= RESERVAS =======================

    suspend fun obtenerReserva(numero: String): Result<Reserva> {
        delay(400)
        return runCatching {
            if (usuarioActual == null) throw SesionNoIniciadaException()
            reservas[numero] ?: throw ReservaNoEncontradaException(numero)
        }
    }

    // ======================= DISPONIBILIDAD =======================

    /** Los próximos 45 días con su estado: libre, con servicio o bloqueado. */
    suspend fun obtenerDisponibilidad(): Result<List<DiaDisponibilidad>> {
        delay(500)
        return runCatching {
            val usuario = usuarioActual ?: throw SesionNoIniciadaException()
            val propios = serviciosDelUsuario().filter { it.estado != EstadoServicio.CANCELADO }
            val bloqueados = db.bloqueoDao().fechasDelUsuario(usuario.id).toSet()   // Room

            // (0 until N).map: genera una lista con un elemento por cada día
            (0 until DIAS_DISPONIBILIDAD).map { dias ->
                val fecha = fechaEnDias(dias)
                val servicio = propios.find { it.fecha == fecha }
                DiaDisponibilidad(
                    fecha = fecha,
                    diasDeAnticipacion = dias,
                    estado = when {
                        servicio != null -> EstadoDia.CON_SERVICIO
                        fecha in bloqueados -> EstadoDia.BLOQUEADO
                        else -> EstadoDia.LIBRE
                    },
                    servicio = servicio?.excursion
                )
            }
        }
    }

    /** Regla del flujo UML: ¿falta más de una semana y el día está libre? */
    suspend fun bloquearDia(fecha: String): Result<Unit> {
        delay(300)
        return runCatching {
            val usuario = usuarioActual ?: throw SesionNoIniciadaException()
            val dias = Formatos.diasDesdeHoy(fecha)

            if (dias <= DIAS_MINIMOS_BLOQUEO) {
                throw BloqueoNoPermitidoException(
                    "No se puede bloquear: debes avisar con más de una semana de anticipación"
                )
            }
            val tieneServicio = serviciosDelUsuario()
                .any { it.fecha == fecha && it.estado != EstadoServicio.CANCELADO }
            if (tieneServicio) {
                throw BloqueoNoPermitidoException("No se puede bloquear: ese día ya tiene un servicio asignado")
            }

            // Room devuelve -1 si la fecha ya existía (OnConflictStrategy.IGNORE)
            val resultado = db.bloqueoDao().insertar(BloqueoEntity(usuario.id, fecha))
            if (resultado == -1L) throw BloqueoNoPermitidoException("Ese día ya estaba bloqueado")
        }
    }

    suspend fun desbloquearDia(fecha: String): Result<Unit> {
        delay(300)
        return runCatching {
            val usuario = usuarioActual ?: throw SesionNoIniciadaException()
            db.bloqueoDao().eliminar(usuario.id, fecha)
        }
    }

    // ======================= INCIDENCIAS (Room + cámara) =======================

    /** FLOW de Room: emite la lista nueva cada vez que se guarda una incidencia del servicio. */
    fun observarIncidencias(servicioId: Int): Flow<List<Incidencia>> =
        db.incidenciaDao().observarDelServicio(servicioId)
            .map { filas -> filas.map { it.aModelo() } }

    /**
     * Guarda la incidencia (y su foto, si hay) en el teléfono.
     * Regla de negocio: solo se reportan incidencias de servicios ya iniciados.
     */
    suspend fun registrarIncidencia(
        servicioId: Int,
        tipo: TipoIncidencia,
        descripcion: String,
        pasajerosPresentes: Int,
        foto: Bitmap?
    ): Result<Incidencia> = runCatching {
        val usuario = usuarioActual ?: throw SesionNoIniciadaException()
        val servicio = servicioDelUsuario(servicioId)
        if (servicio.estado != EstadoServicio.EN_CURSO && servicio.estado != EstadoServicio.FINALIZADO) {
            throw AccionNoPermitidaException("Solo puedes reportar incidencias de un servicio iniciado")
        }

        val fila = IncidenciaEntity(
            servicioId = servicioId,
            usuarioId = usuario.id,
            tipo = tipo.name,
            descripcion = descripcion.trim(),
            pasajerosPresentes = pasajerosPresentes,
            rutaFoto = foto?.let { guardarFoto(it) },
            registradaEn = Formatos.fechaHoraActual()
        )
        val id = db.incidenciaDao().insertar(fila)
        fila.copy(id = id).aModelo()
    }

    /** Escribe la foto como .jpg en la memoria interna de la app (hilo IO: no bloquea la pantalla). */
    private suspend fun guardarFoto(foto: Bitmap): String = withContext(Dispatchers.IO) {
        val archivo = File(carpetaFotos, "incidencia_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { salida -> foto.compress(Bitmap.CompressFormat.JPEG, 90, salida) }
        archivo.absolutePath
    }

    // ======================= PERFIL =======================

    /** Control de errores TRADICIONAL: esta función LANZA excepción (se captura con try-catch). */
    suspend fun obtenerPerfil(): Perfil {
        delay(500)
        val usuario = usuarioActual ?: throw SesionNoIniciadaException()
        val mes = Formatos.mesActualIso()
        return Perfil(
            usuario = usuario,
            telefono = telefonos[usuario.id],
            serviciosDelMes = serviciosDelUsuario().count { it.fecha.startsWith(mes) }
        )
    }

    // ======================= RESUMEN MENSUAL =======================

    /**
     * Resumen de un mes (0 = actual, -1 = anterior...).
     * Regla del equipo: los CANCELADOS igual se pagan al recurso, así que suman al monto.
     * Puntualidad: % de check-in hechos sin atraso (lo que el cliente quiere premiar).
     */
    suspend fun obtenerResumen(desplazamientoMes: Int): Result<ResumenMensual> {
        delay(500)
        return runCatching {
            val mes = Formatos.mesIso(desplazamientoMes)
            val hoy = Formatos.hoy()
            val cerrados = serviciosDelUsuario()
                .filter { it.fecha.startsWith(mes) && it.fecha <= hoy }
                .filter { it.estado == EstadoServicio.FINALIZADO || it.estado == EstadoServicio.CANCELADO }

            val conCheckIn = cerrados.filter { it.horaCheckIn != null }
            val puntualidad = if (conCheckIn.isEmpty()) null
                else conCheckIn.count { !it.conAtraso } * 100 / conCheckIn.size

            ResumenMensual(
                mes = Formatos.nombreMes(desplazamientoMes),
                esMesActual = desplazamientoMes == 0,
                completados = cerrados.count { it.estado == EstadoServicio.FINALIZADO },
                cancelados = cerrados.count { it.estado == EstadoServicio.CANCELADO },
                montoTotal = cerrados.montoTotal(),
                puntualidad = puntualidad,
                pagoEstimado = Formatos.fechaPagoEstimado(desplazamientoMes),
                detalle = cerrados.sortedByDescending { it.fecha + it.horaPickup }
            )
        }
    }

    // ======================= AUXILIARES =======================

    /**
     * filter + map + sortedBy sobre colecciones. Lanza excepción si no hay sesión.
     * A cada servicio asignado se le aplica su registro de check-in / check-out guardado en Room.
     */
    private suspend fun serviciosDelUsuario(): List<Servicio> {
        val usuario = usuarioActual ?: throw SesionNoIniciadaException()
        val registros = db.registroServicioDao().obtenerDelUsuario(usuario.id).associateBy { it.servicioId }
        return asignaciones
            .filter { it.usuarioId == usuario.id }
            .map { it.servicio.conRegistro(registros[it.servicio.id]) }
            .sortedBy { it.fecha + it.horaPickup }
    }

    /** Un servicio del usuario actual, con su registro de Room aplicado. */
    private suspend fun servicioDelUsuario(idServicio: Int): Servicio =
        asignacionDelUsuario(idServicio).servicio
            .conRegistro(db.registroServicioDao().obtenerPorServicio(idServicio))

    /** La asignación del usuario actual para ese servicio (tal como la hizo operaciones). */
    private fun asignacionDelUsuario(idServicio: Int): Asignacion {
        val usuario = usuarioActual ?: throw SesionNoIniciadaException()
        return asignaciones.find { it.usuarioId == usuario.id && it.servicio.id == idServicio }
            ?: throw ServicioNoEncontradoException(idServicio)
    }

    // ----- Conversión entre el modelo de la app y las filas de Room -----

    private fun Servicio.conRegistro(registro: RegistroServicioEntity?): Servicio =
        registro?.let {
            copy(
                estado = EstadoServicio.valueOf(it.estado),
                horaCheckIn = it.horaCheckIn,
                horaCheckOut = it.horaCheckOut,
                conAtraso = it.conAtraso,
                ubicacionCheckIn = it.ubicacionCheckIn,
                ubicacionCheckOut = it.ubicacionCheckOut
            )
        } ?: this

    private fun Servicio.aRegistro(usuarioId: Int) = RegistroServicioEntity(
        servicioId = id,
        usuarioId = usuarioId,
        estado = estado.name,
        horaCheckIn = horaCheckIn,
        horaCheckOut = horaCheckOut,
        conAtraso = conAtraso,
        ubicacionCheckIn = ubicacionCheckIn,
        ubicacionCheckOut = ubicacionCheckOut
    )

    private fun IncidenciaEntity.aModelo() = Incidencia(
        id = id,
        servicioId = servicioId,
        tipo = TipoIncidencia.valueOf(tipo),
        descripcion = descripcion,
        pasajerosPresentes = pasajerosPresentes,
        rutaFoto = rutaFoto,
        registradaEn = registradaEn
    )
}
