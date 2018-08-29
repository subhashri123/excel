package com.tabian.importexceldata;
import com.mongodb.BasicDBList;

import org.json.JSONArray;

import java.util.Date;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by WAVICLE DATA on 8/23/2018.
 */

public interface ProductListPostAPI {
    @FormUrlEncoded
    @POST("/saveitems/")
    public void productlist(
            @Field("storename") String storename,
            @Field("created_date") String created_date,
            @Field("status") String status,
            @Field("items") BasicDBList items,
            Callback<Response> callback);
}
