package org.exoplatform.portal.gadget.core;

import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.signature.RSA_SHA1;

import net.oauth.OAuthServiceProvider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.oauth.BasicOAuthStore;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreTokenIndex;
import org.apache.shindig.gadgets.oauth.OAuthStore;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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

public class ExoOAuthStore implements OAuthStore
{
   private static final Log log = ExoLogger.getLogger(OAuthStore.class);

   /**
    * Key to use when no other key is found.
    */
   private OAuthStoreConsumer defaultConsumer;
   
   /**
    * Callback to use when no per-key callback URL is found.
    */
   private String defaultCallbackUrl;
   
   /** Number of times we looked up an access token */
   private int accessTokenLookupCount = 0;

   /** Number of times we added an access token */
   private int accessTokenAddCount = 0;

   /** Number of times we removed an access token */
   private int accessTokenRemoveCount = 0;

   public ExoOAuthStore(String signingKeyFile, String signingKeyName, String defaultCalbackUrl)
   {
      this.defaultCallbackUrl = defaultCalbackUrl;
      loadDefaultKey(signingKeyFile, signingKeyName);
   }

   public void setDefaultCallbackUrl(String defaultCallbackUrl)
   {
      this.defaultCallbackUrl = defaultCallbackUrl;
   }

   public ConsumerInfo getConsumerKeyAndSecret(SecurityToken securityToken, String serviceName,
      OAuthServiceProvider provider) throws GadgetException
   {
      OAuthStoreConsumerService service =
         (OAuthStoreConsumerService)PortalContainer.getInstance().getComponentInstanceOfType(
            OAuthStoreConsumerService.class);
      OAuthStoreConsumer consumer = service.findMappingKeyAndGadget(serviceName, securityToken.getAppUrl());
      if (consumer == null)
      {
         consumer = defaultConsumer;
      }
      if (consumer == null)
      {
         throw new GadgetException(GadgetException.Code.INTERNAL_SERVER_ERROR,
            "No key for gadget " + securityToken.getAppUrl() + " and service " + serviceName);
      }
      return toConsumerInfo(consumer, provider);
   }

   public TokenInfo getTokenInfo(SecurityToken securityToken, ConsumerInfo consumerInfo, String serviceName,
      String tokenName)
   {
      ++accessTokenLookupCount;
      BasicOAuthStoreTokenIndex tokenKey = makeBasicOAuthStoreTokenIndex(securityToken, serviceName, tokenName);

      ExoContainer container = PortalContainer.getInstance();
      GadgetTokenInfoService tokenSer =
         (GadgetTokenInfoService)container.getComponentInstanceOfType(GadgetTokenInfoService.class);
      return tokenSer.getToken(tokenKey);
   }

   public void setTokenInfo(SecurityToken securityToken, ConsumerInfo consumerInfo, String serviceName,
      String tokenName, TokenInfo tokenInfo)
   {
      ++accessTokenAddCount;
      BasicOAuthStoreTokenIndex tokenKey = makeBasicOAuthStoreTokenIndex(securityToken, serviceName, tokenName);
      ExoContainer container = PortalContainer.getInstance();
      GadgetTokenInfoService tokenSer =
         (GadgetTokenInfoService)container.getComponentInstanceOfType(GadgetTokenInfoService.class);
      tokenSer.createToken(tokenKey, tokenInfo);
   }

   public void removeToken(SecurityToken securityToken, ConsumerInfo consumerInfo, String serviceName, String tokenName)
   {
      ++accessTokenRemoveCount;
      BasicOAuthStoreTokenIndex tokenKey = makeBasicOAuthStoreTokenIndex(securityToken, serviceName, tokenName);
      ExoContainer container = PortalContainer.getInstance();
      GadgetTokenInfoService tokenSer =
         (GadgetTokenInfoService)container.getComponentInstanceOfType(GadgetTokenInfoService.class);
      tokenSer.deleteToken(tokenKey);
   }

   public int getAccessTokenLookupCount()
   {
      return accessTokenLookupCount;
   }

   public int getAccessTokenAddCount()
   {
      return accessTokenAddCount;
   }

   public int getAccessTokenRemoveCount()
   {
      return accessTokenRemoveCount;
   }
   
   private BasicOAuthStoreTokenIndex makeBasicOAuthStoreTokenIndex(SecurityToken securityToken, String serviceName,
      String tokenName)
   {
      BasicOAuthStoreTokenIndex tokenKey = new BasicOAuthStoreTokenIndex();
      tokenKey.setGadgetUri(securityToken.getAppUrl());

      tokenKey.setServiceName(serviceName);
      tokenKey.setTokenName(tokenName);
      tokenKey.setUserId(securityToken.getViewerId());
      return tokenKey;
   }
   
   private void loadDefaultKey(String defaultKeyFile, String defaultKeyName)
   {
      OAuthStoreConsumer consumer = null;
      if (!StringUtils.isBlank(defaultKeyFile))
      {
         try
         {
            log.info("Loading OAuth signing key from " + defaultKeyFile);
            String privateKey = IOUtils.toString(ResourceLoader.open(defaultKeyFile), "UTF-8");
            privateKey = BasicOAuthStore.convertFromOpenSsl(privateKey);
            consumer = new OAuthStoreConsumer(defaultKeyName, null, privateKey, "RSA_PRIVATE", null);
         }
         catch (Throwable t)
         {
            log.warn("Couldn't load key file " + defaultKeyFile);
         }
      }
      if (consumer != null)
      {
         defaultConsumer = consumer;
      }
      else
      {
         log.warn("Couldn't load OAuth signing key.  To create a key, run:\n"
            + "  openssl req -newkey rsa:1024 -days 365 -nodes -x509 -keyout testkey.pem \\\n"
            + "     -out testkey.pem -subj '/CN=mytestkey'\n"
            + "  openssl pkcs8 -in testkey.pem -out oauthkey.pem -topk8 -nocrypt -outform PEM\n" + '\n'
            + "Then edit gadgets.properties and add these lines:\n gadgets.signingKeyFile=<path-to-oauthkey.pem>\n");
      }
   }
   
   private final ConsumerInfo toConsumerInfo(OAuthStoreConsumer storeConsumer, OAuthServiceProvider provider)
   {
      OAuthConsumer consumer = null;
      if (storeConsumer.getKeyType().equals("RSA_PRIVATE")) {
        consumer = new OAuthConsumer(null, storeConsumer.getConsumerKey(), null, provider);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, storeConsumer.getConsumerSecret());
      } else {
        consumer = new OAuthConsumer(null, storeConsumer.getConsumerKey(), storeConsumer.getConsumerSecret(), provider);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
      }
      String callback = (storeConsumer.getCallbackUrl() != null ? storeConsumer.getCallbackUrl() : defaultCallbackUrl);
      return new ConsumerInfo(consumer, storeConsumer.getKeyName(), callback);
   }
}
