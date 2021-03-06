/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.as.clustering.infinispan;

import static org.jboss.as.clustering.infinispan.InfinispanMessages.MESSAGES;

import java.util.Properties;

import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jboss.as.clustering.msc.ServiceContainerHelper;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartException;
import org.jgroups.Channel;

/**
 * @author Paul Ferraro
 */
public class ChannelProvider implements JGroupsChannelLookup {

    private static final String CHANNEL = "channel";

    public static void init(TransportConfigurationBuilder builder, ServiceName channel) {
        Properties properties = new Properties();
        properties.setProperty(JGroupsTransport.CHANNEL_LOOKUP, ChannelProvider.class.getName());
        properties.setProperty(CHANNEL, channel.getCanonicalName());
        builder.transport().defaultTransport().withProperties(properties);
    }

    /**
     * {@inheritDoc}
     * @see org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup#getJGroupsChannel(java.util.Properties)
     */
    @Override
    public Channel getJGroupsChannel(Properties properties) {
        String channel = properties.getProperty(CHANNEL);
        if (channel == null) {
            throw MESSAGES.invalidTransportProperty(CHANNEL, properties);
        }
        ServiceName name = ServiceName.parse(channel);
        ServiceRegistry registry = ServiceContainerHelper.getCurrentServiceContainer();
        ServiceController<Channel> service = ServiceContainerHelper.getService(registry, name);
        try {
            return ServiceContainerHelper.getValue(service);
        } catch (StartException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup#shouldStartAndConnect()
     */
    @Override
    public boolean shouldStartAndConnect() {
        return true;
    }

    /**
     * {@inheritDoc}
     * @see org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup#shouldStopAndDisconnect()
     */
    @Override
    public boolean shouldStopAndDisconnect() {
        return true;
    }
}