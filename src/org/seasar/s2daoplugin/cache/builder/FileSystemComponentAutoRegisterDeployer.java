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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.parser.IElement;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.cache.model.ClassPattern;
import org.seasar.s2daoplugin.util.JavaProjectUtil;
import org.seasar.s2daoplugin.util.JavaUtil;

public class FileSystemComponentAutoRegisterDeployer extends AbstractAutoRegisterDeployer {

	public FileSystemComponentAutoRegisterDeployer(IComponentContainer container,
			IComponentElement autoRegister) {
		super(container, autoRegister);
		if (!FILESYSTEM_COMPONENT_AUTO_REGISTER.equals(
				autoRegister.getComponentClassName())) {
			throw new IllegalArgumentException();
		}
	}
	
	public void deploy() {
		IJavaProject project = JavaCore.create(getAutoRegister().getProject());
		for (int i = 0; i < getClassPatternSize(); i++) {
			ClassPattern cp = getClassPattern(i);
			JavaProjectUtil.visitSourceFolders(
					project, cp.getPackageName(), new ProcessVisitor(cp));
		}
	}
	
	private void process(ClassPattern cp, String packageName, String className) {
		if (isIgnore(packageName, className)) {
			return;
		}
		if (cp.isAppliedPackageName(packageName) &&
				cp.isAppliedShortClassName(className)) {
			IComponentElement component = createComponent(packageName, className);
			addPreparedComponent(component);
		}
	}
	
	private IComponentElement createComponent(String packageName, String className) {
		IComponentElement component =
			(IComponentElement) createElement(DICON_TAG_COMPONENT);
//		component.setStartLocation(2, autoRegister.getStartLine(), 1);
//		component.setEndLocation(autoRegister.getEndLine(), 1);
		component.setAttributes(createAttributes(packageName, className));
//		component.setBody("");
//		component = new VirtualComponentElement(component);
//		component.setRootElement(autoRegister.getContainerElement());
		
//		component.setParent(getAutoRegister().getParent());
		setParent(component, getAutoRegister().getParent());
		return component;
	}
	
	private Map createAttributes(String packageName, String className) {
		Map attribues = new HashMap();
		attribues.put(DICON_ATTR_INSTANCE, "singleton");
		attribues.put(DICON_ATTR_CLASS, packageName + "." + className);
		attribues.put(DICON_ATTR_AUTOBINDING, "auto");
		return attribues;
	}
	
	private void setParent(IElement child, IElement parent) {
		Field field = findParentFiled(child.getClass());
		if (field != null) {
			try {
				field.set(child, parent);
			} catch (IllegalArgumentException e) {
				S2DaoPlugin.log(e);
			} catch (IllegalAccessException e) {
				S2DaoPlugin.log(e);
			}
		}
	}
	
	private Field findParentFiled(Class clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if ("parent".equals(fields[i].getName())) {
				return fields[i];
			}
		}
		Class superClass = clazz.getSuperclass();
		if (clazz != Object.class && superClass != null) {
			return findParentFiled(superClass);
		}
		return null;
	}
	
	private class ProcessVisitor implements IResourceVisitor {

		private ClassPattern cp;
		
		public ProcessVisitor(ClassPattern cp) {
			this.cp = cp;
		}
		
		public boolean visit(IResource resource) throws CoreException {
			if (!JavaUtil.isJavaFile(resource)) {
				return true;
			}
			IType type = JavaUtil.findPrimaryType(resource);
			if (type != null) {
				String packageName = type.getPackageFragment().getElementName();
				String className = type.getElementName();
				process(cp, packageName, className);
			}
			return true;
		}
	}

}
