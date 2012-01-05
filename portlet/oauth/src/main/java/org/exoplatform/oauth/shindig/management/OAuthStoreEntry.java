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
package org.exoplatform.oauth.shindig.management;

import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerIndex;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.juzu.SessionScoped;

import java.io.Serializable;

import javax.inject.Named;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
@Named("OAuthStoreEntry")
@SessionScoped
public class OAuthStoreEntry implements Serializable
{
   private static final long serialVersionUID = 1L;
   private BasicOAuthStoreConsumerIndex key;
   private BasicOAuthStoreConsumerKeyAndSecret value;
   
   public void setKey(BasicOAuthStoreConsumerIndex key)
   {
      this.key = key;
   }
   public BasicOAuthStoreConsumerIndex getKey()
   {
      return key;
   }
   public void setValue(BasicOAuthStoreConsumerKeyAndSecret value)
   {
      this.value = value;
   }
   public BasicOAuthStoreConsumerKeyAndSecret getValue()
   {
      return value;
   }
}
