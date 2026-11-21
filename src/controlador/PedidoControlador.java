/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package controlador;

import modelo.Pedido;
import repositorio.PedidoRepositorio;
import java.util.ArrayList;

public class PedidoControlador {

    private final PedidoRepositorio repositorio =
            new PedidoRepositorio();

    public boolean registrarPedido(
            int idPedido,
            String nombreProducto,
            int cantidad,
            String metodoPago,
            double valorPago) {

        if (idPedido <= 0 ||
            nombreProducto == null ||
            nombreProducto.isBlank() ||
            cantidad <= 0 ||
            metodoPago == null ||
            metodoPago.isBlank() ||
            !Double.isFinite(valorPago) ||
            valorPago < 0) {

            return false;
        }

        Pedido pedido = new Pedido(
                idPedido,
                nombreProducto.trim(),
                cantidad,
                metodoPago.trim(),
                valorPago
        );

        return repositorio.registrar(pedido);
    }

    public Pedido buscarPedido(int idPedido) {
        return repositorio.buscar(idPedido);
    }

    public ArrayList<Pedido> listarPedidos() {
        return repositorio.listar();
    }
}
