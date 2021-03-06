/**
 * Copyright 2021 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.ibm.cloud.appconfiguration.sdk.test.configurations.internal;

import com.ibm.cloud.appconfiguration.sdk.configurations.internal.Connectivity;
import com.ibm.cloud.appconfiguration.sdk.configurations.internal.ConnectivityListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectivityTest {

    @Test
    public void testConnection() throws InterruptedException {

        final Boolean[] isCalled = {false};
        Connectivity connectivity = Connectivity.getInstance();
        connectivity.addConnectivityListener(new ConnectivityListener() {
            @Override
            public void onConnectionChange(Boolean isConnected) {
                isCalled[0] = true;
            }
        });
        connectivity.checkConnection();
        TimeUnit.SECONDS.sleep(2);
        assertTrue(isCalled[0]);
    }
}
