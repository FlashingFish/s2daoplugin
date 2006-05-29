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
package org.seasar.s2daoplugin.cache;

import java.util.Collection;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IComponentHolderElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.rtti.IRtti;

public final class DiconUtil {

	public static IComponentElement[] getComponents(IContainerElement container) {
		if (container == null) {
			return CacheConstants.EMPTY_COMPONENTS;
		}
		List components = container.getComponentList();
		return (IComponentElement[]) components
				.toArray(new IComponentElement[components.size()]);
	}
	
	public static IContainerElement[] toContainerArray(Collection collection) {
		if (collection == null) {
			return CacheConstants.EMPTY_CONTAINERS;
		}
		return (IContainerElement[]) collection
				.toArray(new IContainerElement[collection.size()]);
	}
	
	public static IComponentElement getAvailableComponent(IComponentHolderElement element) {
		if (element == null) {
			return null;
		}
		IRtti rtti = (IRtti) element.getAdapter(IRtti.class);
		if (rtti == null) {
			return null;
		}
		// OGNLŽw’è
		IComponentElement component =
			(IComponentElement) rtti.getAdapter(IComponentElement.class);
		// <component>Žw’è
		if (component == null) {
			List children = element.getChildren();
			if (children.size() == 1 &&
					children.get(0) instanceof IComponentElement) {
				component = (IComponentElement) children.get(0);
			}
		}
		return component;
	}

}
