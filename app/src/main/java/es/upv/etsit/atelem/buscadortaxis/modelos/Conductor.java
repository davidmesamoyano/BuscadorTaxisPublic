package es.upv.etsit.atelem.buscadortaxis.modelos;

public class Conductor {
    String id;
    String nombre;
    String email;
    String marcaVehiculo;
    String matriculaVehiculo;


    public Conductor(String id, String nombre, String email, String marcaVehiculo, String matriculaVehiculo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.marcaVehiculo = marcaVehiculo;
        this.matriculaVehiculo = matriculaVehiculo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMarcaVehiculo() {
        return marcaVehiculo;
    }

    public void setMarcaVehiculo(String marcaVehiculo) {
        this.marcaVehiculo = marcaVehiculo;
    }

    public String getMatriculaVehiculo() {
        return matriculaVehiculo;
    }

    public void setMatriculaVehiculo(String matriculaVehiculo) {
        this.matriculaVehiculo = matriculaVehiculo;
    }
}
