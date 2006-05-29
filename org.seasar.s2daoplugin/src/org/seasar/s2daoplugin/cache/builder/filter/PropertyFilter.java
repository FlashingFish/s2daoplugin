/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2daoplugin.cache.builder.filter;

import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IPropertyElement;
import org.seasar.s2daoplugin.cache.DiconUtil;
import org.seasar.s2daoplugin.util.StringUtil;

public class PropertyFilter extends AbstractDecorationFilter {

	private String propertyName;
	
	public PropertyFilter(String propertyName, IComponentFilter filter) {
		super(filter);
		if (StringUtil.isEmpty(propertyName)) {
			throw new IllegalArgumentException();
		}
		this.propertyName = propertyName;
	}
	
	public boolean isPassable(IComponentElement component) {
		List props = component.getPropertyList();
		for (int i = 0; i < props.size(); i++) {
			IPropertyElement prop = (IPropertyElement) props.get(i);
			if (propertyName.equals(prop.getPropertyName())) {
				IComponentElement comp = DiconUtil.getAvailableComponent(prop);
				if (getFilter().isPassable(comp)) {
					return true;
				}
			}
		}
		return false;
	}

}
