
package pi_e1_g327._minova;

public class Pedido {
    private int idPedido;            //[cite: 1]
    private String nombreProducto;   //[cite: 1]
    private int cantidad;            //[cite: 1]
    private String metodoPago;       //[cite: 1]
    private double valorPago;        //[cite: 1]

    public Pedido() {}

    public Pedido(int idPedido, String nombreProducto, int cantidad, String metodoPago, double valorPago) {
        this.idPedido = idPedido;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.metodoPago = metodoPago;
        this.valorPago = valorPago;
    }

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public double getValorPago() { return valorPago; }
    public void setValorPago(double valorPago) { this.valorPago = valorPago; }
}
