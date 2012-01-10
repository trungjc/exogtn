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

import org.exoplatform.portal.gadget.core.OAuthStoreConsumer;
import org.juzu.SessionScoped;

import java.io.Serializable;

import javax.inject.Named;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
@Named("Session")
@SessionScoped
public class Session implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String gadgetUri;
   private OAuthStoreConsumer consumer;
   
   public void setGadgetUri(String gadgetUri)
   {
      this.gadgetUri = gadgetUri;
   }
   
   public String getGadgetUri()
   {
      return gadgetUri;
   }
   
   public void setConsumer(OAuthStoreConsumer consumer)
   {
      this.consumer = consumer;
   }
   
   public OAuthStoreConsumer getConsumer()
   {
      return consumer;
   }
}
