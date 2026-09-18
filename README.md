# ProyectoLince-Equipo10
Evaluacion n°1 Desarrollo de aplicaciones moviles

# Proyecto Lince: Lince Driver

**Propósito:** Aplicación móvil diseñada para mejorar la trazabilidad y rapidez del proceso de asignación y confirmación de servicios turísticos de Southbound. Permite a los drivers y guías revisar sus asignaciones, confirmar o rechazar servicios, registrar disponibilidad y marcar hitos de check-in/check-out directamente en terreno.

## 🎨 Identidad Visual
* **Logotipo:** Ubicado en `docs/diseno/logo.png`
* **Paleta de Colores:**
  * **Principal:** Verde Oscuro (`#2E7D32`) - Para acciones afirmativas (Confirmar, Check-in).
  * **Secundario:** Ámbar (`#FF8F00`) - Para alertas o bloqueos de disponibilidad.
  * **Fondo:** Gris Oscuro (`#121212`) - Para reducir el cansancio visual del usuario en ruta.
  * **Texto:** Blanco (`#FFFFFF`) - Alto contraste para legibilidad en exteriores.

## 🔄 Flujo de Usuario (UML)

```mermaid
flowchart TD
    Start([Inicio])
    End([Fin])

    Start --> Login[Pantalla: Login]
    Login --> Validar{¿Credenciales válidas?}
    Validar -- No --> Login
    Validar -- Sí --> Home[Pantalla: Servicios del día]

    Home --> Menu{Seleccionar opción}

    %% Consulta de otras fechas
    Menu -- Ver otra fecha --> Cal[Pantalla: Calendario]
    Cal --> Detalle

    %% Flujo principal
    Menu -- Abrir servicio de hoy --> Detalle[Pantalla: Detalle del servicio]
    Detalle --> Accion{¿Qué desea hacer?}

    Accion -- Ver reserva --> Reserva[Pantalla: Reserva completa]
    Reserva --> Detalle

    Accion -- Iniciar servicio --> CheckIn[Registrar Check-in]
    CheckIn --> GPS1[Capturar hora y ubicación]
    GPS1 --> Punt{¿Llegó 15 min antes del pickup?}
    Punt -- No --> Atraso[Marcar atraso y notificar a operaciones]
    Atraso --> Curso
    Punt -- Sí --> Curso[Estado: En curso]
    Curso --> CheckOut[Registrar Check-out]
    CheckOut --> GPS2[Capturar hora y ubicación]
    GPS2 --> Fin1[Estado: Finalizado]
    Fin1 --> End

    %% Disponibilidad
    Menu -- Gestionar disponibilidad --> Disp[Pantalla: Disponibilidad]
    Disp --> Sel[Seleccionar día a bloquear]
    Sel --> Valida{¿Falta más de una semana y el día está libre?}
    Valida -- No --> Error[Mostrar error: no se puede bloquear]
    Error --> Disp
    Valida -- Sí --> Bloq[Registrar día bloqueado]
    Bloq --> End

    %% Reportería
    Menu -- Ver resumen mensual --> Resumen[Pantalla: Resumen mensual]
    Resumen --> End

## 📱 Pantallas Principales (Interfaces)
Las propuestas visuales generadas se encuentran en el directorio `docs/diseno/interfaces/`.
* Login
* Inicio / Home
* Listado de servicios asignados
* Detalle del servicio
* Confirmación / Rechazo
* Disponibilidad
* Check-in / Check-out
* Perfil

## 👥 Integrantes (Equipo 10)
* **Nicolás López** - Diseñador UX/UI
* **Ignacio Oyarzun** - Arquitecto de Software
* **Pablo Velásquez** - Desarrollador Mobile

## 🛠️ Tecnologías
* Kotlin
* Jetpack Compose (Material Design 3)
* Arquitectura MVVM
* Room (Persistencia Local para modo sin conexión)
* Retrofit (Consumo API REST Simulada)
