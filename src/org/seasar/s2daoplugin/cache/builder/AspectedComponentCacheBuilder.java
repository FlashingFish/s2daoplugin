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

import java.util.LinkedList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;

public class AspectedComponentCacheBuilder extends AbstractComponentTargetCacheBuilder {

	private final IComponentHolder aspectHolder;
	
	public AspectedComponentCacheBuilder(String targetClassName) {
		this(new String[] {targetClassName});
	}
	
	public AspectedComponentCacheBuilder(String[] targetClassNames) {
		aspectHolder = createHolder(targetClassNames);
	}
	
	protected List findAdditionalComponents(IComponentElement[] components) {
		List result = new LinkedList();
		for (int i = 0; i < components.length; i++) {
			IComponentElement component = (IComponentElement) components[i];
			List aspects = component.getAspectList();
			for (int j = 0; j < aspects.size(); j++) {
				IComponentElement[] aspectComponents = aspectHolder.getComponents();
				for (int k = 0; k < aspectComponents.length; k++) {
					if (AspectUtil.containsInterceptor(
							(IAspectElement) aspects.get(j), aspectComponents[k])) {
						result.add(component);
					}
				}
			}
		}
		return result;
	}

}
