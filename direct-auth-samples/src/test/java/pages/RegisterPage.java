/*
 * Copyright 2021-Present Okta, Inc.
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
package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RegisterPage extends Page {

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(name = "firstname")
    public WebElement firstnameInput;

    @FindBy(name = "lastname")
    public WebElement lastnameInput;

    @FindBy(name = "email")
    public WebElement emailInput;

    @FindBy(name = "phone")
    public WebElement phoneInput;

    @FindBy(id = "sign-in-btn")
    public WebElement signInButton;

    @FindBy(css = "input[name='authenticator-type'][value='Email']")
    public WebElement emailRadioButton;

    @FindBy(css = "input[name='authenticator-type'][value='Phone']")
    public WebElement phoneRadioButton;

    @FindBy(css = "input[name='authenticator-type'][value='Password']")
    public WebElement passwordRadioButton;

    @FindBy(css = "input[name='mode'][value='sms']")
    public WebElement smsRadioButton;

    @FindBy(css = "input[name='mode'][value='voice']")
    public WebElement voiceRadioButton;

    @FindBy(name = "new-password")
    public WebElement newPasswordInput;

    @FindBy(name = "confirm-new-password")
    public WebElement confirmNewPasswordInput;

    @FindBy(id = "proceed-btn")
    public WebElement proceedButton;

    @FindBy(name = "code")
    public WebElement codeInput;

    @FindBy(id = "verify-btn")
    public WebElement verifyButton;

    @FindBy(id = "submit-btn")
    public WebElement submitButton;

    @FindBy(id = "skip-btn")
    public WebElement skipButton;

    @FindBy(id = "profileTable")
    public WebElement profileTable;

    @FindBy(className = "alert-danger")
    public WebElement alertDanger;
}
