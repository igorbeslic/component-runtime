/**
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;

/**
 * Property node for Table property. It stores nested properties, which represent table columns
 */
public class TablePropertyNode extends PropertyNode {

    private List<PropertyNode> nestedProperties = new ArrayList<>();

    /**
     * @param property {@link SimplePropertyDefinition}
     * @param fieldType widget type, defines UI representation
     * @param root specifies whether this node is root node
     */
    public TablePropertyNode(final SimplePropertyDefinition property, final EParameterFieldType fieldType, final boolean root) {
        super(property, fieldType, root);
    }

    public List<PropertyNode> getColumns() {
        return Collections.unmodifiableList(nestedProperties);
    }

    public void addColumn(final PropertyNode column) {
        nestedProperties.add(column);
    }
}
