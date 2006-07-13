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
import org.seasar.kijimuna.core.dicon.model.IPropertyElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.kijimuna.core.rtti.RttiLoader;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;
import org.seasar.s2daoplugin.cache.util.DiconUtil;

public class InterfaceAspectAutoRegisterDeployer extends AbstractComponentDeployer
		implements CacheConstants {

	private IRtti targetInterface;
	
	public InterfaceAspectAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement autoRegister) {
		super(container, autoRegister, INTERFACE_ASPECT_AUTO_REGISTER);
		buildTargetInterface();
	}
	
	public void doDeploy() {
		if (targetInterface == null) {
			return;
		}
		IComponentElement[] components = getContainer().getPreparedComponents();
		for (int i = 0; i < components.length; i++) {
			if (isApplied(components[i])) {
				process(components[i]);
			}
		}
	}

	public int getType() {
		return TYPE_COMPONENT_TARGET_AUTO;
	}
	
	private IComponentElement getAutoRegister() {
		return getComponent();
	}

	private void buildTargetInterface() {
		IPropertyElement prop = DiconUtil.getProperty(getAutoRegister(),
				"targetInterface");
		if (prop != null) {
			IRtti rtti = findRttiReferencedClassField(prop.getBody());
			targetInterface = rtti != null && rtti.isInterface() ? rtti : null;
		}
	}
	
	private boolean isApplied(IComponentElement component) {
		RttiLoader loader = component.getRttiLoader();
		IRtti rtti = loader.loadRtti(component.getComponentClassName());
		return rtti != null && targetInterface.isAssignableFrom(rtti);
	}
	
	private void process(IComponentElement component) {
		IAspectElement aspect = IsolatedElementFactory.createAspectElement(getAutoRegister());
		aspect.setParent(component);
	}

}
