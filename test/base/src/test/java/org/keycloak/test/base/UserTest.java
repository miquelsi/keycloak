package org.keycloak.test.base;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.test.framework.KeycloakAdminClient;
import org.keycloak.test.framework.KeycloakExtension;
import org.keycloak.test.framework.KeycloakSharedExtension;
import org.keycloak.test.framework.KeycloakTestRealm;
import org.keycloak.test.util.ApiUtil;

import java.util.Collections;

@ExtendWith(KeycloakSharedExtension.class)
public class UserTest {

    @KeycloakAdminClient
    Keycloak adminClient;

    @KeycloakTestRealm
    RealmResource realm;

    @AfterEach
    public void after() {
        realm.users().list().stream().
                forEach(u -> realm.users().delete(u.getId()));
    }

    private String createUser() {
        return createUser("user1", "user1@localhost");
    }

    private String createUser(String username, String email) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setRequiredActions(Collections.emptyList());
        user.setEnabled(true);

        return createUser(user);
    }

    private String createUser(UserRepresentation userRep) {
        final String createdId;
        try (Response response = realm.users().create(userRep)) {
            Assertions.assertEquals(201, response.getStatus());
            createdId = ApiUtil.getCreatedId(response);
        }

        return createdId;
    }

    @Test
    public void testCreateUser() {
        String userId = createUser();
        Assertions.assertNotNull(userId);
    }

    @Test
    public void createUserWithTemporaryPasswordWithAdditionalPasswordUpdateShouldRemoveUpdatePasswordRequiredAction() {

        String userId = createUser();

        CredentialRepresentation credTmp = new CredentialRepresentation();
        credTmp.setType(CredentialRepresentation.PASSWORD);
        credTmp.setValue("temp");
        credTmp.setTemporary(Boolean.TRUE);

        realm.users().get(userId).resetPassword(credTmp);

        CredentialRepresentation credPerm = new CredentialRepresentation();
        credPerm.setType(CredentialRepresentation.PASSWORD);
        credPerm.setValue("perm");
        credPerm.setTemporary(null);

        realm.users().get(userId).resetPassword(credPerm);

        UserRepresentation userRep = realm.users().get(userId).toRepresentation();

        Assertions.assertFalse(userRep.getRequiredActions().contains(UserModel.RequiredAction.UPDATE_PASSWORD.name()));
    }

    @Test
    public void createDuplicatedUser1() {
        createUser();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("user1");
        try (Response response = realm.users().create(userRepresentation)) {
            Assertions.assertEquals(409, response.getStatus());

            // Just to show how to retrieve underlying error message
            ErrorRepresentation error = response.readEntity(ErrorRepresentation.class);
            Assertions.assertEquals("User exists with same username", error.getErrorMessage());
        }
    }

    @Test
    public void createDuplicatedUser2() {
        createUser();

        UserRepresentation user = new UserRepresentation();
        user.setUsername("user2");
        user.setEmail("user1@localhost");

        try (Response response = realm.users().create(user)) {
            Assertions.assertEquals(409, response.getStatus());

            ErrorRepresentation error = response.readEntity(ErrorRepresentation.class);
            Assertions.assertEquals("User exists with same email", error.getErrorMessage());
        }
    }

}
