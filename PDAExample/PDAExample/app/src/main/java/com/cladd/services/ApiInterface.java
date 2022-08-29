package com.cladd.services;


import com.google.gson.JsonElement;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    /************************************ LOGGING **************************************************/

    @POST("Api/Logger")
    Call<JsonElement> Log(@Body RequestBody map);

    /************************************   Base Config  **************************************************/

    @POST("Api/IntegrationStockIT/getBaseConfig")
    Call<JsonElement> getBaseConfig();

    /************************************ Ver Detalle **************************************************/

    @POST("Api/IntegrationStockIT/ConsultaStock")
    Call<JsonElement> getDetalleProducto(@Body RequestBody map);

    /************************************ Trackeo **************************************************/

    @POST("Api/IntegrationStockIT/ConsultarPiezaContainer")
    Call<JsonElement> getContainer(@Body RequestBody map);

    @POST("Api/IntegrationStockIT/ActuAlizarPiezaContainer")
    Call<JsonElement> Trackear(@Body RequestBody map);

    /********************************* Verificacion ***********************************************/

    @POST("Api/IntegrationStockIT/ConsultarPiezasAVerificar")
    Call<JsonElement> getContainerAVerificar(@Body RequestBody map);

    @POST("Api/IntegrationStockIT/ActualizarPiezaContainer")
    Call<JsonElement> Verificar(@Body RequestBody map);

    /************************************ Finder **************************************************/

    @POST("Api/IntegrationStockIT/FinderStock")
    Call<JsonElement> getProductosByCriterio(@Body RequestBody map);

    /************************************  Movimiento Deposito **************************************************/

    @POST("Api/IntegrationStockIT/UbicacionPiezaDeposito")
    Call<JsonElement> asignarUbicacion(@Body RequestBody map);

    /************************************   Write BC to RF **************************************************/

    @POST("Api/IntegrationStockIT/GrabarEtiquetaStockInicial")
    Call<JsonElement> obtenerHexaAGrabar(@Body RequestBody map);

    @POST("Api/IntegrationStockIT/GrabarEtiquetaSinUbicarStockInicial")
    Call<JsonElement> obtenerHexaAGrabarSinUbicar(@Body RequestBody map);

    /************************************   Write Container **************************************************/

    @POST("Api/IntegrationStockIT/GetHexContainer")
    Call<JsonElement> getContainerFromHex(@Body RequestBody map);

    @GET("Api/IntegrationStockIT/SetHexContainer")
    Call<ResponseBody> obtenerHexaAGrabarContainer(@Query("ContainerID") String containerID);

    /************************************   Write Container **************************************************/

    @POST("Api/IntegrationStockIT/GetOperario")
    Call<JsonElement> getOperario(@Body RequestBody map);

    /************************************   Color **************************************************/

    @POST("Api/IntegrationStockIT/GetColoresAfterDate")
    Call<JsonElement> getColores(@Body RequestBody map);

    /************************************   Producto **************************************************/

    @POST("Api/IntegrationStockIT/GetProductosAfterDate")
    Call<JsonElement> getProductos(@Body RequestBody map);

    /************************************   Inventario **************************************************/

    @POST("Api/IntegrationStockIT/NuevoInventario")
    Call<JsonElement> SaveInventario(@Body RequestBody map);

}

