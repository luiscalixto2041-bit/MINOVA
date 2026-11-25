
package repositorio;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import modelo.Producto;

public class ProductoRepositorio {

    private static final String CONSULTA = """
        SELECT p.id_producto,
               p.nombre_producto,
               p.descripcion_producto,
               p.precio_producto,
               p.stock,
               p.estado,
               COALESCE(i.stock_minimo, 5) AS stock_minimo,
               COALESCE(i.stock_maximo, 100) AS stock_maximo
        FROM producto p
        LEFT JOIN inventario i
            ON p.id_producto = i.id_producto
        """;

    private Producto convertirProducto(ResultSet rs)
            throws SQLException {

        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre_producto"),
                rs.getString("descripcion_producto"),
                rs.getDouble("precio_producto"),
                rs.getInt("stock"),
                rs.getBoolean("estado"),
                rs.getInt("stock_minimo"),
                rs.getInt("stock_maximo")
        );
    }

    // VALIDAR DATOS

    private boolean datosValidos(Producto p) {

        if (p == null
                || p.getNombreProducto() == null
                || p.getNombreProducto().isBlank()
                || p.getNombreProducto().trim().length() > 120
                || !Double.isFinite(p.getPrecioProducto())
                || p.getPrecioProducto() < 0
                || p.getPrecioProducto() > 9999999999.99
                || p.getStockMinimo() < 0
                || p.getStockMaximo() < p.getStockMinimo()) {

            return false;
        }

        return p.getDescripcionProducto() == null
                || p.getDescripcionProducto().length() <= 255;
    }

    // REGISTRAR PRODUCTO

    public boolean registrar(Producto producto) {

        if (!datosValidos(producto)
                || producto.getStock() != 0) {

            return false;
        }

        String sqlProducto = """
            INSERT INTO producto
            (nombre_producto, descripcion_producto,
             precio_producto, stock, estado)
            VALUES (?, ?, ?, 0, TRUE)
            """;

        String sqlInventario = """
            INSERT INTO inventario
            (id_producto, stock_minimo, stock_maximo)
            VALUES (?, ?, ?)
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                int idProducto;

                try (PreparedStatement ps =
                        con.prepareStatement(
                                sqlProducto,
                                Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1,
                            producto.getNombreProducto().trim());

                    ps.setString(2,
                            producto.getDescripcionProducto());

                    ps.setBigDecimal(3, BigDecimal.valueOf(
                            producto.getPrecioProducto()));

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se obtuvo el ID del producto."
                            );
                        }

                        idProducto = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps =
                        con.prepareStatement(sqlInventario)) {

                    ps.setInt(1, idProducto);
                    ps.setInt(2, producto.getStockMinimo());
                    ps.setInt(3, producto.getStockMaximo());

                    ps.executeUpdate();
                }

                con.commit();

                producto.setIdProducto(idProducto);

                return true;

            } catch (SQLException e) {

                con.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al registrar producto: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // BUSCAR PRODUCTO POR NOMBRE

    public Producto buscar(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        String sql = CONSULTA + """
             WHERE p.nombre_producto = ?
               AND p.estado = TRUE
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirProducto(rs);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al buscar producto: "
                    + e.getMessage()
            );
        }

        return null;
    }

    // BUSCAR PRODUCTO POR ID

    public Producto buscarPorId(int idProducto) {

        String sql = CONSULTA
                + " WHERE p.id_producto = ?";

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return convertirProducto(rs);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al buscar producto: "
                    + e.getMessage()
            );
        }

        return null;
    }

    // ACTUALIZAR DATOS DEL PRODUCTO
    // El stock no se modifica desde este metodo.

    public boolean actualizar(Producto producto) {

        if (!datosValidos(producto)
                || producto.getIdProducto() <= 0) {

            return false;
        }

        String sqlProducto = """
            UPDATE producto
            SET nombre_producto = ?,
                descripcion_producto = ?,
                precio_producto = ?
            WHERE id_producto = ?
              AND estado = TRUE
            """;

        String sqlInventario = """
            UPDATE inventario
            SET stock_minimo = ?,
                stock_maximo = ?
            WHERE id_producto = ?
            """;

        try (Connection con = ConexionMySQL.conectar()) {

            con.setAutoCommit(false);

            try {

                try (PreparedStatement ps =
                        con.prepareStatement(sqlProducto)) {

                    ps.setString(1,
                            producto.getNombreProducto().trim());

                    ps.setString(2,
                            producto.getDescripcionProducto());

                    ps.setBigDecimal(3, BigDecimal.valueOf(
                            producto.getPrecioProducto()));

                    ps.setInt(4, producto.getIdProducto());

                    if (ps.executeUpdate() == 0) {

                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement ps =
                        con.prepareStatement(sqlInventario)) {

                    ps.setInt(1, producto.getStockMinimo());
                    ps.setInt(2, producto.getStockMaximo());
                    ps.setInt(3, producto.getIdProducto());

                    ps.executeUpdate();
                }

                con.commit();
                return true;

            } catch (SQLException e) {

                con.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al actualizar producto: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // COMPATIBILIDAD CON EL CONTROLADOR ANTERIOR
    // No permite modificar directamente el stock.

    public boolean actualizar(String nombre,
                              double precio,
                              int stock) {

        Producto producto = buscar(nombre);

        if (producto == null
                || producto.getStock() != stock) {

            return false;
        }

        producto.setPrecioProducto(precio);

        return actualizar(producto);
    }

    // ACTIVAR O DESACTIVAR PRODUCTO

    public boolean cambiarEstado(int idProducto,
                                 boolean estado) {

        String sql = """
            UPDATE producto
            SET estado = ?
            WHERE id_producto = ?
            """;

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, estado);
            ps.setInt(2, idProducto);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Error al cambiar estado del producto: "
                    + e.getMessage()
            );

            return false;
        }
    }

    // ELIMINACION LOGICA

    public boolean eliminar(String nombre) {

        Producto producto = buscar(nombre);

        if (producto == null) {
            return false;
        }

        return cambiarEstado(
                producto.getIdProducto(), false
        );
    }

    // LISTAR PRODUCTOS ACTIVOS

    public ArrayList<Producto> listar() {

        return consultarProductos(
                CONSULTA + """
                 WHERE p.estado = TRUE
                 ORDER BY p.nombre_producto
                """
        );
    }

    // LISTAR TODOS LOS PRODUCTOS

    public ArrayList<Producto> listarTodos() {

        return consultarProductos(
                CONSULTA + " ORDER BY p.nombre_producto"
        );
    }

    private ArrayList<Producto> consultarProductos(
            String sql) {

        ArrayList<Producto> productos = new ArrayList<>();

        try (Connection con = ConexionMySQL.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                productos.add(convertirProducto(rs));
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error al listar productos: "
                    + e.getMessage()
            );
        }

        return productos;
    }
}
