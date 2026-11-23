package cl.duoc.lince.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Incidencia de un servicio. Puede crearla el usuario (formulario de la app) o el
 * servidor automáticamente cuando un check-in sale fuera de zona, atrasado o con GPS impreciso.
 */
@Entity
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    private int servicioId;

    /** ATRASO, FUERA_DE_ZONA, GPS_IMPRECISO, PASAJERO_AUSENTE, PROBLEMA_VEHICULO, CAMBIO_ITINERARIO, OTRO. */
    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String descripcion;

    private Integer pasajerosPresentes;

    /** true si la generó el servidor al rechazar un check-in. */
    private boolean automatica;

    @Enumerated(EnumType.STRING)
    private Enums.EstadoIncidencia estado = Enums.EstadoIncidencia.ABIERTA;

    private LocalDateTime fechaHora;

    protected Incidencia() {}

    public Incidencia(Usuario usuario, int servicioId, String tipo, String descripcion,
                      Integer pasajerosPresentes, boolean automatica, LocalDateTime fechaHora) {
        this.usuario = usuario;
        this.servicioId = servicioId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.pasajerosPresentes = pasajerosPresentes;
        this.automatica = automatica;
        this.fechaHora = fechaHora;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public int getServicioId() { return servicioId; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public Integer getPasajerosPresentes() { return pasajerosPresentes; }
    public boolean isAutomatica() { return automatica; }
    public Enums.EstadoIncidencia getEstado() { return estado; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPasajerosPresentes(Integer pasajerosPresentes) { this.pasajerosPresentes = pasajerosPresentes; }
    public void setEstado(Enums.EstadoIncidencia estado) { this.estado = estado; }
}
