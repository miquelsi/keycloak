package org.keycloak.test.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeycloakTestRealm {

    String name() default "";

    Class<DefaultRealmConfiguration> configuration() default DefaultRealmConfiguration.class;

}
