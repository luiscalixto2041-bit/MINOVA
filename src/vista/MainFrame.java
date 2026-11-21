package vista;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import modelo.Usuario;

public class MainFrame extends JFrame {

    private CardLayout cards;
    private JPanel contenido;
    private HashMap<String, JPanel> paneles;

    private String[] secciones = {
        "INICIO", "PEDIDOS", "CLIENTES",
        "RESERVAS", "MESAS", "PRODUCTOS",
        "INVENTARIO", "PAGOS", "REPORTES"
    };

    private JButton[] botones;

    public MainFrame(Usuario usuario) {

        super("MINOVA - INICIO");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1020, 640);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // panel con las secciones
        cards = new CardLayout();
        contenido = new JPanel(cards);

        botones = new JButton[secciones.length];

        // crear los paneles de cada seccion
        paneles = new HashMap<>();

        ReservasPanel reservasPanel = new ReservasPanel();

        paneles.put("INICIO", new InicioPanel(this));
        paneles.put("PEDIDOS", new PedidosPanel());
        paneles.put("CLIENTES", new ClientesPanel());
        paneles.put("RESERVAS", reservasPanel);
        paneles.put("MESAS", new MesasPanel(this, reservasPanel));
        paneles.put("PRODUCTOS", new ProductosPanel());
        paneles.put("INVENTARIO", new InventarioPanel());
        paneles.put("PAGOS", new PagosPanel());
        paneles.put("REPORTES", new ReportesPanel());

        // agregar secciones
        for (int i = 0; i < secciones.length; i++) {
            contenido.add(paneles.get(secciones[i]), secciones[i]);
        }

        add(crearCabecera(usuario), BorderLayout.NORTH);
        add(crearMenu(usuario), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        ir("INICIO");
    }

    private JPanel crearMenu(Usuario usuario) {

        JPanel menu = new JPanel(new BorderLayout());

        menu.setBackground(Tema.OSCURO);
        menu.setPreferredSize(new Dimension(210, 0));
        menu.setBorder(new EmptyBorder(18, 12, 18, 12));
        
        JLabel lblCategoria = new JLabel("MENÚ PRINCIPAL");
        lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCategoria.setForeground(Color.WHITE);
        menu.add(lblCategoria, BorderLayout.NORTH);
        
        // opciones
        JPanel opciones = new JPanel(new GridLayout(0, 1, 0, 7));
        opciones.setOpaque(false);
        opciones.setBorder(new EmptyBorder(25, 0, 10, 0));

        for (int i = 0; i < secciones.length; i++) {

            String seccion = secciones[i];

            botones[i] = Tema.boton(seccion, Tema.GRIS);
            botones[i].setHorizontalAlignment(SwingConstants.LEFT);

            botones[i].addActionListener(e -> ir(seccion));

            opciones.add(botones[i]);
        }
        menu.add(opciones, BorderLayout.CENTER);

        JButton salir = Tema.boton("CERRAR SESIÓN", Tema.ROJO);
        salir.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        menu.add(salir, BorderLayout.SOUTH);

        return menu;
    }
    
    private JPanel crearCabecera(Usuario usuario) {

        JPanel cabecera = new JPanel(new GridLayout(2, 1, 0, 5));
        cabecera.setBackground(Tema.OSCURO);
        cabecera.setBorder(new EmptyBorder(8, 12, 12, 12));

        JLabel marca = new JLabel("MINOVA");
        marca.setFont(new Font("SansSerif", Font.BOLD, 26));
        marca.setForeground(Tema.AMBAR);

        JLabel restaurante = new JLabel("BITS ANGUS BURGER");
        restaurante.setFont(new Font("SansSerif", Font.BOLD, 18));
        restaurante.setForeground(Color.WHITE);

        JLabel nombre = new JLabel(usuario.getNombreUsuario());
        nombre.setForeground(Color.WHITE);
        nombre.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel rol = new JLabel(usuario.getRol());
        rol.setForeground(Tema.GRIS_CLARO);
        rol.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel fila1 = new JPanel(new BorderLayout());
        fila1.setOpaque(false);
        fila1.add(marca, BorderLayout.WEST);
        fila1.add(nombre, BorderLayout.EAST);

        JPanel fila2 = new JPanel(new BorderLayout());
        fila2.setOpaque(false);
        fila2.add(restaurante, BorderLayout.WEST);
        fila2.add(rol, BorderLayout.EAST);

        cabecera.add(fila1);
        cabecera.add(fila2);

        cabecera.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GRIS),
            new EmptyBorder(8, 12, 12, 12)
        ));

        return cabecera;
    }

    public void ir(String nombre) {

        // recargar los datos del panel antes de mostrarlo
        JPanel panel = paneles.get(nombre);

        if (panel instanceof Refrescable) {
            ((Refrescable) panel).refrescar();
        }

        setTitle("MINOVA - " + nombre);
        cards.show(contenido, nombre);

        for (int i = 0; i < secciones.length; i++) {

            if (secciones[i].equals(nombre)) {
                botones[i].setBackground(Tema.MOSTAZA);
            } else {
                botones[i].setBackground(Tema.GRIS);
            }
        }
    }
}
