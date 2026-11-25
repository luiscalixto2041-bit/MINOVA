
package controlador;

import modelo.Pedido;
import modelo.DetallePedido;
import modelo.Usuario;

import repositorio.PedidoRepositorio;
import seguridad.Permisos;

import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoControlador {

    private final PedidoRepositorio repositorio;
    private final Usuario usuarioActual;

    // CONSTRUCTOR PRINCIPAL

    public PedidoControlador(Usuario usuarioActual) {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "PEDIDOS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para acceder a pedidos."
            );
        }

        this.usuarioActual = usuarioActual;
        this.repositorio = new PedidoRepositorio();
    }

    // VALIDAR ACCESO

    private void validarAcceso() {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "PEDIDOS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para consultar pedidos."
            );
        }
    }

    // VALIDAR EDICION

    private void validarEdicion() {

        validarAcceso();

        if (!Permisos.puedeEditar(
                usuarioActual, "PEDIDOS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para modificar pedidos."
            );
        }
    }

    // REGISTRAR NUEVO PEDIDO

    public int registrarPedido(
            Integer idCliente,
            Integer idMesa,
            String tipoPedido,
            List<DetallePedido> detalles)
            throws SQLException {

        validarEdicion();

        if (tipoPedido == null
                || tipoPedido.isBlank()) {

            throw new IllegalArgumentException(
                    "Selecciona el tipo de pedido."
            );
        }

        tipoPedido = tipoPedido.trim()
                .toUpperCase(Locale.ROOT);

        if (!List.of(
                "LOCAL",
                "PARA_LLEVAR",
                "DOMICILIO"
        ).contains(tipoPedido)) {

            throw new IllegalArgumentException(
                    "Tipo de pedido invalido."
            );
        }

        if (idCliente != null && idCliente <= 0) {

            throw new IllegalArgumentException(
                    "El cliente seleccionado no es valido."
            );
        }

        if ("LOCAL".equals(tipoPedido)) {

            if (idMesa == null || idMesa <= 0) {

                throw new IllegalArgumentException(
                        "Selecciona una mesa."
                );
            }

        } else {

            idMesa = null;
        }

        if (detalles == null || detalles.isEmpty()) {

            throw new IllegalArgumentException(
                    "Agrega al menos un producto."
            );
        }

        for (DetallePedido detalle : detalles) {

            if (detalle == null
                    || detalle.getIdProducto() <= 0
                    || detalle.getCantidad() <= 0
                    || detalle.getPrecioUnitario() == null
                    || detalle.getPrecioUnitario().signum() < 0) {

                throw new IllegalArgumentException(
                        "Existe un producto con datos invalidos."
                );
            }
        }

        Pedido pedido = new Pedido();

        pedido.setIdCliente(idCliente);
        pedido.setIdMesa(idMesa);
        pedido.setIdUsuario(usuarioActual.getIdUsuario());
        pedido.setTipoPedido(tipoPedido);
        pedido.setEstado("PENDIENTE");
        pedido.setDetalles(detalles);

        return repositorio.registrar(
                usuarioActual, pedido
        );
    }

    // CALCULAR TOTAL PRELIMINAR

    public BigDecimal calcularTotal(
            List<DetallePedido> detalles) {

        validarAcceso();

        BigDecimal total = BigDecimal.ZERO;

        if (detalles == null) {
            return total;
        }

        for (DetallePedido detalle : detalles) {

            if (detalle == null) {

                throw new IllegalArgumentException(
                        "Detalle de pedido invalido."
                );
            }

            total = total.add(detalle.getSubtotal());
        }

        return total;
    }

    // BUSCAR PEDIDO EN MYSQL

    public Pedido consultarPedido(int idPedido)
            throws SQLException {

        validarAcceso();

        if (idPedido <= 0) {

            throw new IllegalArgumentException(
                    "El numero de pedido no es valido."
            );
        }

        return repositorio.buscar(
                usuarioActual, idPedido
        );
    }

    // LISTAR PEDIDOS DESDE MYSQL

    public ArrayList<Pedido> consultarPedidos()
            throws SQLException {

        validarAcceso();

        return repositorio.listar(usuarioActual);
    }

    // FILTRAR PEDIDOS POR ESTADO

    public ArrayList<Pedido> listarPorEstado(
            String estado) throws SQLException {

        validarAcceso();

        if (estado == null
                || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado.trim())) {

            return consultarPedidos();
        }

        estado = estado.trim()
                .toUpperCase(Locale.ROOT);

        if (!List.of(
                "PENDIENTE",
                "EN_PREPARACION",
                "LISTO",
                "ENTREGADO",
                "CANCELADO"
        ).contains(estado)) {

            throw new IllegalArgumentException(
                    "Estado de pedido invalido."
            );
        }

        ArrayList<Pedido> resultado =
                new ArrayList<>();

        for (Pedido pedido : consultarPedidos()) {

            if (estado.equals(pedido.getEstado())) {

                resultado.add(pedido);
            }
        }

        return resultado;
    }

    // CAMBIAR ESTADO DEL PEDIDO

    public boolean cambiarEstado(
            int idPedido,
            String nuevoEstado)
            throws SQLException {

        validarEdicion();

        if (idPedido <= 0
                || nuevoEstado == null
                || nuevoEstado.isBlank()) {

            throw new IllegalArgumentException(
                    "Selecciona un pedido y un estado valido."
            );
        }

        nuevoEstado = nuevoEstado.trim()
                .toUpperCase(Locale.ROOT);

        if (!List.of(
                "EN_PREPARACION",
                "LISTO",
                "ENTREGADO"
        ).contains(nuevoEstado)) {

            throw new IllegalArgumentException(
                    "Estado de pedido invalido."
            );
        }

        return repositorio.cambiarEstado(
                usuarioActual,
                idPedido,
                nuevoEstado
        );
    }

    // OBTENER SIGUIENTE ESTADO

    public String siguienteEstado(String estado) {

        validarAcceso();

        if (estado == null) {
            return null;
        }

        return switch (estado.trim()
                .toUpperCase(Locale.ROOT)) {

            case "PENDIENTE" ->
                "EN_PREPARACION";

            case "EN_PREPARACION" ->
                "LISTO";

            case "LISTO" ->
                "ENTREGADO";

            default -> null;
        };
    }

    // CANCELAR PEDIDO

    public boolean cancelarPedido(int idPedido)
            throws SQLException {

        validarEdicion();

        if (idPedido <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona un pedido valido."
            );
        }

        return repositorio.cancelar(
                usuarioActual, idPedido
        );
    }

    // CONTAR PEDIDOS POR ESTADO

    public int contarPorEstado(String estado)
            throws SQLException {

        return listarPorEstado(estado).size();
    }

    // CONTAR PEDIDOS REGISTRADOS

    public int totalPedidos() throws SQLException {

        return consultarPedidos().size();
    }

    // TOTAL DE PEDIDOS NO CANCELADOS
    // NO REPRESENTA DINERO RECAUDADO

    public BigDecimal totalPedidosNoCancelados()
            throws SQLException {

        BigDecimal total = BigDecimal.ZERO;

        for (Pedido pedido : consultarPedidos()) {

            if (!"CANCELADO".equals(
                    pedido.getEstado())) {

                total = total.add(pedido.getTotal());
            }
        }

        return total;
    }
}
