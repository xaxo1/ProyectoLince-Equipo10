package cl.duoc.lince.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Hito registrado desde el teléfono: check-in, check-out o posición puntual.
 * La fecha/hora del SERVIDOR es la que vale como evidencia.
 */
@Entity
@Table(name = "hitos")
public class Hito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clave única que genera el teléfono: si se reenvía (offline), no se duplica. */
    @Column(nullable = false, unique = true)
    private String idempotencia;

    @ManyToOne(optional = false)
    private Usuario usuario;

    /** Id del servicio en la Lince API. */
    private int servicioId;

    @Enumerated(EnumType.STRING)
    private Enums.TipoHito tipo;

    private double latitud;
    private double longitud;
    private double precisionMetros;

    /** Distancia al centro de la geocerca y radio usado (null si el lugar no tiene geocerca). */
    private Double distanciaMetros;
    private Integer radioMetros;
    private String lugar;

    @Enumerated(EnumType.STRING)
    private Enums.ResultadoHito resultado;

    private String mensaje;

    private LocalDateTime fechaHoraServidor;
    private String fechaHoraDispositivo;

    protected Hito() {}

    public Hito(String idempotencia, Usuario usuario, int servicioId, Enums.TipoHito tipo,
                double latitud, double longitud, double precisionMetros, String fechaHoraDispositivo) {
        this.idempotencia = idempotencia;
        this.usuario = usuario;
        this.servicioId = servicioId;
        this.tipo = tipo;
        this.latitud = latitud;
        this.longitud = longitud;
        this.precisionMetros = precisionMetros;
        this.fechaHoraDispositivo = fechaHoraDispositivo;
    }

    public Long getId() { return id; }
    public String getIdempotencia() { return idempotencia; }
    public Usuario getUsuario() { return usuario; }
    public int getServicioId() { return servicioId; }
    public Enums.TipoHito getTipo() { return tipo; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public double getPrecisionMetros() { return precisionMetros; }
    public Double getDistanciaMetros() { return distanciaMetros; }
    public Integer getRadioMetros() { return radioMetros; }
    public String getLugar() { return lugar; }
    public Enums.ResultadoHito getResultado() { return resultado; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getFechaHoraServidor() { return fechaHoraServidor; }
    public String getFechaHoraDispositivo() { return fechaHoraDispositivo; }

    public void setDistanciaMetros(Double distanciaMetros) { this.distanciaMetros = distanciaMetros; }
    public void setRadioMetros(Integer radioMetros) { this.radioMetros = radioMetros; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public void setResultado(Enums.ResultadoHito resultado) { this.resultado = resultado; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public void setFechaHoraServidor(LocalDateTime fechaHoraServidor) { this.fechaHoraServidor = fechaHoraServidor; }
}
