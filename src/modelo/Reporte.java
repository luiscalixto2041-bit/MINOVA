/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Reporte {

    private String tipo;
    private String titulo;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private LocalDateTime fechaGeneracion;

    private String generadoPor;
    private String resumen;

    private BigDecimal total = BigDecimal.ZERO;

    private long cantidadRegistros;

    private final List<String> columnas =
            new ArrayList<>();

    private final List<Object[]> filas =
            new ArrayList<>();

    // CONSTRUCTOR VACIO

    public Reporte() {

        this.fechaGeneracion = LocalDateTime.now(
                java.time.ZoneId.of("America/Bogota")
        );
    }

    // CONSTRUCTOR PRINCIPAL

    public Reporte(
            String tipo,
            String titulo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String generadoPor) {

        this();

        this.tipo = tipo;
        this.titulo = titulo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.generadoPor = generadoPor;
    }

    // GETTERS Y SETTERS

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(
            LocalDateTime fechaGeneracion) {

        this.fechaGeneracion = fechaGeneracion;
    }

    public String getGeneradoPor() {
        return generadoPor;
    }

    public void setGeneradoPor(String generadoPor) {
        this.generadoPor = generadoPor;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {

        this.total = total == null
                ? BigDecimal.ZERO
                : total;
    }

    public long getCantidadRegistros() {
        return cantidadRegistros;
    }

    public void setCantidadRegistros(
            long cantidadRegistros) {

        this.cantidadRegistros = cantidadRegistros;
    }

    // CONFIGURAR COLUMNAS

    public void setColumnas(String... nombres) {

        if (nombres == null || nombres.length == 0) {

            throw new IllegalArgumentException(
                    "El reporte debe tener columnas."
            );
        }

        for (String nombre : nombres) {

            if (nombre == null || nombre.isBlank()) {

                throw new IllegalArgumentException(
                        "Nombre de columna invalido."
                );
            }
        }

        columnas.clear();
        filas.clear();

        Collections.addAll(columnas, nombres);
    }

    public String[] getColumnas() {

        return columnas.toArray(new String[0]);
    }

    // AGREGAR FILAS AL REPORTE

    public void agregarFila(Object... valores) {

        if (valores == null
                || columnas.isEmpty()
                || valores.length != columnas.size()) {

            throw new IllegalArgumentException(
                    "La fila no coincide con las columnas."
            );
        }

        filas.add(valores.clone());
    }

    // OBTENER FILAS

    public List<Object[]> getFilas() {

        List<Object[]> copia = new ArrayList<>();

        for (Object[] fila : filas) {

            copia.add(fila.clone());
        }

        return copia;
    }

    // CONVERTIR FILAS PARA JTABLE

    public Object[][] getDatosTabla() {

        Object[][] datos =
                new Object[filas.size()][];

        for (int i = 0; i < filas.size(); i++) {

            datos[i] = filas.get(i).clone();
        }

        return datos;
    }

    // INFORMACION DEL REPORTE

    public int getCantidadFilas() {

        return filas.size();
    }

    public boolean tieneDatos() {

        return !filas.isEmpty();
    }

    @Override
    public String toString() {

        return titulo + " - " + tipo;
    }
}

