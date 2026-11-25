
package modelo;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private int idPedido;

    private Integer idCliente;
    private Integer idMesa;
    private int idUsuario;

    private String nombreCliente;
    private String nombreUsuario;

    private LocalDateTime fechaPedido;

    private String tipoPedido = "LOCAL";
    private String estado = "PENDIENTE";

    private BigDecimal total = BigDecimal.ZERO;

    private List<DetallePedido> detalles =
            new ArrayList<>();

    // CONSTRUCTOR VACIO

    public Pedido() {
    }

    // CONSTRUCTOR PARA MYSQL

    public Pedido(
            int idPedido,
            Integer idCliente,
            Integer idMesa,
            int idUsuario,
            String nombreCliente,
            String nombreUsuario,
            LocalDateTime fechaPedido,
            String tipoPedido,
            String estado,
            BigDecimal total) {

        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.idMesa = idMesa;
        this.idUsuario = idUsuario;
        this.nombreCliente = nombreCliente;
        this.nombreUsuario = nombreUsuario;
        this.fechaPedido = fechaPedido;
        this.tipoPedido = tipoPedido;
        this.estado = estado;
        this.total = total == null
                ? BigDecimal.ZERO : total;
    }

    // GETTERS Y SETTERS

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(Integer idMesa) {
        this.idMesa = idMesa;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getTipoPedido() {
        return tipoPedido;
    }

    public void setTipoPedido(String tipoPedido) {
        this.tipoPedido = tipoPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {

        if (total == null || total.signum() < 0) {

            throw new IllegalArgumentException(
                    "El total del pedido no es valido."
            );
        }

        this.total = total;
    }

    // OBTENER DETALLES DEL PEDIDO

    public List<DetallePedido> getDetalles() {

        return new ArrayList<>(detalles);
    }

    // ESTABLECER DETALLES

    public void setDetalles(List<DetallePedido> detalles) {

        List<DetallePedido> nuevosDetalles =
                new ArrayList<>();

        if (detalles != null) {

            for (DetallePedido detalle : detalles) {

                if (detalle == null) {

                    throw new IllegalArgumentException(
                            "El detalle no puede estar vacio."
                    );
                }

                nuevosDetalles.add(detalle);
            }
        }

        this.detalles = nuevosDetalles;

        calcularTotal();
    }

    // AGREGAR PRODUCTO AL PEDIDO

    public void agregarDetalle(DetallePedido detalle) {

        if (detalle == null) {

            throw new IllegalArgumentException(
                    "El detalle no puede estar vacio."
            );
        }

        detalles.add(detalle);

        calcularTotal();
    }

    // CALCULAR TOTAL DEL PEDIDO

    public BigDecimal calcularTotal() {

        total = BigDecimal.ZERO;

        for (DetallePedido detalle : detalles) {

            total = total.add(
                    detalle.getSubtotal()
            );
        }

        return total;
    }

    @Override
    public String toString() {

        return "Pedido #" + idPedido;
    }
}
