package cl.duoc.lince.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Confirmación o rechazo de una asignación por parte del guía o transportista. */
@Entity
@Table(name = "confirmaciones", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "servicioId"}))
public class Confirmacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    private int servicioId;

    @Enumerated(EnumType.STRING)
    private Enums.EstadoConfirmacion estado;

    private String motivo;

    private LocalDateTime fechaHora;

    protected Confirmacion() {}

    public Confirmacion(Usuario usuario, int servicioId) {
        this.usuario = usuario;
        this.servicioId = servicioId;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public int getServicioId() { return servicioId; }
    public Enums.EstadoConfirmacion getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    public void setEstado(Enums.EstadoConfirmacion estado) { this.estado = estado; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
