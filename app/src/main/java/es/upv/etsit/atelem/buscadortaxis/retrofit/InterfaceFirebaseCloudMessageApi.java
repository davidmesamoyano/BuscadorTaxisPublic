package es.upv.etsit.atelem.buscadortaxis.retrofit;

import es.upv.etsit.atelem.buscadortaxis.modelos.FCMBody;
import es.upv.etsit.atelem.buscadortaxis.modelos.FCMResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface InterfaceFirebaseCloudMessageApi {

    @Headers("Content-Type: application/json")
    @POST("v1/projects/buscadortaxis/messages:send")
    Call<FCMResponse> send(@Body FCMBody body);
}