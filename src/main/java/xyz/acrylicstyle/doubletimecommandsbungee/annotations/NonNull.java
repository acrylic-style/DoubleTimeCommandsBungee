package xyz.acrylicstyle.doubletimecommandsbungee.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })
/**
 * It's not null 100%, so you don't have to put <pre>if (obj == null) return;</pre>.
 */
public @interface NonNull {}
