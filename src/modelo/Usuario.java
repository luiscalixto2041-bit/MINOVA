
package modelo;

public class Usuario {

    private int idUsuario;
    private String nombreCompleto;
    private String nombreUsuario;
    private int idRol;
    private String rol;
    private boolean estado;

    // CONSTRUCTOR VACIO

    public Usuario() {
    }

    // CONSTRUCTOR PARA MYSQL

    public Usuario(int idUsuario,
                   String nombreCompleto,
                   String nombreUsuario,
                   int idRol,
                   String rol,
                   boolean estado) {

        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.nombreUsuario = nombreUsuario;
        this.idRol = idRol;
        this.rol = rol;
        this.estado = estado;
    }

    // GETTERS Y SETTERS

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombreCompleto + " - " + rol;
    }
}
