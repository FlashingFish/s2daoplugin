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

import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;

public class DefaultComponentDeployer extends AbstractComponentDeployer {

	public DefaultComponentDeployer(IDeploymentContainer container,
			IComponentElement component) {
		super(container, component);
	}
	
	public boolean setUp() {
		return false;
	}
	
	public void doDeploy() {
	}
	
	public int getType() {
		return TYPE_COMPONENT;
	}

}
