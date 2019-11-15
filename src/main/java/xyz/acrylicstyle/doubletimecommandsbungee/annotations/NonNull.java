package xyz.acrylicstyle.doubletimecommandsbungee.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })
public @interface NonNull {}
