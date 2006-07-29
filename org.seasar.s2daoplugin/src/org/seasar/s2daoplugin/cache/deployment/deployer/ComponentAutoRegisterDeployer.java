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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.seasar.kijimuna.core.dicon.model.IArgElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IInitMethodElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;
import org.seasar.s2daoplugin.util.JavaProjectUtil;

public class ComponentAutoRegisterDeployer extends
		AbstractComponentAutoRegisterDeployer {

	private Set referenceClasses = new HashSet();
	
	public ComponentAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement component) {
		super(container, component, COMPONENT_AUTO_REGISTER);
		setUp();
	}
	
	public boolean setUp() {
		return setUpReferenceClasses();
	}
	
	protected void doDeploy(IHandler handler) throws CoreException {
		for (Iterator it = referenceClasses.iterator(); it.hasNext();) {
			IRtti rtti = (IRtti) it.next();
			IPackageFragmentRoot root = JavaProjectUtil.findPackageFragmentRoot(
					rtti.getType());
			if (isJar(root)) {
				handler.processPackageFragmentRoot(root);
			} else if (isFileSystem(root)) {
				IPackageFragmentRoot[] roots =
					JavaProjectUtil.findPackageFragmentRootsSharedOutputLocation(root);
				for (int j = 0; j < roots.length; j++) {
					handler.processPackageFragmentRoot(roots[j]);
				}
			}
		}
	}
	
	private boolean setUpReferenceClasses() {
		boolean dirty = false;
		List methods = getAutoRegister().getInitMethodList();
		for (int i = 0; i < methods.size(); i++) {
			IInitMethodElement method = (IInitMethodElement) methods.get(i);
			if (!"addReferenceClass".equals(method.getMethodName())) {
				continue;
			}
			List args = method.getArgList();
			if (args.size() != 1) {
				continue;
			}
			IArgElement arg = (IArgElement) args.get(0);
			IRtti rtti = findRttiReferencedClassField(arg.getBody());
			if (rtti != null) {
				dirty |= referenceClasses.add(rtti);
			}
		}
		return dirty;
	}

	private boolean isJar(IPackageFragmentRoot root) {
		return root != null && root.isArchive() && "jar".equalsIgnoreCase(
				root.getResource().getFileExtension());
	}
	
	private boolean isFileSystem(IPackageFragmentRoot root) {
		return root != null && !root.isArchive();
	}

}
