package cl.duocuc.edutrack.ms.assessment.service;

import java.math.BigDecimal;

/**
 * Resultado del calculo de promedio ponderado: el promedio y cuantas notas lo
 * componen. Es el valor de dominio que devuelve {@link AverageService}; el recurso
 * lo envuelve en el DTO de respuesta junto con el alumno/asignatura/periodo
 * consultados.
 *
 * @param average    promedio ponderado (escala chilena, 1 decimal)
 * @param gradeCount cantidad de notas consideradas en el promedio
 */
public record AverageResult(BigDecimal average, int gradeCount) {}
