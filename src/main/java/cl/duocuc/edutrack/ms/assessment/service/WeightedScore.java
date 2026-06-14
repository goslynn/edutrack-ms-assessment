package cl.duocuc.edutrack.ms.assessment.service;

import java.math.BigDecimal;

/**
 * Par {@code (nota, ponderacion)} que alimenta {@link WeightedAverageCalculator}.
 * Es la proyeccion minima que el repositorio extrae al cruzar una nota con la
 * ponderacion de su evaluacion para calcular el promedio ponderado de un alumno
 * en una asignatura y periodo.
 *
 * @param score  nota en escala chilena (1.0–7.0)
 * @param weight ponderacion de la evaluacion a la que pertenece la nota
 */
public record WeightedScore(BigDecimal score, BigDecimal weight) {}
