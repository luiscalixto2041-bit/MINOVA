/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import modelo.Producto;
import modelo.Usuario;
import modelo.MovimientoInventario;

import repositorio.InventarioRepositorio;
import repositorio.ProductoRepositorio;

import seguridad.Permisos;

public class InventarioControlador {

    private final InventarioRepositorio repositorio =
            new InventarioRepositorio();

    private final ProductoRepositorio productos =
            new ProductoRepositorio();

    private final Usuario usuarioActual;

    // CONSTRUCTOR

    public InventarioControlador(Usuario usuarioActual) {

        this.usuarioActual = usuarioActual;

        validarPermisos();
    }

    // VALIDAR PERMISOS

    private void validarPermisos() {

        if (usuarioActual == null
                || !usuarioActual.isEstado()
                || !Permisos.puedeAcceder(
                        usuarioActual, "INVENTARIO")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para acceder al inventario."
            );
        }
    }

    // REGISTRAR MOVIMIENTO

    public int registrarMovimiento(
            int idProducto,
            String tipo,
            int cantidad,
            String motivo) throws SQLException {

        validarPermisos();

        if (!Permisos.puedeEditar(
                usuarioActual, "INVENTARIO")) {

            throw new IllegalArgumentException(
                    "No tienes permisos para registrar movimientos."
            );
        }

        if (idProducto <= 0) {

            throw new IllegalArgumentException(
                    "Selecciona un producto valido."
            );
        }

        if (cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (tipo == null || tipo.isBlank()) {

            throw new IllegalArgumentException(
                    "Selecciona el tipo de movimiento."
            );
        }

        tipo = tipo.trim().toUpperCase(Locale.ROOT);

        if (!tipo.equals("ENTRADA")
                && !tipo.equals("SALIDA")) {

            throw new IllegalArgumentException(
                    "Tipo de movimiento no valido."
            );
        }

        if (motivo == null
                || motivo.isBlank()
                || motivo.trim().length() > 255) {

            throw new IllegalArgumentException(
                    "Ingresa un motivo de hasta 255 caracteres."
            );
        }

        return repositorio.registrarMovimiento(
                usuarioActual,
                idProducto,
                tipo,
                cantidad,
                motivo.trim()
        );
    }

    // LISTAR HISTORIAL

    public ArrayList<MovimientoInventario>
            listarMovimientos() throws SQLException {

        validarPermisos();

        return repositorio.listarMovimientos(
                usuarioActual
        );
    }

    // LISTAR PRODUCTOS ACTIVOS

    public ArrayList<Producto> listarProductos() {

        validarPermisos();

        return productos.listar();
    }

    // CONSULTAR PRODUCTO POR ID

    public Producto buscarProducto(int idProducto) {

        validarPermisos();

        return productos.buscarPorId(idProducto);
    }

    // TOTAL DE PRODUCTOS CONTROLADOS

    public int totalProductos() {

        validarPermisos();

        return productos.listar().size();
    }

    // TOTAL DE UNIDADES

    public long totalUnidades() {

        validarPermisos();

        long total = 0;

        for (Producto producto : productos.listar()) {

            total += producto.getStock();
        }

        return total;
    }

    // PRODUCTOS CON EXISTENCIAS BAJAS

    public int productosConAlerta() {

        validarPermisos();

        int cantidad = 0;

        for (Producto producto : productos.listar()) {

            if (producto.getStock()
                    <= producto.getStockMinimo()) {

                cantidad++;
            }
        }

        return cantidad;
    }

    // PRODUCTOS AGOTADOS

    public int productosAgotados() {

        validarPermisos();

        int cantidad = 0;

        for (Producto producto : productos.listar()) {

            if (producto.getStock() == 0) {

                cantidad++;
            }
        }

        return cantidad;
    }

    // PRODUCTOS CON EXCESO

    public int productosConExceso() {

        validarPermisos();

        int cantidad = 0;

        for (Producto producto : productos.listar()) {

            if (producto.getStock()
                    > producto.getStockMaximo()) {

                cantidad++;
            }
        }

        return cantidad;
    }
}

