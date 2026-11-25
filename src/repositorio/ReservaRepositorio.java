
package repositorio;

import modelo.Reserva;
import modelo.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ReservaRepositorio {

    // DURACION ESTIMADA DE CADA RESERVA

    public static final int DURACION_MINUTOS = 120;

    private static final ZoneId ZONA =
            ZoneId.of("America/Bogota");

    private static final String CONSULTA = """
        SELECT r.id_reserva,
               r.id_mesa,
               r.id_cliente,
               r.id_usuario,
               m.numero_mesa,
               c.documento_cliente,
               c.nombre_cliente,
               u.nombre_usuario,
               r.fecha_reserva,
               r.hora_reserva,
               r.cantidad_personas,
               r.estado,
               r.observaciones
        FROM reserva r
        INNER JOIN mesa m
            ON r.id_mesa = m.id_mesa
        INNER JOIN cliente c
            ON r.id_cliente = c.id_cliente
        INNER JOIN usuario u
            ON r.id_usuario = u.id_usuario
        """;

    // VALIDAR PERMISOS DEL EMPLEADO

    private void validarPermisos(
            Connection con,
            Usuario usuario) throws SQLException {

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
                AND r.nombre_rol IN (
                    'ADMINISTRADOR',
                    'MESERO'
                )
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, usuario.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "No tienes permisos para gestionar reservas."
                    );
                }
            }
        }
    }

    // VALIDAR DATOS DE LA RESERVA

    private void validarDatos(Reserva reserva) {

        if (reserva == null
                || reserva.getIdMesa() <= 0
                || reserva.getIdCliente() <= 0
                || reserva.getCantidadPersonas() <= 0
                || reserva.getFechaReserva() == null
                || reserva.getHoraReserva() == null) {

            throw new IllegalArgumentException(
                    "Completa todos los datos de la reserva."
            );
        }

        LocalDateTime fechaHora = LocalDateTime.of(
                reserva.getFechaReserva(),
                reserva.getHoraReserva()
        );

        if (fechaHora.isBefore(
                LocalDateTime.now(ZONA))) {

            throw new IllegalArgumentException(
                    "No puedes registrar reservas en el pasado."
            );
        }

        if (reserva.getObservaciones() != null
                && reserva.getObservaciones().length() > 255) {

            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar "
                    + "los 255 caracteres."
            );
        }
    }

    // VALIDAR CLIENTE

    private void validarCliente(
            Connection con,
            int idCliente) throws SQLException {

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

    // VALIDAR MESA Y CAPACIDAD

    private void validarMesa(
            Connection con,
            Reserva reserva) throws SQLException {

        String sql = """
            SELECT numero_mesa,
                   capacidad,
                   estado
            FROM mesa
            WHERE id_mesa = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, reserva.getIdMesa());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new IllegalArgumentException(
                            "La mesa seleccionada no existe."
                    );
                }

                int capacidad = rs.getInt("capacidad");

                String estado = rs.getString("estado");

                if ("MANTENIMIENTO".equals(estado)) {

                    throw new IllegalArgumentException(
                            "La mesa esta en mantenimiento."
                    );
                }

                if (reserva.getCantidadPersonas()
                        > capacidad) {

                    throw new IllegalArgumentException(
                            "La capacidad maxima de esta mesa es "
                            + capacidad + " personas."
                    );
                }

                LocalDateTime inicio = LocalDateTime.of(
                        reserva.getFechaReserva(),
                        reserva.getHoraReserva()
                );

                if (("OCUPADA".equals(estado)
                        || "RESERVADA".equals(estado))
                        && inicio.isBefore(
                                LocalDateTime.now(ZONA)
                                        .plusMinutes(
                                            DURACION_MINUTOS
                                        ))) {

                    throw new IllegalArgumentException(
                            "La mesa no esta disponible "
                            + "para una reserva inmediata."
                    );
                }
            }
        }
    }

    // COMPROBAR CRUCES DE HORARIOS

    private boolean existeCruce(
            Connection con,
            Reserva reserva) throws SQLException {

        LocalDateTime inicio = LocalDateTime.of(
                reserva.getFechaReserva(),
                reserva.getHoraReserva()
        );

        LocalDateTime fin = inicio.plusMinutes(
                DURACION_MINUTOS
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
                    INTERVAL ? MINUTE
                ) > ?
            FOR UPDATE
            """;

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, reserva.getIdMesa());

            ps.setTimestamp(
                    2, Timestamp.valueOf(fin)
            );

            ps.setInt(3, DURACION_MINUTOS);

            ps.setTimestamp(
                    4, Timestamp.valueOf(inicio)
            );

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    // REGISTRAR NUEVA RESERVA

    public int registrar(
            Usuario usuario,
            Reserva reserva) throws SQLException {

        validarDatos(reserva);

        try (Connection con =
                ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario);

                validarCliente(
                        con, reserva.getIdCliente()
                );

                validarMesa(con, reserva);

                // VOLVER A VALIDAR DESPUES DEL BLOQUEO

                validarDatos(reserva);

                if (existeCruce(con, reserva)) {

                    throw new IllegalArgumentException(
                            "La mesa ya tiene una reserva "
                            + "que coincide con ese horario."
                    );
                }

                String sql = """
                    INSERT INTO reserva (
                        id_mesa,
                        id_cliente,
                        id_usuario,
                        fecha_reserva,
                        hora_reserva,
                        cantidad_personas,
                        estado,
                        observaciones
                    )
                    VALUES (
                        ?, ?, ?, ?, ?, ?,
                        'PENDIENTE', ?
                    )
                    """;

                int idReserva;

                try (PreparedStatement ps =
                        con.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )) {

                    ps.setInt(
                            1, reserva.getIdMesa()
                    );

                    ps.setInt(
                            2, reserva.getIdCliente()
                    );

                    ps.setInt(
                            3, usuario.getIdUsuario()
                    );

                    ps.setDate(
                            4,
                            Date.valueOf(
                                    reserva.getFechaReserva()
                            )
                    );

                    ps.setTime(
                            5,
                            Time.valueOf(
                                    reserva.getHoraReserva()
                            )
                    );

                    ps.setInt(
                            6,
                            reserva.getCantidadPersonas()
                    );

                    ps.setString(
                            7,
                            reserva.getObservaciones()
                    );

                    if (ps.executeUpdate() != 1) {

                        throw new SQLException(
                                "No se pudo registrar la reserva."
                        );
                    }

                    try (ResultSet rs =
                            ps.getGeneratedKeys()) {

                        if (!rs.next()) {

                            throw new SQLException(
                                    "No se pudo obtener "
                                    + "el ID de la reserva."
                            );
                        }

                        idReserva = rs.getInt(1);
                    }
                }

                con.commit();

                reserva.setIdReserva(idReserva);

                reserva.setIdUsuario(
                        usuario.getIdUsuario()
                );

                reserva.setEstado("PENDIENTE");

                return idReserva;

            } catch (SQLException | RuntimeException e) {

                con.rollback();

                throw e;
            }
        }
    }

    // CONVERTIR RESULTADO A OBJETO

    private Reserva convertirReserva(
            ResultSet rs) throws SQLException {

        return new Reserva(
                rs.getInt("id_reserva"),
                rs.getInt("id_mesa"),
                rs.getInt("id_cliente"),
                rs.getInt("id_usuario"),
                rs.getInt("numero_mesa"),
                rs.getString("documento_cliente"),
                rs.getString("nombre_cliente"),
                rs.getString("nombre_usuario"),
                rs.getDate("fecha_reserva")
                        .toLocalDate(),
                rs.getTime("hora_reserva")
                        .toLocalTime(),
                rs.getInt("cantidad_personas"),
                rs.getString("estado"),
                rs.getString("observaciones")
        );
    }

    // LISTAR RESERVAS

    public ArrayList<Reserva> listar(
            Usuario usuario) throws SQLException {

        ArrayList<Reserva> reservas =
                new ArrayList<>();

        String sql = CONSULTA + """
             ORDER BY r.fecha_reserva DESC,
                      r.hora_reserva DESC,
                      r.id_reserva DESC
            """;

        try (Connection con =
                ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql);

                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    reservas.add(
                            convertirReserva(rs)
                    );
                }
            }
        }

        return reservas;
    }

    // BUSCAR RESERVA POR ID

    public Reserva buscarPorId(
            Usuario usuario,
            int idReserva) throws SQLException {

        if (idReserva <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona una reserva valida."
            );
        }

        String sql = CONSULTA
                + " WHERE r.id_reserva = ?";

        try (Connection con =
                ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setInt(1, idReserva);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        return convertirReserva(rs);
                    }
                }
            }
        }

        return null;
    }

    // LISTAR RESERVAS POR ESTADO

    public ArrayList<Reserva> listarPorEstado(
            Usuario usuario,
            String estado) throws SQLException {

        if (estado == null
                || estado.isBlank()
                || "TODOS".equalsIgnoreCase(estado.trim())) {

            return listar(usuario);
        }

        estado = estado.trim().toUpperCase(
                java.util.Locale.ROOT
        );

        if (!List.of(
                "PENDIENTE",
                "CONFIRMADA",
                "CANCELADA",
                "FINALIZADA"
        ).contains(estado)) {

            throw new IllegalArgumentException(
                    "Estado de reserva invalido."
            );
        }

        ArrayList<Reserva> resultado =
                new ArrayList<>();

        String sql = CONSULTA + """
             WHERE r.estado = ?
             ORDER BY r.fecha_reserva DESC,
                      r.hora_reserva DESC,
                      r.id_reserva DESC
            """;

        try (Connection con =
                ConexionMySQL.conectar()) {

            validarPermisos(con, usuario);

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                ps.setString(1, estado);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        resultado.add(
                                convertirReserva(rs)
                        );
                    }
                }
            }
        }

        return resultado;
    }

    // CAMBIAR ESTADO DE UNA RESERVA

    public boolean cambiarEstado(
            Usuario usuario,
            int idReserva,
            String nuevoEstado) throws SQLException {

        if (idReserva <= 0
                || nuevoEstado == null
                || nuevoEstado.isBlank()) {

            throw new IllegalArgumentException(
                    "Selecciona una reserva valida."
            );
        }

        nuevoEstado = nuevoEstado.trim().toUpperCase(
                java.util.Locale.ROOT
        );

        if (!List.of(
                "CONFIRMADA",
                "CANCELADA",
                "FINALIZADA"
        ).contains(nuevoEstado)) {

            throw new IllegalArgumentException(
                    "Estado de reserva invalido."
            );
        }

        try (Connection con =
                ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                validarPermisos(con, usuario);

                int idMesa;

                String consultaMesa = """
                    SELECT id_mesa
                    FROM reserva
                    WHERE id_reserva = ?
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(consultaMesa)) {

                    ps.setInt(1, idReserva);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                    "La reserva no existe."
                            );
                        }

                        idMesa = rs.getInt("id_mesa");
                    }
                }

                // BLOQUEAR MESA ANTES DE LA RESERVA

                String estadoMesa;

                String bloquearMesa = """
                    SELECT estado
                    FROM mesa
                    WHERE id_mesa = ?
                    FOR UPDATE
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(bloquearMesa)) {

                    ps.setInt(1, idMesa);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                    "La mesa no existe."
                            );
                        }

                        estadoMesa = rs.getString("estado");
                    }
                }

                String estadoActual;
                LocalDateTime fechaHora;

                String consulta = """
                    SELECT estado,
                           fecha_reserva,
                           hora_reserva
                    FROM reserva
                    WHERE id_reserva = ?
                    FOR UPDATE
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(consulta)) {

                    ps.setInt(1, idReserva);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                    "La reserva no existe."
                            );
                        }

                        estadoActual =
                                rs.getString("estado");

                        fechaHora = LocalDateTime.of(
                                rs.getDate("fecha_reserva")
                                        .toLocalDate(),
                                rs.getTime("hora_reserva")
                                        .toLocalTime()
                        );
                    }
                }

                // VALIDAR TRANSICION DE ESTADO

                boolean permitido = switch (estadoActual) {

                    case "PENDIENTE" ->
                        "CONFIRMADA".equals(nuevoEstado)
                        || "CANCELADA".equals(nuevoEstado);

                    case "CONFIRMADA" ->
                        "FINALIZADA".equals(nuevoEstado)
                        || "CANCELADA".equals(nuevoEstado);

                    default -> false;
                };

                if (!permitido) {

                    throw new IllegalArgumentException(
                            "No se permite ese cambio de estado."
                    );
                }

                if ("CONFIRMADA".equals(nuevoEstado)) {

                    if (fechaHora.isBefore(
                            LocalDateTime.now(ZONA))) {

                        throw new IllegalArgumentException(
                                "La hora de la reserva ya paso."
                        );
                    }

                    if ("MANTENIMIENTO".equals(estadoMesa)) {

                        throw new IllegalArgumentException(
                                "La mesa esta en mantenimiento."
                        );
                    }
                }

                if ("FINALIZADA".equals(nuevoEstado)
                        && fechaHora.isAfter(
                                LocalDateTime.now(ZONA))) {

                    throw new IllegalArgumentException(
                            "No puedes finalizar una reserva "
                            + "antes de su horario."
                    );
                }

                String actualizar = """
                    UPDATE reserva
                    SET estado = ?
                    WHERE id_reserva = ?
                    """;

                try (PreparedStatement ps =
                        con.prepareStatement(actualizar)) {

                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, idReserva);

                    if (ps.executeUpdate() != 1) {

                        throw new SQLException(
                                "No se pudo actualizar la reserva."
                        );
                    }
                }

                con.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                con.rollback();

                throw e;
            }
        }
    }

    // CONFIRMAR RESERVA

    public boolean confirmar(
            Usuario usuario,
            int idReserva) throws SQLException {

        return cambiarEstado(
                usuario,
                idReserva,
                "CONFIRMADA"
        );
    }

    // CANCELAR RESERVA

    public boolean cancelar(
            Usuario usuario,
            int idReserva) throws SQLException {

        return cambiarEstado(
                usuario,
                idReserva,
                "CANCELADA"
        );
    }

    // FINALIZAR RESERVA

    public boolean finalizar(
            Usuario usuario,
            int idReserva) throws SQLException {

        return cambiarEstado(
                usuario,
                idReserva,
                "FINALIZADA"
        );
    }
}
