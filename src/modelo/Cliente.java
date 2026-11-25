
package modelo;

public class Cliente {

    private int idCliente;
    private String documentoCliente;
    private String nombreCliente;
    private String telefono;
    private String correo;
    private boolean estado = true;

    // Constructor vacio
    public Cliente() {
    }

    // Constructor anterior por compatibilidad
    public Cliente(String documentoCliente,
                   String nombreCliente) {

        this.documentoCliente = documentoCliente;
        this.nombreCliente = nombreCliente;
        this.estado = true;
    }

    // Constructor para registrar clientes
    public Cliente(String documentoCliente,
                   String nombreCliente,
                   String telefono,
                   String correo) {

        this.documentoCliente = documentoCliente;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = true;
    }

    // Constructor completo para MySQL
    public Cliente(int idCliente,
                   String documentoCliente,
                   String nombreCliente,
                   String telefono,
                   String correo,
                   boolean estado) {

        this.idCliente = idCliente;
        this.documentoCliente = documentoCliente;
        this.nombreCliente = nombreCliente;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public void setDocumentoCliente(String documentoCliente) {
        this.documentoCliente = documentoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombreCliente + " - " + documentoCliente;
    }
}
