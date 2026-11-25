
package modelo;

public class Producto {

    private int idProducto;
    private String nombreProducto;
    private String descripcionProducto;
    private double precioProducto;
    private int stock;
    private boolean estado = true;

    private int stockMinimo = 5;
    private int stockMaximo = 100;

    public Producto() {
    }

    // Constructor anterior por compatibilidad
    public Producto(String nombreProducto,
                    double precioProducto,
                    int stock) {

        this.nombreProducto = nombreProducto;
        this.precioProducto = precioProducto;
        this.stock = stock;
    }

    // Constructor completo
    public Producto(int idProducto,
                    String nombreProducto,
                    String descripcionProducto,
                    double precioProducto,
                    int stock,
                    boolean estado,
                    int stockMinimo,
                    int stockMaximo) {

        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.descripcionProducto = descripcionProducto;
        this.precioProducto = precioProducto;
        this.stock = stock;
        this.estado = estado;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(
            String descripcionProducto) {

        this.descripcionProducto = descripcionProducto;
    }

    public double getPrecioProducto() {
        return precioProducto;
    }

    public void setPrecioProducto(double precioProducto) {
        this.precioProducto = precioProducto;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(int stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public String getEstadoStock() {

        if (stock == 0) {
            return "Agotado";
        }

        if (stock <= stockMinimo) {
            return "Bajo";
        }

        if (stock > stockMaximo) {
            return "Exceso";
        }

        return "Disponible";
    }

    @Override
    public String toString() {
        return nombreProducto;
    }
}
