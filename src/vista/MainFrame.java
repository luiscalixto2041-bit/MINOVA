
package vista;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import modelo.Usuario;
import seguridad.Permisos;

public class MainFrame extends JFrame {

    private final Usuario usuarioActual;

    private CardLayout cards;
    private JPanel contenido;
    private HashMap<String, JPanel> paneles;

    private final String[] secciones = {
        "INICIO", "PEDIDOS", "CLIENTES",
        "RESERVAS", "MESAS", "PRODUCTOS",
        "INVENTARIO", "PAGOS", "REPORTES",
        "USUARIOS"
    };

    private JButton[] botones;

    public MainFrame(Usuario usuario) {

        super("MINOVA - INICIO");

        this.usuarioActual = usuario;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        cards = new CardLayout();
        contenido = new JPanel(cards);

        botones = new JButton[secciones.length];
        paneles = new HashMap<>();

        // MODULO INICIO

        if (Permisos.puedeAcceder(usuario, "INICIO")) {

            paneles.put(
                    "INICIO",
                    new InicioPanel(this)
            );
        }

        // MODULO PEDIDOS

        if (Permisos.puedeAcceder(usuario, "PEDIDOS")) {

            paneles.put(
                    "PEDIDOS",
                    new PedidosPanel(usuarioActual)
            );
        }

        // MODULO CLIENTES

        if (Permisos.puedeAcceder(usuario, "CLIENTES")) {

            paneles.put(
                    "CLIENTES",
                    new ClientesPanel()
            );
        }

        // MODULO RESERVAS CON MYSQL

        ReservasPanel reservasPanel = null;

        if (Permisos.puedeEditar(usuario, "RESERVAS")) {

            reservasPanel =
                    new ReservasPanel(usuarioActual);

            paneles.put(
                    "RESERVAS",
                    reservasPanel
            );
        }

        // MODULO MESAS CON MYSQL

        if (Permisos.puedeAcceder(usuario, "MESAS")) {

            paneles.put(
                    "MESAS",
                    new MesasPanel(
                            this,
                            reservasPanel
                    )
            );
        }

        // MODULO PRODUCTOS CON MYSQL

        if (Permisos.puedeAcceder(usuario, "PRODUCTOS")) {

            paneles.put(
                    "PRODUCTOS",
                    new ProductosPanel(usuarioActual)
            );
        }

        // MODULO INVENTARIO CON MYSQL

        if (Permisos.puedeAcceder(usuario, "INVENTARIO")) {

            paneles.put(
                    "INVENTARIO",
                    new InventarioPanel(usuarioActual)
            );
        }

        // MODULO PAGOS

        if (Permisos.puedeAcceder(usuarioActual, "PAGOS")) {
    paneles.put("PAGOS", new PagosPanel(usuarioActual));
}

        // MODULO REPORTES

        if (Permisos.puedeAcceder(usuarioActual, "REPORTES")) {
    paneles.put("REPORTES", new ReportesPanel(usuarioActual));
}

        // MODULO GESTION DE EMPLEADOS

        if (Permisos.puedeAcceder(usuario, "USUARIOS")) {

            paneles.put(
                    "USUARIOS",
                    new UsuariosPanel(usuarioActual)
            );
        }

        // AGREGAR PANELES AL CARDLAYOUT

        for (String seccion : secciones) {

            if (paneles.containsKey(seccion)) {

                contenido.add(
                        paneles.get(seccion),
                        seccion
                );
            }
        }

        add(
                crearCabecera(usuario),
                BorderLayout.NORTH
        );

        add(
                crearMenu(),
                BorderLayout.WEST
        );

        add(
                contenido,
                BorderLayout.CENTER
        );

        ir("INICIO");
    }

    // OBTENER USUARIO DE LA SESION

    public Usuario getUsuarioActual() {

        return usuarioActual;
    }

    // MENU PRINCIPAL

