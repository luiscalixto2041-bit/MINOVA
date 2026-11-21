package vista;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class Tema {

    public static final Color ROJO = new Color(0xD1332E);
    public static final Color AMBAR = new Color(0xE5A93C);
    public static final Color OSCURO = new Color(0x222222);
    public static final Color GRIS = new Color(0x666666);
    public static final Color GRIS_CLARO = new Color(0x999999);
    public static final Color FONDO = new Color(0xFAFAFA);
    public static final Color VERDE = new Color(0x4C9A52);
    public static final Color MOSTAZA = new Color(0xE5A93C);

    public static final Font FUENTE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font NEGRITA = new Font("SansSerif", Font.BOLD, 13);

    private Tema() {}

    public static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(Color.WHITE);
        b.setFont(NEGRITA);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        l.setForeground(OSCURO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(NEGRITA);
        l.setForeground(GRIS);
        return l;
    }

    public static JTextField campo() {
        JTextField t = new JTextField();
        t.setFont(FUENTE);
        t.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GRIS_CLARO, 1, true), new EmptyBorder(5, 8, 5, 8)));
        return t;
    }

    public static JPanel panelBase() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(FONDO);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JScrollPane tabla(JTable t) {
        t.setFont(FUENTE);
        t.setRowHeight(26);
        t.setSelectionBackground(AMBAR);
        t.setSelectionForeground(Color.BLACK);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setBackground(OSCURO);
        h.setForeground(Color.WHITE);
        h.setFont(NEGRITA);
        h.setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(GRIS_CLARO));
        return sp;
    }

    public static JPanel formulario(String[] etiquetas, JComponent[] campos) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 8);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < etiquetas.length; i++) {
            g.gridy = i;
            g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            JLabel l = new JLabel(etiquetas[i]);
            l.setFont(NEGRITA);
            l.setForeground(GRIS);
            p.add(l, g);
            g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            p.add(campos[i], g);
        }
        return p;
    }

    public static JPanel filaBotones(JButton... botones) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        for (JButton b : botones) p.add(b);
        return p;
    }

    public static JPanel barraBusqueda(String titulo, JTextField buscador) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.add(subtitulo(titulo), BorderLayout.WEST);
        p.add(buscador, BorderLayout.CENTER);
        return p;
    }

    public static void alCambiar(JTextField campo, Runnable accion) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { accion.run(); }
            public void removeUpdate(DocumentEvent e) { accion.run(); }
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        });
    }

    public static void info(Component c, String msg) {
        JOptionPane.showMessageDialog(c, msg, "MINOVA", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component c, String msg) {
        JOptionPane.showMessageDialog(c, msg, "MINOVA", JOptionPane.WARNING_MESSAGE);
    }

    public static String dinero(double v) {
        return String.format("$ %,.0f", v).replace(',', '.');
    }

    // MARCO OSCURO CON TITULO AMBAR (el mismo diseno de ClientesPanel)
    public static JPanel marcoModulo(String titulo, JPanel contenido) {
        JPanel marco = new JPanel(new BorderLayout(0, 15));
        marco.setBackground(OSCURO);
        marco.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(AMBAR);

        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(20, 20, 20, 20));

        marco.add(lblTitulo, BorderLayout.NORTH);
        marco.add(contenido, BorderLayout.CENTER);

        return marco;
    }

    // SUBTITULO DE SECCION DENTRO DEL PANEL BLANCO
    public static JLabel seccion(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 17));
        l.setForeground(OSCURO);
        return l;
    }

    // CAMPO CON LA ETIQUETA ENCIMA
    public static JPanel campoConEtiqueta(String texto, JComponent campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);

        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 12));
        etiqueta.setForeground(OSCURO);

        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);

        return panel;
    }

    // TARJETA CON UN NUMERO (contadores de Inicio, Inventario y Mesas)
    public static JPanel tarjeta(String texto, JLabel valor, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(color, 2, true));

        JLabel lblTexto = new JLabel(texto, SwingConstants.CENTER);
        lblTexto.setFont(NEGRITA);
        lblTexto.setForeground(Color.WHITE);
        lblTexto.setOpaque(true);
        lblTexto.setBackground(color);
        lblTexto.setBorder(new EmptyBorder(5, 0, 5, 0));

        valor.setFont(new Font("SansSerif", Font.BOLD, 26));
        valor.setHorizontalAlignment(SwingConstants.CENTER);
        valor.setForeground(color);

        panel.add(lblTexto, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);

        return panel;
    }
} 