/**
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.portal.gadget.core.impl;

import org.apache.shindig.common.util.ResourceLoader;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.gadget.core.OAuthStoreConsumer;
import org.exoplatform.portal.gadget.core.OAuthStoreConsumerService;
import org.exoplatform.portal.gadget.core.OAuthStoreError;
import org.exoplatform.portal.gadget.core.OAuthStoreException;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class OAuthStoreConsumerServiceImpl implements OAuthStoreConsumerService
{
   private final Logger log = LoggerFactory.getLogger(OAuthStoreConsumerService.class);
   
   private static final String OAUTH_CONFIG = "oauth.config";
   private static final String CONSUMER_SECRET_KEY = "consumer_secret";
   private static final String CONSUMER_KEY_KEY = "consumer_key";
   private static final String KEY_TYPE_KEY = "key_type";
   private static final String CALLBACK_URL = "callback_url";
   
   private ChromatticLifeCycle chromatticLifeCycle;

   public OAuthStoreConsumerServiceImpl(InitParams params, ChromatticManager chromatticManager)
   {
      chromatticLifeCycle = chromatticManager.getLifeCycle("oauthstore");

      ValueParam oauthConfig = params != null ? params.getValueParam(OAUTH_CONFIG) : null;
      if (oauthConfig != null)
      {
         String oauthFile = oauthConfig.getValue();
         this.loadOAuthStoreConsumer(oauthFile);
      }
   }

   private void loadOAuthStoreConsumer(String file)
   {
      try
      {
         String oauthConfigString = ResourceLoader.getContent(file);
         this.initFromConfigString(oauthConfigString);
      }
      catch (IOException e)
      {
         log.error("Can not load oauth file: " + file + e.getMessage());
      }
      catch (OAuthStoreException e)
      {
         log.error("Error parsing data" + e.getMessage());
      }
   }

   private void initFromConfigString(String oauthConfigStr) throws OAuthStoreException
   {
      try
      {
         JSONObject oauthConfigs = new JSONObject(oauthConfigStr);
         for (Iterator<?> i = oauthConfigs.keys(); i.hasNext();)
         {
            String url = (String)i.next();
            URI gadgetUri = new URI(url);
            JSONObject oauthConfig = oauthConfigs.getJSONObject(url);
            storeConsumerInfos(gadgetUri, oauthConfig);
         }
      }
      catch (JSONException e)
      {
         throw new OAuthStoreException(OAuthStoreError.JSON_SYNTAX_ERROR, e);
      }
      catch (URISyntaxException e)
      {
         throw new OAuthStoreException(OAuthStoreError.URI_SYNTAX_ERROR, e);
      }
   }

   private void storeConsumerInfos(URI gadgetUri, JSONObject oauthConfig) throws JSONException
   {
      for (String keyName : JSONObject.getNames(oauthConfig))
      {
         if (getConsumer(keyName) == null)
         {
            JSONObject consumerInfo = oauthConfig.getJSONObject(keyName);
            storeConsumerInfo(gadgetUri, keyName, consumerInfo);
         }
      }
   }

   private void storeConsumerInfo(URI gadgetUri, String keyName, JSONObject consumerInfo) throws JSONException
   {
      String callbackUrl = consumerInfo.optString(CALLBACK_URL, null);
      String consumerSecret = consumerInfo.getString(CONSUMER_SECRET_KEY);
      String consumerKey = consumerInfo.getString(CONSUMER_KEY_KEY);
      String keyType = consumerInfo.getString(KEY_TYPE_KEY);

      if (keyType.equals("RSA_PRIVATE"))
      {
         consumerSecret = convertFromOpenSsl(consumerSecret);
      }
      
      try
      {
         //store consumer
         storeConsumer(new OAuthStoreConsumer(keyName, consumerKey, consumerSecret, keyType, callbackUrl));
                  
         log.info("Stored consumer with key name " + keyName);
         
         //store mapping of consumer and gadget uri
         addMappingKeyAndGadget(keyName, gadgetUri.toASCIIString());
      }
      catch (OAuthStoreException e)
      {
         log.error("OAuth store error " + e.getMessage());
      }
      catch (Exception e)
      {
         log.error("Has error " + e.getMessage());
      }
   }
   
   private final OAuthStoreContainer getOAuthStoreContainer()
   {
      ChromatticSession session = chromatticLifeCycle.getContext().getSession();
      OAuthStoreContainer container = session.findByPath(OAuthStoreContainer.class, "oauthstore");
      if (container == null)
      {
         container = session.insert(OAuthStoreContainer.class, "oauthstore");
      }
      return container;
   }
   
   /**
    * Support standard openssl keys by stripping out the headers and blank lines
    * @param privateKey
    * @return raw privateKey
    */
   public static String convertFromOpenSsl(String privateKey)
   {
      return privateKey.replaceAll("-----[A-Z ]*-----", "").replace("\n", "");
   }

   public OAuthStoreConsumer getConsumer(String keyName)
   {
      OAuthStoreConsumerEntry consumer = this.getOAuthStoreContainer().getAllOAuthStoreConsumerEntries().get(keyName);
      if (consumer != null)
      {
         return consumer.toOAuthStoreConsumer();
      }
      return null;
   }

   public void storeConsumer(OAuthStoreConsumer consumer) throws OAuthStoreException
   {
      if (getConsumer(consumer.getKeyName()) != null)
      {
         throw new OAuthStoreException(OAuthStoreError.DUPLICATION_DATA);
      }
      
      OAuthStoreContainer storeContainer = this.getOAuthStoreContainer();
      OAuthStoreConsumerEntry o = storeContainer.createOAuthStoreEntry(); 
      storeContainer.getAllOAuthStoreConsumerEntries().put(consumer.getKeyName(), o);
      
      o.setKeyName(consumer.getKeyName());
      o.setConsumerKey(consumer.getConsumerKey());
      o.setConsumerSecret(consumer.getConsumerSecret());
      o.setKeyType(consumer.getKeyType());
      o.setCallbackUrl(consumer.getCallbackUrl());
   }

   public void removeConsumer(String keyName)
   {
      this.getOAuthStoreContainer().getAllOAuthStoreConsumerEntries().remove(keyName);
   }

   public List<OAuthStoreConsumer> getAllConsumers()
   {
      List<OAuthStoreConsumer> consumers = new ArrayList<OAuthStoreConsumer>();
      Map<String, OAuthStoreConsumerEntry> entries =  getOAuthStoreContainer().getAllOAuthStoreConsumerEntries();
      for (OAuthStoreConsumerEntry entry : entries.values())
      {
         consumers.add(entry.toOAuthStoreConsumer());
      }
      return consumers;
   }

   public void addMappingKeyAndGadget(String keyName, String gadgetUri) throws OAuthStoreException
   {
      OAuthStoreContainer storeContainer = this.getOAuthStoreContainer();
      if (getConsumer(keyName) != null)
      {
         try
         {
            URI uri = new URI(gadgetUri);
            OAuthStoreConsumerEntry consumer = storeContainer.getAllOAuthStoreConsumerEntries().get(keyName);
            consumer.addGadgetUri(uri.toASCIIString());
         }
         catch (URISyntaxException e)
         {
            throw new OAuthStoreException(OAuthStoreError.URI_SYNTAX_ERROR);
         }
      }
      else
      {
         throw new OAuthStoreException(OAuthStoreError.NON_EXIST_RELATIONSHIP);
      }
   }

   public OAuthStoreConsumer findMappingKeyAndGadget(String keyName, String gadgetUri)
   {
      OAuthStoreConsumer consumer = getConsumer(keyName);
      if (consumer != null)
      {
         List<String> gadgetUris = consumer.getGadgetUris();
         for (String uri : gadgetUris)
         {
            if (uri.equals(gadgetUri))
            {
               return consumer;
            }
         }
      }
      return null;
   }

   public void removeMappingKeyAndGadget(String keyName, String gadgetUri)
   {
      OAuthStoreConsumerEntry consumer = this.getOAuthStoreContainer().getAllOAuthStoreConsumerEntries().get(keyName);
      if (consumer != null)
      {
         consumer.removeGadgetUri(gadgetUri);
      }
   }
}
