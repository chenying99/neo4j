/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.discovery;

import java.util.Set;

import org.neo4j.coreedge.raft.RaftInstance;
import org.neo4j.coreedge.raft.RaftInstance.BootstrapException;
import org.neo4j.coreedge.raft.membership.MemberIdSet;
import org.neo4j.coreedge.server.MemberId;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

public class RaftDiscoveryServiceConnector extends LifecycleAdapter implements CoreTopologyService.Listener
{
    private final CoreTopologyService discoveryService;
    private final RaftInstance raftInstance;

    public RaftDiscoveryServiceConnector( CoreTopologyService discoveryService,
                                            RaftInstance raftInstance )
    {
        this.discoveryService = discoveryService;
        this.raftInstance = raftInstance;
    }

    @Override
    public void start() throws BootstrapException
    {
        discoveryService.addMembershipListener( this );

        ClusterTopology clusterTopology = discoveryService.currentTopology();
        Set<MemberId> initialMembers = clusterTopology.coreMembers();

        if ( clusterTopology.canBeBootstrapped() )
        {
            raftInstance.bootstrapWithInitialMembers( new MemberIdSet( initialMembers ) );
        }

        onTopologyChange();
    }

    @Override
    public void onTopologyChange()
    {
        raftInstance.setTargetMembershipSet( discoveryService.currentTopology().coreMembers() );
    }
}
