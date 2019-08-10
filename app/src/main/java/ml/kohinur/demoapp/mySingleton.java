package ml.kohinur.demoapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by mahbub on 9/23/17.
 */

public class mySingleton {

    private static mySingleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    private mySingleton(Context context){
        mCtx = context;
        requestQueue=getRequestQueue();
    }

    public static synchronized mySingleton getInstance(Context context){
        mInstance=new mySingleton(context);
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if (requestQueue==null){
            requestQueue= Volley.newRequestQueue(mCtx.getApplicationContext());

        }
        return requestQueue;
    }

    public <T>void addTorequestrue(Request<T> request){
        requestQueue.add(request);
    }


}

