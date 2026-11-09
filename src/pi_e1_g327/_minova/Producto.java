
package pi_e1_g327._minova;

public class Producto {
    private String nombreProducto; //[cite: 1]
    private double precioProducto; //[cite: 1]
    private int stock;             //[cite: 1]

    public Producto() {}

    public Producto(String nombreProducto, double precioProducto, int stock) {
        this.nombreProducto = nombreProducto;
        this.precioProducto = precioProducto;
        this.stock = stock;
    }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public double getPrecioProducto() { return precioProducto; }
    public void setPrecioProducto(double precioProducto) { this.precioProducto = precioProducto; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
