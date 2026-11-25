/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.Reporte;
import modelo.Usuario;
import repositorio.ReporteRepositorio;
import seguridad.Permisos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;

public class ReporteControlador {

    private final ReporteRepositorio repositorio;
    private final Usuario usuarioActual;

    private static final ZoneId ZONA =
            ZoneId.of("America/Bogota");

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final List<String> TIPOS = List.of(
            "VENTAS",
            "PRODUCTOS",
            "PAGOS",
            "EMPLEADOS",
            "INVENTARIO",
            "RESERVAS",
            "PEDIDOS"
    );

    // CONSTRUCTOR

    public ReporteControlador(Usuario usuarioActual) {

        this.usuarioActual = usuarioActual;
        this.repositorio = new ReporteRepositorio();

        validarAcceso();
    }

    // VALIDAR PERMISOS

    private void validarAcceso() {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "REPORTES")
                || !"ADMINISTRADOR".equalsIgnoreCase(
                        usuarioActual.getRol())) {

            throw new IllegalArgumentException(
                    "Solo el administrador puede acceder a reportes."
            );
        }
    }

    // TIPOS DE REPORTES DISPONIBLES

    public String[] obtenerTipos() {

        validarAcceso();

        return TIPOS.toArray(new String[0]);
    }

    // VALIDAR TIPO DE REPORTE

    private String validarTipo(String tipo) {

        if (tipo == null || tipo.isBlank()) {

            throw new IllegalArgumentException(
                    "Selecciona un tipo de reporte."
            );
        }

        tipo = tipo.trim().toUpperCase(Locale.ROOT);

        if (!TIPOS.contains(tipo)) {

            throw new IllegalArgumentException(
                    "Tipo de reporte invalido."
            );
        }

        return tipo;
    }

    // CONVERTIR FECHA

    public LocalDate convertirFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {

            throw new IllegalArgumentException(
                    "Ingresa una fecha."
            );
        }

        try {

            return LocalDate.parse(
                    fecha.trim(), FORMATO_FECHA
            );

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Fecha invalida. Utiliza dd/MM/yyyy."
            );
        }
    }

    // FORMATEAR FECHA

    public String formatearFecha(LocalDate fecha) {

        if (fecha == null) {
            return "No aplica";
        }

        return fecha.format(FORMATO_FECHA);
    }

    // VALIDAR PERIODO

    private void validarPeriodo(
            LocalDate inicio,
            LocalDate fin) {

        if (inicio == null || fin == null) {

            throw new IllegalArgumentException(
                    "Ingresa las fechas del reporte."
            );
        }

        if (inicio.isAfter(fin)) {

            throw new IllegalArgumentException(
                    "La fecha inicial no puede superar la final."
            );
        }

        if (inicio.plusYears(1).isBefore(fin)) {

            throw new IllegalArgumentException(
                    "El periodo no puede superar un año."
            );
        }

        if (inicio.getYear() < 1000
                || fin.getYear() > 9998) {

            throw new IllegalArgumentException(
                    "Las fechas estan fuera del rango permitido."
            );
        }
    }

    // GENERAR REPORTE PERSONALIZADO

    public Reporte generarReporte(
            String tipo,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        validarAcceso();

        tipo = validarTipo(tipo);

        if ("INVENTARIO".equals(tipo)) {

            return repositorio.generar(
                    usuarioActual,
                    tipo,
                    null,
                    null
            );
        }

        validarPeriodo(inicio, fin);

        return repositorio.generar(
                usuarioActual,
                tipo,
                inicio,
                fin
        );
    }

    // GENERAR REPORTE CON FECHAS DE TEXTO

    public Reporte generarReporte(
            String tipo,
            String inicio,
            String fin) throws SQLException {

        validarAcceso();

        tipo = validarTipo(tipo);

        if ("INVENTARIO".equals(tipo)) {

            return generarReporte(
                    tipo,
                    (LocalDate) null,
                    (LocalDate) null
            );
        }

        return generarReporte(
                tipo,
                convertirFecha(inicio),
                convertirFecha(fin)
        );
    }

    // REPORTE DEL DIA

    public Reporte generarReporteHoy(String tipo)
            throws SQLException {

        LocalDate hoy = LocalDate.now(ZONA);

        return generarReporte(tipo, hoy, hoy);
    }

    // REPORTE DE LOS ULTIMOS 7 DIAS

    public Reporte generarReporteSemanal(String tipo)
            throws SQLException {

        LocalDate hoy = LocalDate.now(ZONA);

        return generarReporte(
                tipo,
                hoy.minusDays(6),
                hoy
        );
    }

    // REPORTE DEL MES ACTUAL

    public Reporte generarReporteMensual(String tipo)
            throws SQLException {

        LocalDate hoy = LocalDate.now(ZONA);

        return generarReporte(
                tipo,
                hoy.withDayOfMonth(1),
                hoy
        );
    }

    // REPORTE DEL ANO ACTUAL

    public Reporte generarReporteAnual(String tipo)
            throws SQLException {

        LocalDate hoy = LocalDate.now(ZONA);

        return generarReporte(
                tipo,
                hoy.withDayOfYear(1),
                hoy
        );
    }

    // FECHAS PARA LOS FILTROS DE LA INTERFAZ

    public LocalDate[] obtenerPeriodo(String periodo) {

        validarAcceso();

        LocalDate hoy = LocalDate.now(ZONA);

        if (periodo == null) {

            throw new IllegalArgumentException(
                    "Selecciona un periodo."
            );
        }

        return switch (
                periodo.trim().toUpperCase(Locale.ROOT)) {

            case "HOY" ->
                new LocalDate[]{hoy, hoy};

            case "ULTIMOS 7 DIAS" ->
                new LocalDate[]{
                    hoy.minusDays(6), hoy
                };

            case "ESTE MES" ->
                new LocalDate[]{
                    hoy.withDayOfMonth(1), hoy
                };

            case "ESTE ANO" ->
                new LocalDate[]{
                    hoy.withDayOfYear(1), hoy
                };

            case "PERSONALIZADO" ->
                new LocalDate[]{null, null};

            default ->
                throw new IllegalArgumentException(
                        "Periodo invalido."
                );
        };
    }

    // IDENTIFICAR REPORTES MONETARIOS

    public boolean tieneTotalMonetario(String tipo) {

        tipo = validarTipo(tipo);

        return !"RESERVAS".equals(tipo);
    }

    // ETIQUETA DEL TOTAL

    public String obtenerEtiquetaTotal(String tipo) {

        tipo = validarTipo(tipo);

        return switch (tipo) {

            case "VENTAS", "PAGOS", "EMPLEADOS" ->
                "Total recaudado";

            case "PRODUCTOS" ->
                "Valor de productos vendidos";

            case "INVENTARIO" ->
                "Valor a precio de venta";

            case "PEDIDOS" ->
                "Valor de pedidos no cancelados";

            case "RESERVAS" ->
                "No aplica";

            default -> "Total";
        };
    }

    // ETIQUETA DE CANTIDADES

    public String obtenerEtiquetaCantidad(String tipo) {

        tipo = validarTipo(tipo);

        return switch (tipo) {

            case "VENTAS", "PAGOS", "EMPLEADOS" ->
                "Pagos completados";

            case "PRODUCTOS" ->
                "Unidades vendidas";

            case "INVENTARIO" ->
                "Productos activos";

            case "RESERVAS" ->
                "Reservas registradas";

            case "PEDIDOS" ->
                "Pedidos registrados";

            default -> "Registros";
        };
    }

    // GENERAR RESUMEN DEL REPORTE

    public String generarResumen(Reporte reporte) {

        validarAcceso();

        if (reporte == null) {

            throw new IllegalArgumentException(
                    "Primero genera un reporte."
            );
        }

        StringBuilder texto = new StringBuilder();

        texto.append("MINOVA - BITS ANGUS BURGER\n");
        texto.append(reporte.getTitulo()).append("\n\n");

        texto.append("Generado por: ")
                .append(reporte.getGeneradoPor())
                .append("\n");

        texto.append("Fecha de generacion: ")
                .append(reporte.getFechaGeneracion()
                        .format(DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm:ss")))
                .append("\n");

        if (reporte.getFechaInicio() != null) {

            texto.append("Periodo: ")
                    .append(formatearFecha(
                            reporte.getFechaInicio()))
                    .append(" - ")
                    .append(formatearFecha(
                            reporte.getFechaFin()))
                    .append("\n");
        }

        texto.append("\n")
                .append(obtenerEtiquetaCantidad(
                        reporte.getTipo()))
                .append(": ")
                .append(reporte.getCantidadRegistros())
                .append("\n");

        if (tieneTotalMonetario(reporte.getTipo())) {

            texto.append(obtenerEtiquetaTotal(
                    reporte.getTipo()))
                    .append(": $ ")
                    .append(reporte.getTotal()
                            .toPlainString())
                    .append(" COP\n");
        }

        texto.append("\n")
                .append(reporte.getResumen())
                .append("\n");

        if (!reporte.tieneDatos()) {

            texto.append(
                    "\nNo hay registros para este reporte."
            );
        }

        return texto.toString();
    }

    // NOMBRE SUGERIDO DEL ARCHIVO

    public String obtenerNombreArchivo(Reporte reporte) {

        validarAcceso();

        if (reporte == null) {

            throw new IllegalArgumentException(
                    "Primero genera un reporte."
            );
        }

        String fecha = reporte.getFechaGeneracion()
                .format(DateTimeFormatter.ofPattern(
                        "yyyyMMdd_HHmmss"
                ));

        return "MINOVA_"
                + validarTipo(reporte.getTipo())
                + "_" + fecha + ".csv";
    }

    // PROTEGER Y FORMATEAR CELDAS CSV

    private String escaparCSV(Object valor) {

        if (valor == null) {
            return "\"\"";
        }

        String texto;

        if (valor instanceof BigDecimal decimal) {

            texto = decimal.toPlainString()
                    .replace(".", ",");

        } else if (valor instanceof LocalDate fecha) {

            texto = formatearFecha(fecha);

        } else {

            texto = valor.toString();

            // EVITAR FORMULAS INYECTADAS EN EXCEL

            if (valor instanceof String) {

                String limpio = texto.stripLeading();

                if (limpio.startsWith("=")
                        || limpio.startsWith("+")
                        || limpio.startsWith("-")
                        || limpio.startsWith("@")
                        || texto.startsWith("\t")
                        || texto.startsWith("\r")
                        || texto.startsWith("\n")) {

                    texto = "'" + texto;
                }
            }
        }

        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    // ESCRIBIR FILA CSV

    private void escribirFila(
            BufferedWriter escritor,
            Object[] valores) throws IOException {

        for (int i = 0; i < valores.length; i++) {

            if (i > 0) {
                escritor.write(";");
            }

            escritor.write(escaparCSV(valores[i]));
        }

        escritor.newLine();
    }

    // EXPORTAR REPORTE A CSV

    public Path exportarCSV(
            Reporte reporte,
            Path destino) throws IOException {

        validarAcceso();

        if (reporte == null) {

            throw new IllegalArgumentException(
                    "Primero genera un reporte."
            );
        }

        if (destino == null) {

            throw new IllegalArgumentException(
                    "Selecciona donde guardar el archivo."
            );
        }

        String nombre = destino.getFileName().toString();

        if (!nombre.toLowerCase(Locale.ROOT)
                .endsWith(".csv")) {

            destino = destino.resolveSibling(
                    nombre + ".csv"
            );
        }

        try (BufferedWriter escritor =
                Files.newBufferedWriter(
                        destino,
                        StandardCharsets.UTF_8
                )) {

            // BOM PARA RECONOCER TILDES EN EXCEL

            escritor.write('\uFEFF');

            escribirFila(
                    escritor,
                    reporte.getColumnas()
            );

            for (Object[] fila : reporte.getFilas()) {

                escribirFila(escritor, fila);
            }
        }

        return destino.toAbsolutePath();
    }
}

