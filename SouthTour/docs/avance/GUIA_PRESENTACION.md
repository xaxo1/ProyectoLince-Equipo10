# Dónde está cada requisito (para la presentación)

## Clase 6 · ViewModel + Estado + StateFlow
| Requisito de la guía | Dónde está |
|---|---|
| Dependencias lifecycle-viewmodel-compose y lifecycle-runtime-compose 2.11.0 | app/build.gradle.kts y gradle/libs.versions.toml |
| Estructura MVVM (model / viewmodel / ui) | carpetas del paquete com.equipo10.lince |
| Modelo (data class) | model/Servicio.kt (como OrdenTrabajo) |
| Estado completo de la pantalla con valores por defecto | model/DetalleUiState.kt (como OrdenUiState) y los demás *UiState.kt |
| MutableStateFlow privado + StateFlow público con asStateFlow() | todos los archivos de viewmodel/ |
| update { actual -> actual.copy(...) } | DetalleServicioViewModel.avanzarEstado() |
| Cambio de estado con when (Pendiente -> En proceso -> Finalizada) | RepositorioLince.avanzarEstado(): Confirmado -> En curso -> Finalizado |
| mensaje en el estado ("Estado actualizado a ...") | DetalleUiState.mensaje + BannerMensaje |
| reiniciar() | DetalleServicioViewModel.reiniciar() + botón "Reiniciar estado" |
| collectAsStateWithLifecycle() + viewModel() | todas las pantallas de ui/screens/ |

## Clases 3, 4 y 5 · MVVM, Material 3 y Navigation Compose
| Requisito | Dónde está |
|---|---|
| Proyecto Android Studio con Kotlin y Compose | proyecto completo |
| Material 3 (Scaffold, TopAppBar, NavigationBar, Card, Button, TextField, FilterChip) | ui/navigation/AppNavigation.kt y ui/screens/ |
| Identidad visual del equipo | ui/theme/Color.kt y Theme.kt (paleta SouthTour) |
| Navegación Login -> Inicio -> Servicios -> Perfil | ui/navigation/AppNavigation.kt |
| Avanzar y volver | flecha de la TopAppBar + botón atrás + popUpTo |
| Rutas con argumentos | detalle/{servicioId} (Int) y reserva/{numeroReserva} (String) |
| Cada pantalla en su archivo | ui/screens/ |
| Modelos de datos | model/ |
| Datos ficticios | data/RepositorioLince.kt |

## Contenidos de los PPT de Kotlin
| Contenido | Ejemplo en el código |
|---|---|
| val / var, null safety (?. ?: !!), when | Servicio.contraparte, Transportista.vehiculo, colorEstado() |
| Colecciones: filter, map, find, any, count, groupBy, sortedBy, associateBy, fold | RepositorioLince, agruparPorFecha(), montoTotal() |
| Funciones de orden superior y lambdas | criterio en ServiciosViewModel, Seccion(contenido) |
| Funciones de extensión | List<Servicio>.agruparPorFecha(), NavHostController.navegarSeccion() |
| POO: open class, herencia, override, interface, private | Usuario -> Guia / Transportista, Informable |
| try - catch - finally | PerfilViewModel.cargar() |
| Excepciones personalizadas y Result | model/Excepciones.kt, RepositorioLince.login() |
| Corrutinas: suspend, launch, async/await, delay | RepositorioLince, InicioViewModel (2 consultas en paralelo) |
| data class + copy(), enum class, sealed class | Servicio, EstadoServicio, Ruta |
| Funciones de ámbito: apply, also, let, with | asignaciones, login(), textoBoton?.let, TarjetaServicio |

## Recorrido sugerido para la demo
1. Login con una clave incorrecta (mensaje de error) y luego guia@southbound.cl / 1234.
2. Servicios del día -> tocar "Santiago de Noche" (19:00) -> Iniciar servicio (check-in a tiempo).
3. Volver y abrir "Full Day Valparaíso" (08:00) -> Iniciar servicio (si ya pasaron las 07:45 sale ATRASO).
4. Finalizar servicio (check-out) -> Reiniciar estado.
5. Ver reserva completa.
6. Calendario -> probar filtros.
7. Disponibilidad -> intentar bloquear un día de esta semana (error) y uno de dentro de 10 días (éxito).
8. Perfil -> Resumen mensual (sube "Realizados" si hicieron un check-out) -> Cerrar sesión.
