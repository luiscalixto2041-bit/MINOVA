
package vista;

import controlador.ReporteControlador;
import modelo.Reporte;
import modelo.Usuario;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ReportesPanel extends JPanel implements Refrescable {

    private final ReporteControlador controlador;

    private Reporte reporteActual;

    private JComboBox<String> cmbTipo;
    private JComboBox<String> cmbPeriodo;

    private JTextField txtInicio;
    private JTextField txtFin;
    private JTextField txtBuscar;

    private JLabel lblTotal;
    private JLabel lblCantidad;
    private JLabel lblFilas;

    private JLabel lblEtiquetaTotal;
    private JLabel lblEtiquetaCantidad;

    private JTextArea areaResumen;

    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> ordenador;

    private JButton btnExportar;

    private GraficaPanel grafica;

    // CONSTRUCTOR

    public ReportesPanel(Usuario usuarioActual) {

        controlador = new ReporteControlador(usuarioActual);

        setLayout(new BorderLayout());

        JPanel contenido = new JPanel(
                new BorderLayout(12, 12)
        );

        JPanel superior = new JPanel(
                new BorderLayout(10, 10)
        );

        superior.setOpaque(false);

        superior.add(
                crearFormulario(),
                BorderLayout.NORTH
        );

        superior.add(
                crearResumen(),
                BorderLayout.CENTER
        );

        contenido.add(superior, BorderLayout.NORTH);

        contenido.add(
                crearResultados(),
                BorderLayout.CENTER
        );

        add(
                Tema.marcoModulo(
                        "GESTION DE REPORTES",
                        contenido
                ),
                BorderLayout.CENTER
        );

        actualizarPeriodo();
        actualizarTipo();
    }

    // FORMATO MONETARIO

    private String dinero(BigDecimal valor) {

        NumberFormat formato =
                NumberFormat.getCurrencyInstance(
                        Locale.forLanguageTag("es-CO")
                );

        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(2);

        return formato.format(
                valor == null ? BigDecimal.ZERO : valor
        );
    }

    // FORMULARIO DE FILTROS

    private JPanel crearFormulario() {

        cmbTipo = new JComboBox<>(
                controlador.obtenerTipos()
        );

        cmbPeriodo = new JComboBox<>(
                new String[]{
                    "HOY",
                    "ULTIMOS 7 DIAS",
                    "ESTE MES",
                    "ESTE ANO",
                    "PERSONALIZADO"
                }
        );

        txtInicio = Tema.campo();
        txtFin = Tema.campo();

        JPanel campos = new JPanel(
                new GridLayout(1, 4, 12, 0)
        );

        campos.setOpaque(false);

        campos.add(
                Tema.campoConEtiqueta(
                        "Tipo de reporte",
                        cmbTipo
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Periodo",
                        cmbPeriodo
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Fecha inicial",
                        txtInicio
                )
        );

        campos.add(
                Tema.campoConEtiqueta(
                        "Fecha final",
                        txtFin
                )
        );

        JButton btnGenerar = Tema.boton(
                "GENERAR REPORTE",
                Tema.AMBAR
        );

        btnExportar = Tema.boton(
                "EXPORTAR CSV",
                Tema.VERDE
        );

        JButton btnLimpiar = Tema.boton(
                "LIMPIAR",
                Tema.GRIS
        );

        btnExportar.setEnabled(false);

        btnGenerar.addActionListener(
                e -> generarReporte()
        );

        btnExportar.addActionListener(
                e -> exportarReporte()
        );

        btnLimpiar.addActionListener(
                e -> limpiar()
        );

        cmbPeriodo.addActionListener(
                e -> actualizarPeriodo()
        );

        cmbTipo.addActionListener(
                e -> actualizarTipo()
        );

        JPanel formulario = new JPanel(
                new BorderLayout(8, 10)
        );

        formulario.setOpaque(false);

        formulario.add(
                Tema.seccion("FILTROS DEL REPORTE"),
                BorderLayout.NORTH
        );

        formulario.add(
                campos,
                BorderLayout.CENTER
        );

        formulario.add(
                Tema.filaBotones(
                        btnGenerar,
                        btnExportar,
                        btnLimpiar
                ),
                BorderLayout.SOUTH
        );

        return formulario;
    }

    // CREAR TARJETA ESTADISTICA

    private JPanel crearTarjeta(
            JLabel etiqueta,
            JLabel valor,
            Color color) {

        JPanel panel = new JPanel(
                new BorderLayout(5, 5)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 5, 0, 0, color
                        ),
                        BorderFactory.createEmptyBorder(
                                12, 12, 12, 12
                        )
                )
        );

        etiqueta.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );

        valor.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        22
                )
        );

        valor.setForeground(color);

        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);

        return panel;
    }

    // TARJETAS DE RESUMEN

    private JPanel crearResumen() {

        lblTotal = new JLabel("$ 0");
        lblCantidad = new JLabel("0");
        lblFilas = new JLabel("0");

        lblEtiquetaTotal = new JLabel("Total");
        lblEtiquetaCantidad = new JLabel("Registros");

        JPanel tarjetas = new JPanel(
                new GridLayout(1, 3, 12, 0)
        );

        tarjetas.setOpaque(false);

        tarjetas.add(
                crearTarjeta(
                        lblEtiquetaTotal,
                        lblTotal,
                        Tema.VERDE
                )
        );

        tarjetas.add(
                crearTarjeta(
                        lblEtiquetaCantidad,
                        lblCantidad,
                        Tema.OSCURO
                )
        );

        tarjetas.add(
                crearTarjeta(
                        new JLabel("Filas del reporte"),
                        lblFilas,
                        Tema.AMBAR
                )
        );

        return tarjetas;
    }

    // PESTANAS DE RESULTADOS

    private JTabbedPane crearResultados() {

        JTabbedPane pestanas = new JTabbedPane();

        pestanas.addTab(
                "TABLA DE RESULTADOS",
                crearTabla()
        );

        grafica = new GraficaPanel();

        pestanas.addTab(
                "GRAFICA ESTADISTICA",
                new JScrollPane(grafica)
        );

        areaResumen = new JTextArea();

        areaResumen.setEditable(false);
        areaResumen.setLineWrap(true);
        areaResumen.setWrapStyleWord(true);

        areaResumen.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        13
                )
        );

        areaResumen.setMargin(
                new Insets(15, 15, 15, 15)
        );

        areaResumen.setText(
                "Selecciona un tipo de reporte "
                + "y presiona GENERAR REPORTE."
        );

        pestanas.addTab(
                "RESUMEN",
                new JScrollPane(areaResumen)
        );

        return pestanas;
    }

    // TABLA DE RESULTADOS

    private JPanel crearTabla() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 10)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10
                )
        );

        txtBuscar = Tema.campo();

        txtBuscar.setToolTipText(
                "Buscar dentro del reporte"
        );

        modeloTabla = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(
                    int fila,
                    int columna) {

                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {

                for (int fila = 0;
                        fila < getRowCount();
                        fila++) {

                    Object valor =
                            getValueAt(fila, columna);

                    if (valor != null) {
                        return valor.getClass();
                    }
                }

                return Object.class;
            }
        };

        tabla = new JTable(modeloTabla);

        ordenador = new TableRowSorter<>(
                modeloTabla
        );

        tabla.setRowSorter(ordenador);

        tabla.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );

        Tema.alCambiar(
                txtBuscar,
                () -> aplicarBusqueda()
        );

        panel.add(
                Tema.campoConEtiqueta(
                        "Buscar en resultados",
                        txtBuscar
                ),
                BorderLayout.NORTH
        );

        panel.add(
                Tema.tabla(tabla),
                BorderLayout.CENTER
        );

        return panel;
    }

    // ACTUALIZAR PERIODO

    private void actualizarPeriodo() {

        String periodo = (String)
                cmbPeriodo.getSelectedItem();

        String tipo = (String)
                cmbTipo.getSelectedItem();

        boolean inventario =
                "INVENTARIO".equals(tipo);

        boolean personalizado =
                "PERSONALIZADO".equals(periodo);

        txtInicio.setEnabled(
                personalizado && !inventario
        );

        txtFin.setEnabled(
                personalizado && !inventario
        );

        if (!personalizado && !inventario) {

            java.time.LocalDate[] fechas =
                    controlador.obtenerPeriodo(periodo);

            txtInicio.setText(
                    controlador.formatearFecha(
                            fechas[0]
                    )
            );

            txtFin.setText(
                    controlador.formatearFecha(
                            fechas[1]
                    )
            );
        }
    }

    // ACTUALIZAR TIPO

    private void actualizarTipo() {

        String tipo = (String)
                cmbTipo.getSelectedItem();

        boolean inventario =
                "INVENTARIO".equals(tipo);

        cmbPeriodo.setEnabled(!inventario);

        actualizarPeriodo();
    }

    // GENERAR REPORTE MYSQL

    private void generarReporte() {

        try {

            String tipo = (String)
                    cmbTipo.getSelectedItem();

            reporteActual =
                    controlador.generarReporte(
                            tipo,
                            txtInicio.getText(),
                            txtFin.getText()
                    );

            mostrarReporte(reporteActual);

        } catch (IllegalArgumentException e) {

            Tema.error(
                    this,
                    e.getMessage()
            );

        } catch (SQLException e) {

            System.err.println(
                    "Error en REPORTES: "
                    + e.getMessage()
            );

            Tema.error(
                    this,
                    "No se pudo generar "
                    + "el reporte en MySQL."
            );
        }
    }

    // MOSTRAR RESULTADOS

    private void mostrarReporte(Reporte reporte) {

        lblEtiquetaTotal.setText(
                controlador.obtenerEtiquetaTotal(
                        reporte.getTipo()
                )
        );

        lblEtiquetaCantidad.setText(
                controlador.obtenerEtiquetaCantidad(
                        reporte.getTipo()
                )
        );

        if (controlador.tieneTotalMonetario(
                reporte.getTipo())) {

            lblTotal.setText(
                    dinero(reporte.getTotal())
            );

        } else {

            lblTotal.setText("No aplica");
        }

        lblCantidad.setText(
                String.valueOf(
                        reporte.getCantidadRegistros()
                )
        );

        lblFilas.setText(
                String.valueOf(
                        reporte.getCantidadFilas()
                )
        );

        modeloTabla.setDataVector(
                reporte.getDatosTabla(),
                reporte.getColumnas()
        );

        for (int i = 0;
                i < tabla.getColumnCount();
                i++) {

            tabla.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(150);
        }

        areaResumen.setText(
                controlador.generarResumen(reporte)
        );

        areaResumen.setCaretPosition(0);

        grafica.setReporte(reporte);

        btnExportar.setEnabled(true);

        aplicarBusqueda();
    }

    // BUSQUEDA

    private void aplicarBusqueda() {

        String texto = txtBuscar.getText().trim();

        if (texto.isEmpty()) {

            ordenador.setRowFilter(null);

        } else {

            ordenador.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + Pattern.quote(texto)
                    )
            );
        }
    }

    // EXPORTAR REPORTE

    private void exportarReporte() {

        if (reporteActual == null) {

            Tema.error(
                    this,
                    "Primero genera un reporte."
            );

            return;
        }

        JFileChooser selector =
                new JFileChooser();

        selector.setDialogTitle(
                "Exportar reporte MINOVA"
        );

        selector.setFileFilter(
                new FileNameExtensionFilter(
                        "Archivo CSV para Excel",
                        "csv"
                )
        );

        selector.setSelectedFile(
                new java.io.File(
                        controlador.obtenerNombreArchivo(
                                reporteActual
                        )
                )
        );

        int resultado =
                selector.showSaveDialog(this);

        if (resultado
                != JFileChooser.APPROVE_OPTION) {

            return;
        }

        Path destino =
                selector.getSelectedFile().toPath();

        String nombre =
                destino.getFileName().toString();

        if (!nombre.toLowerCase(Locale.ROOT)
                .endsWith(".csv")) {

            destino = destino.resolveSibling(
                    nombre + ".csv"
            );
        }

        if (Files.exists(destino)) {

            int confirmar =
                    JOptionPane.showConfirmDialog(
                            this,
                            "El archivo ya existe."
                            + "\nDeseas reemplazarlo?",
                            "Confirmar exportacion",
                            JOptionPane.YES_NO_OPTION
                    );

            if (confirmar
                    != JOptionPane.YES_OPTION) {

                return;
            }
        }

        try {

            Path archivo =
                    controlador.exportarCSV(
                            reporteActual,
                            destino
                    );

            Tema.info(
                    this,
                    "Reporte exportado correctamente."
                    + "\n\n" + archivo
            );

        } catch (IOException
                | IllegalArgumentException e) {

            Tema.error(
                    this,
                    "No se pudo exportar el reporte."
                    + "\n" + e.getMessage()
            );
        }
    }

    // LIMPIAR RESULTADOS

    private void limpiar() {

        reporteActual = null;

        modeloTabla.setRowCount(0);
        modeloTabla.setColumnCount(0);

        lblTotal.setText("$ 0");
        lblCantidad.setText("0");
        lblFilas.setText("0");

        lblEtiquetaTotal.setText("Total");
        lblEtiquetaCantidad.setText("Registros");

        txtBuscar.setText("");

        areaResumen.setText(
                "Selecciona los filtros "
                + "y genera un nuevo reporte."
        );

        grafica.setReporte(null);

        btnExportar.setEnabled(false);

        cmbTipo.setSelectedIndex(0);
        cmbPeriodo.setSelectedIndex(0);

        actualizarPeriodo();
        actualizarTipo();
    }

    // REFRESCAR MODULO

    @Override
    public void refrescar() {

        actualizarPeriodo();

        if (reporteActual != null) {

            generarReporte();
        }
    }

    // CLASE INTERNA PARA GRAFICAS

    private class GraficaPanel extends JPanel {

        private Reporte reporte;

        public GraficaPanel() {

            setBackground(Color.WHITE);

            setPreferredSize(
                    new Dimension(650, 350)
            );
        }

        public void setReporte(Reporte reporte) {

            this.reporte = reporte;

            int cantidad = obtenerDatos().size();

            setPreferredSize(
                    new Dimension(
                            650,
                            Math.max(
                                    350,
                                    110 + cantidad * 35
                            )
                    )
            );

            revalidate();
            repaint();
        }

        // DATOS DE LA GRAFICA

        private List<Object[]> obtenerDatos() {

            List<Object[]> datos =
                    new ArrayList<>();

            if (reporte == null) {

                return datos;
            }

            for (Object[] fila
                    : reporte.getFilas()) {

                switch (reporte.getTipo()) {

                    case "VENTAS" -> datos.add(
                            new Object[]{
                                fila[0].toString(),
                                fila[3]
                            }
                    );

                    case "PRODUCTOS" -> datos.add(
                            new Object[]{
                                fila[1].toString(),
                                fila[2]
                            }
                    );

                    case "PAGOS" -> datos.add(
                            new Object[]{
                                fila[0].toString(),
                                fila[2]
                            }
                    );

                    case "EMPLEADOS" -> datos.add(
                            new Object[]{
                                fila[1].toString(),
                                fila[4]
                            }
                    );

                    case "INVENTARIO" -> datos.add(
                            new Object[]{
                                fila[1].toString(),
                                fila[2]
                            }
                    );

                    case "PEDIDOS" -> datos.add(
                            new Object[]{
                                fila[0].toString(),
                                fila[1]
                            }
                    );
                }
            }

            // AGRUPAR RESERVAS POR ESTADO

            if ("RESERVAS".equals(
                    reporte.getTipo())) {

                java.util.Map<String, Long> estados =
                        new java.util.LinkedHashMap<>();

                for (Object[] fila
                        : reporte.getFilas()) {

                    estados.merge(
                            fila[6].toString(),
                            1L,
                            Long::sum
                    );
                }

                estados.forEach(
                        (estado, cantidad) ->
                            datos.add(
                                    new Object[]{
                                        estado,
                                        cantidad
                                    }
                            )
                );
            }

            return datos;
        }

        // TITULO DE LA GRAFICA

        private String tituloGrafica() {

            if (reporte == null) {
                return "";
            }

            return switch (reporte.getTipo()) {

                case "VENTAS" ->
                    "INGRESOS POR DIA";

                case "PRODUCTOS" ->
                    "UNIDADES VENDIDAS POR PRODUCTO";

                case "PAGOS" ->
                    "INGRESOS POR METODO DE PAGO";

                case "EMPLEADOS" ->
                    "RECAUDACION POR EMPLEADO";

                case "INVENTARIO" ->
                    "EXISTENCIAS POR PRODUCTO";

                case "RESERVAS" ->
                    "RESERVAS POR ESTADO";

                case "PEDIDOS" ->
                    "PEDIDOS POR ESTADO";

                default -> "ESTADISTICAS";
            };
        }

        private boolean esMonetario() {

            return reporte != null
                    && List.of(
                            "VENTAS",
                            "PAGOS",
                            "EMPLEADOS"
                    ).contains(reporte.getTipo());
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

                List<Object[]> datos =
                        obtenerDatos();

                if (datos.isEmpty()) {

                    g2.setColor(Tema.GRIS);

                    g2.setFont(
                            new Font(
                                    "SansSerif",
                                    Font.PLAIN,
                                    15
                            )
                    );

                    g2.drawString(
                            "No hay datos para graficar.",
                            25,
                            45
                    );

                    return;
                }

                g2.setColor(Tema.OSCURO);

                g2.setFont(
                        new Font(
                                "SansSerif",
                                Font.BOLD,
                                17
                        )
                );

                g2.drawString(
                        tituloGrafica(),
                        25,
                        35
                );

                double maximo = 0;

                for (Object[] dato : datos) {

                    double valor =
                            ((Number) dato[1])
                                    .doubleValue();

                    maximo = Math.max(
                            maximo,
                            valor
                    );
                }

                if (maximo <= 0) {
                    maximo = 1;
                }

                int xBarra = 200;

                int anchoDisponible =
                        Math.max(
                                60,
                                getWidth() - 350
                        );

                int y = 75;

                g2.setFont(
                        new Font(
                                "SansSerif",
                                Font.PLAIN,
                                12
                        )
                );

                for (int i = 0;
                        i < datos.size();
                        i++) {

                    Object[] dato = datos.get(i);

                    String nombre =
                            dato[0].toString();

                    Number numero =
                            (Number) dato[1];

                    double valor =
                            numero.doubleValue();

                    int ancho = (int) (
                            valor / maximo
                            * anchoDisponible
                    );

                    ancho = Math.max(0, ancho);

                    if (nombre.length() > 24) {

                        nombre = nombre.substring(
                                0, 21
                        ) + "...";
                    }

                    g2.setColor(Tema.OSCURO);

                    g2.drawString(
                            nombre,
                            20,
                            y + 15
                    );

                    g2.setColor(
                            new Color(235, 235, 235)
                    );

                    g2.fillRoundRect(
                            xBarra,
                            y,
                            anchoDisponible,
                            23,
                            8,
                            8
                    );

                    g2.setColor(
                            i % 2 == 0
                                    ? Tema.AMBAR
                                    : Tema.VERDE
                    );

                    if (ancho > 0) {

                        g2.fillRoundRect(
                                xBarra,
                                y,
                                ancho,
                                23,
                                8,
                                8
                        );
                    }

                    String etiqueta;

                    if (esMonetario()) {

                        etiqueta = dinero(
                                new BigDecimal(
                                        numero.toString()
                                )
                        );

                    } else {

                        etiqueta =
                                NumberFormat
                                    .getIntegerInstance(
                                        Locale.forLanguageTag(
                                                "es-CO"
                                        )
                                    )
                                    .format(numero);
                    }

                    g2.setColor(Tema.OSCURO);

                    g2.drawString(
                            etiqueta,
                            xBarra
                            + anchoDisponible + 10,
                            y + 15
                    );

                    y += 35;
                }

            } finally {

                g2.dispose();
            }
        }
    }
}
