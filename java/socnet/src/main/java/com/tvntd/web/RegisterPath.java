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
package com.tvntd.web;

import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tvntd.service.api.GenericRequest.VerifyToken;
import com.tvntd.service.api.GenericResponse;
import com.tvntd.service.api.IAnnonService;
import com.tvntd.service.api.IAnnonService.AnnonUserDTO;
import com.tvntd.service.api.IUserService;
import com.tvntd.service.api.LoginResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tvntd.error.EmailExistsException;
import com.tvntd.events.RegistrationEvent;
import com.tvntd.forms.RegisterForm;
import com.tvntd.models.User;
import com.tvntd.models.VerificationToken;

@Controller
public class RegisterPath
{
    static private Logger s_log = LoggerFactory.getLogger(RegisterPath.class);

    @Autowired
    protected IAnnonService annonSvc;
    
    @Autowired
    private IUserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MessageSource messages;

    /**
     * Handle registration page.
     */
    @RequestMapping(value = "/register",
            consumes = "application/json", method = RequestMethod.POST)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ResponseBody
    public GenericResponse
    register(@Valid @RequestBody RegisterForm reg,
            Locale locale, HttpSession session,
            HttpServletRequest request, HttpServletResponse response)
    {
        s_log.debug("Register for " + reg.getEmail());
        try {
            AnnonUserDTO annon = annonSvc.getAnnonUser(request, response, session);
            User user = userService.registerNewUserAccount(reg, annon.getUserUuid());
            RegistrationEvent event = new RegistrationEvent(user, request);

            s_log.debug("OK, send email " + event.toString());
            eventPublisher.publishEvent(event);

            return new LoginResponse(GenericResponse.REG_WAIT_EMAIL,
                    reg.getEmail(), null, null);

        } catch(EmailExistsException e) {
            s_log.debug("Email exist: " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new LoginResponse(GenericResponse.REG_USER_EXISTS,
                    e.getMessage(), "Email exists", null);
        }
    }

    @RequestMapping(value = "/register/resend",
            consumes = "application/json", method = RequestMethod.POST)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ResponseBody
    public GenericResponse
    resendRegLink(Locale locale, @RequestBody RegisterForm reg,
            HttpServletRequest request, HttpServletResponse response)
    {
        String email = reg.getEmail();

        s_log.debug("Resend reg for " + email);
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return new LoginResponse(GenericResponse.REG_NO_USER,
                    "Could not find email account", email, null);
        }
        RegistrationEvent event = new RegistrationEvent(user, request);
        eventPublisher.publishEvent(event);
        return new LoginResponse(GenericResponse.REG_WAIT_EMAIL,
                reg.getEmail(), null, null);
    }

    /**
     * Shortcut to validate registration token.
     */
    @RequestMapping(value = "/register/verify",
            consumes = "application/json", method = RequestMethod.POST)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ResponseBody
    public GenericResponse
    shortcutVerify(Locale locale, @RequestBody VerifyToken input)
    {
        VerificationToken verify =
            userService.getVerificationToken(input.getAuthVerifToken());
        User user = (verify != null) ? verify.getUser() : null;

        if (user == null) {
            return errorResponse("Invalid Token",
                    "reg.confirm.invalid.token", null, locale);
        }
        s_log.debug("Verify token " + input.getAuthVerifToken() +
                " for user " + user.getEmail());
        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        return new LoginResponse(GenericResponse.REG_OK_LOGIN, "ok", null, null);
    }

    /**
     * Common code to return failure response.
     */
    protected LoginResponse
    errorResponse(String mesg, String err, Object[] args, Locale locale)
    {
        s_log.debug("Error response: " + mesg);
        return new LoginResponse(GenericResponse.REG_FAILED,
                mesg, messages.getMessage(err, args, locale), null);
    }

    /**
     * Confirm the registration through email.
     */
    @RequestMapping(value = "/register/confirm", method = RequestMethod.GET)
    public ModelAndView confirmRegister(Locale locale, ModelMap model,
            @RequestParam("token") String token, RedirectAttributes rattr)
    {
        model.addAttribute("lang", locale.getLanguage());
        VerificationToken verify = userService.getVerificationToken(token);
        if (verify == null) {
            rattr.addFlashAttribute("message",
                messages.getMessage("reg.confirm.invalid.token", null, locale));
            return new ModelAndView("redirect:/error/bad-user", model);
        }
        User user = verify.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verify.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            model.addAttribute("message",
                    messages.getMessage("reg.confirm.expired.token", null, locale));
            model.addAttribute("expired", true);
            model.addAttribute("token", token);
            return new ModelAndView("redirect:/error/bad-user", model);
        }
        user.setEnabled(true);
        userService.saveRegisteredUser(user);
        model.addAttribute("message",
                messages.getMessage("reg.confirm.verified", null, locale));
        return new ModelAndView("redirect:/#/login", model);
    }

    protected UserDetails getPrincipal()
    {
        Object user = SecurityContextHolder.getContext().
            getAuthentication().getPrincipal();
        if (user instanceof UserDetails) {
            return (UserDetails)user;
        }
        return null;
    }
}
