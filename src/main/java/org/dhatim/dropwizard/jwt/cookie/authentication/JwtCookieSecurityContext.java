/**
 * Copyright 2016 Dhatim
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dhatim.dropwizard.jwt.cookie.authentication;

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

/**
 * Security context set after a JWT cookie authentication
 */
public class JwtCookieSecurityContext implements SecurityContext{

    private final JwtCookiePrincipal subject;
    private final boolean secure;

    public JwtCookieSecurityContext(JwtCookiePrincipal subject, boolean secure) {
        this.subject = subject;
        this.secure = secure;
    }
    
    @Override
    public Principal getUserPrincipal() {
        return subject;
    }

    @Override
    public boolean isUserInRole(String role) {
        return subject.hasRole(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT_COOKIE";
    }
    
}