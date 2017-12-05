/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingManager;

/**
 * This is the test case related with {@link CustomAPIIndexer}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GovernanceUtils.class, IndexingManager.class})
public class CustomAPIIndexerTest {
    private CustomAPIIndexer indexer;
    private AsyncIndexer.File2Index file2Index;
    private UserRegistry userRegistry;

    @Before
    public void init() throws RegistryException {
        PowerMockito.mockStatic(GovernanceUtils.class);
        PowerMockito.mockStatic(IndexingManager.class);
        IndexingManager indexingManager = Mockito.mock(IndexingManager.class);
        PowerMockito.when(IndexingManager.getInstance()).thenReturn(indexingManager);
        userRegistry = Mockito.mock(UserRegistry.class);
        Mockito.doReturn(userRegistry).when(indexingManager).getRegistry(Mockito.anyInt());
        Mockito.doReturn(true).when(userRegistry).resourceExists(Mockito.anyString());
        PowerMockito.when(GovernanceUtils.getGovernanceSystemRegistry(userRegistry)).thenReturn(userRegistry);
        String path = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + "/api";
        file2Index = new AsyncIndexer.File2Index("".getBytes(), null, path, -1234, "");
        indexer = new CustomAPIIndexer();
    }

    /**
     * This method checks the indexer's behaviour for migrated APIs which does not have the relevant properties.
     *
     * @throws RegistryException Registry Exception.
     */
    @Test
    public void testIndexDocumentForMigratedAPI() throws RegistryException {
        Resource resource = new ResourceImpl();
        Mockito.doReturn(resource).when(userRegistry).get(Mockito.anyString());
        indexer.getIndexedDocument(file2Index);
        Assert.assertNotNull(APIConstants.PUBLISHER_ROLES + " property was not set for the API",
                resource.getProperty(APIConstants.PUBLISHER_ROLES));
        Assert.assertNotNull(APIConstants.ACCESS_CONTROL + " property was not set for the API",
                resource.getProperty(APIConstants.ACCESS_CONTROL));
        Assert.assertNotNull(APIConstants.CUSTOM_API_INDEXER_PROPERTY + " property was not set for the API",
                resource.getProperty(APIConstants.CUSTOM_API_INDEXER_PROPERTY));
    }

    /**
     * This method checks the indexer's behaviour for new APIs which does not have the relevant properties.
     *
     * @throws RegistryException Registry Exception.
     */
    @Test
    public void testIndexDocumentForNewAPI() throws RegistryException {
        Resource resource = new ResourceImpl();
        resource.setProperty(APIConstants.ACCESS_CONTROL, APIConstants.NO_ACCESS_CONTROL);
        resource.setProperty(APIConstants.PUBLISHER_ROLES, APIConstants.NULL_USER_ROLE_LIST);
        Mockito.doReturn(resource).when(userRegistry).get(Mockito.anyString());
        indexer.getIndexedDocument(file2Index);
        Assert.assertNull(APIConstants.CUSTOM_API_INDEXER_PROPERTY + " property was set for the API which does not "
                + "require migration", resource.getProperty(APIConstants.CUSTOM_API_INDEXER_PROPERTY));
    }
}