/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import modelo.Reporte;
import modelo.Usuario;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReporteRepositorio {

    // VALIDAR PERMISOS DEL ADMINISTRADOR

    private void validarPermisos(
            Connection con,
            Usuario usuario) throws SQLException {

        if (usuario == null || usuario.getIdUsuario() <= 0) {

            throw new IllegalArgumentException(
                    "Debes iniciar sesion."
            );
        }

        String sql = """
            SELECT u.id_usuario
            FROM usuario u
            INNER JOIN roles r
                ON u.id_rol = r.id_rol
            WHERE u.id_usuario = ?
                AND u.estado = TRUE
                AND r.nombre_rol = 'ADMINISTRADOR'
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuario.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "Solo el administrador puede generar reportes."
                    );
                }
            }
        }
    }

    // VALIDAR RANGO DE FECHAS

    private void validarFechas(
            LocalDate inicio,
            LocalDate fin) {

        if (inicio == null || fin == null) {

            throw new IllegalArgumentException(
                    "Selecciona las fechas del reporte."
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
    }

    // CONFIGURAR PARAMETROS DE FECHA

    private void asignarFechas(
            PreparedStatement ps,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        ps.setTimestamp(
                1,
                Timestamp.valueOf(inicio.atStartOfDay())
        );

        ps.setTimestamp(
                2,
                Timestamp.valueOf(
                        fin.plusDays(1).atStartOfDay()
                )
        );
    }

    // GENERAR REPORTE SEGUN EL TIPO

    public Reporte generar(
            Usuario usuario,
            String tipo,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        if (tipo == null || !List.of(
                "VENTAS",
                "PRODUCTOS",
                "PAGOS",
                "EMPLEADOS",
                "INVENTARIO",
                "RESERVAS",
                "PEDIDOS"
        ).contains(tipo)) {

            throw new IllegalArgumentException(
                    "Tipo de reporte invalido."
            );
        }

        if (!"INVENTARIO".equals(tipo)) {
            validarFechas(inicio, fin);
        }

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            return switch (tipo) {

                case "VENTAS" ->
                    reporteVentas(con, usuario, inicio, fin);

                case "PRODUCTOS" ->
                    reporteProductos(con, usuario, inicio, fin);

                case "PAGOS" ->
                    reportePagos(con, usuario, inicio, fin);

                case "EMPLEADOS" ->
                    reporteEmpleados(con, usuario, inicio, fin);

                case "INVENTARIO" ->
                    reporteInventario(con, usuario);

                case "RESERVAS" ->
                    reporteReservas(con, usuario, inicio, fin);

                case "PEDIDOS" ->
                    reportePedidos(con, usuario, inicio, fin);

                default ->
                    throw new IllegalArgumentException(
                            "Reporte no disponible."
                    );
            };
        }
    }

    // 1. REPORTE DE VENTAS DIARIAS

    private Reporte reporteVentas(
            Connection con,
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        Reporte reporte = new Reporte(
                "VENTAS",
                "REPORTE DE VENTAS",
                inicio,
                fin,
                usuario.getNombreUsuario()
        );

        reporte.setColumnas(
                "Fecha",
                "Pagos completados",
                "Pedidos pagados",
                "Ingresos"
        );

        String sql = """
            SELECT DATE(pg.fecha_pago) AS fecha,
                   COUNT(*) AS cantidad_pagos,
                   COUNT(DISTINCT pg.id_pedido) AS pedidos,
                   SUM(pg.valor_pago) AS ingresos
            FROM pago pg
            WHERE pg.estado = 'COMPLETADO'
                AND pg.fecha_pago >= ?
                AND pg.fecha_pago < ?
            GROUP BY DATE(pg.fecha_pago)
            ORDER BY fecha
            """;

        BigDecimal total = BigDecimal.ZERO;
        long cantidad = 0;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            asignarFechas(ps, inicio, fin);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    BigDecimal ingresos =
                            rs.getBigDecimal("ingresos");

                    reporte.agregarFila(
                            rs.getDate("fecha").toLocalDate(),
                            rs.getLong("cantidad_pagos"),
                            rs.getLong("pedidos"),
                            ingresos
                    );

                    total = total.add(ingresos);

                    cantidad += rs.getLong("cantidad_pagos");
                }
            }
        }

        reporte.setTotal(total);
        reporte.setCantidadRegistros(cantidad);

        reporte.setResumen(
                "Ingresos reales correspondientes "
                + "a pagos completados durante el periodo."
        );

        return reporte;
    }

    // 2. REPORTE DE PRODUCTOS MAS VENDIDOS

    private Reporte reporteProductos(
            Connection con,
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        Reporte reporte = new Reporte(
                "PRODUCTOS",
                "PRODUCTOS MAS VENDIDOS",
                inicio,
                fin,
                usuario.getNombreUsuario()
        );

        reporte.setColumnas(
                "ID",
                "Producto",
                "Unidades vendidas",
                "Ingresos"
        );

        String sql = """
            SELECT pr.id_producto,
                   pr.nombre_producto,
                   SUM(d.cantidad) AS unidades,
                   SUM(d.subtotal) AS ingresos
            FROM detalle_pedido d
            INNER JOIN producto pr
                ON d.id_producto = pr.id_producto
            INNER JOIN pedido p
                ON d.id_pedido = p.id_pedido
            WHERE p.estado = 'ENTREGADO'
                AND EXISTS (
                    SELECT 1
                    FROM pago pg
                    WHERE pg.id_pedido = p.id_pedido
                        AND pg.estado = 'COMPLETADO'
                        AND pg.fecha_pago >= ?
                        AND pg.fecha_pago < ?
                )
            GROUP BY pr.id_producto, pr.nombre_producto
            ORDER BY unidades DESC, ingresos DESC
            """;

        BigDecimal total = BigDecimal.ZERO;
        long unidades = 0;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            asignarFechas(ps, inicio, fin);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    BigDecimal ingresos =
                            rs.getBigDecimal("ingresos");

                    reporte.agregarFila(
                            rs.getInt("id_producto"),
                            rs.getString("nombre_producto"),
                            rs.getLong("unidades"),
                            ingresos
                    );

                    total = total.add(ingresos);

                    unidades += rs.getLong("unidades");
                }
            }
        }

        reporte.setTotal(total);
        reporte.setCantidadRegistros(unidades);

        reporte.setResumen(
                "Productos vendidos en pedidos entregados "
                + "y pagados durante el periodo, "
                + "ordenados por unidades vendidas."
        );

        return reporte;
    }

    // 3. REPORTE POR METODO DE PAGO

    private Reporte reportePagos(
            Connection con,
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        Reporte reporte = new Reporte(
                "PAGOS",
                "INGRESOS POR METODO DE PAGO",
                inicio,
                fin,
                usuario.getNombreUsuario()
        );

        reporte.setColumnas(
                "Metodo de pago",
                "Transacciones",
                "Total recaudado"
        );

        String sql = """
            SELECT metodo_pago,
                   COUNT(*) AS cantidad,
                   SUM(valor_pago) AS total
            FROM pago
            WHERE estado = 'COMPLETADO'
                AND fecha_pago >= ?
                AND fecha_pago < ?
            GROUP BY metodo_pago
            ORDER BY total DESC
            """;

        BigDecimal total = BigDecimal.ZERO;
        long cantidad = 0;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            asignarFechas(ps, inicio, fin);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    BigDecimal valor =
                            rs.getBigDecimal("total");

                    reporte.agregarFila(
                            rs.getString("metodo_pago"),
                            rs.getLong("cantidad"),
                            valor
                    );

                    total = total.add(valor);

                    cantidad += rs.getLong("cantidad");
                }
            }
        }

        reporte.setTotal(total);
        reporte.setCantidadRegistros(cantidad);

        reporte.setResumen(
                "Distribucion de ingresos entre efectivo, "
                + "tarjeta y transferencia."
        );

        return reporte;
    }

    // 4. REPORTE DE RECAUDACION POR EMPLEADO

    private Reporte reporteEmpleados(
            Connection con,
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        Reporte reporte = new Reporte(
                "EMPLEADOS",
                "RECAUDACION POR EMPLEADO",
                inicio,
                fin,
                usuario.getNombreUsuario()
        );

        reporte.setColumnas(
                "ID",
                "Empleado",
                "Usuario",
                "Pagos registrados",
                "Total recaudado"
        );

        String sql = """
            SELECT u.id_usuario,
                   u.nombre_completo,
                   u.nombre_usuario,
                   COUNT(pg.id_pago) AS cantidad,
                   SUM(pg.valor_pago) AS total
            FROM pago pg
            INNER JOIN usuario u
                ON pg.id_usuario = u.id_usuario
            WHERE pg.estado = 'COMPLETADO'
                AND pg.fecha_pago >= ?
                AND pg.fecha_pago < ?
            GROUP BY u.id_usuario,
                     u.nombre_completo,
                     u.nombre_usuario
            ORDER BY total DESC
            """;

        BigDecimal total = BigDecimal.ZERO;
        long cantidad = 0;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            asignarFechas(ps, inicio, fin);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    BigDecimal valor =
                            rs.getBigDecimal("total");

                    reporte.agregarFila(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_completo"),
                            rs.getString("nombre_usuario"),
                            rs.getLong("cantidad"),
                            valor
                    );

                    total = total.add(valor);

                    cantidad += rs.getLong("cantidad");
                }
            }
        }

        reporte.setTotal(total);
        reporte.setCantidadRegistros(cantidad);

        reporte.setResumen(
                "Pagos recibidos por cada empleado "
                + "durante el periodo seleccionado."
        );

        return reporte;
    }

    // 5. REPORTE DE INVENTARIO ACTUAL

    private Reporte reporteInventario(
            Connection con,
            Usuario usuario) throws SQLException {

        Reporte reporte = new Reporte(
                "INVENTARIO",
                "REPORTE DE INVENTARIO ACTUAL",
                null,
                null,
                usuario.getNombreUsuario()
        );

        reporte.setColumnas(
                "ID",
                "Producto",
                "Stock",
                "Minimo",
                "Maximo",
                "Estado",
                "Precio unitario",
                "Valor a precio de venta"
        );

        String sql = """
            SELECT p.id_producto,
                   p.nombre_producto,
                   p.stock,
                   COALESCE(i.stock_minimo, 5) AS minimo,
                   COALESCE(i.stock_maximo, 100) AS maximo,
                   p.precio_producto,
                   p.stock * p.precio_producto AS valor,
                   CASE
                       WHEN p.stock = 0
                           THEN 'AGOTADO'
                       WHEN i.id_inventario IS NULL
                           THEN 'SIN CONFIGURAR'
                       WHEN p.stock <= i.stock_minimo
                           THEN 'STOCK BAJO'
                       WHEN p.stock > i.stock_maximo
                           THEN 'EXCESO'
                       ELSE 'NORMAL'
                   END AS estado_stock
            FROM producto p
            LEFT JOIN inventario i
                ON p.id_producto = i.id_producto
            WHERE p.estado = TRUE
            ORDER BY p.stock ASC, p.nombre_producto
            """;

        BigDecimal total = BigDecimal.ZERO;

        long cantidad = 0;
        long alertas = 0;
        long unidades = 0;

        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                BigDecimal valor =
                        rs.getBigDecimal("valor");

                String estado =
                        rs.getString("estado_stock");

                reporte.agregarFila(
                        rs.getInt("id_producto"),
                        rs.getString("nombre_producto"),
                        rs.getInt("stock"),
                        rs.getInt("minimo"),
                        rs.getInt("maximo"),
                        estado,
                        rs.getBigDecimal("precio_producto"),
                        valor
                );

                total = total.add(valor);

                unidades += rs.getLong("stock");

                cantidad++;

                if (!"NORMAL".equals(estado)) {
                    alertas++;
                }
            }
        }

        reporte.setTotal(total);
        reporte.setCantidadRegistros(cantidad);

        reporte.setResumen(
                "Productos activos: " + cantidad
                + " | Unidades disponibles: " + unidades
                + " | Productos con alertas: " + alertas
                + " | Valor estimado a precio de venta."
        );

        return reporte;
    }

    // 6. REPORTE DE RESERVAS

    private Reporte reporteReservas(
            Connection con,
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        Reporte reporte = new Reporte(
                "RESERVAS",
                "REPORTE DE RESERVAS",
                inicio,
                fin,
                usuario.getNombreUsuario()
        );

        reporte.setColumnas(
                "ID",
                "Mesa",
                "Cliente",
                "Fecha",
                "Hora",
                "Personas",
                "Estado",
                "Empleado"
        );

        String sql = """
            SELECT r.id_reserva,
                   m.numero_mesa,
                   c.nombre_cliente,
                   r.fecha_reserva,
                   r.hora_reserva,
                   r.cantidad_personas,
                   r.estado,
                   u.nombre_usuario
            FROM reserva r
            INNER JOIN mesa m
                ON r.id_mesa = m.id_mesa
            INNER JOIN cliente c
                ON r.id_cliente = c.id_cliente
            INNER JOIN usuario u
                ON r.id_usuario = u.id_usuario
            WHERE r.fecha_reserva >= ?
                AND r.fecha_reserva <= ?
            ORDER BY r.fecha_reserva, r.hora_reserva
            """;

        long cantidad = 0;
        long pendientes = 0;
        long confirmadas = 0;
        long canceladas = 0;
        long finalizadas = 0;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String estado = rs.getString("estado");

                    reporte.agregarFila(
                            rs.getInt("id_reserva"),
                            rs.getInt("numero_mesa"),
                            rs.getString("nombre_cliente"),
                            rs.getDate("fecha_reserva")
                                    .toLocalDate(),
                            rs.getTime("hora_reserva")
                                    .toLocalTime(),
                            rs.getInt("cantidad_personas"),
                            estado,
                            rs.getString("nombre_usuario")
                    );

                    cantidad++;

                    switch (estado) {

                        case "PENDIENTE" -> pendientes++;

                        case "CONFIRMADA" -> confirmadas++;

                        case "CANCELADA" -> canceladas++;

                        case "FINALIZADA" -> finalizadas++;
                    }
                }
            }
        }

        reporte.setCantidadRegistros(cantidad);

        reporte.setResumen(
                "Total: " + cantidad
                + " | Pendientes: " + pendientes
                + " | Confirmadas: " + confirmadas
                + " | Canceladas: " + canceladas
                + " | Finalizadas: " + finalizadas
        );

        return reporte;
    }

    // 7. REPORTE DE PEDIDOS POR ESTADO

    private Reporte reportePedidos(
            Connection con,
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin) throws SQLException {

        Reporte reporte = new Reporte(
                "PEDIDOS",
                "REPORTE DE PEDIDOS POR ESTADO",
                inicio,
                fin,
                usuario.getNombreUsuario()
        );

        reporte.setColumnas(
                "Estado",
                "Cantidad de pedidos",
                "Valor de pedidos"
        );

        String sql = """
            SELECT p.estado,
                   COUNT(*) AS cantidad,
                   SUM(
                       COALESCE((
                           SELECT SUM(d.subtotal)
                           FROM detalle_pedido d
                           WHERE d.id_pedido = p.id_pedido
                       ), 0)
                   ) AS total
            FROM pedido p
            WHERE p.fecha_pedido >= ?
                AND p.fecha_pedido < ?
            GROUP BY p.estado
            ORDER BY cantidad DESC
            """;

        long cantidad = 0;
        long cancelados = 0;

        BigDecimal total = BigDecimal.ZERO;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            asignarFechas(ps, inicio, fin);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String estado = rs.getString("estado");

                    long numero = rs.getLong("cantidad");

                    BigDecimal valor =
                            rs.getBigDecimal("total");

                    reporte.agregarFila(
                            estado,
                            numero,
                            valor
                    );

                    cantidad += numero;

                    if ("CANCELADO".equals(estado)) {

                        cancelados += numero;

                    } else {

                        total = total.add(valor);
                    }
                }
            }
        }

        reporte.setTotal(total);
        reporte.setCantidadRegistros(cantidad);

        reporte.setResumen(
                "Pedidos registrados: " + cantidad
                + " | Cancelados: " + cancelados
                + " | El total excluye pedidos cancelados "
                + "y no representa necesariamente dinero recaudado."
        );

        return reporte;
    }
}

