package org.exoplatform.portal.gadget.core;

import com.google.common.collect.Maps;
import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import net.oauth.signature.RSA_SHA1;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerIndex;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreTokenIndex;
import org.apache.shindig.gadgets.oauth.OAuthStore;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret.KeyType;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

/* 
* Created by The eXo Platform SAS
* Author : tung.dang
*          tungcnw@gmail.com
* Dec 10, 2009  
* 
*/

/**
 * Simple implementation of the {@link OAuthStore} interface. We use a
 * in-memory hash map. If initialized with a private key, then the store will
 * return an OAuthAccessor in {@code getOAuthAccessor} that uses that private
 * key if no consumer key and secret could be found.
 */

public class ExoOAuthStore implements OAuthStore {
  private ExoOAuthDataService dataService;
  
  public ExoOAuthStore() {
      dataService =
         (ExoOAuthDataService)PortalContainer.getInstance().getComponentInstanceOfType(ExoOAuthDataService.class);
  }
  
  @Deprecated
  public void initFromConfigString(String oauthConfigStr) throws GadgetException {
    dataService.initFromConfigString(oauthConfigStr);
  }

  @Deprecated
  public static String convertFromOpenSsl(String privateKey) {
    return privateKey.replaceAll("-----[A-Z ]*-----", "").replace("\n", "");
  }

  public void setDefaultKey(BasicOAuthStoreConsumerKeyAndSecret defaultKey) {
    dataService.setDefaultKey(defaultKey);
  }
  
  public void setDefaultCallbackUrl(String defaultCallbackUrl) {
    dataService.setDefaultCallbackUrl(defaultCallbackUrl);
  }

  @Deprecated
  public void setConsumerKeyAndSecret(
      BasicOAuthStoreConsumerIndex providerKey, BasicOAuthStoreConsumerKeyAndSecret keyAndSecret) {
    dataService.setConsumerKeyAndSecret(providerKey, keyAndSecret);
  }

  public ConsumerInfo getConsumerKeyAndSecret(
      SecurityToken securityToken, String serviceName, OAuthServiceProvider provider)
      throws GadgetException {
    return dataService.getConsumerKeyAndSecret(securityToken, serviceName, provider);
  }

  public TokenInfo getTokenInfo(SecurityToken securityToken, ConsumerInfo consumerInfo,
      String serviceName, String tokenName) {
    return dataService.getTokenInfo(securityToken, consumerInfo, serviceName, tokenName);
  }

  public void setTokenInfo(SecurityToken securityToken, ConsumerInfo consumerInfo,
      String serviceName, String tokenName, TokenInfo tokenInfo) {
    dataService.setTokenInfo(securityToken, consumerInfo, serviceName, tokenName, tokenInfo);
  }

  public void removeToken(SecurityToken securityToken, ConsumerInfo consumerInfo,
      String serviceName, String tokenName) {
    dataService.removeToken(securityToken, consumerInfo, serviceName, tokenName);
  }

  @Deprecated
  public int getConsumerKeyLookupCount() {
    return dataService.getConsumerKeyLookupCount();
  }

  @Deprecated
  public int getAccessTokenLookupCount() {
    return dataService.getAccessTokenLookupCount();
  }

  @Deprecated
  public int getAccessTokenAddCount() {
    return dataService.getAccessTokenAddCount();
  }

  @Deprecated
  public int getAccessTokenRemoveCount() {
    return dataService.getAccessTokenRemoveCount();
  }
}
