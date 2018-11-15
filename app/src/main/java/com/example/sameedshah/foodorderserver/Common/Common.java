package com.example.sameedshah.foodorderserver.Common;

import com.example.sameedshah.foodorderserver.Model.Request;
import com.example.sameedshah.foodorderserver.Model.User;

public class Common {

    public static  User currentUser;
    public static Request currentRequest;

    public static final String UPDATE = "Update";
    public static final String DELETE    = "Delete";
    public static  final int  IMAGE_REQUEST = 71;

    public static String convertCodeToStatus(String code){

        if(code.equals("0"))
            return "Placed";
       else if(code.equals("1"))
            return "On my way";

       else
           return "Shipped";
    }
}
