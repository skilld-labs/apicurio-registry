/*
 * Copyright 2020 Red Hat
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

package io.apicurio.registry.storage.impl.ksql.values;

/**
 * Base class for all message value classes.
 * @author eric.wittmann@gmail.com
 */
public abstract class AbstractMessageValue implements MessageValue {

    private ActionType action;

    /**
     * @return the action
     */
    public ActionType getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(ActionType action) {
        this.action = action;
    }
    
}