/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.welcomepage;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.testsuite.KeycloakTest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WelcomePageTest extends KeycloakTest {

    @BeforeAll
    public static void restartKeycloak() {
        keycloak.stop();
        keycloak.start();
    }

    @Test
    @Order(1)
    public void test_6_CheckProductNameOnWelcomePage() {
        page.navigate(keycloak.getBaseUrl());
        Assertions.assertTrue("Welcome to Keycloak".equals(page.title()));
    }
    @Test
    @Order(2)
    public void test_1_LocalAccessNoAdmin() {
        page.navigate(keycloak.getBaseUrl());
        Locator passwordInput = page.locator("input[name=\"passwordConfirmation\"]");
        Assertions.assertTrue(passwordInput.inputValue().isEmpty(), "Welcome page did not ask to create a new admin user.");
    }

    @Test
    @Order(3)
    public void test_2_RemoteAccessNoAdmin() throws Exception {
        page.navigate(getPublicServerUrl().toString());
        Locator text = page.getByText("Local access required");
        Assertions.assertTrue(text.isVisible(), "Remote access should have not been allowed.");
    }

    @Test
    @Order(4)
    public void test_3_LocalAccessWithAdmin() throws Exception {
        page.navigate(keycloak.getBaseUrl());
        Locator usernameInput = page.locator("input[name=\"username\"]");
        usernameInput.fill("admin");

        Locator passwordInput = page.locator("input[name=\"password\"]");
        passwordInput.fill("admin");

        Locator passwordConfirmationInput = page.locator("input[name=\"passwordConfirmation\"]");
        passwordConfirmationInput.fill("admin");

        Locator submitButton = page.locator("button[type=\"submit\"]");
        submitButton.click();
        Assertions.assertTrue(page.getByText("User created").isVisible());
    }

    /**
     * Attempt to resolve the floating IP address. This is where EAP/WildFly
     * will be accessible. See "-Djboss.bind.address=0.0.0.0".
     *
     * @return
     * @throws Exception
     */
    private String getFloatingIpAddress() throws Exception {
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface ni : Collections.list(netInterfaces)) {
            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
            for (InetAddress a : Collections.list(inetAddresses)) {
                if (!a.isLoopbackAddress() && a.isSiteLocalAddress()) {
                    return a.getHostAddress();
                }
            }
        }
        return null;
    }

    private URL getPublicServerUrl() throws Exception {
        String floatingIp = getFloatingIpAddress();
        if (floatingIp == null) {
            throw new RuntimeException("Could not determine floating IP address.");
        }
        return new URL("http", floatingIp, keycloak.getPort(), "");
    }

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {

    }
}
