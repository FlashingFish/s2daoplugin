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

import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;
import org.seasar.s2daoplugin.util.JavaUtil;

public class AspectAutoRegisterDeployer extends AbstractAutoRegisterDeployer {

	public AspectAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement component) {
		super(container, component);
		assertValidComponentName(ASPECT_AUTO_REGISTER);
	}
	
	public void doDeploy() {
		IComponentElement[] components = getPreparedComponents();
		for (int i = 0; i < components.length; i++) {
			if (isAppliedComponent(components[i])) {
				process(components[i]);
			}
		}
	}
	
	public int getType() {
		return TYPE_COMPONENT_TARGET_AUTO;
	}
	
	private boolean isAppliedComponent(IComponentElement component) {
		String fqcn = component.getComponentClassName();
		String packageName = JavaUtil.getPackageName(fqcn);
		String typeName = JavaUtil.getShortClassName(fqcn);
		return isApplied(packageName, typeName);
	}
	
	private void process(IComponentElement component) {
		IAspectElement aspect = ElementFactory.createAspectElement(getAutoRegister());
		aspect.setParent(component);
	}

}
