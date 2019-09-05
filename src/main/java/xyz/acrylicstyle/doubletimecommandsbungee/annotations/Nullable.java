package xyz.acrylicstyle.doubletimecommandsbungee.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })
/**
 * Nullable means it may be null so you have to consider about returned null.
 */
public @interface Nullable {}
