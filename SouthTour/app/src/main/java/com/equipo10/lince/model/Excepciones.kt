package com.equipo10.lince.model

/** EXCEPCIONES PERSONALIZADAS del dominio (control de errores). */
class CredencialesInvalidasException :
    Exception("Correo o contraseña incorrectos")

class SesionNoIniciadaException :
    Exception("No hay una sesión iniciada")

class ServicioNoEncontradoException(id: Int) :
    Exception("No existe el servicio con id $id")

class ReservaNoEncontradaException(numero: String) :
    Exception("No se encontró la reserva $numero")

/** Acciones que la regla de negocio no permite (ej: iniciar un servicio que no es de hoy). */
class AccionNoPermitidaException(mensaje: String) :
    Exception(mensaje)

/** Bloqueos de disponibilidad que no cumplen la regla de una semana de anticipación. */
class BloqueoNoPermitidoException(mensaje: String) :
    Exception(mensaje)
