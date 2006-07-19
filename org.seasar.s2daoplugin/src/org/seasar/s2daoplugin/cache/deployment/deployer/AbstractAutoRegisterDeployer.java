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

import java.util.ArrayList;
import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IArgElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IInitMethodElement;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;

public abstract class AbstractAutoRegisterDeployer extends AbstractComponentDeployer
		implements CacheConstants {

	private List classPatterns = new ArrayList();
	private List ignoreClassPatterns = new ArrayList();
	
	public AbstractAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement autoRegister) {
		super(container, autoRegister);
		buildPatterns(autoRegister);
	}
	
	protected IComponentElement getAutoRegister() {
		return getComponent();
	}
	
	protected void addPreparedComponent(IComponentElement component) {
		getContainer().addPreparedComponent(component);
	}
	
	protected void deploy(IComponentElement component) {
		getContainer().addComponent(component);
	}
	
	protected IComponentElement[] getPreparedComponents() {
		return getContainer().getPreparedComponents();
	}
	
    protected boolean isIgnore(String packageName, String shortClassName) {
        if (ignoreClassPatterns.isEmpty()) {
            return false;
        }
        for (int i = 0; i < ignoreClassPatterns.size(); ++i) {
            ClassPattern cp = (ClassPattern) ignoreClassPatterns.get(i);
            if (!cp.isAppliedPackageName(packageName)) {
                continue;
            }
            if (cp.isAppliedShortClassName(shortClassName)) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean isApplied(String packageName, String typeName) {
		if (isIgnore(packageName, typeName)) {
			return false;
		}
		for (int i = 0; i < getClassPatternSize(); i++) {
			ClassPattern cp = getClassPattern(i);
			if (cp.isAppliedPackageName(packageName) &&
					cp.isAppliedShortClassName(typeName)) {
				return true;
			}
		}
		return false;
    }
    
    protected int getClassPatternSize() {
    	return classPatterns.size();
    }
    
    protected ClassPattern getClassPattern(int index) {
    	return (ClassPattern) classPatterns.get(index);
    }
    
	private void buildPatterns(IComponentElement component) {
		List initMethods = component.getInitMethodList();
		for (int i = 0; i < initMethods.size(); i++) {
			IInitMethodElement im = (IInitMethodElement) initMethods.get(i);
			if ("addClassPattern".equals(im.getMethodName())) {
				buildClassPattern(im);
			} else if ("addIgnoreClassPattern".equals(im.getMethodName())) {
				buildIgnoreClassPattern(im);
			}
		}
	}

	private void buildClassPattern(IInitMethodElement initMethod) {
		List args = initMethod.getArgList();
		if (args.size() == 2) {
			addClassPattern((IArgElement) args.get(0), (IArgElement) args.get(1));
		}
	}
	
	private void addClassPattern(IArgElement packageArg, IArgElement classArg) {
		addClassPattern(trimQuote(packageArg.getBody()), trimQuote(classArg.getBody()));
	}
	
	private void addClassPattern(String packageName, String className) {
		classPatterns.add(new ClassPattern(packageName, className));
	}
	
	private void buildIgnoreClassPattern(IInitMethodElement initMethod) {
		List args = initMethod.getArgList();
		if (args.size() == 2) {
			setIgnoreClassPattern((IArgElement) args.get(0), (IArgElement) args.get(1));
		}
	}
	
	private void setIgnoreClassPattern(IArgElement packageArg, IArgElement classArg) {
		setIgnoreClassPattern(trimQuote(packageArg.getBody()), trimQuote(classArg.getBody()));
	}
	
	private void setIgnoreClassPattern(String packageName, String className) {
		ignoreClassPatterns.add(new ClassPattern(packageName, className));
	}

}
