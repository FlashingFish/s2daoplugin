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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IPropertyElement;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.DiconUtil;
import org.seasar.s2daoplugin.cache.model.IAutoRegisterElement;
import org.seasar.s2daoplugin.util.JavaProjectUtil;
import org.seasar.s2daoplugin.util.JavaUtil;

public class AutoRegisterUtil implements CacheConstants {

	private static final Set autoRegisterNames;
	
	static {
		int size = COMPONENT_AUTO_REGISTERS.length + ASPECT_AUTO_REGISTERS.length;
		autoRegisterNames = new HashSet(size);
		autoRegisterNames.addAll(Arrays.asList(COMPONENT_AUTO_REGISTERS));
		autoRegisterNames.addAll(Arrays.asList(ASPECT_AUTO_REGISTERS));
	}
	
	public static boolean hasInterceptor(IAutoRegisterElement autoRegister, IType type) {
		if (autoRegister == null || type == null) {
			return false;
		}
		if (autoRegister.getAutoRegisterType() !=
				IAutoRegisterElement.TYPE_COMPONENT_TARGET) {
			return false;
		}
		List props = autoRegister.getPropertyList();
		for (int i = 0; i < props.size(); i++) {
			IPropertyElement prop = (IPropertyElement) props.get(i);
			if ("interceptor".equals(prop.getPropertyName())) {
				return AspectUtil.hasInterceptor(DiconUtil.getAvailableComponent(prop),
						type);
			}
		}
		return false;
	}
	
	public static List getAppliedTypes(IAutoRegisterElement autoRegister) {
		List result = new ArrayList();
		if (autoRegister == null) {
			return result;
		}
		IJavaProject project = JavaCore.create(autoRegister.getProject());
		TypeCollectingVisitor v = new TypeCollectingVisitor(autoRegister);
		JavaProjectUtil.visitSourceFolders(project, autoRegister.getPackageName(), v);
		return v.getResult();
	}
	
	public static boolean isAutoRegister(IComponentElement component) {
		if (component == null) {
			return false;
		}
		if (component instanceof IAutoRegisterElement) {
			return true;
		}
		return autoRegisterNames.contains(component.getComponentClassName());
	}
	
	
	private static class TypeCollectingVisitor implements IResourceVisitor {

		private IAutoRegisterElement autoRegister;
		private Set result = new HashSet();
		
		public TypeCollectingVisitor(IAutoRegisterElement autoRegister) {
			this.autoRegister = autoRegister;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (!"java".equals(resource.getFileExtension())) {
				return true;
			}
			IType type = JavaUtil.findPrimaryType(resource);
			if (autoRegister.isApplied(type)) {
				result.add(type);
			}
			return true;
		}
		
		public List getResult() {
			return new ArrayList(result);
		}
	}

}
