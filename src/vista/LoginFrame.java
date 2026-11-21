package vista;

import java.awt.*;
import javax.swing.*;
import modelo.Usuario;
import controlador.DataStore;

public class LoginFrame extends JFrame {
    
    private JTextField txtUsuario;
    private JPasswordField txtClave;

    public LoginFrame() {
        
        super("MINOVA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Tema.OSCURO);
        setLayout(new BorderLayout());

        JPanel contenedor = new JPanel(new BorderLayout());

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        JPanel panelImagen = new JPanel();

        panelFormulario.setBackground(Color.WHITE);
        panelImagen.setBackground(Tema.OSCURO);
        contenedor.setBackground(Tema.OSCURO);

        panelFormulario.setPreferredSize(new Dimension(400, 550));
        contenedor.add(panelFormulario, BorderLayout.WEST);
        contenedor.add(panelImagen, BorderLayout.CENTER);

        add(contenedor, BorderLayout.CENTER);

        // objeto para organizar los componentes
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblMinova = new JLabel("MINOVA");
        lblMinova.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblMinova.setForeground(Tema.AMBAR);
        lblMinova.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panelFormulario.add(lblMinova, gbc);

        JLabel lblBienvenido = new JLabel("BIENVENIDO DE VUELTA");
        lblBienvenido.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblBienvenido.setForeground(Tema.OSCURO);
        lblBienvenido.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        panelFormulario.add(lblBienvenido, gbc);

        JLabel lblUsuario = new JLabel("Usuario:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        panelFormulario.add(lblUsuario, gbc);

        txtUsuario = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelFormulario.add(txtUsuario, gbc);

        JLabel lblClave = new JLabel("Contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        panelFormulario.add(lblClave, gbc);

        txtClave = new JPasswordField();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelFormulario.add(txtClave, gbc);
        
        JButton btnEntrar = new JButton("INICIAR SESIÓN");
        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEntrar.setBackground(Tema.MOSTAZA);
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.addActionListener(e -> entrar());
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelFormulario.add(btnEntrar, gbc);
        
        JLabel lblRegistro = enlace("¿No tienes cuenta? Regístrate", () -> registrar());
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panelFormulario.add(lblRegistro, gbc);
    }

    private JLabel enlace(String texto, Runnable accion) {
        JLabel l = new JLabel("<html><u>" + texto + "</u></html>");
        l.setForeground(Tema.GRIS);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { accion.run(); }
        });
        return l;
    }

    private void entrar() {
        Usuario u = DataStore.get().login(txtUsuario.getText().trim(), new String(txtClave.getPassword()));
        if (u == null) {
            Tema.error(this, "Usuario o contraseña incorrectos.");
            return;
        }
        new MainFrame(u).setVisible(true);
        dispose();
    }

    private void registrar() {
        JTextField nombre = Tema.campo();
        JPasswordField clave = new JPasswordField();
        JComboBox<String> rol = new JComboBox<>(new String[]{"Mesero", "Administrador"});
        JPanel p = Tema.formulario(new String[]{"Usuario:", "Contraseña:", "Rol:"},
                new JComponent[]{nombre, clave, rol});
        p.setPreferredSize(new Dimension(300, 120));
        int r = JOptionPane.showConfirmDialog(this, p, "Registrarse",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        String n = nombre.getText().trim();
        String c = new String(clave.getPassword());
        if (n.isEmpty() || c.isEmpty()) { Tema.error(this, "Complete usuario y contraseña."); return; }
        if (DataStore.get().existeUsuario(n)) { Tema.error(this, "Ese usuario ya existe."); return; }
        DataStore.get().registrarUsuario(new Usuario(n, c, (String) rol.getSelectedItem()));
        Tema.info(this, "Usuario registrado. Ya puede iniciar sesión.");
    }
}
