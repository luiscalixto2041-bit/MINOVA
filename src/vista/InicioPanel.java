
package vista;

import controlador.InicioControlador;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class InicioPanel extends JPanel implements Refrescable {

    private final MainFrame marco;
    private final InicioControlador controlador;

    private final Map<String, JLabel> indicadores =
            new HashMap<>();

    private JPanel tarjetas;

    private JLabel lblActualizacion;
    private JLabel lblTotalSemanal;

    private GraficaVentas grafica;

    private static final ZoneId ZONA =
            ZoneId.of("America/Bogota");

    // CONSTRUCTOR

    public InicioPanel(MainFrame marco) {

        this.marco = marco;

        this.controlador = new InicioControlador(
                marco.getUsuarioActual()
        );

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);

        JPanel contenido = new JPanel();

        contenido.setLayout(
                new BoxLayout(
                        contenido,
                        BoxLayout.Y_AXIS
                )
        );

        contenido.setBackground(Tema.FONDO);

        contenido.setBorder(
                new EmptyBorder(24, 28, 24, 28)
        );

        contenido.add(crearBienvenida());
        contenido.add(Box.createVerticalStrut(20));

        contenido.add(crearIndicadores());
        contenido.add(Box.createVerticalStrut(20));

        if (controlador.esAdministrador()) {

            contenido.add(crearGrafica());
            contenido.add(Box.createVerticalStrut(20));
        }

        contenido.add(crearAccesosRapidos());
        contenido.add(Box.createVerticalStrut(15));

        contenido.add(crearPie());

        JScrollPane scroll =
                new JScrollPane(contenido);

        scroll.setBorder(null);

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.getVerticalScrollBar()
                .setUnitIncrement(18);

        add(scroll, BorderLayout.CENTER);

        refrescar();
    }

    // BIENVENIDA PERSONALIZADA

    private JPanel crearBienvenida() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 10)
        );

        panel.setOpaque(false);

        JLabel titulo = Tema.titulo(
                "Bienvenido, "
                + controlador.obtenerNombre()
        );

        titulo.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        JLabel subtitulo = Tema.subtitulo(
                controlador.obtenerMensajeBienvenida()
        );

        JLabel rol = new JLabel(
                "ROL: " + controlador.obtenerRol()
        );

        rol.setForeground(Tema.OSCURO);

        rol.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );

        JPanel informacion = new JPanel(
                new GridLayout(0, 1, 0, 6)
        );

        informacion.setOpaque(false);

        informacion.add(titulo);
        informacion.add(subtitulo);
        informacion.add(rol);

        JButton btnActualizar = Tema.boton(
                "ACTUALIZAR",
                Tema.OSCURO
        );

        btnActualizar.addActionListener(
                e -> refrescar()
        );

        JPanel acciones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        acciones.setOpaque(false);
        acciones.add(btnActualizar);

        panel.add(informacion, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.EAST);

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        115
                )
        );

        return panel;
    }

    // TARJETAS SEGUN EL ROL

    private JPanel crearIndicadores() {

        tarjetas = new JPanel(
                new GridLayout(0, 3, 12, 12)
        );

        tarjetas.setOpaque(false);

        boolean admin =
                controlador.esAdministrador();

        boolean cajero =
                controlador.esCajero();

        boolean mesero =
                controlador.esMesero();

        boolean inventario =
                controlador.esInventario();

        if (admin || cajero) {

            agregarIndicador(
                    "ingresosHoy",
                    "Ingresos de hoy",
                    Tema.VERDE
            );

            agregarIndicador(
                    "pendientesPago",
                    "Pendientes de pago",
                    Tema.AMBAR
            );
        }

        if (admin || cajero || mesero) {

            agregarIndicador(
                    "pedidosActivos",
                    "Pedidos activos",
                    Tema.OSCURO
            );

            agregarIndicador(
                    "mesasDisponibles",
                    "Mesas disponibles",
                    Tema.VERDE
            );

            agregarIndicador(
                    "mesasOcupadas",
                    "Mesas ocupadas",
                    Tema.ROJO
            );
        }

        if (admin || mesero) {

            agregarIndicador(
                    "reservasHoy",
                    "Reservas activas de hoy",
                    Tema.AMBAR
            );
        }

        agregarIndicador(
                "productosActivos",
                "Productos activos",
                Tema.OSCURO
        );

        if (admin || inventario) {

            agregarIndicador(
                    "totalUnidades",
                    "Unidades en inventario",
                    Tema.OSCURO
            );

            agregarIndicador(
                    "alertasInventario",
                    "Alertas de inventario",
                    Tema.ROJO
            );

            agregarIndicador(
                    "productosAgotados",
                    "Productos agotados",
                    Tema.ROJO
            );
        }

        if (admin) {

            agregarIndicador(
                    "clientesActivos",
                    "Clientes activos",
                    Tema.OSCURO
            );

            agregarIndicador(
                    "empleadosActivos",
                    "Empleados activos",
                    Tema.VERDE
            );
        }

        int filas = (indicadores.size() + 2) / 3;

        tarjetas.setPreferredSize(
                new Dimension(
                        700,
                        filas * 95 + (filas - 1) * 12
                )
        );

        tarjetas.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        filas * 95 + (filas - 1) * 12
                )
        );

        return tarjetas;
    }

    // AGREGAR INDICADOR

    private void agregarIndicador(
            String clave,
            String titulo,
            Color color) {

        JLabel valor = new JLabel("--");

        indicadores.put(clave, valor);

        tarjetas.add(
                Tema.tarjeta(
                        titulo,
                        valor,
                        color
                )
        );
    }

    // GRAFICA EXCLUSIVA DEL ADMINISTRADOR

    private JPanel crearGrafica() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 10)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(
                        15, 15, 15, 15
                )
        );

        JLabel titulo = new JLabel(
                "INGRESOS DE LOS ULTIMOS 7 DIAS"
        );

        titulo.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        15
                )
        );

        lblTotalSemanal = new JLabel("$ 0");

        lblTotalSemanal.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        20
                )
        );

        lblTotalSemanal.setForeground(Tema.VERDE);

        JPanel encabezado = new JPanel(
                new BorderLayout()
        );

        encabezado.setOpaque(false);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(
                lblTotalSemanal,
                BorderLayout.EAST
        );

        grafica = new GraficaVentas();

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(grafica, BorderLayout.CENTER);

        panel.setPreferredSize(
                new Dimension(700, 260)
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        260
                )
        );

        return panel;
    }

    // ACCESOS RAPIDOS SEGUN PERMISOS

    private JPanel crearAccesosRapidos() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 10)
        );

        panel.setOpaque(false);

        JPanel botones = new JPanel(
                new GridLayout(0, 3, 10, 10)
        );

        botones.setOpaque(false);

        agregarAcceso(
                botones,
                "PEDIDOS",
                "+ NUEVO PEDIDO",
                Tema.AMBAR
        );

        agregarAcceso(
                botones,
                "CLIENTES",
                "CLIENTES",
                Tema.OSCURO
        );

        agregarAcceso(
                botones,
                "RESERVAS",
                "RESERVAS",
                Tema.AMBAR
        );

        agregarAcceso(
                botones,
                "MESAS",
                "MESAS",
                Tema.OSCURO
        );

        agregarAcceso(
                botones,
                "PAGOS",
                "REGISTRAR PAGO",
                Tema.VERDE
        );

        agregarAcceso(
                botones,
                "PRODUCTOS",
                "PRODUCTOS",
                Tema.OSCURO
        );

        agregarAcceso(
                botones,
                "INVENTARIO",
                "INVENTARIO",
                Tema.AMBAR
        );

        agregarAcceso(
                botones,
                "REPORTES",
                "REPORTES",
                Tema.OSCURO
        );

        agregarAcceso(
                botones,
                "USUARIOS",
                "EMPLEADOS",
                Tema.OSCURO
        );

        panel.add(
                Tema.seccion("ACCESOS RAPIDOS"),
                BorderLayout.NORTH
        );

        panel.add(botones, BorderLayout.CENTER);

        int filas =
                (botones.getComponentCount() + 2) / 3;

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        40 + filas * 55
                )
        );

        return panel;
    }

    private void agregarAcceso(
            JPanel panel,
            String modulo,
            String texto,
            Color color) {

        if (!controlador.puedeAcceder(modulo)) {
            return;
        }

        JButton boton = Tema.boton(texto, color);

        boton.addActionListener(
                e -> marco.ir(modulo)
        );

        panel.add(boton);
    }

    // PIE DEL PANEL

    private JPanel crearPie() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        panel.setOpaque(false);

        lblActualizacion = new JLabel(
                "Consultando datos..."
        );

        lblActualizacion.setForeground(
                Tema.OSCURO
        );

        lblActualizacion.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        JLabel sistema = new JLabel(
                "MINOVA | Bits Angus Burger"
        );

        panel.add(
                lblActualizacion,
                BorderLayout.WEST
        );

        panel.add(
                sistema,
                BorderLayout.EAST
        );

        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        30
                )
        );

        return panel;
    }

    // ACTUALIZAR ESTADISTICAS DESDE MYSQL

    @Override
    public void refrescar() {

        try {

            Map<String, Object> resumen =
                    controlador.obtenerResumen();

            for (Map.Entry<String, JLabel> entrada
                    : indicadores.entrySet()) {

                String clave = entrada.getKey();

                JLabel etiqueta = entrada.getValue();

                if (!controlador.tieneEstadistica(
                        resumen, clave)) {

                    etiqueta.setText("--");

                    continue;
                }

                if ("ingresosHoy".equals(clave)) {

                    etiqueta.setText(
                            controlador.dinero(
                                    controlador.obtenerValor(
                                            resumen,
                                            clave
                                    )
                            )
                    );

                } else if (
                        "mesasDisponibles".equals(clave)) {

                    etiqueta.setText(
                            controlador.obtenerMesasDisponibles(
                                    resumen
                            )
                    );

                } else {

                    etiqueta.setText(
                            controlador.cantidad(
                                    controlador.obtenerCantidad(
                                            resumen,
                                            clave
                                    )
                            )
                    );
                }
            }

            if (controlador.esAdministrador()
                    && grafica != null) {

                Map<LocalDate, BigDecimal> ventas =
                        controlador.obtenerVentasSemanales();

                grafica.setVentas(ventas);

                lblTotalSemanal.setText(
                        controlador.dinero(
                                controlador.totalSemanal(ventas)
                        )
                );
            }

            String fecha = LocalDateTime.now(ZONA)
                    .format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy HH:mm:ss"
                            )
                    );

            lblActualizacion.setText(
                    "Datos de MySQL | Actualizado: "
                    + fecha
            );

            revalidate();
            repaint();

        } catch (SQLException e) {

            limpiarIndicadores();

            lblActualizacion.setText(
                    "Error de conexion con MySQL."
            );

            System.err.println(
                    "Error en INICIO: "
                    + e.getMessage()
            );

            Tema.error(
                    this,
                    "No se pudieron consultar "
                    + "las estadisticas de MySQL."
            );

        } catch (IllegalArgumentException e) {

            limpiarIndicadores();

            lblActualizacion.setText(
                    "Estadisticas no disponibles."
            );

            Tema.error(this, e.getMessage());
        }
    }

    private void limpiarIndicadores() {

        for (JLabel etiqueta
                : indicadores.values()) {

            etiqueta.setText("--");
        }

        if (grafica != null) {

            grafica.setVentas(
                    new LinkedHashMap<>()
            );
        }

        if (lblTotalSemanal != null) {

            lblTotalSemanal.setText("--");
        }
    }

    // GRAFICA DE INGRESOS SEMANALES

    private class GraficaVentas extends JPanel {

        private Map<LocalDate, BigDecimal> ventas =
                new LinkedHashMap<>();

        private final DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM");

        public GraficaVentas() {

            setBackground(Color.WHITE);

            setPreferredSize(
                    new Dimension(650, 200)
            );

            ToolTipManager.sharedInstance()
                    .registerComponent(this);
        }

        public void setVentas(
                Map<LocalDate, BigDecimal> ventas) {

            this.ventas =
                    new LinkedHashMap<>(ventas);

            repaint();
        }

        // MOSTRAR VALOR AL PASAR EL MOUSE

        @Override
        public String getToolTipText(
                java.awt.event.MouseEvent evento) {

            if (ventas.isEmpty()) {
                return null;
            }

            int ancho = getWidth() - 80;

            if (ancho <= 0) {
                return null;
            }

            double espacio =
                    ancho / (double) ventas.size();

            int indice = (int) Math.floor(
                    (evento.getX() - 40) / espacio
            );

            if (indice < 0
                    || indice >= ventas.size()) {

                return null;
            }

            List<LocalDate> fechas =
                    new ArrayList<>(ventas.keySet());

            LocalDate fecha = fechas.get(indice);

            return fecha.format(formato)
                    + ": "
                    + controlador.dinero(
                            ventas.get(fecha)
                    );
        }

        // DIBUJAR GRAFICA

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            try {

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                if (ventas.isEmpty()) {

                    g2.setColor(Tema.OSCURO);

                    g2.drawString(
                            "No hay datos disponibles.",
                            25,
                            40
                    );

                    return;
                }

                int margen = 40;

                int anchoDisponible =
                        getWidth() - margen * 2;

                int altoDisponible =
                        getHeight() - 65;

                if (anchoDisponible <= 0
                        || altoDisponible <= 0) {

                    return;
                }

                int base = getHeight() - 35;

                double maximo = 0;

                for (BigDecimal valor
                        : ventas.values()) {

                    maximo = Math.max(
                            maximo,
                            valor.doubleValue()
                    );
                }

                boolean sinVentas = maximo == 0;

                if (maximo <= 0) {
                    maximo = 1;
                }

                // LINEAS DE REFERENCIA

                g2.setColor(
                        new Color(235, 235, 235)
                );

                for (int i = 0; i <= 4; i++) {

                    int y = base
                            - altoDisponible * i / 4;

                    g2.drawLine(
                            margen,
                            y,
                            getWidth() - margen,
                            y
                    );
                }

                double espacio =
                        anchoDisponible
                        / (double) ventas.size();

                int anchoBarra = Math.max(
                        4,
                        (int) (espacio * 0.55)
                );

                int indice = 0;

                LocalDate hoy =
                        LocalDate.now(ZONA);

                g2.setFont(
                        new Font(
                                "SansSerif",
                                Font.PLAIN,
                                11
                        )
                );

                for (Map.Entry<LocalDate, BigDecimal> dato
                        : ventas.entrySet()) {

                    LocalDate fecha = dato.getKey();

                    BigDecimal valor = dato.getValue();

                    int alto = (int) (
                            valor.doubleValue()
                            / maximo
                            * altoDisponible
                    );

                    int x = margen
                            + (int) (
                                    indice * espacio
                                    + (espacio - anchoBarra) / 2
                            );

                    int y = base - alto;

                    g2.setColor(
                            fecha.equals(hoy)
                                    ? Tema.VERDE
                                    : Tema.AMBAR
                    );

                    if (alto > 0) {

                        g2.fillRoundRect(
                                x,
                                y,
                                anchoBarra,
                                alto,
                                8,
                                8
                        );
                    }

                    g2.setColor(Tema.OSCURO);

                    String dia = fecha.format(formato);

                    int anchoTexto =
                            g2.getFontMetrics()
                                    .stringWidth(dia);

                    g2.drawString(
                            dia,
                            x + (anchoBarra - anchoTexto) / 2,
                            base + 20
                    );

                    String textoValor =
                            controlador.dinero(valor);

                    int anchoValor =
                            g2.getFontMetrics()
                                    .stringWidth(textoValor);

                    if (anchoValor < espacio - 4) {

                        g2.drawString(
                                textoValor,
                                x + (anchoBarra - anchoValor) / 2,
                                y - 6
                        );
                    }

                    indice++;
                }

                if (sinVentas) {

                    g2.setColor(Tema.OSCURO);

                    g2.drawString(
                            "Sin ingresos durante este periodo.",
                            margen + 10,
                            20
                    );
                }

            } finally {

                g2.dispose();
            }
        }
    }
}
