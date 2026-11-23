package cl.duoc.lince.modelo;

import jakarta.persistence.*;

/**
 * Geocerca de un hotel o punto de pickup/dropoff: coordenada central + radio.
 * La Lince API entrega el pickup solo como texto ("Hotel Las Condes"), por eso
 * mantenemos este catálogo ficticio de coordenadas, enlazado por el nombre.
 */
@Entity
@Table(name = "geocercas")
public class Geocerca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String oficina;

    private double latitud;
    private double longitud;

    /** Radio en metros (inicial 150, lo puede cambiar coordinación). */
    private int radioMetros;

    protected Geocerca() {}

    public Geocerca(String nombre, String oficina, double latitud, double longitud, int radioMetros) {
        this.nombre = nombre;
        this.oficina = oficina;
        this.latitud = latitud;
        this.longitud = longitud;
        this.radioMetros = radioMetros;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getOficina() { return oficina; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public int getRadioMetros() { return radioMetros; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setOficina(String oficina) { this.oficina = oficina; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public void setRadioMetros(int radioMetros) { this.radioMetros = radioMetros; }
}
