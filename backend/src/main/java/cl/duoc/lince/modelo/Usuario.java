package cl.duoc.lince.modelo;

import jakarta.persistence.*;

/**
 * Usuario académico de la app. Los guías y transportistas se vinculan con su recurso
 * en la Lince API del cliente (linceRecursoId = id de guía o de transportista).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false, unique = true)
    private String rut;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.Rol rol;

    /** Id del guía o transportista en la Lince API; null para coordinación. */
    private Integer linceRecursoId;

    protected Usuario() {}

    public Usuario(String correo, String rut, String nombre, String claveHash, Enums.Rol rol, Integer linceRecursoId) {
        this.correo = correo;
        this.rut = rut;
        this.nombre = nombre;
        this.claveHash = claveHash;
        this.rol = rol;
        this.linceRecursoId = linceRecursoId;
    }

    public Long getId() { return id; }
    public String getCorreo() { return correo; }
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getClaveHash() { return claveHash; }
    public Enums.Rol getRol() { return rol; }
    public Integer getLinceRecursoId() { return linceRecursoId; }
}
