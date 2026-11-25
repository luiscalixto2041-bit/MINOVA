
package repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Usuario;
import seguridad.SeguridadContrasena;

public class UsuarioRepositorio {

    private static final String CONSULTA_USUARIOS = """
        SELECT u.id_usuario,
               u.nombre_completo,
               u.nombre_usuario,
               u.id_rol,
               r.nombre_rol,
               u.estado
        FROM usuario u
        INNER JOIN roles r
            ON u.id_rol = r.id_rol
        """;

    // CONVERTIR RESULTADO MYSQL A OBJETO USUARIO

    private Usuario convertirUsuario(
            ResultSet rs) throws SQLException {

        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre_completo"),
                rs.getString("nombre_usuario"),
                rs.getInt("id_rol"),
                rs.getString("nombre_rol"),
                rs.getBoolean("estado")
        );
    }

    // VALIDAR PERMISOS DEL ADMINISTRADOR

    private void validarAdministrador(
            Usuario actor) throws SQLException {

        if (actor == null || actor.getIdUsuario() <= 0) {

            throw new SQLException(
                    "Sesion no autorizada."
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

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setInt(1, actor.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No tienes permisos de administrador."
                    );
                }
            }
        }
    }

    // VALIDAR DATOS DEL USUARIO

    private void validarDatos(
            String nombreCompleto,
            String nombreUsuario,
            int idRol) {

        if (nombreCompleto == null
                || nombreCompleto.isBlank()
                || nombreCompleto.trim().length() > 150) {

            throw new IllegalArgumentException(
                    "Nombre completo no valido."
            );
        }

        if (nombreUsuario == null
                || nombreUsuario.isBlank()
                || nombreUsuario.trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Nombre de usuario no valido."
            );
        }

        if (idRol < 1 || idRol > 4) {

            throw new IllegalArgumentException(
                    "Selecciona un rol valido."
            );
        }
    }

    // VALIDAR CONTRASENA

    private void validarContrasena(String contrasena) {

        if (contrasena == null
                || contrasena.length() < 12) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener minimo 12 caracteres."
            );
        }
    }

    // 1. INICIAR SESION

    public Usuario login(
            String nombreUsuario,
            String contrasena) throws SQLException {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()
                || contrasena == null
                || contrasena.isBlank()) {

            return null;
        }

        String sql = """
            SELECT u.id_usuario,
                   u.nombre_completo,
                   u.nombre_usuario,
                   u.contrasena,
                   u.id_rol,
                   r.nombre_rol,
                   u.estado
            FROM usuario u
            INNER JOIN roles r
                ON u.id_rol = r.id_rol
            WHERE u.nombre_usuario = ?
                AND u.estado = TRUE
            """;

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String hash =
                            rs.getString("contrasena");

                    if (SeguridadContrasena.verificar(
                            contrasena, hash)) {

                        return convertirUsuario(rs);
                    }
                }
            }
        }

        return null;
    }

    // 2. REGISTRAR EMPLEADO CON AUTORIZACION

    public boolean registrarUsuario(
            Usuario actor,
            String nombreCompleto,
            String nombreUsuario,
            String contrasena,
            int idRol) throws SQLException {

        validarAdministrador(actor);

        return insertarUsuario(
                nombreCompleto,
                nombreUsuario,
                contrasena,
                idRol
        );
    }

    // INSERTAR USUARIO EN MYSQL

    private boolean insertarUsuario(
            String nombreCompleto,
            String nombreUsuario,
            String contrasena,
            int idRol) throws SQLException {

        validarDatos(
                nombreCompleto,
                nombreUsuario,
                idRol
        );

        validarContrasena(contrasena);

        String hash =
                SeguridadContrasena.generarHash(
                        contrasena
                );

        String sql = """
            INSERT INTO usuario (
                nombre_completo,
                nombre_usuario,
                contrasena,
                id_rol
            )
            VALUES (?, ?, ?, ?)
            """;

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setString(1, nombreCompleto.trim());
            ps.setString(2, nombreUsuario.trim());
            ps.setString(3, hash);
            ps.setInt(4, idRol);

            return ps.executeUpdate() > 0;
        }
    }

    // 3. COMPROBAR SI EXISTE UN USUARIO

    public boolean existeUsuario(
            String nombreUsuario) throws SQLException {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()) {

            return false;
        }

        String sql = """
            SELECT id_usuario
            FROM usuario
            WHERE nombre_usuario = ?
            """;

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario.trim());

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    // 4. LISTAR TODOS LOS EMPLEADOS

    public List<Usuario> listarUsuarios(
            Usuario actor) throws SQLException {

        validarAdministrador(actor);

        List<Usuario> usuarios = new ArrayList<>();

        String sql = CONSULTA_USUARIOS
                + " ORDER BY u.id_usuario";

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                usuarios.add(convertirUsuario(rs));
            }
        }

        return usuarios;
    }

    // 5. BUSCAR USUARIO POR ID

    public Usuario buscarPorId(
            Usuario actor,
            int idUsuario) throws SQLException {

        validarAdministrador(actor);

        String sql = CONSULTA_USUARIOS
                + " WHERE u.id_usuario = ?";

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return convertirUsuario(rs);
                }
            }
        }

        return null;
    }

    // 6. ACTUALIZAR DATOS Y ROL

    public boolean actualizarUsuario(
            Usuario actor,
            int idUsuario,
            String nombreCompleto,
            String nombreUsuario,
            int idRol) throws SQLException {

        validarAdministrador(actor);

        validarDatos(
                nombreCompleto,
                nombreUsuario,
                idRol
        );

        // IMPEDIR QUE EL ADMINISTRADOR
        // ELIMINE SU PROPIO ROL

        if (actor.getIdUsuario() == idUsuario
                && idRol != 1) {

            throw new SQLException(
                    "No puedes quitarte tu propio "
                    + "rol de administrador."
            );
        }

        String sql = """
            UPDATE usuario
            SET nombre_completo = ?,
                nombre_usuario = ?,
                id_rol = ?
            WHERE id_usuario = ?
            """;

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setString(1, nombreCompleto.trim());
            ps.setString(2, nombreUsuario.trim());
            ps.setInt(3, idRol);
            ps.setInt(4, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }

    // 7. ACTIVAR O DESACTIVAR USUARIO

    public boolean cambiarEstado(
            Usuario actor,
            int idUsuario,
            boolean estado) throws SQLException {

        validarAdministrador(actor);

        // IMPEDIR QUE EL ADMINISTRADOR
        // DESACTIVE SU PROPIA CUENTA

        if (actor.getIdUsuario() == idUsuario
                && !estado) {

            throw new SQLException(
                    "No puedes desactivar tu propia cuenta."
            );
        }

        String sql = """
            UPDATE usuario
            SET estado = ?
            WHERE id_usuario = ?
            """;

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setBoolean(1, estado);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }

    // 8. RESTABLECER CONTRASENA

    public boolean restablecerContrasena(
            Usuario actor,
            int idUsuario,
            String nuevaContrasena) throws SQLException {

        validarAdministrador(actor);

        validarContrasena(nuevaContrasena);

        String hash =
                SeguridadContrasena.generarHash(
                        nuevaContrasena
                );

        String sql = """
            UPDATE usuario
            SET contrasena = ?
            WHERE id_usuario = ?
            """;

        try (Connection con = ConexionMySQL.conectar();
                PreparedStatement ps =
                        con.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }
}
