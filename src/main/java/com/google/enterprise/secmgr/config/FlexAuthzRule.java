// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.secmgr.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.enterprise.secmgr.json.ProxyTypeAdapter;
import com.google.enterprise.secmgr.json.TypeProxy;
import com.google.gson.GsonBuilder;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the Flexible Authorization Rule.
 *
 */
@Immutable
@ParametersAreNonnullByDefault
public class FlexAuthzRule {

  /**
   * Mechanism-specific parameter names.
   */
  public static enum ParamName {
    /**
     * Indicates a connector-instance name.  This is a required string-valued
     * parameter.
     */
    CONNECTOR_NAME,
    /**
     * Indicates the entity ID of a SAML PDP.  This is a required string-valued
     * parameter.
     */
    SAML_ENTITY_ID,
    /**
     * Indicates whether to use the "multi-request" authz protocol.  This is a
     * required boolean-valued parameter.
     */
    SAML_USE_BATCHED_REQUESTS,
  }

  /**
   * The empty authentication ID, for use with mechanisms like
   * {@link AuthzMechanism#CACHE} and {@link AuthzMechanism#POLICY}.
   */
  public static final String EMPTY_AUTHN_ID = "";

  /**
   * The legacy authentication ID, for configuration migration.
   */
  public static final String LEGACY_AUTHN_ID = "Default";

  /**
   * The empty connector name, for use with {@link #LEGACY_CONNECTOR_URL_PATTERN}.
   */
  public static final String EMPTY_CONNECTOR_NAME = "";

  /**
   * The legacy connector URL pattern {@code "^googleconnector://"}.
   */
  static final String LEGACY_CONNECTOR_URL_PATTERN = "^googleconnector://";

  static final String HTTP_URL_PATTERN = "regexpIgnoreCase:^(http|https)://";

  private static final Pattern LEGACY_CONNECTOR_URL_PATTERN_PATTERN
      = Pattern.compile("^\\^googleconnector://(([a-z_][a-z0-9_-]*)\\.localhost/)?$");

  /**
   * The SAML entity ID to use for legacy SAML clients.
   */
  public static final String LEGACY_SAML_ENTITY_ID = "http://example.com/legacy-saml-id";

  /**
   * Timeout value that indicates this rule has no specific time limit.
   */
  public static final int NO_TIME_LIMIT = -1;

  // We list auth mechanisms that are currently supported. Some of the mechanisms
  // that were previously supported are not supported anymore.
  private static final ImmutableList<AuthzMechanism> SUPPORTED_AUTHZ_MECHANISMS =
      ImmutableList.copyOf(EnumSet.complementOf(EnumSet.of(AuthzMechanism.FILE_SYSTEM)));

  @Nonnull private final String authnId;
  @Nonnull private final AuthzMechanism authzMechType;
  @Nonnull private final ImmutableMap<ParamName, String> mechSpecificParams;
  @Nonnull private final String displayName;
  private final int timeout;

  static final String ROOT_URL_PATTERN = "/";

  /**
   * Constructor for flex authz rules.
   *
   * @param authnId The name of a credential group to use for authorization, or
   *     an empty string to use the "primary verified identity".
   * @param authzMechType The authorization mechanism type, for example policy
   *     ACLs, head requests, connector authorization, SAML, etc.
   * @param mechSpecificParams A map of parameters that are specific to the
   *     mechanism type.  A null value is equivalent to an empty map.
   * @param displayName The display name for this rule's row, which can be used
   *     as a reference in the routing table.
   * @param timeout  The timeout for this rule, in milliseconds.
   */
  public FlexAuthzRule(String authnId, AuthzMechanism authzMechType,
      @Nullable Map<ParamName, String> mechSpecificParams, String displayName, int timeout) {
    Preconditions.checkNotNull(authnId);
    Preconditions.checkNotNull(authzMechType);
    Preconditions.checkNotNull(displayName);
    this.authnId = authnId;
    this.authzMechType = authzMechType;
    if (mechSpecificParams != null) {
      this.mechSpecificParams = ImmutableMap.copyOf(mechSpecificParams);
    } else {
      this.mechSpecificParams = ImmutableMap.of();
    }
    this.displayName = displayName;
    this.timeout = timeout;
  }

  /**
   * A constructor for flex authz rules with no mechanism-specific parameters.
   */
  public FlexAuthzRule(String authnId, AuthzMechanism mechType, String displayName, int timeout) {
    this(authnId, mechType, null, displayName, timeout);
  }

  /**
   * @return The authentication identity name for this rule: a credential-group
   * name, or the empty string to use the "primary verified identity".
   */
  @Nonnull
  public String getAuthnId() {
    return authnId;
  }

