package org.keycloak.test.framework.injection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.test.framework.AbstractKeycloakExtension;
import org.keycloak.test.framework.KeycloakAdminClient;
import org.keycloak.test.framework.KeycloakTestRealm;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

public class KeycloakTestRealmProvider implements InjectionProvider {

    private final AbstractKeycloakExtension extension;

    public KeycloakTestRealmProvider(AbstractKeycloakExtension extension) {
        this.extension = extension;
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return KeycloakTestRealm.class;
    }

    @Override
    public Object getValue(ExtensionContext context, Annotation annotation) {
        KeycloakTestRealm keycloakTestRealm = (KeycloakTestRealm) annotation;
        if (!extension.hasResource(RealmResource.class)) {
            String realmName = ((KeycloakTestRealm) annotation).name();
            if (realmName == null || realmName.isEmpty()) {
                realmName = RandomStringUtils.randomAlphabetic(10);
            }

            Keycloak keycloak = extension.getResource(Keycloak.class);
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(realmName);
            keycloak.realms().create(realm);

            RealmResource realmResource = keycloak.realm(realmName);
            extension.putResource(RealmResource.class, realmResource);

            System.out.println("Created realm " + realmName);
        }
        return extension.getResource(RealmResource.class);
    }

    @Override
    public void beforeAll() {
        System.out.println("============== KeycloakTestRealmProvider.beforeAll ==============");
        InjectionProvider.super.beforeAll();
    }

    @Override
    public void beforeEach() {
        System.out.println("============== KeycloakTestRealmProvider.beforeEach ==============");
        InjectionProvider.super.beforeEach();
    }

    @Override
    public void afterEach() {
        System.out.println("============== KeycloakTestRealmProvider.afterEach ==============");
    }

    @Override
    public void afterAll() {
        System.out.println("============== KeycloakTestRealmProvider.afterAll ==============");
        RealmResource resource = extension.getResource(RealmResource.class);
        if (resource != null) {
            System.out.println("Deleted realm");
            resource.remove();
        }
    }
}
