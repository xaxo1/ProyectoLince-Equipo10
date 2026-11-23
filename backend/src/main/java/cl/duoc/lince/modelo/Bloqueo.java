package cl.duoc.lince.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Día que un guía o transportista bloquea en su disponibilidad. */
@Entity
@Table(name = "bloqueos", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "fecha"}))
public class Bloqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    private String motivo;

    protected Bloqueo() {}

    public Bloqueo(Usuario usuario, LocalDate fecha, String motivo) {
        this.usuario = usuario;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public LocalDate getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
}
