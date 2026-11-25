
package controlador;

import modelo.Producto;
import repositorio.ProductoRepositorio;
import java.util.ArrayList;

public class ProductoControlador {

    public static final int STOCK_BAJO = 5;

    private final ProductoRepositorio repositorio =
            new ProductoRepositorio();

    // VALIDAR DATOS

    private boolean validarDatos(
            String nombre,
            String descripcion,
            double precio,
            int stockMinimo,
            int stockMaximo) {

        return nombre != null
                && !nombre.isBlank()
                && nombre.trim().length() <= 120
                && (descripcion == null
                    || descripcion.length() <= 255)
                && Double.isFinite(precio)
                && precio >= 0
                && precio <= 9999999999.99
                && stockMinimo >= 0
                && stockMaximo >= stockMinimo;
    }

    // REGISTRAR PRODUCTO COMPLETO

    public boolean registrarProducto(
            String nombre,
            String descripcion,
            double precio,
            int stockMinimo,
            int stockMaximo) {

        if (!validarDatos(nombre, descripcion,
                precio, stockMinimo, stockMaximo)) {

            return false;
        }

        Producto producto = new Producto(
                0,
                nombre.trim(),
                descripcion == null
                        ? null : descripcion.trim(),
                precio,
                0,
                true,
                stockMinimo,
                stockMaximo
        );

        return repositorio.registrar(producto);
    }

    // COMPATIBILIDAD CON MODULOS ANTERIORES

    public boolean registrarProducto(
            String nombre,
            double precio,
            int stock) {

        if (stock != 0) {
            return false;
        }

        return registrarProducto(
                nombre, null, precio, 5, 100
        );
    }

    // BUSCAR PRODUCTO POR NOMBRE

    public Producto buscarProducto(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        return repositorio.buscar(nombre.trim());
    }

    // BUSCAR PRODUCTO POR ID

    public Producto buscarPorId(int idProducto) {

        if (idProducto <= 0) {
            return null;
        }

        return repositorio.buscarPorId(idProducto);
    }

    // ACTUALIZAR DATOS COMPLETOS

    public boolean actualizarProducto(
            int idProducto,
            String nombre,
            String descripcion,
            double precio,
            int stockMinimo,
            int stockMaximo) {

        if (idProducto <= 0
                || !validarDatos(nombre, descripcion,
                        precio, stockMinimo, stockMaximo)) {

            return false;
        }

        Producto producto =
                repositorio.buscarPorId(idProducto);

        if (producto == null || !producto.isEstado()) {
            return false;
        }

        producto.setNombreProducto(nombre.trim());

        producto.setDescripcionProducto(
                descripcion == null
                        ? null : descripcion.trim()
        );

        producto.setPrecioProducto(precio);
        producto.setStockMinimo(stockMinimo);
        producto.setStockMaximo(stockMaximo);

        return repositorio.actualizar(producto);
    }

    // COMPATIBILIDAD CON MODULOS ANTERIORES
    // No permite modificar directamente el stock.

    public boolean actualizarProducto(
            String nombre,
            double precio,
            int stock) {

        if (nombre == null
                || nombre.isBlank()
                || !Double.isFinite(precio)
                || precio < 0
                || stock < 0) {

            return false;
        }

        return repositorio.actualizar(
                nombre.trim(), precio, stock
        );
    }

    // ELIMINACION LOGICA

    public boolean eliminarProducto(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        return repositorio.eliminar(nombre.trim());
    }

    // ACTIVAR O DESACTIVAR PRODUCTO

    public boolean cambiarEstado(
            int idProducto,
            boolean estado) {

        if (idProducto <= 0) {
            return false;
        }

        return repositorio.cambiarEstado(
                idProducto, estado
        );
    }

    // LISTAR PRODUCTOS ACTIVOS

    public ArrayList<Producto> listarProductos() {

        return repositorio.listar();
    }

    // LISTAR TODOS LOS PRODUCTOS

    public ArrayList<Producto> listarTodos() {

        return repositorio.listarTodos();
    }

    // ESTADO DEL STOCK POR PRODUCTO

    public String estadoStock(Producto producto) {

        if (producto == null) {
            return "No disponible";
        }

        return producto.getEstadoStock();
    }

    // COMPATIBILIDAD CON EL DASHBOARD ANTERIOR

    public String estadoStock(int stock) {

        if (stock <= 0) {
            return "Agotado";
        }

        if (stock <= STOCK_BAJO) {
            return "Bajo";
        }

        return "Disponible";
    }

    // CONTAR PRODUCTOS CON STOCK BAJO

    public int productosConAlerta() {

        int cantidad = 0;

        for (Producto producto : repositorio.listar()) {

            if (producto.getStock()
                    <= producto.getStockMinimo()) {

                cantidad++;
            }
        }

        return cantidad;
    }

    // CONTAR UNIDADES DISPONIBLES

    public long totalUnidades() {

        long total = 0;

        for (Producto producto : repositorio.listar()) {

            total += producto.getStock();
        }

        return total;
    }

    // CONTAR PRODUCTOS AGOTADOS

    public int productosAgotados() {

        int cantidad = 0;

        for (Producto producto : repositorio.listar()) {

            if (producto.getStock() == 0) {

                cantidad++;
            }
        }

        return cantidad;
    }

    // CONTAR PRODUCTOS CON EXCESO DE STOCK

    public int productosConExceso() {

        int cantidad = 0;

        for (Producto producto : repositorio.listar()) {

            if (producto.getStock()
                    > producto.getStockMaximo()) {

                cantidad++;
            }
        }

        return cantidad;
    }
}
