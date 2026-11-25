/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import modelo.Pago;
import modelo.Pedido;
import modelo.Usuario;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PagoRepositorio {

    // VALIDAR PERMISOS

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
                AND r.nombre_rol IN (
                    'ADMINISTRADOR',
                    'CAJERO'
                )
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuario.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "No tienes permisos para gestionar pagos."
                    );
                }
            }
        }
    }

    // CONSULTAR TOTAL REAL DEL PEDIDO

    private BigDecimal obtenerTotal(
            Connection con,
            int idPedido) throws SQLException {

        String sql = """
            SELECT COALESCE(SUM(subtotal), 0) AS total
            FROM detalle_pedido
            WHERE id_pedido = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    // CONSULTAR MESA DEL PEDIDO

    private Integer obtenerMesa(
            Connection con,
            int idPedido) throws SQLException {

        String sql = """
            SELECT id_mesa
            FROM pedido
            WHERE id_pedido = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getObject("id_mesa", Integer.class);
                }
            }
        }

        throw new IllegalArgumentException(
                "El pedido no existe."
        );
    }

    // BLOQUEAR MESA DURANTE LA TRANSACCION

    private void bloquearMesa(
            Connection con,
            Integer idMesa) throws SQLException {

        if (idMesa == null) {
            return;
        }

        String sql = """
            SELECT id_mesa
            FROM mesa
            WHERE id_mesa = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException("La mesa no existe.");
                }
            }
        }
    }

    // VALIDAR ESTADO DEL PEDIDO

    private void validarPedido(
            Connection con,
            int idPedido) throws SQLException {

        String sql = """
            SELECT estado
            FROM pedido
            WHERE id_pedido = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "El pedido no existe."
                    );
                }

                if (!"ENTREGADO".equals(rs.getString("estado"))) {

                    throw new IllegalArgumentException(
                            "Solo puedes pagar pedidos entregados."
                    );
                }
            }
        }
    }

    // EVITAR PAGOS DUPLICADOS

    private void validarPagoDuplicado(
            Connection con,
            int idPedido) throws SQLException {

        String sql = """
            SELECT id_pago
            FROM pago
            WHERE id_pedido = ?
                AND estado IN ('COMPLETADO', 'PENDIENTE')
            FOR UPDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    throw new IllegalArgumentException(
                            "El pedido ya tiene un pago registrado."
                    );
                }
            }
        }
    }

    // REGISTRAR PAGO COMPLETO

    public int registrar(
            Usuario usuario,
            Pago pago) throws SQLException {

        if (pago == null || pago.getIdPedido() <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona un pedido valido."
            );
        }

        if (pago.getMetodoPago() == null
                || !List.of(
                    "EFECTIVO",
                    "TARJETA",
                    "TRANSFERENCIA"
                ).contains(pago.getMetodoPago())) {

            throw new IllegalArgumentException(
                    "Metodo de pago invalido."
            );
        }

        if (pago.getValorPago() == null
                || pago.getValorPago().signum() <= 0) {

            throw new IllegalArgumentException(
                    "El valor del pago debe ser mayor que cero."
            );
        }

        String referencia = pago.getReferenciaPago();

        if (referencia != null) {

            referencia = referencia.trim();

            if (referencia.length() > 100) {
                throw new IllegalArgumentException(
                        "La referencia no puede superar 100 caracteres."
                );
            }

            if (referencia.isEmpty()) {
                referencia = null;
            }
        }

        if (!"EFECTIVO".equals(pago.getMetodoPago())
                && referencia == null) {

            throw new IllegalArgumentException(
                    "Ingresa la referencia de la transaccion."
            );
        }

        try (Connection con = ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario);

                Integer idMesa = obtenerMesa(
                        con, pago.getIdPedido()
                );

                bloquearMesa(con, idMesa);

                validarPedido(con, pago.getIdPedido());

                validarPagoDuplicado(
                        con, pago.getIdPedido()
                );

                BigDecimal total = obtenerTotal(
                        con, pago.getIdPedido()
                );

                if (total.signum() <= 0) {

                    throw new IllegalArgumentException(
                            "El pedido no tiene un total valido."
                    );
                }

                if (pago.getValorPago().compareTo(total) != 0) {

                    throw new IllegalArgumentException(
                            "El pago debe coincidir con el total del pedido."
                    );
                }

                String sql = """
                    INSERT INTO pago (
                        id_pedido,
                        id_usuario,
                        metodo_pago,
                        valor_pago,
                        estado,
                        referencia_pago
                    )
                    VALUES (?, ?, ?, ?, 'COMPLETADO', ?)
                    """;

                int idPago;

                try (PreparedStatement ps = con.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, pago.getIdPedido());
                    ps.setInt(2, usuario.getIdUsuario());
                    ps.setString(3, pago.getMetodoPago());
                    ps.setBigDecimal(4, total);
                    ps.setString(5, referencia);

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo obtener el ID del pago."
                            );
                        }

                        idPago = rs.getInt(1);
                    }
                }

                liberarMesaSiCorresponde(con, idMesa);

                con.commit();

                pago.setIdPago(idPago);
                pago.setIdUsuario(usuario.getIdUsuario());
                pago.setEstado("COMPLETADO");

                return idPago;

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }

    // LIBERAR MESA CUANDO NO EXISTAN PEDIDOS ABIERTOS

    private void liberarMesaSiCorresponde(
            Connection con,
            Integer idMesa) throws SQLException {

        if (idMesa == null) {
            return;
        }

        String consulta = """
            SELECT p.id_pedido
            FROM pedido p
            WHERE p.id_mesa = ?
                AND p.estado <> 'CANCELADO'
                AND (
                    p.estado <> 'ENTREGADO'
                    OR NOT EXISTS (
                        SELECT 1
                        FROM pago pg
                        WHERE pg.id_pedido = p.id_pedido
                            AND pg.estado = 'COMPLETADO'
                            AND pg.valor_pago = (
                                SELECT COALESCE(SUM(d.subtotal), 0)
                                FROM detalle_pedido d
                                WHERE d.id_pedido = p.id_pedido
                            )
                    )
                )
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(consulta)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return;
                }
            }
        }

        String actualizar = """
            UPDATE mesa
            SET estado = 'DISPONIBLE'
            WHERE id_mesa = ?
                AND estado = 'OCUPADA'
            """;

        try (PreparedStatement ps =
                con.prepareStatement(actualizar)) {

            ps.setInt(1, idMesa);
            ps.executeUpdate();
        }
    }

    // LISTAR PEDIDOS PENDIENTES DE PAGO

    public ArrayList<Pedido> listarPedidosPendientes(
            Usuario usuario) throws SQLException {

        ArrayList<Pedido> pedidos = new ArrayList<>();

        String sql = """
            SELECT p.id_pedido,
                   p.id_cliente,
                   p.id_mesa,
                   p.id_usuario,
                   COALESCE(c.nombre_cliente,
                            'Consumidor final') AS cliente,
                   u.nombre_usuario,
                   p.fecha_pedido,
                   p.tipo_pedido,
                   p.estado,
                   COALESCE((
                       SELECT SUM(d.subtotal)
                       FROM detalle_pedido d
                       WHERE d.id_pedido = p.id_pedido
                   ), 0) AS total
            FROM pedido p
            LEFT JOIN cliente c
                ON p.id_cliente = c.id_cliente
            INNER JOIN usuario u
                ON p.id_usuario = u.id_usuario
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
            ORDER BY p.id_pedido DESC
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    pedidos.add(new Pedido(
                            rs.getInt("id_pedido"),
                            rs.getObject("id_cliente", Integer.class),
                            rs.getObject("id_mesa", Integer.class),
                            rs.getInt("id_usuario"),
                            rs.getString("cliente"),
                            rs.getString("nombre_usuario"),
                            rs.getTimestamp("fecha_pedido")
                                    .toLocalDateTime(),
                            rs.getString("tipo_pedido"),
                            rs.getString("estado"),
                            rs.getBigDecimal("total")
                    ));
                }
            }
        }

        return pedidos;
    }

    // CONSULTA GENERAL DE PAGOS

    private static final String CONSULTA = """
        SELECT pg.id_pago,
               pg.id_pedido,
               pg.id_usuario,
               u.nombre_usuario,
               COALESCE(c.nombre_cliente,
                        'Consumidor final') AS cliente,
               pg.metodo_pago,
               pg.valor_pago,
               pg.fecha_pago,
               pg.estado,
               pg.referencia_pago
        FROM pago pg
        INNER JOIN pedido p
            ON pg.id_pedido = p.id_pedido
        INNER JOIN usuario u
            ON pg.id_usuario = u.id_usuario
        LEFT JOIN cliente c
            ON p.id_cliente = c.id_cliente
        """;

    // CONVERTIR RESULTADO MYSQL A OBJETO

    private Pago convertirPago(
            ResultSet rs) throws SQLException {

        return new Pago(
                rs.getInt("id_pago"),
                rs.getInt("id_pedido"),
                rs.getInt("id_usuario"),
                rs.getString("nombre_usuario"),
                rs.getString("cliente"),
                rs.getString("metodo_pago"),
                rs.getBigDecimal("valor_pago"),
                rs.getTimestamp("fecha_pago")
                        .toLocalDateTime(),
                rs.getString("estado"),
                rs.getString("referencia_pago")
        );
    }

    // LISTAR PAGOS REGISTRADOS

    public ArrayList<Pago> listar(
            Usuario usuario) throws SQLException {

        ArrayList<Pago> pagos = new ArrayList<>();

        String sql = CONSULTA
                + " ORDER BY pg.id_pago DESC";

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    pagos.add(convertirPago(rs));
                }
            }
        }

        return pagos;
    }

    // BUSCAR PAGO POR ID

    public Pago buscarPorId(
            Usuario usuario,
            int idPago) throws SQLException {

        String sql = CONSULTA
                + " WHERE pg.id_pago = ?";

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setInt(1, idPago);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return convertirPago(rs);
                    }
                }
            }
        }

        return null;
    }

    // TOTAL RECAUDADO

    public BigDecimal totalRecaudado(
            Usuario usuario) throws SQLException {

        String sql = """
            SELECT COALESCE(SUM(valor_pago), 0) AS total
            FROM pago
            WHERE estado = 'COMPLETADO'
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }

        return BigDecimal.ZERO;
    }

    // CANTIDAD DE PAGOS COMPLETADOS

    public int cantidadPagos(
            Usuario usuario) throws SQLException {

        String sql = """
            SELECT COUNT(*) AS cantidad
            FROM pago
            WHERE estado = 'COMPLETADO'
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("cantidad");
                }
            }
        }

        return 0;
    }
}
