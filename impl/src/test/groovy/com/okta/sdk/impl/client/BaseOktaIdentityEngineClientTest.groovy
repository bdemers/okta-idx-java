/*
 * Copyright 2020-Present Okta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okta.sdk.impl.client

import com.okta.commons.http.DefaultResponse
import com.okta.commons.http.MediaType
import com.okta.commons.http.Request
import com.okta.commons.http.RequestExecutor
import com.okta.commons.http.Response
import com.okta.sdk.api.client.OktaIdentityEngineClient
import com.okta.sdk.api.model.Authenticator
import com.okta.sdk.api.model.Credentials
import com.okta.sdk.api.model.FormValue
import com.okta.sdk.api.model.Options
import com.okta.sdk.api.model.RemediationOption
import com.okta.sdk.api.request.AnswerChallengeRequest
import com.okta.sdk.api.request.ChallengeRequest
import com.okta.sdk.api.request.IdentifyRequest
import com.okta.sdk.api.response.OktaIdentityEngineResponse

import org.testng.annotations.Test

import static org.mockito.Mockito.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItemInArray
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.nullValue

class BaseOktaIdentityEngineClientTest {

    @Test
    void testInteractResponse() {

        RequestExecutor requestExecutor = mock(RequestExecutor)

        final OktaIdentityEngineClient oktaIdentityEngineClient =
            new BaseOktaIdentityEngineClient("https://example.com", "test-client-id", ["test-scope"].toSet(), requestExecutor)

        final Response stubbedResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/json"),
            new FileInputStream(getClass().getClassLoader().getResource("interact-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedResponse)

        OktaIdentityEngineResponse response = oktaIdentityEngineClient.interact()

        assertThat(response, notNullValue())
        assertThat(response.remediation(), notNullValue())
        assertThat(response.getMessages(), nullValue())
        assertThat(response.remediation().remediationOptions(), notNullValue())
    }

    @Test
    void testIntrospectResponse() {

        RequestExecutor requestExecutor = mock(RequestExecutor)

        final OktaIdentityEngineClient oktaIdentityEngineClient =
            new BaseOktaIdentityEngineClient("https://example.com", "test-client-id", ["test-scope"].toSet(), requestExecutor)

        final Response stubbedResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("introspect-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedResponse)

        OktaIdentityEngineResponse response = oktaIdentityEngineClient.introspect("test-state-handle")

        assertThat(response, notNullValue())
        assertThat(response.remediation(), notNullValue())
        assertThat(response.getMessages(), nullValue())
        assertThat(response.remediation().remediationOptions(), notNullValue())

        assertThat(response.expiresAt, equalTo("2020-10-31T01:42:02.000Z"))
        assertThat(response.intent, equalTo("LOGIN"))
        assertThat(response.remediation.type, equalTo("array"))
        assertThat(response.remediation.value.first().rel, hasItemInArray("create-form"))
        assertThat(response.remediation.value.first().name, equalTo("identify"))
        assertThat(response.remediation.value.first().href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/identify"))
        assertThat(response.remediation.value.first().method, equalTo("POST"))
        assertThat(response.remediation.value.first().accepts, equalTo("application/ion+json; okta-version=1.0.0"))

        assertThat(response.remediation().remediationOptions().first().form(), notNullValue())

        FormValue[] formValues = response.remediation().remediationOptions().first().form()

        Optional<FormValue> stateHandleForm = Arrays.stream(formValues)
            .filter({ x -> ("stateHandle" == x.getName()) })
            .findFirst()

        FormValue stateHandleFormValue = stateHandleForm.get()

        assertThat(stateHandleFormValue, notNullValue())
        assertThat(stateHandleFormValue.required, equalTo(true))
        assertThat(stateHandleFormValue.value, equalTo("02tYS1NHhCPLcOpT3GByBBRHmGU63p7LGRXJx5cOvp"))
        assertThat(stateHandleFormValue.visible, equalTo(false))
        assertThat(stateHandleFormValue.mutable, equalTo(false))

        Optional<FormValue> identifierForm = Arrays.stream(formValues)
            .filter({ x -> ("identifier" == x.getName()) })
            .findFirst()

        FormValue identifierFormValue = identifierForm.get()

        assertThat(identifierFormValue, notNullValue())
        assertThat(identifierFormValue.label, equalTo("Username"))

        Optional<FormValue> rememberMeForm = Arrays.stream(formValues)
            .filter({ x -> ("rememberMe" == x.getName()) })
            .findFirst()

        FormValue rememberMeFormValue = rememberMeForm.get()

        assertThat(rememberMeFormValue, notNullValue())
        assertThat(rememberMeFormValue.label, equalTo("Remember this device"))
        assertThat(rememberMeFormValue.type, equalTo("boolean"))
    }

    @Test
    void testIdentifyResponse() {

        RequestExecutor requestExecutor = mock(RequestExecutor)

        final OktaIdentityEngineClient oktaIdentityEngineClient =
            new BaseOktaIdentityEngineClient("https://example.com", "test-client-id", ["test-scope"].toSet(), requestExecutor)

        final Response stubbedIntrospectResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("introspect-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedIntrospectResponse)

        OktaIdentityEngineResponse introspectResponse = oktaIdentityEngineClient.introspect("test-state-handle")

        assertThat(introspectResponse.remediation().remediationOptions(), notNullValue())
        assertThat(introspectResponse.remediation.value.first().href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/identify"))

        IdentifyRequest identifyRequest = new IdentifyRequest("test-identifier", "test-state-handle", false)

        final Response stubbedIdentifyResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("identify-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedIdentifyResponse)

        OktaIdentityEngineResponse identifyResponse =
            introspectResponse.remediation().remediationOptions().first().proceed(oktaIdentityEngineClient, identifyRequest)

        assertThat(identifyResponse, notNullValue())
        assertThat(identifyResponse.stateHandle, notNullValue())
        assertThat(identifyResponse.version, notNullValue())
        assertThat(identifyResponse.expiresAt, equalTo("2020-10-30T23:47:46.000Z"))
        assertThat(identifyResponse.intent, equalTo("LOGIN"))
        assertThat(identifyResponse.remediation.type, equalTo("array"))

        assertThat(identifyResponse.remediation().remediationOptions(), notNullValue())
        assertThat(identifyResponse.remediation.value.first().rel, hasItemInArray("create-form"))
        assertThat(identifyResponse.remediation.value.first().name, equalTo("select-authenticator-authenticate"))
        assertThat(identifyResponse.remediation.value.first().href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/challenge"))
        assertThat(identifyResponse.remediation.value.first().method, equalTo("POST"))
        assertThat(identifyResponse.remediation.value.first().accepts, equalTo("application/ion+json; okta-version=1.0.0"))

        assertThat(identifyResponse.remediation.value.first().form(), notNullValue())

        FormValue[] formValues = identifyResponse.remediation().remediationOptions().first().form()

        Optional<FormValue> stateHandleForm = Arrays.stream(formValues)
            .filter({ x -> ("stateHandle" == x.getName()) })
            .findFirst()

        FormValue stateHandleFormValue = stateHandleForm.get()

        assertThat(stateHandleFormValue, notNullValue())
        assertThat(stateHandleFormValue.required, equalTo(true))
        assertThat(stateHandleFormValue.value, equalTo("02tYS1NHhCPLcOpT3GByBBRHmGU63p7LGRXJx5cOvp"))
        assertThat(stateHandleFormValue.visible, equalTo(false))
        assertThat(stateHandleFormValue.mutable, equalTo(false))

        Optional<FormValue> authenticatorForm = Arrays.stream(formValues)
            .filter({ x -> ("authenticator" == x.getName()) })
            .findFirst()

        FormValue authenticatorFormValue = authenticatorForm.get()

        assertThat(authenticatorFormValue, notNullValue())
        assertThat(authenticatorFormValue.type, equalTo("object"))

        // Email
        Options emailOption = authenticatorFormValue.options().find {it.label == "Email"}

        FormValue idForm = emailOption.getValue().getForm().getValue().find {it.name == "id"}
        assertThat(idForm, notNullValue())
        assertThat(idForm.required, equalTo(true))
        assertThat(idForm.value, equalTo("aut2ihzk1gHl7ynhd1d6"))
        assertThat(idForm.mutable, equalTo(false))

        FormValue methodTypeForm = emailOption.getValue().getForm().getValue().find {it.name == "methodType"}
        assertThat(methodTypeForm, notNullValue())
        assertThat(methodTypeForm.required, equalTo(false))
        assertThat(methodTypeForm.value, equalTo("email"))
        assertThat(methodTypeForm.mutable, equalTo(false))

        // Password
        Options passwordOption = authenticatorFormValue.options().find {it.label == "Password"}

        idForm = passwordOption.getValue().getForm().getValue().find {it.name == "id"}
        assertThat(idForm, notNullValue())
        assertThat(idForm.required, equalTo(true))
        assertThat(idForm.value, equalTo("aut2ihzk2n15tsQnQ1d6"))
        assertThat(idForm.mutable, equalTo(false))

        methodTypeForm = passwordOption.getValue().getForm().getValue().find {it.name == "methodType"}
        assertThat(methodTypeForm, notNullValue())
        assertThat(methodTypeForm.required, equalTo(false))
        assertThat(methodTypeForm.value, equalTo("password"))
        assertThat(methodTypeForm.mutable, equalTo(false))

        // Security Question
        Options secQnOption = authenticatorFormValue.options().find {it.label == "Security Question"}

        idForm = secQnOption.getValue().getForm().getValue().find {it.name == "id"}
        assertThat(idForm, notNullValue())
        assertThat(idForm.required, equalTo(true))
        assertThat(idForm.value, equalTo("aut2ihzk4hgf9sIQa1d6"))
        assertThat(idForm.mutable, equalTo(false))

        methodTypeForm = secQnOption.getValue().getForm().getValue().find {it.name == "methodType"}
        assertThat(methodTypeForm, notNullValue())
        assertThat(methodTypeForm.required, equalTo(false))
        assertThat(methodTypeForm.value, equalTo("security_question"))
        assertThat(methodTypeForm.mutable, equalTo(false))
    }

    @Test
    void testChallengeResponse() {

        RequestExecutor requestExecutor = mock(RequestExecutor)

        final OktaIdentityEngineClient oktaIdentityEngineClient =
            new BaseOktaIdentityEngineClient("https://example.com", "test-client-id", ["test-scope"].toSet(), requestExecutor)

        final Response stubbedIntrospectResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("introspect-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedIntrospectResponse)

        OktaIdentityEngineResponse introspectResponse = oktaIdentityEngineClient.introspect("test-state-handle")

        assertThat(introspectResponse.remediation().remediationOptions(), notNullValue())
        assertThat(introspectResponse.remediation.value.first().href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/identify"))

        IdentifyRequest identifyRequest = new IdentifyRequest("test-identifier", "test-state-handle", false)

        final Response stubbedIdentifyResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("identify-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedIdentifyResponse)

        OktaIdentityEngineResponse identifyResponse =
            introspectResponse.remediation().remediationOptions().first().proceed(oktaIdentityEngineClient, identifyRequest)

        assertThat(identifyResponse, notNullValue())
        assertThat(identifyResponse.stateHandle, notNullValue())
        assertThat(identifyResponse.version, notNullValue())
        assertThat(identifyResponse.expiresAt, equalTo("2020-10-30T23:47:46.000Z"))
        assertThat(identifyResponse.intent, equalTo("LOGIN"))
        assertThat(identifyResponse.remediation.type, equalTo("array"))

        assertThat(identifyResponse.remediation().remediationOptions(), notNullValue())
        assertThat(identifyResponse.remediation.value.first().rel, hasItemInArray("create-form"))
        assertThat(identifyResponse.remediation.value.first().name, equalTo("select-authenticator-authenticate"))
        assertThat(identifyResponse.remediation.value.first().href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/challenge"))
        assertThat(identifyResponse.remediation.value.first().method, equalTo("POST"))
        assertThat(identifyResponse.remediation.value.first().accepts, equalTo("application/ion+json; okta-version=1.0.0"))

        assertThat(identifyResponse.remediation.value.first().form(), notNullValue())

        // proceed with password authenticator challenge
        ChallengeRequest passwordAuthenticatorChallengeRequest =
            new ChallengeRequest("test-state-handle", new Authenticator("aut2ihzk2n15tsQnQ1d6", "password"))

        final Response stubbedChallengeResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("challenge-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedChallengeResponse)

        OktaIdentityEngineResponse passwordAuthenticatorChallengeResponse =
            identifyResponse.remediation().remediationOptions().first().proceed(oktaIdentityEngineClient, passwordAuthenticatorChallengeRequest)

        assertThat(passwordAuthenticatorChallengeResponse.stateHandle, notNullValue())
        assertThat(passwordAuthenticatorChallengeResponse.version, notNullValue())
        assertThat(passwordAuthenticatorChallengeResponse.expiresAt, equalTo("2020-10-29T21:17:28.000Z"))
        assertThat(passwordAuthenticatorChallengeResponse.intent, equalTo("LOGIN"))
        assertThat(passwordAuthenticatorChallengeResponse.remediation.type, equalTo("array"))

        assertThat(passwordAuthenticatorChallengeResponse.remediation().remediationOptions(), notNullValue())
        assertThat(passwordAuthenticatorChallengeResponse.remediation.value.first().rel, hasItemInArray("create-form"))
        assertThat(passwordAuthenticatorChallengeResponse.remediation.value.first().name, equalTo("challenge-authenticator"))
        assertThat(passwordAuthenticatorChallengeResponse.remediation.value.first().href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/challenge/answer"))
        assertThat(passwordAuthenticatorChallengeResponse.remediation.value.first().method, equalTo("POST"))
        assertThat(passwordAuthenticatorChallengeResponse.remediation.value.first().accepts, equalTo("application/ion+json; okta-version=1.0.0"))

        assertThat(passwordAuthenticatorChallengeResponse.remediation.value.first().form(), notNullValue())

        FormValue[] formValues = passwordAuthenticatorChallengeResponse.remediation().remediationOptions().first().form()

        Optional<FormValue> stateHandleForm = Arrays.stream(formValues)
            .filter({ x -> ("stateHandle" == x.getName()) })
            .findFirst()

        FormValue stateHandleFormValue = stateHandleForm.get()

        assertThat(stateHandleFormValue, notNullValue())
        assertThat(stateHandleFormValue.required, equalTo(true))
        assertThat(stateHandleFormValue.value, equalTo("025r9Yn758Z-zwhMGDm1saTaW1pVRy4t9oTxM7dLYE"))
        assertThat(stateHandleFormValue.visible, equalTo(false))
        assertThat(stateHandleFormValue.mutable, equalTo(false))

        Optional<FormValue> credentialsFormOptional = Arrays.stream(formValues)
            .filter({ x -> ("credentials" == x.getName()) })
            .findFirst()

        FormValue credentialsForm = credentialsFormOptional.get()

        assertThat(credentialsForm, notNullValue())
        assertThat(credentialsForm.required, equalTo(true))
        assertThat(credentialsForm.form(), notNullValue())
        assertThat(credentialsForm.form().getValue(), notNullValue())

        FormValue credentialsFormValue = credentialsForm.form().getValue()
            .find {it.name == "passcode" && it.label == "Password" && it.secret }

        assertThat(credentialsFormValue, notNullValue())

        // other authenticators
        RemediationOption authenticatorOption = passwordAuthenticatorChallengeResponse.remediation().remediationOptions()
            .find {it.name == "select-authenticator-authenticate"}

        assertThat(authenticatorOption, notNullValue())
        assertThat(authenticatorOption.rel, hasItemInArray("create-form"))
        assertThat(authenticatorOption.href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/challenge"))
        assertThat(authenticatorOption.method, equalTo("POST"))
        assertThat(authenticatorOption.form(), notNullValue())

        FormValue authenticatorOptions = authenticatorOption.form().find {it.name == "authenticator"}
        assertThat(authenticatorOptions, notNullValue())
        assertThat(authenticatorOptions.type, equalTo("object"))

        // Email
        Options emailOption = authenticatorOptions.options().find {it.label == "Email"}

        FormValue idForm = emailOption.getValue().getForm().getValue().find {it.name == "id"}
        assertThat(idForm, notNullValue())
        assertThat(idForm.required, equalTo(true))
        assertThat(idForm.value, equalTo("aut2ihzk1gHl7ynhd1d6"))
        assertThat(idForm.mutable, equalTo(false))

        FormValue methodTypeForm = emailOption.getValue().getForm().getValue().find {it.name == "methodType"}
        assertThat(methodTypeForm, notNullValue())
        assertThat(methodTypeForm.required, equalTo(false))
        assertThat(methodTypeForm.value, equalTo("email"))
        assertThat(methodTypeForm.mutable, equalTo(false))

        // Password
        Options passwordOption = authenticatorOptions.options().find {it.label == "Password"}
        assertThat(passwordOption, notNullValue())

        idForm = passwordOption.getValue().getForm().getValue().find {it.name == "id"}
        assertThat(idForm, notNullValue())
        assertThat(idForm.required, equalTo(true))
        assertThat(idForm.value, equalTo("aut2ihzk2n15tsQnQ1d6"))
        assertThat(idForm.mutable, equalTo(false))

        methodTypeForm = passwordOption.getValue().getForm().getValue().find {it.name == "methodType"}
        assertThat(methodTypeForm, notNullValue())
        assertThat(methodTypeForm.required, equalTo(false))
        assertThat(methodTypeForm.value, equalTo("password"))
        assertThat(methodTypeForm.mutable, equalTo(false))

        // Security Question
        Options secQnOption = authenticatorOptions.options().find {it.label == "Security Question"}
        assertThat(secQnOption, notNullValue())

        idForm = secQnOption.getValue().getForm().getValue().find {it.name == "id"}
        assertThat(idForm, notNullValue())
        assertThat(idForm.required, equalTo(true))
        assertThat(idForm.value, equalTo("aut2ihzk4hgf9sIQa1d6"))
        assertThat(idForm.mutable, equalTo(false))

        methodTypeForm = secQnOption.getValue().getForm().getValue().find {it.name == "methodType"}
        assertThat(methodTypeForm, notNullValue())
        assertThat(methodTypeForm.required, equalTo(false))
        assertThat(methodTypeForm.value, equalTo("security_question"))
        assertThat(methodTypeForm.mutable, equalTo(false))
    }

    @Test
    void testAnswerChallengeResponse() {

        RequestExecutor requestExecutor = mock(RequestExecutor)

        final OktaIdentityEngineClient oktaIdentityEngineClient =
            new BaseOktaIdentityEngineClient("https://example.com", "test-client-id", ["test-scope"].toSet(), requestExecutor)

        ChallengeRequest passwordAuthenticatorChallengeRequest =
            new ChallengeRequest("test-state-handle", new Authenticator("aut2ihzk2n15tsQnQ1d6", "password"))

        final Response stubbedChallengeResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("challenge-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedChallengeResponse)

        OktaIdentityEngineResponse passwordAuthenticatorChallengeResponse =
            oktaIdentityEngineClient.challenge(passwordAuthenticatorChallengeRequest)

        assertThat(passwordAuthenticatorChallengeResponse, notNullValue())

        AnswerChallengeRequest passwordAuthenticatorAnswerChallengeRequest =
            new AnswerChallengeRequest("test-state-handle", new Credentials("some-password", null))

        final Response stubbedAnswerChallengeResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("answer-challenge-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedAnswerChallengeResponse)

        OktaIdentityEngineResponse passwordAuthenticatorAnswerChallengeResponse =
            passwordAuthenticatorChallengeResponse.remediation().remediationOptions().first().proceed(oktaIdentityEngineClient, passwordAuthenticatorAnswerChallengeRequest)

        assertThat(passwordAuthenticatorAnswerChallengeResponse.stateHandle, notNullValue())
        assertThat(passwordAuthenticatorAnswerChallengeResponse.version, notNullValue())
        assertThat(passwordAuthenticatorAnswerChallengeResponse.expiresAt, equalTo("2020-10-29T21:17:36.000Z"))
        assertThat(passwordAuthenticatorAnswerChallengeResponse.intent, equalTo("LOGIN"))
        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation.type, equalTo("array"))

        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation().remediationOptions(), notNullValue())
        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation.value.first().rel, hasItemInArray("create-form"))
        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation.value.first().name, equalTo("select-authenticator-authenticate"))
        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation.value.first().href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/challenge"))
        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation.value.first().method, equalTo("POST"))
        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation.value.first().accepts, equalTo("application/ion+json; okta-version=1.0.0"))

        assertThat(passwordAuthenticatorAnswerChallengeResponse.remediation.value.first().form(), notNullValue())

        FormValue[] formValues = passwordAuthenticatorAnswerChallengeResponse.remediation().remediationOptions().first().form()

        Optional<FormValue> stateHandleForm = Arrays.stream(formValues)
            .filter({ x -> ("stateHandle" == x.getName()) })
            .findFirst()

        FormValue stateHandleFormValue = stateHandleForm.get()

        assertThat(stateHandleFormValue, notNullValue())
        assertThat(stateHandleFormValue.required, equalTo(true))
        assertThat(stateHandleFormValue.value, equalTo("025r9Yn758Z-zwhMGDm1saTaW1pVRy4t9oTxM7dLYE"))
        assertThat(stateHandleFormValue.visible, equalTo(false))
        assertThat(stateHandleFormValue.mutable, equalTo(false))

        RemediationOption authenticatorOption = passwordAuthenticatorChallengeResponse.remediation().remediationOptions()
            .find {it.name == "select-authenticator-authenticate"}

        assertThat(authenticatorOption.rel, hasItemInArray("create-form"))
        assertThat(authenticatorOption.href, equalTo("https://devex-idx-testing.oktapreview.com/idp/idx/challenge"))
        assertThat(authenticatorOption.method, equalTo("POST"))
        assertThat(authenticatorOption.form(), notNullValue())

        FormValue authenticatorOptions = authenticatorOption.form().find {it.name == "authenticator"}
        assertThat(authenticatorOptions, notNullValue())
        assertThat(authenticatorOptions.type, equalTo("object"))

        // Email
        Options emailOption = authenticatorOptions.options().find {it.label == "Email"}
        assertThat(emailOption, notNullValue())

        FormValue idForm = emailOption.getValue().getForm().getValue().find {it.name == "id"}
        assertThat(idForm, notNullValue())
        assertThat(idForm.required, equalTo(true))
        assertThat(idForm.value, equalTo("aut2ihzk1gHl7ynhd1d6"))
        assertThat(idForm.mutable, equalTo(false))

        FormValue methodTypeForm = emailOption.getValue().getForm().getValue().find {it.name == "methodType"}
        assertThat(methodTypeForm, notNullValue())
        assertThat(methodTypeForm.required, equalTo(false))
        assertThat(methodTypeForm.value, equalTo("email"))
        assertThat(methodTypeForm.mutable, equalTo(false))
    }

    @Test
    void testSecondFactorSuccessResponse() {

        RequestExecutor requestExecutor = mock(RequestExecutor)

        final OktaIdentityEngineClient oktaIdentityEngineClient =
            new BaseOktaIdentityEngineClient("https://example.com", "test-client-id", ["test-scope"].toSet(), requestExecutor)

        AnswerChallengeRequest secondFactorAuthenticatorAnswerChallengeRequest =
            new AnswerChallengeRequest("test-state-handle", new Credentials("some-email-passcode", null))

        final Response stubbedAnswerChallengeResponse = new DefaultResponse(
            200,
            MediaType.valueOf("application/ion+json; okta-version=1.0.0"),
            new FileInputStream(getClass().getClassLoader().getResource("success-response.json").getFile()),
            -1)

        when(requestExecutor.executeRequest(any(Request.class))).thenReturn(stubbedAnswerChallengeResponse)

        OktaIdentityEngineResponse secondFactorAuthenticatorAnswerChallengeResponse =
            oktaIdentityEngineClient.answerChallenge(secondFactorAuthenticatorAnswerChallengeRequest)

        assertThat(secondFactorAuthenticatorAnswerChallengeResponse, notNullValue())
        assertThat(secondFactorAuthenticatorAnswerChallengeResponse.remediation(), nullValue())
        assertThat(secondFactorAuthenticatorAnswerChallengeResponse.stateHandle, notNullValue())
        assertThat(secondFactorAuthenticatorAnswerChallengeResponse.version, notNullValue())
        assertThat(secondFactorAuthenticatorAnswerChallengeResponse.expiresAt, equalTo("2020-10-30T23:49:21.000Z"))
        assertThat(secondFactorAuthenticatorAnswerChallengeResponse.intent, equalTo("LOGIN"))

        assertThat(secondFactorAuthenticatorAnswerChallengeResponse.success, notNullValue())
    }
}
