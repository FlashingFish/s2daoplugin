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
package org.seasar.s2daoplugin.cache.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.DiconModelManager;

public class ComponentFilter implements IComponentFilter {

	private Set targetClassNames;
	private DiconModelManager manager;
	
	public ComponentFilter(String[] targetClassNames) {
		if (targetClassNames == null) {
			throw new IllegalArgumentException();
		}
		this.targetClassNames = new HashSet(Arrays.asList(targetClassNames));
	}
	
	public void setDiconModelManager(DiconModelManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException();
		}
		this.manager = manager;
	}
	
	public void initialize() {
//		buildTargetClassTypes();
	}
	
	public IComponentElement[] filtering(IComponentElement[] components) {
		if (components == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		Set result = new HashSet();
		for (int i = 0; i < components.length; i++) {
			String className = components[i].getComponentClassName();
			if (!targetClassNames.isEmpty() &&
					!targetClassNames.contains(className)) {
				continue;
			}
			IRtti rtti = getManager().getRtti(className);
			if (rtti != null && rtti.getType() != null) {
				result.add(components[i]);
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	protected DiconModelManager getManager() {
		return manager;
	}

}
