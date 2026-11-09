
package pi_e1_g327._minova;

public class Cliente {
    private String documentoCliente; //
    private String nombreCliente;    //

    public Cliente() {}

    public Cliente(String documentoCliente, String nombreCliente) {
        this.documentoCliente = documentoCliente;
        this.nombreCliente = nombreCliente;
    }

    public String getDocumentoCliente() { return documentoCliente; }
    public void setDocumentoCliente(String documentoCliente) { this.documentoCliente = documentoCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}
