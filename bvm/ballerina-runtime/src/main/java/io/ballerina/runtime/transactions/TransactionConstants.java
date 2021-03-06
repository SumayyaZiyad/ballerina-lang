/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package io.ballerina.runtime.transactions;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.BLangConstants;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.ORG_NAME_SEPARATOR;
import static io.ballerina.runtime.util.BLangConstants.VERSION_SEPARATOR;

/**
 * {@code TransactionConstants} Define transaction related constants.
 *
 * @since 1.0
 */
public class TransactionConstants {
    //Co-ordinator functions
    public static final String COORDINATOR_ABORT_TRANSACTION = "abortTransaction";

    public static final String TRANSACTION_PACKAGE_NAME = "lang.transaction";
    public static final String TRANSACTION_PACKAGE_FQN = BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX + "." +
            TRANSACTION_PACKAGE_NAME;
    public static final String TRANSACTION_PACKAGE_VERSION = "0.0.1";
    public static final String TRANSACTION_PACKAGE_PATH =
            BALLERINA_BUILTIN_PKG_PREFIX + ORG_NAME_SEPARATOR + TRANSACTION_PACKAGE_NAME + VERSION_SEPARATOR +
                    TRANSACTION_PACKAGE_VERSION;

    public static final Module TRANSACTION_PACKAGE_ID = new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                   TRANSACTION_PACKAGE_NAME,
                                                                   TRANSACTION_PACKAGE_VERSION);
    public static final String TRANSACTION_BLOCK_CLASS_NAME = "transaction_block";
    public static final String COORDINATOR_PACKAGE = TRANSACTION_PACKAGE_PATH;

    public static final int DEFAULT_RETRY_COUNT = 3;

    public static final String DEFAULT_COORDINATION_TYPE = "2pc";

    public static final String DEFAULT_CONTEXT_VERSION  = "1.0";

    // TransactionContext struct field names
    public static final String CONTEXT_VERSION = "contextVersion";
    public static final BString TRANSACTION_BLOCK_ID = StringUtils.fromString("transactionBlockId");
    public static final BString TRANSACTION_ID = StringUtils.fromString("transactionId");
    public static final BString CORDINATION_TYPE = StringUtils.fromString("coordinationType");
    public static final BString REGISTER_AT_URL = StringUtils.fromString("registerAtURL");

    public static final String ANN_NAME_TRX_PARTICIPANT_CONFIG = "Participant";
}
