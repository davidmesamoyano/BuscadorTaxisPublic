package es.upv.etsit.atelem.buscadortaxis.modelos;

public class ClienteReserva {

    String idHistorialReserva;
    String idCliente;
    String idConductor;
    String destination;
    String origin;
    String time;
    String km;
    String status;
    double originLat;
    double originLng;
    double destinationLat;
    double destinationLng;

    public ClienteReserva(){

    }

    public ClienteReserva(String idCliente, String idConductor, String destination, String origin, String time, String km, String status, double originLat, double originLng, double destinationLat, double destinationLng) {
        this.idCliente = idCliente;
        this.idConductor = idConductor;
        this.destination = destination;
        this.origin = origin;
        this.time = time;
        this.km = km;
        this.status = status;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    public ClienteReserva(String idHistorialReserva, String idCliente, String idConductor, String destination, String origin, String time, String km, String status, double originLat, double originLng, double destinationLat, double destinationLng) {
        this.idHistorialReserva = idHistorialReserva;
        this.idCliente = idCliente;
        this.idConductor = idConductor;
        this.destination = destination;
        this.origin = origin;
        this.time = time;
        this.km = km;
        this.status = status;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    public String getIdHistorialReserva() {
        return idHistorialReserva;
    }

    public void setIdHistorialReserva(String idHistorialReserva) {
        this.idHistorialReserva = idHistorialReserva;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(String idConductor) {
        this.idConductor = idConductor;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(double originLat) {
        this.originLat = originLat;
    }

    public double getOriginLng() {
        return originLng;
    }

    public void setOriginLng(double originLng) {
        this.originLng = originLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }


    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }
}
