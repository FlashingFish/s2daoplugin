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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;

public interface IComponentCache extends IDiconChangeListener {

	IComponentElement[] getComponents(IType type);
	
	IComponentElement[] getComponents(IType type, IPath containerPath);
	
	IComponentElement[] getAllComponents();
	
	IPath[] getAllContainerPaths();
	
	IType[] getAllAppliedTypes();
	
	IType[] getAppliedTypes(IPath containerPath);
	
	boolean contains(IType type);
	
	boolean contains(IType type, IPath containerPath);
	
	void addComponent(IComponentElement component);
	
	void removeComponent(IComponentElement component);
	
	void clearCache();

}
