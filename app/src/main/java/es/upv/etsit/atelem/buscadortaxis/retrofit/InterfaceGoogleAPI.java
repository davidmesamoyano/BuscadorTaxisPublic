package es.upv.etsit.atelem.buscadortaxis.retrofit;
//Donde haremos peticion al servicio

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface InterfaceGoogleAPI {
    @GET
    Call<String> getDirecciones(@Url String url);


}
