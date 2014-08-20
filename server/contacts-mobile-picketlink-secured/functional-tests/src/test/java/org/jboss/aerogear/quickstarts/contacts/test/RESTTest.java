/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.quickstarts.contacts.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;

/**
 * Test for REST API of the application
 * 
 * @author Oliver Kiss
 * @author Karel Piwko
 */
@RunAsClient
@RunWith(Arquillian.class)
public class RESTTest {

    private static final String USERNAME = "john";
    private static final String PASSWORD = "john";

    private static final String NEW_CONTACT_FIRSTNAME = "John";
    private static final String NEW_CONTACT_LASTNAME = "Doe";
    private static final String NEW_CONTACT_EMAIL = "john.doe@redhat.com";
    private static final String NEW_CONTACT_BIRTHDATE = "1970-01-01";
    private static final String NEW_CONTACT_PHONE = "7894561239";

    private static final String DEFAULT_CONTACT_FIRSTNAME = "John";
    private static final String DEFAULT_CONTACT_LASTNAME = "Smith";
    private static final int DEFAULT_CONTACT_ID = 10001;

    /**
     * Injects URL on which application is running.
     */
    @ArquillianResource
    URI contextPath;

    /**
     * Creates deployment which is sent to the container upon test's start.
     * 
     * @return war file which is deployed while testing, the whole application in our case
     */
    @Deployment(testable = false)
    public static WebArchive deployment() {
        return Deployments.contacts();
    }

    @Before
    public void setContextPath() {
        // set base path according to URL returned by Arquillian
        RestAssured.baseURI = contextPath.getScheme() + "://" + contextPath.getHost();
        RestAssured.port = contextPath.getPort();
        RestAssured.basePath = contextPath.getPath();

        // uncomment this line if you want to log requests and responses
        // RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    @InSequence(1)
    public void testGetContact() throws Exception {

        auth(USERNAME, PASSWORD);

        JsonPath json = given().cookies(auth(USERNAME, PASSWORD))
            .when()
            .get("/rest/contacts/{contactId}", DEFAULT_CONTACT_ID)
            .then()
            .statusCode(is(200))
            .extract()
            .jsonPath();

        assertEquals(DEFAULT_CONTACT_ID, json.get("id"));
        assertEquals(DEFAULT_CONTACT_FIRSTNAME, json.get("firstName"));
        assertEquals(DEFAULT_CONTACT_LASTNAME, json.get("lastName"));
    }

    @Test
    @InSequence(2)
    public void testAddContact() throws Exception {

        Contact contact = new Contact();
        contact.setFirstName(NEW_CONTACT_FIRSTNAME);
        contact.setLastName(NEW_CONTACT_LASTNAME);
        contact.setEmail(NEW_CONTACT_EMAIL);
        contact.setPhoneNumber(NEW_CONTACT_PHONE);
        contact.setBirthDate(NEW_CONTACT_BIRTHDATE);

        given().contentType("application/json; charset=UTF-8")
            .body(contact)
            .when()
            .post("/rest/contacts")
            .then()
            .statusCode(is(200));

    }

    @Test
    @InSequence(3)
    public void testGetAllContacts() throws Exception {

        Contact[] contacts = given().cookies(auth(USERNAME, PASSWORD))
            .when()
            .get("/rest/contacts/")
            .then()
            .statusCode(is(200))
            .extract()
            .as(Contact[].class);

        assertThat(contacts.length, is(3));

        Contact john = contacts[0];

        assertThat(john.getFirstName(), is(NEW_CONTACT_FIRSTNAME));
        assertThat(john.getLastName(), is(NEW_CONTACT_LASTNAME));
        assertThat(john.getEmail(), is(NEW_CONTACT_EMAIL));
        assertThat(john.getBirthDate(), is(NEW_CONTACT_BIRTHDATE));
        assertThat(john.getPhoneNumber(), is(NEW_CONTACT_PHONE));
    }

    // get authentication cookie
    private static Map<String, String> auth(String username, String password) {

        return given().auth()
            .preemptive()
            .basic(username, password)
            .when()
            .get("/rest/security/user/info")
            .then()
            .statusCode(is(200))
            .extract()
            .response()
            .getCookies();

    }
}
