package definition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// L'annotation sera utilisée sur les méthodes des contrôleurs
@Target(ElementType.METHOD)

// L'annotation doit être accessible pendant l'exécution
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {

}