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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public class AutoRegisterAppiedFilter extends AbstractComponentFilter implements
		IExtractionComponentFilter {

	private IExtractionComponentFilter filter;
	private Set components = new HashSet();
	
	public AutoRegisterAppiedFilter(IExtractionComponentFilter filter) {
		if (filter == null) {
			throw new IllegalArgumentException();
		}
		this.filter = filter;
	}
	
	public boolean isPassable(IComponentElement component) {
		return filter.isPassable(component);
	}
	
	public boolean addComponentIfNecessary(IComponentElement component) {
		if (!filter.addComponentIfNecessary(component)) {
			components.add(component);
		}
		return true;
	}
	
	public void clearComponents() {
		components.clear();
		filter.clearComponents();
	}
	
	public IComponentElement[] getComponents() {
		Set result = new HashSet();
		IComponentElement[] autos = filter.getComponents();
		for (int i = 0; i < autos.length; i++) {
			if (!(autos[i] instanceof IAutoRegisterElement)) {
				continue;
			}
			IAutoRegisterElement auto = (IAutoRegisterElement) autos[i];
			for (Iterator it = components.iterator(); it.hasNext();) {
				IComponentElement comp = (IComponentElement) it.next();
				if (isApplied(auto, comp)) {
					result.add(comp);
				}
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	protected void onManagerSet() {
		filter.setManager(getManager());
	}
	
	private boolean isApplied(IAutoRegisterElement autoRegister, IComponentElement target) {
		IRtti rtti = getRtti(target.getComponentClassName());
		if (rtti == null) {
			return false;
		}
		// S2Containerの仕様 - http://s2container.seasar.org/ja/DIContainer.html#AspectAutoRegister
		// 同一Dicon内でかつ、対象がAutoRegisterより後に出現すること
		return target.getStartLine() > autoRegister.getStartLine() &&
				autoRegister.isApplied(rtti.getType());
	}

}
