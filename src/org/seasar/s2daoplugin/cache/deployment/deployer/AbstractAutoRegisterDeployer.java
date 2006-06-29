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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.seasar.kijimuna.core.ConstCore;
import org.seasar.kijimuna.core.dicon.DiconElementFactory;
import org.seasar.kijimuna.core.dicon.model.IArgElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IInitMethodElement;
import org.seasar.kijimuna.core.parser.IElement;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.deployment.IDeploymentContainer;

public abstract class AbstractAutoRegisterDeployer implements
		IComponentDeployer, CacheConstants, ConstCore {

	private static final DiconElementFactory factory = new DiconElementFactory();
	
	private IDeploymentContainer container;
	private IComponentElement autoRegister;
	private List classPatterns = new ArrayList();
	private List ignoreClassPatterns = new ArrayList();
	
	public AbstractAutoRegisterDeployer(IDeploymentContainer container,
			IComponentElement autoRegister) {
		if (container == null || autoRegister == null) {
			throw new IllegalArgumentException();
		}
		this.container = container;
		this.autoRegister = autoRegister;
		buildPatterns(autoRegister);
	}
	
	protected IComponentElement getAutoRegister() {
		return autoRegister;
	}
	
	protected void addPreparedComponent(IComponentElement component) {
		container.addPreparedComponent(component);
	}
	
	protected void deploy(IComponentElement component) {
		container.addComponent(component);
	}
	
	protected IComponentElement[] getPreparedComponents() {
		return container.getPreparedComponents();
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
    
    protected int getClassPatternSize() {
    	return classPatterns.size();
    }
    
    protected ClassPattern getClassPattern(int index) {
    	return (ClassPattern) classPatterns.get(index);
    }
    
    protected IElement createElement(String elementName) {
    	IElement element = factory.createElement(autoRegister.getProject(),
    			autoRegister.getStorage(), elementName);
		element.setStartLocation(2, autoRegister.getStartLine(), 1);
		element.setEndLocation(autoRegister.getEndLine(), 1);
		element.setRootElement(autoRegister.getContainerElement());
		return element;
    }
    
	protected void buildPatterns(IComponentElement component) {
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
	
	protected void setParent(IElement child, IElement parent) {
		Field field = findParentField(child.getClass());
		if (field == null) {
			throw new IllegalStateException("found no parent field");
		}
		try {
			field.set(child, parent);
		} catch (IllegalArgumentException e) {
			S2DaoPlugin.log(e);
		} catch (IllegalAccessException e) {
			S2DaoPlugin.log(e);
		}
	}
	
	private Field findParentField(Class clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			if ("parent".equals(fields[i].getName())) {
				return fields[i];
			}
		}
		Class superClass = clazz.getSuperclass();
		if (clazz != Object.class && superClass != null) {
			return findParentField(superClass);
		}
		return null;
	}
	
	private void buildClassPattern(IInitMethodElement initMethod) {
		List args = initMethod.getArgList();
		if (args.size() == 2) {
			addClassPattern((IArgElement) args.get(0), (IArgElement) args.get(1));
		}
	}
	
	private void addClassPattern(IArgElement packageArg, IArgElement classArg) {
		addClassPattern(trim(packageArg.getBody()), trim(classArg.getBody()));
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
		setIgnoreClassPattern(trim(packageArg.getBody()), trim(classArg.getBody()));
	}
	
	private void setIgnoreClassPattern(String packageName, String className) {
		ignoreClassPatterns.add(new ClassPattern(packageName, className));
	}
	
	private String trim(String value) {
		return value.replaceAll("\"", "").trim();
	}

}
