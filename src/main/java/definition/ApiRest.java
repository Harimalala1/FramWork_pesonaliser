package definition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiRest {
    // true : la reponse est du JSON (pas de vue) / false : comportement normal (vue)
    boolean json() default true;
}
