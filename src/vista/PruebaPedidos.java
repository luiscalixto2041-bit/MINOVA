package vista;

import javax.swing.JFrame;
import controlador.ProductoControlador;

public class PruebaPedidos {

    public static void main(String[] args) {
        
        ProductoControlador pc = new ProductoControlador();

        pc.registrarProducto("Hamburguesa clásica", 15000, 20);
        pc.registrarProducto("Papas fritas", 6000, 30);

        JFrame ventana = new JFrame("Prueba de Pedidos");

        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(900, 600);
        ventana.setLocationRelativeTo(null);

        PedidosPanel panel = new PedidosPanel();

        ventana.add(panel);

        ventana.setVisible(true);
    }
}