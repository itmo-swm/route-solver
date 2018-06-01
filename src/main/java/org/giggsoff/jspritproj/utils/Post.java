/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.utils;

/**
 *
 * @author giggs
 */
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Post {

    public static void unirestPost(String url, String content) {
        System.out.println("TRY");
        Unirest.post(url).header("accept", "*/*").body(content).asStringAsync(new Callback<String>() {
            @Override
            public void failed(UnirestException e) {
                System.out.println("The request has failed");
            }

            @Override
            public void completed(com.mashape.unirest.http.HttpResponse<String> response) {
                System.out.println(response.getStatus());
                System.out.println(response.getBody());
            }

            @Override
            public void cancelled() {
                System.out.println("The request has been cancelled");
            }
        });
    }

}
