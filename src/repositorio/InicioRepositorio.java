/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import modelo.Usuario;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class InicioRepositorio {

    private static final ZoneId ZONA =
            ZoneId.of("America/Bogota");

    // VALIDAR USUARIO Y CONSULTAR ROL REAL

    private String consultarRol(
            Connection con,
            Usuario usuario) throws SQLException {

        if (usuario == null || usuario.getIdUsuario() <= 0) {

            throw new IllegalArgumentException(
                    "Debes iniciar sesion."
            );
        }

        String sql = """
            SELECT r.nombre_rol
            FROM usuario u
            INNER JOIN roles r
                ON u.id_rol = r.id_rol
            WHERE u.id_usuario = ?
                AND u.estado = TRUE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, usuario.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String rol = rs.getString("nombre_rol");

                    if (Set.of(
                            "ADMINISTRADOR",
                            "CAJERO",
                            "MESERO",
                            "INVENTARIO"
                    ).contains(rol)) {

                        return rol;
                    }
                }
            }
        }

        throw new IllegalArgumentException(
                "Usuario inactivo o sin permisos."
        );
    }

    // CONSULTAR CANTIDADES

    private long consultarCantidad(
            Connection con,
            String sql) throws SQLException {

        try (PreparedStatement ps =
                con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        }

        return 0;
    }

    // CONSULTAR INGRESOS DE HOY

    private BigDecimal ingresosHoy(
            Connection con) throws SQLException {

        LocalDate hoy = LocalDate.now(ZONA);

        String sql = """
            SELECT COALESCE(SUM(valor_pago), 0)
            FROM pago
            WHERE estado = 'COMPLETADO'
                AND fecha_pago >= ?
                AND fecha_pago < ?
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setTimestamp(
                    1,
                    Timestamp.valueOf(hoy.atStartOfDay())
            );

            ps.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            hoy.plusDays(1).atStartOfDay()
                    )
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    // CONSULTAR RESERVAS ACTIVAS DE HOY

    private long reservasHoy(
            Connection con) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM reserva
            WHERE fecha_reserva = ?
                AND estado IN (
                    'PENDIENTE',
                    'CONFIRMADA'
                )
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setDate(
                    1,
                    Date.valueOf(LocalDate.now(ZONA))
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }

    // CONSULTAR ESTADISTICAS SEGUN EL ROL

    public Map<String, Object> obtenerResumen(
            Usuario usuario) throws SQLException {

        Map<String, Object> resumen = new HashMap<>();

        try (Connection con = ConexionMySQL.conectar()) {

            String rol = consultarRol(con, usuario);

            boolean admin =
                    "ADMINISTRADOR".equals(rol);

            boolean cajero =
                    "CAJERO".equals(rol);

            boolean mesero =
                    "MESERO".equals(rol);

            boolean inventario =
                    "INVENTARIO".equals(rol);

            // ESTADISTICAS FINANCIERAS

            if (admin || cajero) {

                resumen.put(
                        "ingresosHoy",
                        ingresosHoy(con)
                );

                resumen.put(
                        "pendientesPago",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM pedido p
                            WHERE p.estado = 'ENTREGADO'
                                AND NOT EXISTS (
                                    SELECT 1
                                    FROM pago pg
                                    WHERE pg.id_pedido = p.id_pedido
                                        AND pg.estado IN (
                                            'COMPLETADO',
                                            'PENDIENTE'
                                        )
                                )
                            """)
                );
            }

            // PEDIDOS ACTIVOS

            if (admin || cajero || mesero) {

                resumen.put(
                        "pedidosActivos",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM pedido
                            WHERE estado IN (
                                'PENDIENTE',
                                'EN_PREPARACION',
                                'LISTO'
                            )
                            """)
                );

                // MESAS DISPONIBLES

                resumen.put(
                        "mesasDisponibles",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM mesa
                            WHERE estado = 'DISPONIBLE'
                            """)
                );

                resumen.put(
                        "mesasTotales",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM mesa
                            """)
                );

                // MESAS OCUPADAS

                resumen.put(
                        "mesasOcupadas",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM mesa
                            WHERE estado = 'OCUPADA'
                            """)
                );
            }

            // RESERVAS

            if (admin || mesero) {

                resumen.put(
                        "reservasHoy",
                        reservasHoy(con)
                );
            }

            // PRODUCTOS ACTIVOS

            resumen.put(
                    "productosActivos",
                    consultarCantidad(con, """
                        SELECT COUNT(*)
                        FROM producto
                        WHERE estado = TRUE
                        """)
            );

            // ESTADISTICAS DE INVENTARIO

            if (admin || inventario) {

                resumen.put(
                        "totalUnidades",
                        consultarCantidad(con, """
                            SELECT COALESCE(SUM(stock), 0)
                            FROM producto
                            WHERE estado = TRUE
                            """)
                );

                resumen.put(
                        "alertasInventario",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM producto p
                            LEFT JOIN inventario i
                                ON p.id_producto = i.id_producto
                            WHERE p.estado = TRUE
                                AND (
                                    p.stock = 0
                                    OR i.id_inventario IS NULL
                                    OR p.stock <= i.stock_minimo
                                    OR p.stock > i.stock_maximo
                                )
                            """)
                );

                resumen.put(
                        "productosAgotados",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM producto
                            WHERE estado = TRUE
                                AND stock = 0
                            """)
                );
            }

            // ESTADISTICAS ADMINISTRATIVAS

            if (admin) {

                resumen.put(
                        "clientesActivos",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM cliente
                            WHERE estado = TRUE
                            """)
                );

                resumen.put(
                        "empleadosActivos",
                        consultarCantidad(con, """
                            SELECT COUNT(*)
                            FROM usuario
                            WHERE estado = TRUE
                            """)
                );
            }
        }

        return resumen;
    }

    // INGRESOS DE LOS ULTIMOS 7 DIAS

    public Map<LocalDate, BigDecimal> ventasSemanales(
            Usuario usuario) throws SQLException {

        Map<LocalDate, BigDecimal> ventas =
                new LinkedHashMap<>();

        LocalDate hoy = LocalDate.now(ZONA);

        LocalDate inicio = hoy.minusDays(6);

        // INCLUIR LOS DIAS SIN VENTAS

        for (int i = 0; i < 7; i++) {

            ventas.put(
                    inicio.plusDays(i),
                    BigDecimal.ZERO
            );
        }

        String sql = """
            SELECT DATE(fecha_pago) AS fecha,
                   SUM(valor_pago) AS total
            FROM pago
            WHERE estado = 'COMPLETADO'
                AND fecha_pago >= ?
                AND fecha_pago < ?
            GROUP BY DATE(fecha_pago)
            ORDER BY fecha
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            String rol = consultarRol(con, usuario);

            if (!"ADMINISTRADOR".equals(rol)) {

                throw new IllegalArgumentException(
                        "No tienes permisos para consultar "
                        + "las ventas semanales."
                );
            }

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setTimestamp(
                        1,
                        Timestamp.valueOf(
                                inicio.atStartOfDay()
                        )
                );

                ps.setTimestamp(
                        2,
                        Timestamp.valueOf(
                                hoy.plusDays(1).atStartOfDay()
                        )
                );

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        LocalDate fecha =
                                rs.getDate("fecha")
                                        .toLocalDate();

                        BigDecimal total =
                                rs.getBigDecimal("total");

                        ventas.put(fecha, total);
                    }
                }
            }
        }

        return ventas;
    }
}
