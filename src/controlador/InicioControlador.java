/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.Usuario;
import repositorio.InicioRepositorio;
import seguridad.Permisos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class InicioControlador {

    private final Usuario usuarioActual;
    private final InicioRepositorio repositorio;

    // CONSTRUCTOR

    public InicioControlador(Usuario usuarioActual) {

        this.usuarioActual = usuarioActual;
        this.repositorio = new InicioRepositorio();

        validarAcceso();
    }

    // VALIDAR USUARIO AUTENTICADO

    private void validarAcceso() {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "INICIO")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para acceder al inicio."
            );
        }
    }

    // INFORMACION DEL USUARIO

    public String obtenerNombre() {

        validarAcceso();

        String nombre = usuarioActual.getNombreCompleto();

        if (nombre == null || nombre.isBlank()) {

            return usuarioActual.getNombreUsuario();
        }

        return nombre;
    }

    public String obtenerRol() {

        validarAcceso();

        return usuarioActual.getRol();
    }

    // IDENTIFICAR ROLES

    public boolean esAdministrador() {

        return "ADMINISTRADOR".equalsIgnoreCase(
                obtenerRol()
        );
    }

    public boolean esCajero() {

        return "CAJERO".equalsIgnoreCase(
                obtenerRol()
        );
    }

    public boolean esMesero() {

        return "MESERO".equalsIgnoreCase(
                obtenerRol()
        );
    }

    public boolean esInventario() {

        return "INVENTARIO".equalsIgnoreCase(
                obtenerRol()
        );
    }

    // VALIDAR ACCESOS RAPIDOS

    public boolean puedeAcceder(String modulo) {

        validarAcceso();

        return Permisos.puedeAcceder(
                usuarioActual,
                modulo
        );
    }

    public boolean puedeEditar(String modulo) {

        validarAcceso();

        return Permisos.puedeEditar(
                usuarioActual,
                modulo
        );
    }

    // MENSAJE DE BIENVENIDA SEGUN EL ROL

    public String obtenerMensajeBienvenida() {

        validarAcceso();

        if (esAdministrador()) {

            return "Resumen general y financiero "
                    + "de Bits Angus Burger.";
        }

        if (esCajero()) {

            return "Gestiona los pedidos, pagos "
                    + "y operaciones de caja.";
        }

        if (esMesero()) {

            return "Consulta los pedidos, mesas "
                    + "y reservas del restaurante.";
        }

        if (esInventario()) {

            return "Controla las existencias, productos "
                    + "y movimientos de inventario.";
        }

        return "Bienvenido al sistema MINOVA.";
    }

    // OBTENER ESTADISTICAS DESDE MYSQL

    public Map<String, Object> obtenerResumen()
            throws SQLException {

        validarAcceso();

        return repositorio.obtenerResumen(
                usuarioActual
        );
    }

    // OBTENER VENTAS SEMANALES

    public Map<LocalDate, BigDecimal>
            obtenerVentasSemanales()
            throws SQLException {

        validarAcceso();

        if (!esAdministrador()) {

            throw new IllegalArgumentException(
                    "Solo el administrador puede consultar "
                    + "las ventas semanales."
            );
        }

        return repositorio.ventasSemanales(
                usuarioActual
        );
    }

    // CONSULTAR SI EXISTE UNA ESTADISTICA

    public boolean tieneEstadistica(
            Map<String, Object> resumen,
            String clave) {

        return resumen != null
                && resumen.containsKey(clave);
    }

    // OBTENER CANTIDAD NUMERICA

    public long obtenerCantidad(
            Map<String, Object> resumen,
            String clave) {

        if (!tieneEstadistica(resumen, clave)) {

            throw new IllegalArgumentException(
                    "Estadistica no disponible: " + clave
            );
        }

        Object valor = resumen.get(clave);

        if (valor instanceof Number numero) {

            return numero.longValue();
        }

        throw new IllegalArgumentException(
                "La estadistica no es numerica."
        );
    }

    // OBTENER VALOR MONETARIO

    public BigDecimal obtenerValor(
            Map<String, Object> resumen,
            String clave) {

        if (!tieneEstadistica(resumen, clave)) {

            throw new IllegalArgumentException(
                    "Estadistica no disponible: " + clave
            );
        }

        Object valor = resumen.get(clave);

        if (valor instanceof BigDecimal decimal) {

            return decimal;
        }

        if (valor instanceof Number numero) {

            return new BigDecimal(
                    numero.toString()
            );
        }

        throw new IllegalArgumentException(
                "Valor monetario invalido."
        );
    }

    // FORMATEAR DINERO COLOMBIANO

    public String dinero(BigDecimal valor) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag("es-CO")
                );

        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(2);

        return formato.format(
                valor == null
                        ? BigDecimal.ZERO
                        : valor
        );
    }

    // FORMATEAR CANTIDADES

    public String cantidad(long valor) {

        return NumberFormat.getIntegerInstance(
                Locale.forLanguageTag("es-CO")
        ).format(valor);
    }

    // MOSTRAR MESAS DISPONIBLES

    public String obtenerMesasDisponibles(
            Map<String, Object> resumen) {

        long disponibles = obtenerCantidad(
                resumen,
                "mesasDisponibles"
        );

        long total = obtenerCantidad(
                resumen,
                "mesasTotales"
        );

        return disponibles + " / " + total;
    }

    // CALCULAR PORCENTAJE DE MESAS OCUPADAS

    public double porcentajeOcupacion(
            Map<String, Object> resumen) {

        long ocupadas = obtenerCantidad(
                resumen,
                "mesasOcupadas"
        );

        long total = obtenerCantidad(
                resumen,
                "mesasTotales"
        );

        if (total <= 0) {
            return 0;
        }

        return ocupadas * 100.0 / total;
    }

    // CALCULAR TOTAL DE INGRESOS SEMANALES

    public BigDecimal totalSemanal(
            Map<LocalDate, BigDecimal> ventas) {

        validarAcceso();

        if (!esAdministrador()) {

            throw new IllegalArgumentException(
                    "No tienes permisos para consultar "
                    + "los ingresos semanales."
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        if (ventas != null) {

            for (BigDecimal valor : ventas.values()) {

                if (valor != null) {

                    total = total.add(valor);
                }
            }
        }

        return total;
    }
}
