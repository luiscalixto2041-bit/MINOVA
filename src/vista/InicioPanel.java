package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import controlador.PedidoControlador;
import controlador.ProductoControlador;
import controlador.ReservaControlador;

public class InicioPanel extends JPanel implements Refrescable {

    private final PedidoControlador pedidoControlador = new PedidoControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();
    private final ReservaControlador reservaControlador = new ReservaControlador();

    private JLabel lblPedidos;
    private JLabel lblReservas;
    private JLabel lblMesasLibres;
    private JLabel lblAlertas;

    public InicioPanel(MainFrame marco) {

        setLayout(new BorderLayout(10, 20));
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // BIENVENIDA
        JLabel titulo = Tema.titulo("Bienvenido");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));

        JPanel bienvenida = new JPanel(new GridLayout(0, 1, 0, 8));
        bienvenida.setOpaque(false);
        bienvenida.add(titulo);
        bienvenida.add(Tema.subtitulo("Resumen de su restaurante"));

        add(bienvenida, BorderLayout.NORTH);

        // TARJETAS DE RESUMEN
        lblPedidos = new JLabel("0");
        lblReservas = new JLabel("0");
        lblMesasLibres = new JLabel("0");
        lblAlertas = new JLabel("0");

        JPanel tarjetas = new JPanel(new GridLayout(2, 2, 18, 18));
        tarjetas.setOpaque(false);
        tarjetas.add(Tema.tarjeta("Pedidos", lblPedidos, Tema.OSCURO));
        tarjetas.add(Tema.tarjeta("Reservas", lblReservas, Tema.AMBAR));
        tarjetas.add(Tema.tarjeta("Mesas libres hoy", lblMesasLibres, Tema.VERDE));
        tarjetas.add(Tema.tarjeta("Alertas de inventario", lblAlertas, Tema.ROJO));

        add(tarjetas, BorderLayout.CENTER);

        // ACCESO RAPIDO
        JButton btnNuevoPedido = Tema.boton("+ NUEVO PEDIDO", Tema.AMBAR);
        btnNuevoPedido.addActionListener(e -> marco.ir("PEDIDOS"));

        JPanel acceso = new JPanel(new BorderLayout(0, 8));
        acceso.setOpaque(false);
        acceso.add(Tema.seccion("ACCESO RÁPIDO"), BorderLayout.NORTH);
        acceso.add(Tema.filaBotones(btnNuevoPedido), BorderLayout.CENTER);

        add(acceso, BorderLayout.SOUTH);

        refrescar();
    }

    @Override
    public void refrescar() {

        lblPedidos.setText(String.valueOf(pedidoControlador.listarPedidos().size()));
        lblReservas.setText(String.valueOf(reservaControlador.listarReservas().size()));
        lblMesasLibres.setText(String.valueOf(reservaControlador.mesasLibresHoy()));
        lblAlertas.setText(String.valueOf(productoControlador.productosConAlerta()));
    }
}
