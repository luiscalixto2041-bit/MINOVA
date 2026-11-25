/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DetallePedido {

    private int idDetalle;
    private int idPedido;
    private int idProducto;

    private String nombreProducto;

    private int cantidad;

    private BigDecimal precioUnitario =
            BigDecimal.ZERO;

    // CONSTRUCTOR VACIO

    public DetallePedido() {
    }

    // CONSTRUCTOR PARA NUEVOS PEDIDOS

    public DetallePedido(
            int idProducto,
            String nombreProducto,
            int cantidad,
            BigDecimal precioUnitario) {

        setIdProducto(idProducto);
        setNombreProducto(nombreProducto);
        setCantidad(cantidad);
        setPrecioUnitario(precioUnitario);
    }

    // CONSTRUCTOR PARA MYSQL

    public DetallePedido(
            int idDetalle,
            int idPedido,
            int idProducto,
            String nombreProducto,
            int cantidad,
            BigDecimal precioUnitario) {

        this(idProducto, nombreProducto,
                cantidad, precioUnitario);

        this.idDetalle = idDetalle;
        this.idPedido = idPedido;
    }

    // GETTERS Y SETTERS

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {

        if (idProducto <= 0) {

            throw new IllegalArgumentException(
                    "El producto no es valido."
            );
        }

        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {

        if (cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {

        if (precioUnitario == null
                || precioUnitario.signum() < 0) {

            throw new IllegalArgumentException(
                    "El precio no es valido."
            );
        }

        this.precioUnitario = precioUnitario.setScale(
                2, RoundingMode.HALF_UP
        );
    }

    // CALCULAR SUBTOTAL

    public BigDecimal getSubtotal() {

        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
        );
    }
}

