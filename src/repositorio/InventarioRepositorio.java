/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import java.sql.*;
import java.util.ArrayList;
import modelo.MovimientoInventario;
import modelo.Usuario;

public class InventarioRepositorio {

    // VALIDAR EMPLEADO Y ROL EN MYSQL

    private void validarPermisos(Connection con,
                                 Usuario usuario)
            throws SQLException {

        if (usuario == null
                || usuario.getIdUsuario() <= 0) {

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
              AND r.nombre_rol IN
                  ('ADMINISTRADOR', 'INVENTARIO')
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, usuario.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "No tienes permisos de inventario."
                    );
                }
            }
        }
    }

    // REGISTRAR ENTRADA O SALIDA

    public int registrarMovimiento(
            Usuario usuario,
            int idProducto,
            String tipo,
            int cantidad,
            String motivo) throws SQLException {

        if (idProducto <= 0 || cantidad <= 0) {

            throw new IllegalArgumentException(
                    "Producto o cantidad no validos."
            );
        }

        if (!"ENTRADA".equals(tipo)
                && !"SALIDA".equals(tipo)) {

            throw new IllegalArgumentException(
                    "Tipo de movimiento no valido."
            );
        }

        if (motivo == null
                || motivo.isBlank()
                || motivo.trim().length() > 255) {

            throw new IllegalArgumentException(
                    "Ingresa un motivo de hasta 255 caracteres."
            );
        }

        String sqlConsultar = """
            SELECT stock
            FROM producto
            WHERE id_producto = ?
              AND estado = TRUE
            FOR UPDATE
            """;

        String sqlActualizar = """
            UPDATE producto
            SET stock = ?
            WHERE id_producto = ?
            """;

        String sqlMovimiento = """
            INSERT INTO movimiento_inventario
            (id_producto, id_usuario,
             tipo_movimiento, cantidad, motivo)
            VALUES (?, ?, ?, ?, ?)
            """;

        String sqlInventario = """
            UPDATE inventario
            SET ultima_actualizacion = CURRENT_TIMESTAMP
            WHERE id_producto = ?
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario);

                int stockActual;

                // Bloquear producto durante la transaccion

                try (PreparedStatement ps =
                        con.prepareStatement(sqlConsultar)) {

                    ps.setInt(1, idProducto);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                    "El producto no existe "
                                    + "o esta desactivado."
                            );
                        }

                        stockActual = rs.getInt("stock");
                    }
                }

                // CALCULAR NUEVO STOCK

                long resultado;

                if ("ENTRADA".equals(tipo)) {

                    resultado = (long) stockActual + cantidad;

                } else {

                    resultado = (long) stockActual - cantidad;
                }

                if (resultado < 0) {

                    throw new IllegalArgumentException(
                            "Stock insuficiente. Solo hay "
                            + stockActual + " unidades."
                    );
                }

                if (resultado > Integer.MAX_VALUE) {

                    throw new IllegalArgumentException(
                            "La cantidad supera el limite permitido."
                    );
                }

                int nuevoStock = (int) resultado;

                // ACTUALIZAR EXISTENCIAS

                try (PreparedStatement ps =
                        con.prepareStatement(sqlActualizar)) {

                    ps.setInt(1, nuevoStock);
                    ps.setInt(2, idProducto);

                    if (ps.executeUpdate() != 1) {

                        throw new SQLException(
                                "No se pudo actualizar el stock."
                        );
                    }
                }

                // REGISTRAR HISTORIAL

                try (PreparedStatement ps =
                        con.prepareStatement(sqlMovimiento)) {

                    ps.setInt(1, idProducto);
                    ps.setInt(2, usuario.getIdUsuario());
                    ps.setString(3, tipo);
                    ps.setInt(4, cantidad);
                    ps.setString(5, motivo.trim());

                    ps.executeUpdate();
                }

                // ACTUALIZAR FECHA DEL INVENTARIO

                try (PreparedStatement ps =
                        con.prepareStatement(sqlInventario)) {

                    ps.setInt(1, idProducto);
                    ps.executeUpdate();
                }

                // GUARDAR TODOS LOS CAMBIOS

                con.commit();

                return nuevoStock;

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }

    // CONSULTAR HISTORIAL DE MOVIMIENTOS

    public ArrayList<MovimientoInventario> listarMovimientos(
            Usuario usuario) throws SQLException {

        ArrayList<MovimientoInventario> movimientos =
                new ArrayList<>();

        String sql = """
            SELECT m.id_movimiento,
                   m.id_producto,
                   m.id_usuario,
                   p.nombre_producto,
                   u.nombre_usuario,
                   m.tipo_movimiento,
                   m.cantidad,
                   m.motivo,
                   m.fecha_movimiento
            FROM movimiento_inventario m
            INNER JOIN producto p
                ON m.id_producto = p.id_producto
            INNER JOIN usuario u
                ON m.id_usuario = u.id_usuario
            ORDER BY m.fecha_movimiento DESC,
                     m.id_movimiento DESC
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    MovimientoInventario movimiento =
                            new MovimientoInventario(
                                    rs.getInt("id_movimiento"),
                                    rs.getInt("id_producto"),
                                    rs.getInt("id_usuario"),
                                    rs.getString("nombre_producto"),
                                    rs.getString("nombre_usuario"),
                                    rs.getString("tipo_movimiento"),
                                    rs.getInt("cantidad"),
                                    rs.getString("motivo"),
                                    rs.getTimestamp("fecha_movimiento")
                                            .toLocalDateTime()
                            );

                    movimientos.add(movimiento);
                }
            }
        }

        return movimientos;
    }
}
