package org.keycloak.testsuite.forms2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.testsuite.KeycloakTest;

import java.util.List;

public class SimpleTest extends KeycloakTest {

    @Test
    public void simpleTest() {
        Assertions.assertTrue(true);
    }

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {

    }
}
