
package vista;

import java.awt.*;
import javax.swing.*;
import modelo.Usuario;
import repositorio.UsuarioRepositorio;
import java.sql.SQLException;

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

        GridBagConstraints gbc = new GridBagConstraints();

        // TITULO
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

        // BIENVENIDA
        JLabel lblBienvenido = new JLabel("BIENVENIDO DE VUELTA");
        lblBienvenido.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblBienvenido.setForeground(Tema.OSCURO);
        lblBienvenido.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);

        panelFormulario.add(lblBienvenido, gbc);

        // USUARIO
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

        // CONTRASENA
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

        // BOTON INICIAR SESION
        JButton btnEntrar = new JButton("INICIAR SESIÓN");

        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEntrar.setBackground(Tema.MOSTAZA);
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        btnEntrar.addActionListener(e -> entrar());

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        panelFormulario.add(btnEntrar, gbc);

        // MENSAJE INFORMATIVO
        JLabel lblInformacion = new JLabel(
                "Acceso exclusivo para personal autorizado"
        );

        lblInformacion.setFont(
                new Font("SansSerif", Font.PLAIN, 11)
        );

        lblInformacion.setForeground(Tema.GRIS);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        panelFormulario.add(lblInformacion, gbc);

        // PERMITIR INICIAR SESION CON ENTER
        getRootPane().setDefaultButton(btnEntrar);
    }

    // AUTENTICACION CON MYSQL
    private void entrar() {

        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtClave.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {

            Tema.error(
                    this,
                    "Complete usuario y contraseña."
            );

            return;
        }

        UsuarioRepositorio repositorio =
                new UsuarioRepositorio();

        try {

            Usuario u = repositorio.login(
                    usuario,
                    contrasena
            );

            if (u == null) {

                Tema.error(
                        this,
                        "Usuario o contraseña incorrectos."
                );

                txtClave.setText("");
                txtClave.requestFocus();

                return;
            }

            txtClave.setText("");

            new MainFrame(u).setVisible(true);

            dispose();

        } catch (SQLException e) {

            Tema.error(
                    this,
                    "No se pudo consultar la base de datos."
            );

            System.err.println(e.getMessage());
        }
    }
}
