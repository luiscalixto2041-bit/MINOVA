/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.Producto;
import repositorio.ProductoRepositorio;

public class ProductoControlador {

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
}
