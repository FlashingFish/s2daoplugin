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
import java.util.LinkedList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;

public class AutoAspectedComponentCacheBuilder extends AbstractComponentTargetCacheBuilder {

	private final IComponentHolder aspectHolder;
	private final IComponentHolder aspectAutoRegisterHolder;
	
	public AutoAspectedComponentCacheBuilder(String targetClassName) {
		this(new String[] {targetClassName});
	}
	
	public AutoAspectedComponentCacheBuilder(String[] targetClassNames) {
		aspectHolder = createHolder(targetClassNames);
		aspectAutoRegisterHolder = createHolder(CacheConstants.ASPECT_AUTO_REGISTERS);
	}
	
	protected List findAdditionalComponents(IComponentElement[] components) {
		List result = new LinkedList();
		List s2daoAutoRegisters = findS2DaoAutoRegisters();
		for (int i = 0; i < components.length; i++) {
			for (int j = 0; j < s2daoAutoRegisters.size(); j++) {
				IAutoRegisterElement ar = (IAutoRegisterElement) s2daoAutoRegisters.get(j);
				if (isApplied(ar, components[i])) {
					result.add(components[i]);
				}
			}
		}
		return result;
	}
	
	private List findS2DaoAutoRegisters() {
		List s2daoAutoRegisters = new ArrayList();
		IComponentElement[] components = (IComponentElement[]) aspectAutoRegisterHolder.getComponents();
		for (int i = 0; i < components.length; i++) {
			IComponentElement[] aspectComponents = aspectHolder.getComponents();
			for (int j = 0; j < aspectComponents.length; j++) {
				IAutoRegisterElement auto = (IAutoRegisterElement) components[i];
				if (AutoRegisterUtil.hasInterceptor(auto, aspectComponents[j])) {
					s2daoAutoRegisters.add(auto);
				}
			}
		}
		return s2daoAutoRegisters;
	}
	
	private boolean isApplied(IAutoRegisterElement autoRegister, IComponentElement target) {
		IRtti rtti = (IRtti) target.getAdapter(IRtti.class);
		if (rtti == null) {
			return false;
		}
		// S2Containerの仕様 - http://s2container.seasar.org/ja/DIContainer.html#AspectAutoRegister
		// 同一Dicon内でかつ、対象がAutoRegisterより後に出現すること
		if (target.getStorage().getFullPath().equals(autoRegister.getStorage().getFullPath()) &&
				target.getStartLine() > autoRegister.getStartLine() &&
				autoRegister.isApplied(
						rtti.getType().getPackageFragment().getElementName(),
						rtti.getType().getElementName())) {
			return true;
		}
		return false;
	}

}
