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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;
import org.seasar.s2daoplugin.util.JavaUtil;

public class FileSystemComponentAutoRegisterDeployer extends AbstractAutoRegisterDeployer {

	public FileSystemComponentAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement autoRegister) {
		super(container, autoRegister);
		if (!FILESYSTEM_COMPONENT_AUTO_REGISTER.equals(
				autoRegister.getComponentClassName())) {
			throw new IllegalArgumentException();
		}
	}
	
	public void deploy() {
		IPackageFragmentRoot[] packages = findPackageFragmentRootSharedOutputLocation(
				getAutoRegister().getStorage());
		for (int i = 0; i < packages.length; i++) {
			for (int j = 0; j < getClassPatternSize(); j++) {
				ClassPattern cp = getClassPattern(j);
				IPackageFragment base = packages[i].getPackageFragment(cp.getPackageName());
				try {
					base.getResource().accept(new ProcessVisitor(cp),
							IResource.DEPTH_INFINITE, false);
				} catch (CoreException e) {
					S2DaoPlugin.log(e);
				}
			}
		}
		deploy(getAutoRegister());
	}
	
	public int getType() {
		return TYPE_COMPONENT_AUTO;
	}
	
	private void process(IType type) {
		IComponentElement component = createComponent(type.getFullyQualifiedName());
		addPreparedComponent(component);
	}
	
	private IComponentElement createComponent(String fqcn) {
		IComponentElement component = (IComponentElement) createElement(
				DICON_TAG_COMPONENT);
		component.setAttributes(createAttributes(fqcn));
		setParent(component, getAutoRegister().getParent());
		return component;
	}
	
	private Map createAttributes(String fqcn) {
		Map attribues = new HashMap();
		attribues.put(DICON_ATTR_INSTANCE, "singleton");
		attribues.put(DICON_ATTR_CLASS, fqcn);
		attribues.put(DICON_ATTR_AUTOBINDING, "auto");
		return attribues;
	}

	private IPackageFragmentRoot findPackageFragmentRoot(IResource dicon) {
		IJavaElement element = JavaCore.create(dicon.getParent());
		if (element instanceof IPackageFragmentRoot) {
			return (IPackageFragmentRoot) element;
		}
		while (element instanceof IPackageFragment) {
			element = ((IPackageFragment) element).getParent();
			if (element instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot) element;
			}
		}
		return null;
	}
	
	private IPackageFragmentRoot[] findPackageFragmentRootSharedOutputLocation(
			IStorage storage) {
		if (!(storage instanceof IResource)) {
			return new IPackageFragmentRoot[0];
		}
		IPackageFragmentRoot root = findPackageFragmentRoot((IResource) storage);
		IJavaProject project = root.getJavaProject();
		Set result = new HashSet();
		try {
			IPath output1 = root.getRawClasspathEntry().getOutputLocation();
			if (output1 == null) {
				output1 = project.getOutputLocation();
			}
			IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind() == IPackageFragmentRoot.K_BINARY) {
					continue;
				}
				IPath output2 = roots[i].getRawClasspathEntry().getOutputLocation();
				if (output2 == null) {
					output2 = project.getOutputLocation();
				}
				if (output1.equals(output2)) {
					result.add(roots[i]);
				}
			}
		} catch (JavaModelException e) {
			S2DaoPlugin.log(e);
		}
		return (IPackageFragmentRoot[]) result.toArray(new IPackageFragmentRoot[result.size()]);
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
			if (isApplied(type)) {
				process(type);
			}
			return true;
		}
		
		private boolean isApplied(IType type) {
			if (type == null) {
				return false;
			}
			String packageName = type.getPackageFragment().getElementName();
			String shortClassName = type.getElementName();
			if (isIgnore(packageName, shortClassName)) {
				return false;
			}
			if (cp.isAppliedPackageName(packageName) &&
					cp.isAppliedShortClassName(shortClassName)) {
				return true;
			}
			return false;
		}
	}

}
