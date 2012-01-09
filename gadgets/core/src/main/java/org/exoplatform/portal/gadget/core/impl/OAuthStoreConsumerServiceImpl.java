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

import org.apache.shindig.gadgets.GadgetException;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.gadget.core.GateInContainerConfigLoader;
import org.exoplatform.portal.gadget.core.GateInGuiceServletContextListener;
import org.exoplatform.portal.gadget.core.OAuthStoreConsumer;
import org.exoplatform.portal.gadget.core.OAuthStoreConsumerService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class OAuthStoreConsumerServiceImpl implements OAuthStoreConsumerService
{
   private static final String OAUTH_CONFIG = "oauth.config";
   private static final String CONSUMER_SECRET_KEY = "consumer_secret";
   private static final String CONSUMER_KEY_KEY = "consumer_key";
   private static final String KEY_TYPE_KEY = "key_type";
   private static final String CALLBACK_URL = "callback_url";
   private String defaultKeyName;
   
   private ChromatticLifeCycle chromatticLifeCycle;

   public OAuthStoreConsumerServiceImpl(InitParams params, ChromatticManager chromatticManager)
   {
      chromatticLifeCycle = chromatticManager.getLifeCycle("shindig:oauthstore");

      ValueParam oauthConfig = params.getValueParam(OAUTH_CONFIG);
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
         GateInContainerConfigLoader currentLoader = GateInGuiceServletContextListener.getCurrentLoader();
         String oauthConfigString = currentLoader.loadContentAsString(file, "UTF-8");
         this.initFromConfigString(oauthConfigString);
      }
      catch (IOException e)
      {
         //Should log this
         e.printStackTrace();
      }
      catch (GadgetException e)
      {
         //Should log this
         e.printStackTrace();
      }
   }

   private void initFromConfigString(String oauthConfigStr) throws GadgetException
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
         throw new GadgetException(GadgetException.Code.OAUTH_STORAGE_ERROR, e);
      }
      catch (URISyntaxException e)
      {
         throw new GadgetException(GadgetException.Code.OAUTH_STORAGE_ERROR, e);
      }
   }

   private void storeConsumerInfos(URI gadgetUri, JSONObject oauthConfig) throws JSONException, GadgetException
   {
      for (String keyName : JSONObject.getNames(oauthConfig))
      {
         JSONObject consumerInfo = oauthConfig.getJSONObject(keyName);
         storeConsumerInfo(gadgetUri, keyName, consumerInfo);
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
      
      //store consumer
      storeConsumer(new OAuthStoreConsumer(keyName, consumerKey, consumerSecret, keyType, callbackUrl));
      
      try
      {
         //store mapping of consumer and gadget uri
         addMappingKeyAndGadget(keyName, gadgetUri.toASCIIString());
      }
      catch (Exception e)
      {
         //should log this
         e.printStackTrace();
      }
   }
   
   private final OAuthStoreContainer getOAuthStoreContainer()
   {
      ChromatticSession session = chromatticLifeCycle.getContext().getSession();
      OAuthStoreContainer container = session.findByPath(OAuthStoreContainer.class, "shindig:oauthstore");
      if (container == null)
      {
         container = session.insert(OAuthStoreContainer.class, "shindig:oauthstore");
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
   
   public void storeDefaultConsumer(OAuthStoreConsumer consumer)
   {
      storeConsumer(consumer);
      defaultKeyName = consumer.getKeyName();
      getOAuthStoreContainer().setDefaultKeyName(defaultKeyName);
   }

   public OAuthStoreConsumer getDefaultConsumer()
   {
      if (defaultKeyName == null)
      {
         defaultKeyName = getOAuthStoreContainer().getDefaultKeyName();
      }
      
      return getConsumer(defaultKeyName);
   }

   public OAuthStoreConsumer getConsumer(String name)
   {
      OAuthStoreConsumerEntry consumer = this.getOAuthStoreContainer().getAllOAuthStoreConsumerEntries().get(name);
      if (consumer != null)
      {
         return consumer.toOAuthStoreConsumer();
      }
      return null;
   }

   public void storeConsumer(OAuthStoreConsumer consumer)
   {
      OAuthStoreContainer storeContainer = this.getOAuthStoreContainer();
      OAuthStoreConsumerEntry o = storeContainer.createOAuthStoreEntry(); 
      storeContainer.getAllOAuthStoreConsumerEntries().put(consumer.getKeyName(), o);
      
      o.setKeyName(consumer.getKeyName());
      o.setConsumerKey(consumer.getConsumerKey());
      o.setConsumerSecret(consumer.getConsumerSecret());
      o.setKeyType(consumer.getKeyType());
      o.setCallbackUrl(consumer.getCallbackUrl());
   }

   public void removeConsumer(String name)
   {
      this.getOAuthStoreContainer().getAllOAuthStoreConsumerEntries().remove(name);
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

   public Map<String, OAuthStoreConsumer> getAllMappingKeyAndGadget()
   {
      Map<String, OAuthStoreConsumer> consumers = new HashMap<String, OAuthStoreConsumer>();
      Map<String, OAuthStoreMappingEntry> mappings = this.getOAuthStoreContainer().getAllOAuthStoreMappingEntries();
      for (String uri : mappings.keySet())
      {
         String keyName = mappings.get(uri).getKeyName();
         OAuthStoreConsumerEntry consumer =
            this.getOAuthStoreContainer().getAllOAuthStoreConsumerEntries().get(keyName);
         if (consumer != null)
         {
            try
            {
               consumers.put(URLDecoder.decode(uri, "UTF-8"), consumer.toOAuthStoreConsumer());
            }
            catch (Exception e)
            {
               //should log this
               e.printStackTrace();
            }
         }
      }
      
      return consumers;
   }

   public void addMappingKeyAndGadget(String keyName, String gadgetUri) throws Exception
   {
      OAuthStoreContainer storeContainer = this.getOAuthStoreContainer();
      if (getConsumer(keyName) == null)
      {
         throw new Exception("Consumer " + keyName + " is not exist");
      }
   
      OAuthStoreMappingEntry gadget = storeContainer.createOAuthStoreMappingEntry();
      storeContainer.getAllOAuthStoreMappingEntries().put(URLEncoder.encode(gadgetUri, "UTF-8"), gadget);
      gadget.setGadgetUri(URLEncoder.encode(gadgetUri, "UTF-8"));
      gadget.setKeyName(keyName);      
   }

   public OAuthStoreConsumer findMappingKeyAndGadget(String keyName, String gadgetUri)
   {
      Map<String, OAuthStoreConsumer> mappings = getAllMappingKeyAndGadget();
      for (String uri : mappings.keySet())
      {
         if (uri.equals(gadgetUri) && mappings.get(uri).getKeyName().equals(keyName))
         {
            return mappings.get(uri);
         }
      }
      return null;
   }

   public void removeMappingKeyAndGadget(String keyName, String gadgetUri)
   {
      Map<String, OAuthStoreConsumer> mappings = getAllMappingKeyAndGadget();
      for (String uri : mappings.keySet())
      {
         if (uri.equals(gadgetUri) && mappings.get(uri).getKeyName().equals(keyName))
         {
            mappings.remove(uri);
            return;
         }
      }
   }
}
