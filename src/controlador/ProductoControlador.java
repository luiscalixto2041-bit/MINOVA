package controlador;

import modelo.Producto;
import repositorio.ProductoRepositorio;
import java.util.ArrayList;

public class ProductoControlador {

    public static final int STOCK_BAJO = 10;

    private final ProductoRepositorio repositorio;

    public ProductoControlador() {
        repositorio = new ProductoRepositorio();
    }

    // REGISTRAR PRODUCTO
    public boolean registrarProducto(String nombre, double precio, int stock) {

        if (nombre == null || nombre.isBlank()
                || !Double.isFinite(precio)
                || precio < 0 || stock < 0) {
            return false;
        }

        Producto producto = new Producto(nombre.trim(), precio, stock);

        return repositorio.registrar(producto);
    }

    // BUSCAR PRODUCTO
    public Producto buscarProducto(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return null;
        }

        return repositorio.buscar(nombre.trim());
    }

    // ACTUALIZAR PRODUCTO
    public boolean actualizarProducto(String nombre, double precio, int stock) {

        if (nombre == null || nombre.isBlank()
                || !Double.isFinite(precio)
                || precio < 0 || stock < 0) {
            return false;
        }

        return repositorio.actualizar(nombre.trim(), precio, stock);
    }

    // ELIMINAR PRODUCTO
    public boolean eliminarProducto(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return false;
        }

        return repositorio.eliminar(nombre.trim());
    }
    
    // LISTAR PRODUCTOS
    public ArrayList<Producto> listarProductos() {
        return repositorio.listar();
    }

    // ESTADO DEL STOCK: Disponible, Bajo o Agotado
    public String estadoStock(int stock) {
        if (stock <= 0) {
            return "Agotado";
        }
        if (stock <= STOCK_BAJO) {
            return "Bajo";
        }
        return "Disponible";
    }

    // CUANTOS PRODUCTOS ESTAN BAJOS O AGOTADOS
    public int productosConAlerta() {
        int cantidad = 0;

        for (Producto producto : repositorio.listar()) {
            if (producto.getStock() <= STOCK_BAJO) {
                cantidad++;
            }
        }

        return cantidad;
    }
}
