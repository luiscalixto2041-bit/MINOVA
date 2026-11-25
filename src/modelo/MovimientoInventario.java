/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package modelo;

import java.time.LocalDateTime;

public class MovimientoInventario {

    private int idMovimiento;
    private int idProducto;
    private int idUsuario;

    private String nombreProducto;
    private String nombreUsuario;
    private String tipoMovimiento;
    private int cantidad;
    private String motivo;

    private LocalDateTime fechaMovimiento;

    public MovimientoInventario() {
    }

    public MovimientoInventario(
            int idMovimiento,
            int idProducto,
            int idUsuario,
            String nombreProducto,
            String nombreUsuario,
            String tipoMovimiento,
            int cantidad,
            String motivo,
            LocalDateTime fechaMovimiento) {

        this.idMovimiento = idMovimiento;
        this.idProducto = idProducto;
        this.idUsuario = idUsuario;
        this.nombreProducto = nombreProducto;
        this.nombreUsuario = nombreUsuario;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fechaMovimiento = fechaMovimiento;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(
            LocalDateTime fechaMovimiento) {

        this.fechaMovimiento = fechaMovimiento;
    }
}