    private JPanel crearMenu() {

        JPanel menu =
                new JPanel(new BorderLayout());

        menu.setBackground(Tema.OSCURO);

        menu.setPreferredSize(
                new Dimension(210, 0)
        );

        menu.setBorder(
                new EmptyBorder(18, 12, 18, 12)
        );

        JLabel lblCategoria =
                new JLabel("MENÚ PRINCIPAL");

        lblCategoria.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        lblCategoria.setForeground(Color.WHITE);

        menu.add(
                lblCategoria,
                BorderLayout.NORTH
        );

        JPanel opciones = new JPanel();

        opciones.setLayout(
                new BoxLayout(
                        opciones,
                        BoxLayout.Y_AXIS
                )
        );

        opciones.setOpaque(false);

        opciones.setBorder(
                new EmptyBorder(25, 0, 10, 0)
        );

        for (int i = 0;
                i < secciones.length; i++) {

            String seccion = secciones[i];

            if (!Permisos.puedeAcceder(
                    usuarioActual, seccion)) {

                continue;
            }

            if (!paneles.containsKey(seccion)) {

                continue;
            }

            botones[i] = Tema.boton(
                    seccion,
                    Tema.GRIS
            );

            botones[i].setHorizontalAlignment(
                    SwingConstants.LEFT
            );

            botones[i].setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            36
                    )
            );

            botones[i].setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );

            botones[i].addActionListener(
                    e -> ir(seccion)
            );

            opciones.add(botones[i]);

            opciones.add(
                    Box.createVerticalStrut(7)
            );
        }

        JScrollPane scroll =
                new JScrollPane(opciones);

        scroll.setBorder(null);
        scroll.setOpaque(false);

        scroll.getViewport().setOpaque(false);

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        menu.add(
                scroll,
                BorderLayout.CENTER
        );

        // CERRAR SESION

        JButton salir = Tema.boton(
                "CERRAR SESIÓN",
                Tema.ROJO
        );

        salir.addActionListener(e -> {

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            this,
                            "¿Deseas cerrar sesión?",
                            "Cerrar sesión",
                            JOptionPane.YES_NO_OPTION
                    );

            if (respuesta
                    == JOptionPane.YES_OPTION) {

                dispose();

                new LoginFrame()
                        .setVisible(true);
            }
        });

        menu.add(
                salir,
                BorderLayout.SOUTH
        );

        return menu;
    }

    // CABECERA

    private JPanel crearCabecera(
            Usuario usuario) {

        JPanel cabecera = new JPanel(
                new GridLayout(2, 1, 0, 5)
        );

        cabecera.setBackground(Tema.OSCURO);

        JLabel marca = new JLabel("MINOVA");

        marca.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        26
                )
        );

        marca.setForeground(Tema.AMBAR);

        JLabel restaurante =
                new JLabel("BITS ANGUS BURGER");

        restaurante.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                )
        );

        restaurante.setForeground(Color.WHITE);

        JLabel nombre = new JLabel(
                "Usuario: "
                + usuario.getNombreUsuario()
        );

        nombre.setForeground(Color.WHITE);

        nombre.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        JLabel rol = new JLabel(
                "Rol: " + usuario.getRol()
        );

        rol.setForeground(Tema.GRIS_CLARO);

        rol.setHorizontalAlignment(
                SwingConstants.RIGHT
        );

        JPanel fila1 =
                new JPanel(new BorderLayout());

        fila1.setOpaque(false);

        fila1.add(
                marca,
                BorderLayout.WEST
        );

        fila1.add(
                nombre,
                BorderLayout.EAST
        );

        JPanel fila2 =
                new JPanel(new BorderLayout());

        fila2.setOpaque(false);

        fila2.add(
                restaurante,
                BorderLayout.WEST
        );

        fila2.add(
                rol,
                BorderLayout.EAST
        );

        cabecera.add(fila1);
        cabecera.add(fila2);

        cabecera.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 1, 0,
                                Tema.GRIS
                        ),
                        new EmptyBorder(
                                8, 12, 12, 12
                        )
                )
        );

        return cabecera;
    }

    // NAVEGACION SEGUN PERMISOS

    public void ir(String nombre) {

        if (!Permisos.puedeAcceder(
                usuarioActual, nombre)) {

            Tema.error(
                    this,
                    "No tienes permisos para acceder a este módulo."
            );

            return;
        }

        JPanel panel = paneles.get(nombre);

        if (panel == null) {

            Tema.error(
                    this,
                    "El módulo no está disponible."
            );

            return;
        }

        if (panel instanceof Refrescable) {

            ((Refrescable) panel).refrescar();
        }

        setTitle("MINOVA - " + nombre);

        cards.show(contenido, nombre);

        for (int i = 0;
                i < secciones.length; i++) {

            if (botones[i] == null) {

                continue;
            }

            if (secciones[i].equals(nombre)) {

                botones[i].setBackground(
                        Tema.MOSTAZA
                );

            } else {

                botones[i].setBackground(
                        Tema.GRIS
                );
            }
        }
    }
}
