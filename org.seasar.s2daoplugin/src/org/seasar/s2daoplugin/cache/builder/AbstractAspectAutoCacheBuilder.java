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

import java.util.ArrayList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public abstract class AbstractAspectAutoCacheBuilder extends
		AbstractComponentTargetCacheBuilder {

	private final IComponentHolder aspectHolder;
	private final IComponentHolder aspectAutoRegisterHolder;
	
	public AbstractAspectAutoCacheBuilder(String targetClassName) {
		this(new String[] {targetClassName});
	}
	
	public AbstractAspectAutoCacheBuilder(String[] targetClassNames) {
		aspectHolder = createHolder(targetClassNames);
		aspectAutoRegisterHolder = createHolder(CacheConstants.ASPECT_AUTO_REGISTERS);
	}
	
	protected List findTargetAutoRegisters() {
		List autoRegisters = new ArrayList();
		IComponentElement[] components = (IComponentElement[]) aspectAutoRegisterHolder.getComponents();
		for (int i = 0; i < components.length; i++) {
			IComponentElement[] aspectComponents = aspectHolder.getComponents();
			for (int j = 0; j < aspectComponents.length; j++) {
				IAutoRegisterElement auto = (IAutoRegisterElement) components[i];
				if (AutoRegisterUtil.hasInterceptor(auto, aspectComponents[j])) {
					autoRegisters.add(auto);
				}
			}
		}
		return autoRegisters;
	}

}
