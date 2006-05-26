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

import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;

public class AspectedComponentCacheBuilder extends AbstractCacheBuilder {

	private final String[] targetClassNames;
	
	public AspectedComponentCacheBuilder(String targetClassName) {
		this(new String[] {targetClassName});
	}
	
	public AspectedComponentCacheBuilder(String[] targetClassNames) {
		if (targetClassNames == null) {
			throw new IllegalArgumentException();
		}
		this.targetClassNames = targetClassNames;
	}
	
	public void initialize() {
		// do nothing
	}
	
	public void build(IComponentElement[] components) {
		for (int i = 0; i < components.length; i++) {
			List aspects = components[i].getAspectList();
			for (int j = 0; j < aspects.size(); j++) {
				if (isTargetAspected((IAspectElement) aspects.get(j))) {
					addComponent(components[i]);
					break;
				}
			}
		}
	}

	public void clear(IComponentElement[] components) {
		removeComponents(components);
	}

	public void finishBuild() {
		// do nothing
	}
	
	private boolean isTargetAspected(IAspectElement aspect) {
		for (int i = 0; i < targetClassNames.length; i++) {
			IRtti rtti = getManager().getRtti(targetClassNames[i]);
			if (rtti != null) {
				if (AspectUtil.containsInterceptorType(aspect, rtti.getType())) {
					return true;
				}
			}
		}
		return false;
	}

}
