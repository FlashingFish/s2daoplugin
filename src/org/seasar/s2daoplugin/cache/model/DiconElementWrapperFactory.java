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
package org.seasar.s2daoplugin.cache.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.seasar.kijimuna.core.ConstCore;
import org.seasar.kijimuna.core.dicon.DiconElementFactory;
import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.parser.IElement;

public class DiconElementWrapperFactory implements ConstCore {

	private static final DiconElementFactory factory = new DiconElementFactory();
	
	public static IElement createElement(IProject project, IStorage storage,
			String elementName) {
		if (DICON_TAG_COMPONENT.equals(elementName)) {
//			return new ComponentElementWrapper(createComponent(project, storage));
		} else if (DICON_TAG_ASPECT.equals(elementName)) {
//			return new AspectElementWrapper(createAspect(project, storage));
		}
		return factory.createElement(project, storage, elementName);
	}
	
	private static IComponentElement createComponent(IProject project, IStorage storage) {
		return (IComponentElement) factory.createElement(project, storage,
				DICON_TAG_COMPONENT);
	}
	
	private static IAspectElement createAspect(IProject project, IStorage storage) {
		return (IAspectElement) factory.createElement(project, storage,
				DICON_TAG_ASPECT);
	}

}
