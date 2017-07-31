package com.cap.simpleapp.general.service.impl.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cap.simpleapp.general.common.api.UserProfile;
import com.cap.simpleapp.general.common.api.Usermanagement;
import com.cap.simpleapp.general.logic.impl.UsermanagementDummyImpl;

import io.oasp.module.security.common.api.accesscontrol.AccessControl;
import io.oasp.module.security.common.api.accesscontrol.AccessControlProvider;
import io.oasp.module.security.common.base.accesscontrol.AccessControlGrantedAuthority;
import waffle.spring.NegotiateSecurityFilter;

/**
 * @author pparrado
 *
 */
public class NegotiateSecurityFilterCustomized extends NegotiateSecurityFilter {
  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(NegotiateSecurityFilterCustomized.class);

  private Usermanagement usermanagement = new UsermanagementDummyImpl();

  private AccessControlProvider accessControlProvider;

  /**
   * The constructor.
   *
   * @param accessControlProvider is the provider that help us to get the permissions
   */
  public NegotiateSecurityFilterCustomized(AccessControlProvider accessControlProvider) {
    super();
    this.accessControlProvider = accessControlProvider;
  }

  /**
   * The constructor.
   */
  public NegotiateSecurityFilterCustomized() {
    super();
  }

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {

    // Here you can customize your own filer functionality
    super.doFilter(req, res, chain);
  }

  @Override
  protected boolean setAuthentication(final HttpServletRequest request, final HttpServletResponse response,
      final Authentication authentication) {

    try {
      String principal[] = authentication.getPrincipal().toString().split("\\\\", 2);

      String username = principal[1];

      UserProfile profile = this.usermanagement.findUserProfileByLogin(username);

      // UsernamePasswordAuthenticationToken auth =
      // new UsernamePasswordAuthenticationToken(profile, getAutoritiesByProfile(profile));

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(username, null, getAutoritiesByProfile(profile));

      LOGGER.info("Auth --> " + auth.getName());

      SecurityContextHolder.getContext().setAuthentication(auth);

    } catch (Exception e) {
      NegotiateSecurityFilterCustomized.LOGGER.warn("error authenticating user");
      NegotiateSecurityFilterCustomized.LOGGER.trace("", e);
    }

    return true;
  }

  private Set<GrantedAuthority> getAutoritiesByProfile(UserProfile profile) {

    Set<GrantedAuthority> authorities = new HashSet<>();
    Collection<String> accessControlIds = new ArrayList<>();
    accessControlIds.add(profile.getRole().getName());
    Set<AccessControl> accessControlSet = new HashSet<>();
    for (String id : accessControlIds) {
      boolean success = this.accessControlProvider.collectAccessControls(id, accessControlSet);
      if (!success) {
        // authorities.add(new SimpleGrantedAuthority(id));
      }
    }
    for (AccessControl accessControl : accessControlSet) {
      authorities.add(new AccessControlGrantedAuthority(accessControl));
    }
    return authorities;
  }
}
