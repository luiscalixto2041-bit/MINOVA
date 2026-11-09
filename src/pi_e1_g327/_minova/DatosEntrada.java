
package pi_e1_g327._minova;
import java.time.LocalDate;
class DatosEntrada {
     private String nombreUsuario;
    private String contraseña;
    private String rol;
    private String documentoCliente;
    private String nombreCliente;
    private int numeroMesa;
    private LocalDate fechaReserva;
    private LocalDate horaReserva;
    private int idPedido;
    private String nombreProducto;
    private int cantidad;
    private double precioProducto;
    private int stock;
    private String metodoPago;
    private double valorPago;

    public DatosEntrada() {}

    public DatosEntrada(String nombreUsuario, String contraseña, String rol, String documentoCliente, 
                        String nombreCliente, int numeroMesa, LocalDate fechaReserva, LocalDate horaReserva, 
                        int idPedido, String nombreProducto, int cantidad, double precioProducto, 
                        int stock, String metodoPago, double valorPago) {
        this.nombreUsuario = nombreUsuario;
        this.contraseña = contraseña;
        this.rol = rol;
        this.documentoCliente = documentoCliente;
        this.nombreCliente = nombreCliente;
        this.numeroMesa = numeroMesa;
        this.fechaReserva = fechaReserva;
        this.horaReserva = horaReserva;
        this.idPedido = idPedido;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioProducto = precioProducto;
        this.stock = stock;
        this.metodoPago = metodoPago;
        this.valorPago = valorPago;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getDocumentoCliente() { return documentoCliente; }
    public void setDocumentoCliente(String documentoCliente) { this.documentoCliente = documentoCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }

    public LocalDate getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDate fechaReserva) { this.fechaReserva = fechaReserva; }

    public LocalDate getHoraReserva() { return horaReserva; }
    public void setHoraReserva(LocalDate horaReserva) { this.horaReserva = horaReserva; }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioProducto() { return precioProducto; }
    public void setPrecioProducto(double precioProducto) { this.precioProducto = precioProducto; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public double getValorPago() { return valorPago; }
    public void setValorPago(double valorPago) { this.valorPago = valorPago; }
    
    public void mostrarDetallesPedido() {
        System.out.println("Pedido ID: " + idPedido + " - Producto: " + nombreProducto + " (Cantidad: " + cantidad + ")");
    }
}
