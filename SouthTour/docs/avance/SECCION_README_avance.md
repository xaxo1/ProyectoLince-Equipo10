## 🏗️ Desarrollo de la app (clases 3 a 6)

La app está construida en Android Studio con **Kotlin + Jetpack Compose + Material 3**, arquitectura **MVVM** y **Navigation Compose**. El estado de cada pantalla se maneja con **ViewModel + StateFlow** (Clase 6). Trabaja con **datos ficticios** que simulan la API REST (`data/RepositorioLince.kt`); Room, Retrofit y el GPS real quedan para las próximas entregas.

### Arquitectura MVVM

```
app/src/main/java/com/equipo10/lince/
├── MainActivity.kt
├── model/                     → Entidades del dominio y estados de pantalla
│   ├── Usuario.kt             (Usuario, Guia, Transportista, Rol, Informable)
│   ├── Servicio.kt · EstadoServicio.kt · Asignacion.kt
│   ├── Reserva.kt             (Reserva, ActividadItinerario)
│   ├── DiaDisponibilidad.kt   (EstadoDia)
│   ├── Perfil.kt              (Perfil, ResumenMensual)
│   ├── Excepciones.kt
│   └── *UiState.kt            → Login, Inicio, Servicios, Detalle, Reserva, Disponibilidad, Perfil
├── data/RepositorioLince.kt   → API simulada con funciones suspend
├── util/Formatos.kt           → Fechas, horas, regla de 15 min y montos en CLP
├── viewmodel/                 → Un ViewModel por pantalla (MutableStateFlow privado + StateFlow público)
└── ui/
    ├── navigation/            → Rutas (sealed class) y NavHost con argumentos
    ├── screens/               → 7 pantallas en Compose
    ├── components/            → Tarjeta de servicio, etiquetas, banners, carga y error
    └── theme/                 → Paleta SouthTour en Material 3
```

### Pantallas implementadas

| # | Pantalla | Implementación |
|---|---|---|
| 1 | Login | Validación de credenciales ficticias y carga del rol |
| 2 | Servicios del día | Servicios de hoy con hora de pickup destacada y próximo servicio |
| 3 | Calendario de servicios | Agrupado por fecha con filtros (próximos, en curso, finalizados, cancelados, todos) |
| 4 | Detalle del servicio | Pickup, drop off, reserva, contraparte y requisitos de la agencia |
| 5 | Reserva completa | Titular, pasajeros, contacto de la agencia e itinerario completo |
| 6 | Check-in / Check-out | En el Detalle: Confirmado → En curso → Finalizado, con hora y control de atraso (15 min) |
| 7 | Disponibilidad | Bloqueo de días con validación de más de una semana de anticipación y día libre |
| 8 | Resumen mensual | En Perfil: servicios realizados, cancelados y monto acumulado del mes |

**Navegación:** Login → Inicio → Servicios → Perfil con `NavigationBar`; Detalle (`detalle/{servicioId}`), Reserva (`reserva/{numeroReserva}`) y Disponibilidad se abren desde las pantallas principales y se vuelve con la flecha o el botón atrás.

### Estado con ViewModel + StateFlow (Clase 6)

- Cada pantalla tiene su `UiState` (`data class` con valores por defecto) en `model/`.
- Cada ViewModel expone `val uiState: StateFlow<...> = _uiState.asStateFlow()` y modifica el estado con `_uiState.update { it.copy(...) }`.
- Las pantallas observan el estado con `collectAsStateWithLifecycle()`.
- `DetalleServicioViewModel` aplica `avanzarEstado()` (check-in → check-out) y `reiniciar()`, con un `mensaje` que informa cada cambio.

**Usuarios de prueba** (clave `1234`): `guia@southbound.cl` · `transportista@southbound.cl`

### Capturas

| Login | Servicios del día | Calendario | Detalle / Check-in | Reserva | Disponibilidad | Perfil |
|---|---|---|---|---|---|---|
| ![](docs/evidencias/app/01_login.png) | ![](docs/evidencias/app/02_inicio.png) | ![](docs/evidencias/app/03_calendario.png) | ![](docs/evidencias/app/04_detalle.png) | ![](docs/evidencias/app/05_reserva.png) | ![](docs/evidencias/app/06_disponibilidad.png) | ![](docs/evidencias/app/07_perfil.png) |