  /**
   * @return The authorization mechanism type for this rule.
   */
  @Nonnull
  public AuthzMechanism getAuthzMechType() {
    return authzMechType;
  }

  /**
   * @return A collection of the currently supported mechanism types.
   */
  @Nonnull
  public static Collection<AuthzMechanism> getAuthzMechTypes() {
    return SUPPORTED_AUTHZ_MECHANISMS;
  }

  /**
   * @return An immutable copy of the mechanism-specific parameters.
   */
  @Nonnull
  public Map<ParamName, String> getMechSpecificParams() {
    return mechSpecificParams;
  }

  @Nonnull
  public String requiredStringParam(ParamName name) {
    String value = mechSpecificParams.get(name);
    Preconditions.checkArgument(value != null);
    return value;
  }

  @Nonnull
  public boolean requiredBooleanParam(ParamName name) {
    return Boolean.parseBoolean(requiredStringParam(name));
  }

  /**
   * @return The display name for this rule's row.
   */
  @Nonnull
  public String getRowDisplayName() {
    return displayName;
  }

  /**
   * @return The timeout for this rule, in milliseconds.
   */
  public int getTimeout() {
    return timeout;
  }

  public boolean hasTimeout() {
    return NO_TIME_LIMIT != timeout;
  }

  /**
   * Extracts a connector name from a given URL pattern.  Used for getting the
   * connector name out of legacy googleconnector: patterns.
   *
   * @param urlPattern A URL pattern to examine.
   * @return The corresponding connector name, or {@code null} if there's none.
   */
  @Nullable
  public static String extractConnectorNameFromPattern(String urlPattern) {
    Matcher matcher = LEGACY_CONNECTOR_URL_PATTERN_PATTERN.matcher(urlPattern);
    if (!matcher.matches()) {
      return null;
    }
    String name = matcher.group(2);
    return Strings.isNullOrEmpty(name) ? EMPTY_CONNECTOR_NAME : name;
  }

  @Override
  public String toString() {
    return ConfigSingleton.getGson().toJson(this);
  }

  @Override
  public synchronized boolean equals(Object object) {
    if (object == this) { return true; }
    if (!(object instanceof FlexAuthzRule)) { return false; }
    FlexAuthzRule other = (FlexAuthzRule) object;
    return Objects.equals(getAuthnId(), other.getAuthnId())
        && Objects.equals(getAuthzMechType(), other.getAuthzMechType())
        && Objects.equals(getMechSpecificParams(), other.getMechSpecificParams())
        && Objects.equals(getRowDisplayName(), other.getRowDisplayName())
        && Objects.equals(getTimeout(), other.getTimeout());
  }

  @Override
  public synchronized int hashCode() {
    return Objects.hash(getAuthnId(), getAuthzMechType(), getMechSpecificParams(),
                            getRowDisplayName(), getTimeout());
  }

  static void registerTypeAdapters(GsonBuilder builder) {
    builder.registerTypeAdapter(FlexAuthzRule.class,
        ProxyTypeAdapter.make(FlexAuthzRule.class, LocalProxy.class));
  }

  private static final class LocalProxy implements TypeProxy<FlexAuthzRule> {
    String authnId;
    AuthzMechanism authzMechType;
    String connectorName;
    String samlEntityId;
    String samlUseBatchedRequests;
    String displayName;
    int timeout;

    @SuppressWarnings("unused")
    LocalProxy() {
    }

    @SuppressWarnings("unused")
    LocalProxy(FlexAuthzRule rule) {
      authnId = rule.getAuthnId();
      authzMechType = rule.getAuthzMechType();
      connectorName = rule.getMechSpecificParams().get(ParamName.CONNECTOR_NAME);
      samlEntityId = rule.getMechSpecificParams().get(ParamName.SAML_ENTITY_ID);
      samlUseBatchedRequests
          = rule.getMechSpecificParams().get(ParamName.SAML_USE_BATCHED_REQUESTS);
      displayName = rule.getRowDisplayName();
      timeout = rule.getTimeout();
    }

    @Override
    public FlexAuthzRule build() {
      Map<ParamName, String> mechSpecificParams = Maps.newHashMap();
      if (connectorName != null) {
        mechSpecificParams.put(ParamName.CONNECTOR_NAME, connectorName);
      }
      if (samlEntityId != null) {
        mechSpecificParams.put(ParamName.SAML_ENTITY_ID, samlEntityId);
      }
      if (samlUseBatchedRequests != null) {
        mechSpecificParams.put(ParamName.SAML_USE_BATCHED_REQUESTS, samlUseBatchedRequests);
      }
      if (mechSpecificParams.isEmpty()) {
        mechSpecificParams = null;
      }
      return new FlexAuthzRule(authnId, authzMechType, mechSpecificParams, displayName, timeout);
    }
  }
}
