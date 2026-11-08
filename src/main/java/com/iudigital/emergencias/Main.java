package com.iudigital.emergencias;

import com.iudigital.emergencias.core.ColaEmergencias;
import com.iudigital.emergencias.core.GestorRecursos;
import com.iudigital.emergencias.model.Hospital;
import com.iudigital.emergencias.threads.Despachador;
import com.iudigital.emergencias.threads.Monitor;
import com.iudigital.emergencias.threads.Operador;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        
        final int NUM_OPERADORES    = 4;
        final int NUM_DESPACHADORES = 2;
        final int NUM_AMBULANCIAS   = 2;
        final int NUM_EQUIPOS       = 1;
        final long DURACION_MS      = 25_000;
        final long INTERVALO_MONITOR = 5_000;   

        Hospital hospital = new Hospital("HOSP-1", "San Vicente");
        Thread hiloHospital = new Thread(hospital, "hilo-hospital");
        hiloHospital.setDaemon(true);
        hiloHospital.start();

        ColaEmergencias cola   = new ColaEmergencias();
        GestorRecursos  gestor = new GestorRecursos(NUM_AMBULANCIAS, NUM_EQUIPOS, hospital);

        AtomicBoolean activo    = new AtomicBoolean(true);
        AtomicInteger totalLlam = new AtomicInteger(0);
        AtomicInteger atendidas = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(
                NUM_OPERADORES + NUM_DESPACHADORES + 1);

        for (int i = 1; i <= NUM_OPERADORES; i++) {
            pool.submit(new Operador("OP-" + i, cola, activo, totalLlam, 4000));
        }
        for (int i = 1; i <= NUM_DESPACHADORES; i++) {
            pool.submit(new Despachador("DSP-" + i, cola, gestor, activo, atendidas));
        }
        pool.submit(new Monitor(cola, gestor, activo, INTERVALO_MONITOR));

        ScheduledExecutorService repriorizador = Executors.newSingleThreadScheduledExecutor();
        repriorizador.scheduleAtFixedRate(cola::repriorizar, 1, 1, TimeUnit.SECONDS);

        System.out.printf(">>> Sistema iniciado | op=%d desp=%d amb=%d eq=%d | %ds%n",
                NUM_OPERADORES, NUM_DESPACHADORES, NUM_AMBULANCIAS, NUM_EQUIPOS,
                DURACION_MS / 1000);

        Thread.sleep(DURACION_MS);

        activo.set(false);
        repriorizador.shutdownNow();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        gestor.detener();
        hospital.detener();

        System.out.printf("%n>>> FIN | llamadas=%d resueltas=%d enCola=%d hospitalRecibio=%d%n",
                totalLlam.get(), atendidas.get(), cola.size(), hospital.getTotalRecibidos());
    }
}