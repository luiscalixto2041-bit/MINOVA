
package repositorio;

import modelo.Pedido;
import modelo.DetallePedido;
import modelo.Usuario;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PedidoRepositorio {

    // VALIDAR PERMISOS DEL EMPLEADO

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
                    'CAJERO',
                    'MESERO'
                )
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, usuario.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "No tienes permisos para gestionar pedidos."
                    );
                }
            }
        }
    }

    // REGISTRAR PEDIDO COMPLETO

    public int registrar(
            Usuario usuario,
            Pedido pedido) throws SQLException {

        if (pedido == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "Agrega al menos un producto."
            );
        }

        String tipo = pedido.getTipoPedido();

        if (tipo == null || !List.of(
                "LOCAL",
                "PARA_LLEVAR",
                "DOMICILIO"
        ).contains(tipo)) {

            throw new IllegalArgumentException(
                    "Tipo de pedido invalido."
            );
        }

        if ("LOCAL".equals(tipo)
                && (pedido.getIdMesa() == null
                || pedido.getIdMesa() <= 0)) {

            throw new IllegalArgumentException(
                    "Selecciona una mesa."
            );
        }

        if (!"LOCAL".equals(tipo)
                && pedido.getIdMesa() != null) {

            throw new IllegalArgumentException(
                    "Este tipo de pedido no requiere mesa."
            );
        }

        if (pedido.getIdCliente() != null
                && pedido.getIdCliente() <= 0) {

            throw new IllegalArgumentException(
                    "El cliente seleccionado no es valido."
            );
        }

        // AGRUPAR PRODUCTOS REPETIDOS

        Map<Integer, Integer> cantidades = new TreeMap<>();

        for (DetallePedido detalle : pedido.getDetalles()) {

            if (detalle == null
                    || detalle.getIdProducto() <= 0
                    || detalle.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "Producto o cantidad invalida."
                );
            }

            cantidades.merge(
                    detalle.getIdProducto(),
                    detalle.getCantidad(),
                    Math::addExact
            );
        }

        try (Connection con = ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario);

                validarCliente(con, pedido.getIdCliente());
                validarMesa(con, pedido.getIdMesa());

                int idPedido = insertarPedido(
                        con, usuario, pedido
                );

                for (Map.Entry<Integer, Integer> item
                        : cantidades.entrySet()) {

                    registrarDetalle(
                            con,
                            idPedido,
                            usuario.getIdUsuario(),
                            item.getKey(),
                            item.getValue()
                    );
                }

                if (pedido.getIdMesa() != null) {
                    ocuparMesa(con, pedido.getIdMesa());
                }

                con.commit();

                pedido.setIdPedido(idPedido);

                return idPedido;

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }

    // VALIDAR CLIENTE

    private void validarCliente(
            Connection con,
            Integer idCliente) throws SQLException {

        if (idCliente == null) {
            return;
        }

        String sql = """
            SELECT id_cliente
            FROM cliente
            WHERE id_cliente = ?
                AND estado = TRUE
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "El cliente no existe o esta inactivo."
                    );
                }
            }
        }
    }

    // VALIDAR MESA

    private void validarMesa(
            Connection con,
            Integer idMesa) throws SQLException {

        if (idMesa == null) {
            return;
        }

        String sql = """
            SELECT estado
            FROM mesa
            WHERE id_mesa = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "La mesa no existe."
                    );
                }

                String estado = rs.getString("estado");

                if ("MANTENIMIENTO".equals(estado)
                        || "RESERVADA".equals(estado)) {

                    throw new IllegalArgumentException(
                            "La mesa no esta disponible para este pedido."
                    );
                }
            }
        }

        comprobarReservas(con, idMesa);
    }

    // COMPROBAR RESERVAS VIGENTES Y PROXIMAS

    private void comprobarReservas(
            Connection con,
            int idMesa) throws SQLException {

        LocalDateTime ahora = LocalDateTime.now(
                ZoneId.of("America/Bogota")
        );

        String sql = """
            SELECT id_reserva
            FROM reserva
            WHERE id_mesa = ?
                AND estado IN (
                    'PENDIENTE',
                    'CONFIRMADA'
                )
                AND TIMESTAMP(
                    fecha_reserva,
                    hora_reserva
                ) < ?
                AND DATE_ADD(
                    TIMESTAMP(
                        fecha_reserva,
                        hora_reserva
                    ),
                    INTERVAL 120 MINUTE
                ) > ?
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);
            ps.setObject(2, ahora.plusMinutes(120));
            ps.setObject(3, ahora);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    throw new IllegalArgumentException(
                            "La mesa tiene una reserva vigente o proxima."
                    );
                }
            }
        }
    }

    // OCUPAR MESA

    private void ocuparMesa(
            Connection con,
            int idMesa) throws SQLException {

        String sql = """
            UPDATE mesa
            SET estado = 'OCUPADA'
            WHERE id_mesa = ?
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);
            ps.executeUpdate();
        }
    }

    // BLOQUEAR MESA DEL PEDIDO

    private Integer bloquearMesaDelPedido(
            Connection con,
            int idPedido) throws SQLException {

        Integer idMesa;

        String consulta = """
            SELECT id_mesa
            FROM pedido
            WHERE id_pedido = ?
            """;

        try (PreparedStatement ps =
                con.prepareStatement(consulta)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "El pedido no existe."
                    );
                }

                idMesa = rs.getObject(
                        "id_mesa",
                        Integer.class
                );
            }
        }

        if (idMesa != null) {

            String bloquear = """
                SELECT id_mesa
                FROM mesa
                WHERE id_mesa = ?
                FOR UPDATE
                """;

            try (PreparedStatement ps =
                    con.prepareStatement(bloquear)) {

                ps.setInt(1, idMesa);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {

                        throw new SQLException(
                                "La mesa no existe."
                        );
                    }
                }
            }
        }

        return idMesa;
    }

    // LIBERAR MESA SI NO TIENE PEDIDOS ABIERTOS

    private void liberarMesaSiVacia(
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
                                SELECT COALESCE(
                                    SUM(d.subtotal), 0
                                )
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

    // INSERTAR ENCABEZADO DEL PEDIDO

    private int insertarPedido(
            Connection con,
            Usuario usuario,
            Pedido pedido) throws SQLException {

        String sql = """
            INSERT INTO pedido (
                id_cliente,
                id_mesa,
                id_usuario,
                tipo_pedido,
                estado
            )
            VALUES (?, ?, ?, ?, 'PENDIENTE')
            """;

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            if (pedido.getIdCliente() == null) {

                ps.setNull(1, Types.INTEGER);

            } else {

                ps.setInt(1, pedido.getIdCliente());
            }

            if (pedido.getIdMesa() == null) {

                ps.setNull(2, Types.INTEGER);

            } else {

                ps.setInt(2, pedido.getIdMesa());
            }

            ps.setInt(3, usuario.getIdUsuario());
            ps.setString(4, pedido.getTipoPedido());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }

                throw new SQLException(
                        "No se pudo obtener el ID del pedido."
                );
            }
        }
    }

    // REGISTRAR DETALLE Y DESCONTAR STOCK

    private void registrarDetalle(
            Connection con,
            int idPedido,
            int idUsuario,
            int idProducto,
            int cantidad) throws SQLException {

        String consulta = """
            SELECT nombre_producto,
                   precio_producto,
                   stock
            FROM producto
            WHERE id_producto = ?
                AND estado = TRUE
            FOR UPDATE
            """;

        BigDecimal precio;
        String nombre;
        int stock;

        try (PreparedStatement ps =
                con.prepareStatement(consulta)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "El producto no existe o esta inactivo."
                    );
                }

                nombre = rs.getString("nombre_producto");

                precio = rs.getBigDecimal(
                        "precio_producto"
                );

                stock = rs.getInt("stock");
            }
        }

        if (stock < cantidad) {

            throw new IllegalArgumentException(
                    "Stock insuficiente para " + nombre
                    + ". Disponible: " + stock
            );
        }

        String detalle = """
            INSERT INTO detalle_pedido (
                id_pedido,
                id_producto,
                cantidad,
                precio_unitario
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement ps =
                con.prepareStatement(detalle)) {

            ps.setInt(1, idPedido);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            ps.setBigDecimal(4, precio);

            ps.executeUpdate();
        }

        String actualizar = """
            UPDATE producto
            SET stock = stock - ?
            WHERE id_producto = ?
                AND stock >= ?
            """;

        try (PreparedStatement ps =
                con.prepareStatement(actualizar)) {

            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);

            if (ps.executeUpdate() != 1) {

                throw new SQLException(
                        "No se pudo descontar el stock."
                );
            }
        }

        registrarMovimiento(
                con,
                idProducto,
                idUsuario,
                "SALIDA",
                cantidad,
                "Registro del pedido #" + idPedido
        );
    }

    // REGISTRAR MOVIMIENTO DE INVENTARIO

    private void registrarMovimiento(
            Connection con,
            int idProducto,
            int idUsuario,
            String tipo,
            int cantidad,
            String motivo) throws SQLException {

        String sql = """
            INSERT INTO movimiento_inventario (
                id_producto,
                id_usuario,
                tipo_movimiento,
                cantidad,
                motivo
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.setInt(2, idUsuario);
            ps.setString(3, tipo);
            ps.setInt(4, cantidad);
            ps.setString(5, motivo);

            ps.executeUpdate();
        }

        String actualizar = """
            UPDATE inventario
            SET ultima_actualizacion = CURRENT_TIMESTAMP
            WHERE id_producto = ?
            """;

        try (PreparedStatement ps =
                con.prepareStatement(actualizar)) {

            ps.setInt(1, idProducto);
            ps.executeUpdate();
        }
    }

    // CONSULTA GENERAL DE PEDIDOS

    private static final String CONSULTA = """
        SELECT p.id_pedido,
               p.id_cliente,
               p.id_mesa,
               p.id_usuario,
               COALESCE(
                   c.nombre_cliente,
                   'Consumidor final'
               ) AS cliente,
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
        """;

    // CONVERTIR RESULTADO A OBJETO

    private Pedido convertirPedido(
            ResultSet rs) throws SQLException {

        return new Pedido(
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
        );
    }

    // LISTAR PEDIDOS

    public ArrayList<Pedido> listar(
            Usuario usuario) throws SQLException {

        ArrayList<Pedido> pedidos = new ArrayList<>();

        String sql = CONSULTA
                + " ORDER BY p.id_pedido DESC";

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    pedidos.add(convertirPedido(rs));
                }
            }
        }

        return pedidos;
    }

    // BUSCAR PEDIDO POR ID

    public Pedido buscar(
            Usuario usuario,
            int idPedido) throws SQLException {

        String sql = CONSULTA
                + " WHERE p.id_pedido = ?";

        try (Connection con = ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            Pedido pedido = null;

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setInt(1, idPedido);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        pedido = convertirPedido(rs);
                    }
                }
            }

            if (pedido != null) {

                pedido.setDetalles(
                        consultarDetalles(con, idPedido)
                );
            }

            return pedido;
        }
    }

    // CONSULTAR DETALLES DEL PEDIDO

    private ArrayList<DetallePedido> consultarDetalles(
            Connection con,
            int idPedido) throws SQLException {

        ArrayList<DetallePedido> detalles =
                new ArrayList<>();

        String sql = """
            SELECT d.id_detalle,
                   d.id_pedido,
                   d.id_producto,
                   p.nombre_producto,
                   d.cantidad,
                   d.precio_unitario
            FROM detalle_pedido d
            INNER JOIN producto p
                ON d.id_producto = p.id_producto
            WHERE d.id_pedido = ?
            ORDER BY d.id_detalle
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    detalles.add(new DetallePedido(
                            rs.getInt("id_detalle"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_producto"),
                            rs.getString("nombre_producto"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario")
                    ));
                }
            }
        }

        return detalles;
    }

    // CAMBIAR ESTADO DEL PEDIDO

    public boolean cambiarEstado(
            Usuario usuario,
            int idPedido,
            String nuevoEstado) throws SQLException {

        try (Connection con = ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario);

                String actual = obtenerEstado(
                        con, idPedido
                );

                boolean permitido = switch (actual) {

                    case "PENDIENTE" ->
                        "EN_PREPARACION".equals(nuevoEstado);

                    case "EN_PREPARACION" ->
                        "LISTO".equals(nuevoEstado);

                    case "LISTO" ->
                        "ENTREGADO".equals(nuevoEstado);

                    default -> false;
                };

                if (!permitido) {

                    throw new IllegalArgumentException(
                            "No se permite ese cambio de estado."
                    );
                }

                actualizarEstado(
                        con, idPedido, nuevoEstado
                );

                con.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }

    // OBTENER ESTADO CON BLOQUEO

    private String obtenerEstado(
            Connection con,
            int idPedido) throws SQLException {

        String sql = """
            SELECT estado
            FROM pedido
            WHERE id_pedido = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("estado");
                }

                throw new IllegalArgumentException(
                        "El pedido no existe."
                );
            }
        }
    }

    // ACTUALIZAR ESTADO

    private void actualizarEstado(
            Connection con,
            int idPedido,
            String estado) throws SQLException {

        String sql = """
            UPDATE pedido
            SET estado = ?
            WHERE id_pedido = ?
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idPedido);

            if (ps.executeUpdate() != 1) {

                throw new SQLException(
                        "No se pudo actualizar el pedido."
                );
            }
        }
    }

    // CANCELAR PEDIDO Y DEVOLVER STOCK

    public boolean cancelar(
            Usuario usuario,
            int idPedido) throws SQLException {

        try (Connection con = ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario);

                Integer idMesa = bloquearMesaDelPedido(
                        con, idPedido
                );

                String estado = obtenerEstado(
                        con, idPedido
                );

                if (!"PENDIENTE".equals(estado)) {

                    throw new IllegalArgumentException(
                            "Solo se pueden cancelar pedidos pendientes."
                    );
                }

                String pagos = """
                    SELECT id_pago
                    FROM pago
                    WHERE id_pedido = ?
                        AND estado IN (
                            'PENDIENTE',
                            'COMPLETADO'
                        )
                    FOR UPDATE
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(pagos)) {

                    ps.setInt(1, idPedido);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {

                            throw new IllegalArgumentException(
                                    "El pedido tiene pagos asociados."
                            );
                        }
                    }
                }

                Map<Integer, Integer> cantidades =
                        new TreeMap<>();

                for (DetallePedido detalle
                        : consultarDetalles(con, idPedido)) {

                    cantidades.merge(
                            detalle.getIdProducto(),
                            detalle.getCantidad(),
                            Math::addExact
                    );
                }

                String devolver = """
                    UPDATE producto
                    SET stock = stock + ?
                    WHERE id_producto = ?
                        AND stock <= 2147483647 - ?
                    """;

                for (Map.Entry<Integer, Integer> item
                        : cantidades.entrySet()) {

                    try (PreparedStatement ps =
                            con.prepareStatement(devolver)) {

                        ps.setInt(1, item.getValue());
                        ps.setInt(2, item.getKey());
                        ps.setInt(3, item.getValue());

                        if (ps.executeUpdate() != 1) {

                            throw new SQLException(
                                    "No se pudo devolver el stock."
                            );
                        }
                    }

                    registrarMovimiento(
                            con,
                            item.getKey(),
                            usuario.getIdUsuario(),
                            "ENTRADA",
                            item.getValue(),
                            "Cancelacion del pedido #" + idPedido
                    );
                }

                actualizarEstado(
                        con, idPedido, "CANCELADO"
                );

                liberarMesaSiVacia(con, idMesa);

                con.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }
}
