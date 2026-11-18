/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package repositorio;

import java.util.ArrayList;
import modelo.Pedido;

public class PedidoRepositorio {

    private static final ArrayList<Pedido> pedidos = new ArrayList<>();

    public boolean registrar(Pedido pedido) {

        if (pedido == null) {
            return false;
        }

        if (pedido.getIdPedido() <= 0 ||
            pedido.getNombreProducto() == null ||
            pedido.getNombreProducto().isBlank() ||
            pedido.getCantidad() <= 0 ||
            pedido.getMetodoPago() == null ||
            pedido.getMetodoPago().isBlank() ||
            !Double.isFinite(pedido.getValorPago()) ||
            pedido.getValorPago() < 0) {

            return false;
        }

        for (Pedido existente : pedidos) {
            if (existente.getIdPedido() == pedido.getIdPedido()) {
                return false;
            }
        }

        pedidos.add(pedido);
        return true;
    }

    public Pedido buscar(int idPedido) {

        for (Pedido pedido : pedidos) {

            if (pedido.getIdPedido() == idPedido) {
                return pedido;
            }
        }

        return null;
    }

    public ArrayList<Pedido> listar() {
        return new ArrayList<>(pedidos);
    }
}

