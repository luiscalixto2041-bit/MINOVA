/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.Pago;
import modelo.Pedido;
import modelo.Usuario;

import repositorio.PagoRepositorio;
import seguridad.Permisos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PagoControlador {

    private final PagoRepositorio repositorio;
    private final Usuario usuarioActual;

    // CONSTRUCTOR

    public PagoControlador(Usuario usuarioActual) {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "PAGOS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para acceder a pagos."
            );
        }

        this.usuarioActual = usuarioActual;
        this.repositorio = new PagoRepositorio();
    }

    // VALIDAR ACCESO

    private void validarAcceso() {

        if (!usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "PAGOS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para consultar pagos."
            );
        }
    }

    // VALIDAR EDICION

    private void validarEdicion() {

        validarAcceso();

        if (!Permisos.puedeEditar(
                usuarioActual, "PAGOS")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para registrar pagos."
            );
        }
    }

    // VALIDAR METODO DE PAGO

    private String validarMetodo(String metodo) {

        if (metodo == null || metodo.isBlank()) {

            throw new IllegalArgumentException(
                    "Selecciona un metodo de pago."
            );
        }

        metodo = metodo.trim().toUpperCase(Locale.ROOT);

        if (!List.of(
                "EFECTIVO",
                "TARJETA",
                "TRANSFERENCIA"
        ).contains(metodo)) {

            throw new IllegalArgumentException(
                    "Metodo de pago invalido."
            );
        }

        return metodo;
    }

    // CONVERTIR VALOR MONETARIO

    public BigDecimal convertirValor(String valor) {

        if (valor == null || valor.isBlank()) {

            throw new IllegalArgumentException(
                    "Ingresa un valor valido."
            );
        }

        try {

            BigDecimal resultado = new BigDecimal(
                    valor.trim().replace(",", ".")
            ).setScale(2, RoundingMode.UNNECESSARY);

            if (resultado.signum() < 0) {

                throw new IllegalArgumentException(
                        "El valor no puede ser negativo."
                );
            }

            return resultado;

        } catch (NumberFormatException
                | ArithmeticException e) {

            throw new IllegalArgumentException(
                    "Ingresa un valor numerico valido "
                    + "sin separadores de miles "
                    + "y con maximo dos decimales."
            );
        }
    }

    // CALCULAR CAMBIO EN EFECTIVO

    public BigDecimal calcularCambio(
            BigDecimal total,
            BigDecimal recibido) {

        if (total == null || total.signum() <= 0) {

            throw new IllegalArgumentException(
                    "El total del pedido no es valido."
            );
        }

        if (recibido == null
                || recibido.compareTo(total) < 0) {

            throw new IllegalArgumentException(
                    "El dinero recibido es insuficiente."
            );
        }

        return recibido.subtract(total)
                .setScale(2, RoundingMode.UNNECESSARY);
    }

    // REGISTRAR PAGO

    public int registrarPago(
            int idPedido,
            String metodoPago,
            BigDecimal total,
            BigDecimal dineroRecibido,
            String referencia) throws SQLException {

        validarEdicion();

        if (idPedido <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona un pedido valido."
            );
        }

        metodoPago = validarMetodo(metodoPago);

        if (total == null || total.signum() <= 0) {

            throw new IllegalArgumentException(
                    "El total debe ser mayor que cero."
            );
        }

        try {

            total = total.setScale(
                    2, RoundingMode.UNNECESSARY
            );

        } catch (ArithmeticException e) {

            throw new IllegalArgumentException(
                    "El total tiene decimales invalidos."
            );
        }

        if (total.compareTo(
                new BigDecimal("999999999999.99")) > 0) {

            throw new IllegalArgumentException(
                    "El valor supera el limite permitido."
            );
        }

        if ("EFECTIVO".equals(metodoPago)) {

            calcularCambio(total, dineroRecibido);

            referencia = null;

        } else {

            if (referencia == null
                    || referencia.isBlank()) {

                throw new IllegalArgumentException(
                        "Ingresa la referencia de la transaccion."
                );
            }

            referencia = referencia.trim();

            if (referencia.length() > 100) {

                throw new IllegalArgumentException(
                        "La referencia no puede superar "
                        + "100 caracteres."
                );
            }
        }

        Pago pago = new Pago(
                idPedido,
                usuarioActual.getIdUsuario(),
                metodoPago,
                total,
                referencia
        );

        return repositorio.registrar(
                usuarioActual, pago
        );
    }

    // LISTAR PEDIDOS PENDIENTES DE PAGO

    public ArrayList<Pedido> listarPedidosPendientes()
            throws SQLException {

        validarAcceso();

        return repositorio.listarPedidosPendientes(
                usuarioActual
        );
    }

    // LISTAR PAGOS REGISTRADOS

    public ArrayList<Pago> listarPagos()
            throws SQLException {

        validarAcceso();

        return repositorio.listar(usuarioActual);
    }

    // BUSCAR PAGO POR ID

    public Pago buscarPago(int idPago)
            throws SQLException {

        validarAcceso();

        if (idPago <= 0) {

            throw new IllegalArgumentException(
                    "ID de pago invalido."
            );
        }

        return repositorio.buscarPorId(
                usuarioActual, idPago
        );
    }

    // FILTRAR PAGOS POR METODO

    public ArrayList<Pago> listarPorMetodo(
            String metodo) throws SQLException {

        validarAcceso();

        if (metodo == null
                || metodo.isBlank()
                || "TODOS".equalsIgnoreCase(metodo)) {

            return listarPagos();
        }

        metodo = validarMetodo(metodo);

        ArrayList<Pago> resultado = new ArrayList<>();

        for (Pago pago : listarPagos()) {

            if (metodo.equals(pago.getMetodoPago())) {

                resultado.add(pago);
            }
        }

        return resultado;
    }

    // TOTAL RECAUDADO

    public BigDecimal totalRecaudado()
            throws SQLException {

        validarAcceso();

        return repositorio.totalRecaudado(
                usuarioActual
        );
    }

    // CANTIDAD DE PAGOS COMPLETADOS

    public int cantidadPagos()
            throws SQLException {

        validarAcceso();

        return repositorio.cantidadPagos(
                usuarioActual
        );
    }

    // CANTIDAD DE PEDIDOS PENDIENTES DE PAGO

    public int cantidadPedidosPendientes()
            throws SQLException {

        return listarPedidosPendientes().size();
    }

    // TOTAL PENDIENTE POR COBRAR

    public BigDecimal totalPendientePorCobrar()
            throws SQLException {

        BigDecimal total = BigDecimal.ZERO;

        for (Pedido pedido : listarPedidosPendientes()) {

            total = total.add(pedido.getTotal());
        }

        return total;
    }

    // TOTAL RECAUDADO POR METODO

    public BigDecimal totalPorMetodo(String metodo)
            throws SQLException {

        BigDecimal total = BigDecimal.ZERO;

        for (Pago pago : listarPorMetodo(metodo)) {

            if (pago.isCompletado()) {

                total = total.add(pago.getValorPago());
            }
        }

        return total;
    }

    // GENERAR COMPROBANTE

    public String generarComprobante(int idPago)
            throws SQLException {

        Pago pago = buscarPago(idPago);

        if (pago == null) {

            throw new IllegalArgumentException(
                    "No se encontro el pago."
            );
        }

        java.time.format.DateTimeFormatter formato =
                java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy HH:mm:ss");

        String referencia = pago.getReferenciaPago();

        if (referencia == null || referencia.isBlank()) {
            referencia = "No aplica";
        }

        return """
                ====================================
                   MINOVA - BITS ANGUS BURGER
                      COMPROBANTE DE PAGO
                ====================================

                """
                + "Comprobante: #" + pago.getIdPago() + "\n"
                + "Pedido: #" + pago.getIdPedido() + "\n"
                + "Cliente: " + pago.getNombreCliente() + "\n"
                + "Empleado: " + pago.getNombreUsuario() + "\n"
                + "Fecha: " + pago.getFechaPago()
                        .format(formato) + "\n"
                + "Metodo: " + pago.getMetodoPago() + "\n"
                + "Referencia: " + referencia + "\n"
                + "Estado: " + pago.getEstado() + "\n\n"
                + "TOTAL: $ "
                + pago.getValorPago().toPlainString()
                + " COP\n\n"
                + "====================================\n"
                + "       GRACIAS POR SU COMPRA\n"
                + "====================================\n"
                + "Comprobante interno de pago.";
    }
}

