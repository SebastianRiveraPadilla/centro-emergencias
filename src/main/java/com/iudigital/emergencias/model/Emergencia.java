package com.iudigital.emergencias.model;

import java.util.concurrent.atomic.AtomicLong;

public class Emergencia implements Comparable<Emergencia> {

    private static final AtomicLong SEQ = new AtomicLong();

    private static final double PESO_GRAVEDAD   = 0.60;
    private static final double PESO_ESPERA     = 0.25;
    private static final double PESO_DISTANCIA  = 0.15;
    private static final double SATURACION_ESPERA_SEG = 60.0;
    private static final double DISTANCIA_MAX_KM = 50.0;

    private final long id;
    private final String ubicacion;
    private final double distanciaKm;
    private final NivelGravedad gravedad;
    private final long horaRecepcionMillis;

    private volatile EstadoEmergencia estado = EstadoEmergencia.PENDIENTE;
    private volatile String recursoAsignado = "";

    public Emergencia(String ubicacion, double distanciaKm, NivelGravedad gravedad) {
        this.id = SEQ.incrementAndGet();
        this.ubicacion = ubicacion;
        this.distanciaKm = distanciaKm;
        this.gravedad = gravedad;
        this.horaRecepcionMillis = System.currentTimeMillis();
    }

    public double calcularPrioridad() {
        double esperaSeg = getSegundosEspera();
        double normEspera = Math.min(esperaSeg / SATURACION_ESPERA_SEG, 1.0);
        double normDist   = Math.max(0, 1.0 - (distanciaKm / DISTANCIA_MAX_KM));
        double normGrav   = gravedad.getPeso() / 4.0;
        return PESO_GRAVEDAD * normGrav
             + PESO_ESPERA   * normEspera
             + PESO_DISTANCIA * normDist;
    }

    public double getSegundosEspera() {
        return (System.currentTimeMillis() - horaRecepcionMillis) / 1000.0;
    }

    @Override
    public int compareTo(Emergencia o) {
        return Double.compare(o.calcularPrioridad(), this.calcularPrioridad());
    }

    // --- getters/setters ---
    public long getId() { return id; }
    public String getUbicacion() { return ubicacion; }
    public double getDistanciaKm() { return distanciaKm; }
    public NivelGravedad getGravedad() { return gravedad; }
    public EstadoEmergencia getEstado() { return estado; }
    public void setEstado(EstadoEmergencia e) { this.estado = e; }
    public String getRecursoAsignado() { return recursoAsignado; }
    public void setRecursoAsignado(String r) { this.recursoAsignado = r; }
}