package com.iudigital.emergencias.model;

import java.util.concurrent.CompletableFuture;

public record OrdenTrabajo(Emergencia emergencia, CompletableFuture<Void> completado) { }
