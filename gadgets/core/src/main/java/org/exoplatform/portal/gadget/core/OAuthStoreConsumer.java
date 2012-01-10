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
package org.exoplatform.portal.gadget.core;

import java.util.List;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class OAuthStoreConsumer
{
   private String keyName;//represent as identifier of consumer

   private String consumerKey;

   private String consumerSecret;

   private String keyType;//Only HMAC_SYMMETRIC and RSA_PRIVATE

   private String callbackUrl;//Can be null
   
   private List<String> gadgetUris;

   public OAuthStoreConsumer(String keyName, String consumerKey, String consumerSecret, String keyType, String callbackUrl)
   {
      this.keyName = keyName;
      this.consumerKey = consumerKey;
      this.consumerSecret = consumerSecret;
      this.keyType = keyType;
      this.callbackUrl = callbackUrl;
   }
   
   public OAuthStoreConsumer(String keyName, String consumerKey, String consumerSecret, String keyType, String callbackUrl, List<String> gadgetUris)
   {
      this.keyName = keyName;
      this.consumerKey = consumerKey;
      this.consumerSecret = consumerSecret;
      this.keyType = keyType;
      this.callbackUrl = callbackUrl;
      this.gadgetUris = gadgetUris;
   }
   
   public void setKeyName(String keyName)
   {
      this.keyName = keyName;
   }

   public String getKeyName()
   {
      return keyName;
   }

   public void setConsumerKey(String consumerKey)
   {
      this.consumerKey = consumerKey;
   }

   public String getConsumerKey()
   {
      return consumerKey;
   }

   public void setConsumerSecret(String consumerSecret)
   {
      this.consumerSecret = consumerSecret;
   }

   public String getConsumerSecret()
   {
      return consumerSecret;
   }

   public void setKeyType(String keyType)
   {
      this.keyType = keyType;
   }

   public String getKeyType()
   {
      return keyType;
   }

   public void setCallbackUrl(String callbackUrl)
   {
      this.callbackUrl = callbackUrl;
   }

   public String getCallbackUrl()
   {
      return callbackUrl;
   }

   public void setGadgetUris(List<String> gadgetUris)
   {
      this.gadgetUris = gadgetUris;
   }

   public List<String> getGadgetUris()
   {
      return gadgetUris;
   }
}
