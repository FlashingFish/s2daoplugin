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
package org.seasar.s2daoplugin.cache.deployment.deployer;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;
import org.seasar.s2daoplugin.cache.util.FlagsUtil;

public abstract class AbstractComponentAutoRegisterDeployer extends
		AbstractAutoRegisterDeployer {

	public AbstractComponentAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement component) {
		super(container, component);
	}
	
	public int getType() {
		return TYPE_COMPONENT_AUTO;
	}
	
	protected void process(IPackageFragmentRoot root) {
		IJavaElement[] elements;
		try {
			elements = root.getChildren();
		} catch (JavaModelException e) {
			return;
		}
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getElementType() != IJavaElement.PACKAGE_FRAGMENT) {
				continue;
			}
			IPackageFragment fragment = (IPackageFragment) elements[i];
			for (int j = 0; j < getClassPatternSize(); j++) {
				ClassPattern cp = getClassPattern(j);
				if (cp.isAppliedPackageName(fragment.getElementName())) {
					processChild(fragment);
				}
			}
		}
	}
	
	private void processChild(IPackageFragment pkg) {
		try {
			IJavaElement[] elements = pkg.getChildren();
			for (int i = 0; i < elements.length; i++) {
				registerType(elements[i]);
			}
		} catch (JavaModelException e) {
		}
	}
	
	private void registerType(IJavaElement element) {
		try {
			if (element instanceof ICompilationUnit) {
				IType[] types = ((ICompilationUnit) element).getAllTypes();
				for (int i = 0; i < types.length; i++) {
					register(types[i]);
				}
			} else if (element instanceof IClassFile) {
				IType type = ((IClassFile) element).getType();
				register(type);
			}
		} catch (JavaModelException e) {
		}
	}
	
	private void register(IType type) {
		if (isRegisterableType(type) && isAppliedType(type)) {
			IComponentElement component = ElementFactory.createComponentElement(
					getAutoRegister(), type.getFullyQualifiedName());
			addPreparedComponent(component);
		}
	}

	private boolean isRegisterableType(IType type) {
		try {
			if (type == null || type.isLocal() || type.isAnonymous()) {
				return false;
			}
		} catch (JavaModelException e) {
			return false;
		}
		return !isEnclosingType(type) || FlagsUtil.isInterface(type) ||
				(FlagsUtil.isStatic(type) && FlagsUtil.isPublic(type)); 
	}
	
	private boolean isEnclosingType(IType type) {
		return type.getTypeQualifiedName().indexOf('$') != -1;
	}

	private boolean isAppliedType(IType type) {
		if (type == null) {
			return false;
		}
		String packageName = type.getPackageFragment().getElementName();
		String typeName = type.getElementName();
		return isApplied(packageName, typeName);
	}

}
