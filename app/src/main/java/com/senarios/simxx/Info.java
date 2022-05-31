package com.senarios.simxx;


import com.senarios.simxx.activities.MainActivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

@Retention(RetentionPolicy.RUNTIME)
public @interface Info {
enum Priority{
    LOW,MEDIUM,HIGH
    }
    enum CallType{
    API_CALL,API_RESPONSE,CUSTOM,EVENT,TRANSCODER,SERVICE,ACTIVITY,FRAGMENT,BROADCAST_RECEIVER,VIEW_MODEL,DATABASE,AWS_S3
    }
    class def {};
    Priority priority() default Priority.LOW;
    String author() default "Mr-Hashim";
    String type() default "None";
    CallType callType() default CallType.CUSTOM;
    String description() default "None";
    Class classname() default Exception.class;
    String method() default "";

}

