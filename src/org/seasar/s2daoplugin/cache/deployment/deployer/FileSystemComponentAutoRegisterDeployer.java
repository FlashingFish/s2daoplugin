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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;
import org.seasar.s2daoplugin.util.JavaProjectUtil;

public class FileSystemComponentAutoRegisterDeployer extends
		AbstractComponentAutoRegisterDeployer {

	public FileSystemComponentAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement autoRegister) {
		super(container, autoRegister);
		assertValidComponentName(FILESYSTEM_COMPONENT_AUTO_REGISTER);
	}
	
	public void doDeploy() {
		IStorage s = getAutoRegister().getStorage();
		if (!(s instanceof IResource)) {
			return;
		}
		IPackageFragmentRoot root = JavaProjectUtil.findPackageFragmentRoot((IResource) s);
		IPackageFragmentRoot[] packages = findPackageFragmentRootsSharedOutputLocation(root);
		for (int i = 0; i < packages.length; i++) {
			process(packages[i]);
		}
	}

}
