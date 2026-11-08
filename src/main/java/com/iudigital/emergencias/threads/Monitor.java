package com.iudigital.emergencias.threads;

import com.iudigital.emergencias.core.ColaEmergencias;
import com.iudigital.emergencias.core.GestorRecursos;
import com.iudigital.emergencias.model.Emergencia;
import com.iudigital.emergencias.model.Recurso;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Monitor implements Runnable {

    private final ColaEmergencias cola;
    private final GestorRecursos gestor;
    private final AtomicBoolean activo;
    private final long intervaloMs;

    public Monitor(ColaEmergencias cola, GestorRecursos gestor,
                   AtomicBoolean activo, long intervaloMs) {
        this.cola = cola;
        this.gestor = gestor;
        this.activo = activo;
        this.intervaloMs = intervaloMs;
    }

    @Override
    public void run() {
        while (activo.get()) {
            try {
                Thread.sleep(intervaloMs);
                imprimir();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void imprimir() {
        System.out.println("\n============== MONITOR EN TIEMPO REAL ==============");
        System.out.printf("Ambulancias disponibles: %d | Equipos disponibles: %d%n",
                gestor.getAmbulanciasDisponibles(), gestor.getEquiposDisponibles());

        System.out.println("--- Cola de emergencias (mayor prioridad primero) ---");
        List<Emergencia> pend = cola.snapshot();
        if (pend.isEmpty()) {
            System.out.println("  (vacía)");
        } else {
            for (Emergencia e : pend) {
                System.out.printf("  #%d %-12s %-9s %.1fkm prioridad=%.3f espera=%.1fs%n",
                        e.getId(), e.getUbicacion(), e.getGravedad(),
                        e.getDistanciaKm(), e.calcularPrioridad(), e.getSegundosEspera());
            }
        }

        System.out.println("--- Estado de recursos ---");
        for (Recurso r : gestor.getTodos()) {
            System.out.printf("  %-7s [%-11s] %s%n", r.getId(), r.getTipo(), r.getEstado());
        }
        System.out.println("====================================================\n");
    }
}