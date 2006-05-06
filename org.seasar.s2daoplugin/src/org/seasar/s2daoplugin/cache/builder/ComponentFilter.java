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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.DiconModelManager;
import org.seasar.s2daoplugin.util.StringUtil;

public class ComponentFilter implements IComponentFilter {

	private String[] targetClassNames;
	private Set targetClassTypeSet = new HashSet();
	private DiconModelManager manager;
	
	public ComponentFilter(String[] targetClassNames) {
		if (targetClassNames == null) {
			throw new IllegalArgumentException();
		}
		this.targetClassNames = targetClassNames;
	}
	
	public void setDiconModelManager(DiconModelManager manager) {
		if (manager == null) {
			throw new IllegalArgumentException();
		}
		this.manager = manager;
	}
	
	public void initialize() {
		buildTargetClassTypes();
	}
	
	public IComponentElement[] filtering(IComponentElement[] components) {
		if (components == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		List result = new LinkedList();
		for (int i = 0; i < components.length; i++) {
			IComponentElement component = components[i];
			IRtti componentRtti = getManager().getRtti(component.getComponentClassName());
			// targetClassが空ならすべてのコンポーネントが対象
			if (StringUtil.isEmpty(targetClassNames[0]) || isTarget(componentRtti)) {
				result.add(component);
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	protected DiconModelManager getManager() {
		return manager;
	}
	
	private void buildTargetClassTypes() {
		for (int i = 0; i < targetClassNames.length; i++) {
			IRtti rtti = getManager().getRtti(targetClassNames[i]);
			addType(rtti);
		}
	}
	
	private void addType(IRtti rtti) {
		if (rtti == null) {
			return;
		}
		targetClassTypeSet.add(rtti.getType());
	}
	
	private boolean isTarget(IRtti rtti) {
		if (rtti == null) {
			return false;
		}
		return targetClassTypeSet.contains(rtti.getType());
	}

}
