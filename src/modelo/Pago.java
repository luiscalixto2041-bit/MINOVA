/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {

    private int idPago;
    private int idPedido;
    private int idUsuario;

    private String nombreUsuario;
    private String nombreCliente;
    private String metodoPago;

    private BigDecimal valorPago = BigDecimal.ZERO;

    private LocalDateTime fechaPago;

    private String estado = "COMPLETADO";
    private String referenciaPago;

    // CONSTRUCTOR VACIO

    public Pago() {
    }

    // CONSTRUCTOR PARA REGISTRAR PAGOS

    public Pago(int idPedido,
                int idUsuario,
                String metodoPago,
                BigDecimal valorPago,
                String referenciaPago) {

        this.idPedido = idPedido;
        this.idUsuario = idUsuario;
        this.metodoPago = metodoPago;
        this.valorPago = valorPago;
        this.referenciaPago = referenciaPago;
        this.estado = "COMPLETADO";
    }

    // CONSTRUCTOR PARA CONSULTAS MYSQL

    public Pago(int idPago,
                int idPedido,
                int idUsuario,
                String nombreUsuario,
                String nombreCliente,
                String metodoPago,
                BigDecimal valorPago,
                LocalDateTime fechaPago,
                String estado,
                String referenciaPago) {

        this.idPago = idPago;
        this.idPedido = idPedido;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.nombreCliente = nombreCliente;
        this.metodoPago = metodoPago;
        this.valorPago = valorPago;
        this.fechaPago = fechaPago;
        this.estado = estado;
        this.referenciaPago = referenciaPago;
    }

    // GETTERS Y SETTERS

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }

    // ESTADO DEL PAGO

    public boolean isCompletado() {
        return "COMPLETADO".equals(estado);
    }

    public boolean isPendiente() {
        return "PENDIENTE".equals(estado);
    }

    public boolean isAnulado() {
        return "ANULADO".equals(estado);
    }

    @Override
    public String toString() {
        return "Pago #" + idPago
                + " - Pedido #" + idPedido;
    }
}

