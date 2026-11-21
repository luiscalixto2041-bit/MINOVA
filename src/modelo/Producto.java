package modelo;

public class Producto {
    private String nombreProducto; 
    private double precioProducto; 
    private int stock;            

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
