/*
 * Copyright (C) 2014-2015 Vy Nguyen
 * Github https://github.com/vy-nguyen/tvntd
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tvntd.forms;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.tvntd.util.PasswordMatches;
import com.tvntd.util.Util;
import com.tvntd.util.ValidEmail;
import com.tvntd.util.ValidPassword;

@PasswordMatches
public class RegisterForm
{
    @NotNull
    @Size(min = 4)
    @ValidEmail
    private String email;

    @NotNull
    @Size(min = 1, max = 16)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 16)
    private String lastName;

    @NotNull
    @ValidPassword
    @Size(min = 1, max = 64)
    private String password0;

    @NotNull
    @Size(min = 1, max = 64)
    private String password1;

    @Size(max = 64)
    private String gender;

    @Size(max = 32)
    private String okTerm;

    @Size(max = 32)
    private String locale;

    @Size(max = 32)
    protected String role;

    public RegisterForm()
    {
        role   = "User";
        locale = "VN";
    }

    public RegisterForm(String email)
    {
        super();
        this.email     = email;
        this.firstName = "auto";
        this.lastName  = null;
        this.password0 = UUID.randomUUID().toString();
    }

    public boolean cleanInput()
    {
        if (email == null) {
            return false;
        }
        Whitelist wlist = Util.allowedTags;
        if (firstName == null) {
            firstName = "";
        }
        firstName = Jsoup.clean(firstName, wlist);
        if (lastName == null) {
            lastName = "";
        }
        lastName = Jsoup.clean(lastName, wlist);
        if (password0 == null) {
            password0 = "";
        }
        if (password1 == null) {
            password1 = "";
        }
        if (gender == null) {
            gender = "Male";
        }
        if (okTerm == null) {
            okTerm = "ok";
        }
        return true;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the password0
     */
    public String getPassword0() {
        return password0;
    }

    /**
     * @param password0 the password0 to set
     */
    public void setPassword0(String password0) {
        this.password0 = password0;
    }

    /**
     * @return the password1
     */
    public String getPassword1() {
        return password1;
    }

    /**
     * @param password1 the password1 to set
     */
    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return the okTerm
     */
    public String getOkTerm() {
        return okTerm;
    }

    /**
     * @param okTerm the okTerm to set
     */
    public void setOkTerm(String okTerm) {
        this.okTerm = okTerm;
    }

    /**
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("First name : ").append(firstName)
            .append("Last name: ").append(lastName)
            .append("Email    : ").append(email)
            .append("Password : ").append(password0)
            .append("Locale   : ").append(locale);
        return sb.toString();
    }
}
