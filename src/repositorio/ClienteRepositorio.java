
package repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import modelo.Cliente;

public class ClienteRepositorio {

    private static final String CONSULTA = """
        SELECT id_cliente,
               documento_cliente,
               nombre_cliente,
               telefono,
               correo,
               estado
        FROM cliente
        """;

    // Convertir resultado SQL en objeto Cliente
    private Cliente convertirCliente(ResultSet rs)
            throws SQLException {

        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("documento_cliente"),
                rs.getString("nombre_cliente"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getBoolean("estado")
        );
    }

    // Validar datos del cliente
    private boolean datosValidos(Cliente cliente) {

        if (cliente == null) {
            return false;
        }

        String documento = cliente.getDocumentoCliente();
        String nombre = cliente.getNombreCliente();
        String telefono = cliente.getTelefono();
        String correo = cliente.getCorreo();

        return documento != null
                && !documento.isBlank()
                && documento.trim().length() <= 20
                && nombre != null
                && !nombre.isBlank()
                && nombre.trim().length() <= 150
                && (telefono == null || telefono.length() <= 20)
                && (correo == null || correo.length() <= 150);
    }

    // 1. REGISTRAR CLIENTE
    public boolean registrar(Cliente cliente) {

        if (!datosValidos(cliente)) {
            return false;
        }

        String sql = """
            INSERT INTO cliente
            (documento_cliente, nombre_cliente,
             telefono, correo, estado)
            VALUES (?, ?, ?, ?, TRUE)
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getDocumentoCliente().trim());
            ps.setString(2, cliente.getNombreCliente().trim());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al registrar cliente: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // 2. BUSCAR CLIENTE POR DOCUMENTO
    public Cliente buscar(String documento) {

        if (documento == null || documento.isBlank()) {
            return null;
        }

        String sql = CONSULTA + """
             WHERE documento_cliente = ?
               AND estado = TRUE
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirCliente(rs);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al buscar cliente: "
                    + e.getMessage()
            );
        }

        return null;
    }

    // 3. BUSCAR CLIENTE POR ID
    public Cliente buscarPorId(int idCliente) {

        String sql = CONSULTA
                + " WHERE id_cliente = ?";

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirCliente(rs);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al buscar cliente: "
                    + e.getMessage()
            );
        }

        return null;
    }

    // 4. ACTUALIZAR NOMBRE
    // Metodo anterior por compatibilidad
    public boolean actualizar(String documento,
                              String nuevoNombre) {

        if (documento == null || documento.isBlank()
                || nuevoNombre == null
                || nuevoNombre.isBlank()
                || nuevoNombre.trim().length() > 150) {

            return false;
        }

        String sql = """
            UPDATE cliente
            SET nombre_cliente = ?
            WHERE documento_cliente = ?
              AND estado = TRUE
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoNombre.trim());
            ps.setString(2, documento.trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar cliente: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // 5. ACTUALIZAR TODOS LOS DATOS
    public boolean actualizar(Cliente cliente) {

        if (!datosValidos(cliente)
                || cliente.getIdCliente() <= 0) {

            return false;
        }

        String sql = """
            UPDATE cliente
            SET documento_cliente = ?,
                nombre_cliente = ?,
                telefono = ?,
                correo = ?
            WHERE id_cliente = ?
              AND estado = TRUE
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getDocumentoCliente().trim());
            ps.setString(2, cliente.getNombreCliente().trim());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());
            ps.setInt(5, cliente.getIdCliente());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar cliente: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // 6. ELIMINACION LOGICA
    public boolean eliminar(String documento) {

        if (documento == null || documento.isBlank()) {
            return false;
        }

        String sql = """
            UPDATE cliente
            SET estado = FALSE
            WHERE documento_cliente = ?
              AND estado = TRUE
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento.trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al desactivar cliente: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // 7. ACTIVAR O DESACTIVAR CLIENTE
    public boolean cambiarEstado(int idCliente,
                                 boolean estado) {

        String sql = """
            UPDATE cliente
            SET estado = ?
            WHERE id_cliente = ?
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, estado);
            ps.setInt(2, idCliente);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al cambiar estado: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // 8. LISTAR CLIENTES ACTIVOS
    public ArrayList<Cliente> listar() {

        return consultarClientes(
                CONSULTA + """
                 WHERE estado = TRUE
                 ORDER BY nombre_cliente
                """
        );
    }

    // 9. LISTAR TODOS LOS CLIENTES
    public ArrayList<Cliente> listarTodos() {

        return consultarClientes(
                CONSULTA + " ORDER BY nombre_cliente"
        );
    }

    // Ejecutar consulta de clientes
    private ArrayList<Cliente> consultarClientes(String sql) {

        ArrayList<Cliente> clientes = new ArrayList<>();

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                clientes.add(convertirCliente(rs));
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al listar clientes: "
                    + e.getMessage()
            );
        }

        return clientes;
    }
}
