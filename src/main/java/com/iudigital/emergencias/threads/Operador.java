package com.iudigital.emergencias.threads;

import com.iudigital.emergencias.core.ColaEmergencias;
import com.iudigital.emergencias.model.Emergencia;
import com.iudigital.emergencias.model.NivelGravedad;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Operador implements Runnable {

    private static final String[] UBICACIONES = {
        "Centro", "Laureles", "Belén", "Robledo", "Aranjuez",
        "El Poblado", "Castilla", "Buenos Aires", "Manrique", "Guayabal"
    };

    private final String id;
    private final ColaEmergencias cola;
    private final AtomicBoolean activo;
    private final AtomicInteger totalLlamadas;
    private final long intervaloBaseMs;
    private final Random rnd = new Random();

    public Operador(String id, ColaEmergencias cola, AtomicBoolean activo,
                    AtomicInteger totalLlamadas, long intervaloBaseMs) {
        this.id = id;
        this.cola = cola;
        this.activo = activo;
        this.totalLlamadas = totalLlamadas;
        this.intervaloBaseMs = intervaloBaseMs;
    }

    @Override
    public void run() {
        while (activo.get()) {
            try {
                Thread.sleep(intervaloBaseMs + rnd.nextInt((int) intervaloBaseMs));
                Emergencia e = generar();
                cola.agregar(e);
                totalLlamadas.incrementAndGet();
                System.out.printf("[Operador %s] Llamada #%d: %s (%s, %.1f km)%n",
                        id, e.getId(), e.getUbicacion(), e.getGravedad(), e.getDistanciaKm());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Emergencia generar() {
        NivelGravedad g = NivelGravedad.values()[rnd.nextInt(4)];
        String ubi = UBICACIONES[rnd.nextInt(UBICACIONES.length)];
        double dist = 1 + rnd.nextDouble() * 20;
        return new Emergencia(ubi, dist, g);
    }
}