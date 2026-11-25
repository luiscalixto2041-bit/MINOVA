/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import modelo.Mesa;
import modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;

public class MesaRepositorio {

    private static final String CONSULTA = """
        SELECT id_mesa,
               numero_mesa,
               capacidad,
               estado
        FROM mesa
        """;

    // VALIDAR PERMISOS

    private void validarPermisos(
            Connection con,
            Usuario usuario,
            boolean administrador)
            throws SQLException {

        if (usuario == null
                || usuario.getIdUsuario() <= 0) {

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

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "Usuario inactivo o inexistente."
                    );
                }

                String rol = rs.getString("nombre_rol");

                boolean permitido;

                if (administrador) {

                    permitido =
                            "ADMINISTRADOR".equals(rol);

                } else {

                    permitido =
                            "ADMINISTRADOR".equals(rol)
                            || "MESERO".equals(rol)
                            || "CAJERO".equals(rol);
                }

                if (!permitido) {

                    throw new IllegalArgumentException(
                            "No tienes permisos para esta operacion."
                    );
                }
            }
        }
    }

    // CONVERTIR RESULTADO A OBJETO MESA

    private Mesa convertirMesa(ResultSet rs)
            throws SQLException {

        return new Mesa(
                rs.getInt("id_mesa"),
                rs.getInt("numero_mesa"),
                rs.getInt("capacidad"),
                rs.getString("estado")
        );
    }

    // LISTAR TODAS LAS MESAS

    public ArrayList<Mesa> listar(Usuario usuario)
            throws SQLException {

        ArrayList<Mesa> mesas = new ArrayList<>();

        String sql = CONSULTA
                + " ORDER BY numero_mesa";

        try (Connection con =
                ConexionMySQL.conectar()) {

            validarPermisos(con, usuario, false);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);

                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    mesas.add(convertirMesa(rs));
                }
            }
        }

        return mesas;
    }

    // BUSCAR MESA POR ID

    public Mesa buscarPorId(
            Usuario usuario,
            int idMesa) throws SQLException {

        String sql = CONSULTA
                + " WHERE id_mesa = ?";

        try (Connection con =
                ConexionMySQL.conectar()) {

            validarPermisos(con, usuario, false);

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setInt(1, idMesa);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        return convertirMesa(rs);
                    }
                }
            }
        }

        return null;
    }

    // BUSCAR MESA POR NUMERO

    public Mesa buscarPorNumero(
            Usuario usuario,
            int numeroMesa) throws SQLException {

        String sql = CONSULTA
                + " WHERE numero_mesa = ?";

        try (Connection con =
                ConexionMySQL.conectar()) {

            validarPermisos(con, usuario, false);

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setInt(1, numeroMesa);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        return convertirMesa(rs);
                    }
                }
            }
        }

        return null;
    }

    // REGISTRAR NUEVA MESA
    // EXCLUSIVO PARA ADMINISTRADORES

    public int registrar(
            Usuario usuario,
            Mesa mesa) throws SQLException {

        if (mesa == null
                || mesa.getNumeroMesa() <= 0
                || mesa.getCapacidad() <= 0) {

            throw new IllegalArgumentException(
                    "Numero de mesa o capacidad invalida."
            );
        }

        String sql = """
            INSERT INTO mesa (
                numero_mesa,
                capacidad,
                estado
            )
            VALUES (?, ?, 'DISPONIBLE')
            """;

        try (Connection con =
                ConexionMySQL.conectar()) {

            validarPermisos(con, usuario, true);

            try (PreparedStatement ps =
                    con.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    )) {

                ps.setInt(1, mesa.getNumeroMesa());
                ps.setInt(2, mesa.getCapacidad());

                ps.executeUpdate();

                try (ResultSet rs =
                        ps.getGeneratedKeys()) {

                    if (rs.next()) {

                        int id = rs.getInt(1);

                        mesa.setIdMesa(id);

                        return id;
                    }
                }

                throw new SQLException(
                        "No se pudo obtener el ID de la mesa."
                );
            }
        }
    }

    // ACTUALIZAR NUMERO Y CAPACIDAD

    public boolean actualizar(
            Usuario usuario,
            Mesa mesa) throws SQLException {

        if (mesa == null
                || mesa.getIdMesa() <= 0
                || mesa.getNumeroMesa() <= 0
                || mesa.getCapacidad() <= 0) {

            throw new IllegalArgumentException(
                    "Los datos de la mesa no son validos."
            );
        }

        try (Connection con =
                ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario, true);

                Mesa actual = bloquearMesa(
                        con, mesa.getIdMesa()
                );

                if (actual.isOcupada()
                        || actual.isReservada()) {

                    throw new IllegalArgumentException(
                            "No puedes modificar una mesa "
                            + "ocupada o reservada."
                    );
                }

                String reservas = """
                    SELECT id_reserva
                    FROM reserva
                    WHERE id_mesa = ?
                        AND estado IN (
                            'PENDIENTE',
                            'CONFIRMADA'
                        )
                        AND cantidad_personas > ?
                    FOR UPDATE
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(reservas)) {

                    ps.setInt(1, mesa.getIdMesa());
                    ps.setInt(2, mesa.getCapacidad());

                    try (ResultSet rs =
                            ps.executeQuery()) {

                        if (rs.next()) {

                            throw new IllegalArgumentException(
                                    "La nueva capacidad es insuficiente "
                                    + "para una reserva activa."
                            );
                        }
                    }
                }

                String sql = """
                    UPDATE mesa
                    SET numero_mesa = ?,
                        capacidad = ?
                    WHERE id_mesa = ?
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {

                    ps.setInt(1, mesa.getNumeroMesa());
                    ps.setInt(2, mesa.getCapacidad());
                    ps.setInt(3, mesa.getIdMesa());

                    ps.executeUpdate();
                }

                con.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                con.rollback();

                throw e;
            }
        }
    }

    // BLOQUEAR MESA DURANTE UNA TRANSACCION

    private Mesa bloquearMesa(
            Connection con,
            int idMesa) throws SQLException {

        String sql = CONSULTA
                + " WHERE id_mesa = ? FOR UPDATE";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return convertirMesa(rs);
                }

                throw new IllegalArgumentException(
                        "La mesa no existe."
                );
            }
        }
    }

    // COMPROBAR PEDIDOS ACTIVOS

    private boolean tienePedidosActivos(
            Connection con,
            int idMesa) throws SQLException {

        String sql = """
            SELECT id_pedido
            FROM pedido
            WHERE id_mesa = ?
                AND estado IN (
                    'PENDIENTE',
                    'EN_PREPARACION',
                    'LISTO'
                )
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    // COMPROBAR RESERVAS ACTIVAS

    private boolean tieneReservasActivas(
            Connection con,
            int idMesa) throws SQLException {

        String sql = """
            SELECT id_reserva
            FROM reserva
            WHERE id_mesa = ?
                AND estado IN (
                    'PENDIENTE',
                    'CONFIRMADA'
                )
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    // ACTIVAR O DESACTIVAR MANTENIMIENTO
    // EXCLUSIVO PARA ADMINISTRADORES

    public boolean cambiarMantenimiento(
            Usuario usuario,
            int idMesa,
            boolean mantenimiento)
            throws SQLException {

        try (Connection con =
                ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario, true);

                Mesa mesa = bloquearMesa(con, idMesa);

                if (mantenimiento) {

                    if (!mesa.isDisponible()) {

                        throw new IllegalArgumentException(
                                "Solo una mesa disponible puede "
                                + "entrar en mantenimiento."
                        );
                    }

                    if (tienePedidosActivos(con, idMesa)
                            || tieneReservasActivas(
                                    con, idMesa)) {

                        throw new IllegalArgumentException(
                                "La mesa tiene pedidos "
                                + "o reservas activas."
                        );
                    }

                } else {

                    if (!mesa.isMantenimiento()) {

                        throw new IllegalArgumentException(
                                "La mesa no esta en mantenimiento."
                        );
                    }
                }

                String nuevoEstado = mantenimiento
                        ? "MANTENIMIENTO"
                        : "DISPONIBLE";

                actualizarEstado(
                        con,
                        idMesa,
                        nuevoEstado
                );

                con.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                con.rollback();

                throw e;
            }
        }
    }

    // ACTUALIZAR ESTADO INTERNAMENTE

    private void actualizarEstado(
            Connection con,
            int idMesa,
            String estado) throws SQLException {

        String sql = """
            UPDATE mesa
            SET estado = ?
            WHERE id_mesa = ?
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idMesa);

            ps.executeUpdate();
        }
    }

    // LISTAR MESAS SEGUN ESTADO

    public ArrayList<Mesa> listarPorEstado(
            Usuario usuario,
            String estado) throws SQLException {

        ArrayList<Mesa> resultado =
                new ArrayList<>();

        for (Mesa mesa : listar(usuario)) {

            if (mesa.getEstado()
                    .equalsIgnoreCase(estado)) {

                resultado.add(mesa);
            }
        }

        return resultado;
    }

    // CONTAR MESAS

    public int totalMesas(Usuario usuario)
            throws SQLException {

        return listar(usuario).size();
    }

    // CONTAR MESAS SEGUN ESTADO

    public int contarPorEstado(
            Usuario usuario,
            String estado) throws SQLException {

        return listarPorEstado(
                usuario, estado
        ).size();
    }

    // CALCULAR CAPACIDAD TOTAL DEL RESTAURANTE

    public long capacidadTotal(Usuario usuario)
            throws SQLException {

        long capacidad = 0;

        for (Mesa mesa : listar(usuario)) {

            capacidad += mesa.getCapacidad();
        }

        return capacidad;
    }
}
