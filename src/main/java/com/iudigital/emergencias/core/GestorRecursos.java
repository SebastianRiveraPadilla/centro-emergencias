package com.iudigital.emergencias.core;

import com.iudigital.emergencias.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class GestorRecursos {

    private final List<Ambulancia> ambulancias = new ArrayList<>();
    private final List<EquipoMedico> equipos = new ArrayList<>();
    private final Semaphore ambulanciasDisponibles;
    private final Semaphore equiposDisponibles;
    private final ReentrantLock lock = new ReentrantLock();

    public GestorRecursos(int numAmb, int numEq, Hospital hospital) {
        for (int i = 1; i <= numAmb; i++) {
            Ambulancia a = new Ambulancia("AMB-" + i, hospital);
            ambulancias.add(a);
            Thread t = new Thread(a, "hilo-amb-" + i);
            t.setDaemon(true);
            t.start();
        }
        for (int i = 1; i <= numEq; i++) {
            EquipoMedico e = new EquipoMedico("MED-" + i);
            equipos.add(e);
            Thread t = new Thread(e, "hilo-med-" + i);
            t.setDaemon(true);
            t.start();
        }
        this.ambulanciasDisponibles = new Semaphore(numAmb);
        this.equiposDisponibles = new Semaphore(numEq);
    }

    public Asignacion reservarPar() throws InterruptedException {
        if (!ambulanciasDisponibles.tryAcquire(2, TimeUnit.SECONDS)) return null;

        if (!equiposDisponibles.tryAcquire(2, TimeUnit.SECONDS)) {
            ambulanciasDisponibles.release();
            return null;
        }

        lock.lock();
        try {
            Ambulancia amb = buscarAmbulanciaLibre();
            EquipoMedico med = buscarEquipoLibre();

            if (amb == null || med == null) {
                ambulanciasDisponibles.release();
                equiposDisponibles.release();
                return null;
            }

            amb.setEstado(EstadoRecurso.EN_CAMINO);
            med.setEstado(EstadoRecurso.EN_CAMINO);
            return new Asignacion(amb, med);
        } finally {
            lock.unlock();
        }
    }

    private Ambulancia buscarAmbulanciaLibre() {
        for (Ambulancia a : ambulancias) if (a.getEstado() == EstadoRecurso.DISPONIBLE) return a;
        return null;
    }

    private EquipoMedico buscarEquipoLibre() {
        for (EquipoMedico e : equipos) if (e.getEstado() == EstadoRecurso.DISPONIBLE) return e;
        return null;
    }

    public void liberar(Asignacion asig) {
    asig.ambulancia().setEstado(EstadoRecurso.DISPONIBLE);
    asig.equipo().setEstado(EstadoRecurso.DISPONIBLE);
    ambulanciasDisponibles.release();
    equiposDisponibles.release();
    }

    public int getAmbulanciasDisponibles() { return ambulanciasDisponibles.availablePermits(); }
    public int getEquiposDisponibles()     { return equiposDisponibles.availablePermits(); }

    public List<Recurso> getTodos() {
        List<Recurso> todos = new ArrayList<>();
        todos.addAll(ambulancias);
        todos.addAll(equipos);
        return todos;
    }

    public void detener() {
        for (Ambulancia a : ambulancias) a.detener();
        for (EquipoMedico e : equipos) e.detener();
    }
}