/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import java.util.ArrayList;
import modelo.Producto;

public class ProductoRepositorio {

    private static final ArrayList<Producto> productos = new ArrayList<>();

    // REGISTRAR PRODUCTO
    public boolean registrar(Producto producto) {
        if (producto == null
                || producto.getNombreProducto() == null
                || producto.getNombreProducto().isBlank()
                || producto.getPrecioProducto() < 0
                || producto.getStock() < 0
                || buscar(producto.getNombreProducto()) != null) {
            return false;
        }

        productos.add(producto);
        return true;
    }

    // BUSCAR PRODUCTO
    public Producto buscar(String nombre) {
        if (nombre == null) {
            return null;
        }

        for (Producto producto : productos) {
            if (producto.getNombreProducto().equalsIgnoreCase(nombre.trim())) {
                return producto;
            }
        }

        return null;
    }

    // ACTUALIZAR PRODUCTO
    public boolean actualizar(String nombre, double precio, int stock) {
        Producto producto = buscar(nombre);

        if (producto != null && precio >= 0 && stock >= 0) {
            producto.setPrecioProducto(precio);
            producto.setStock(stock);
            return true;
        }

        return false;
    }

    // ELIMINAR PRODUCTO
    public boolean eliminar(String nombre) {
        Producto producto = buscar(nombre);

        if (producto != null) {
            productos.remove(producto);
            return true;
        }

        return false;
    }
}
