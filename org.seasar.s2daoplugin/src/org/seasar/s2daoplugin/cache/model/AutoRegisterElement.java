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
package org.seasar.s2daoplugin.cache.model;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.seasar.kijimuna.core.dicon.info.IComponentKey;
import org.seasar.kijimuna.core.dicon.model.IArgElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IContainerElement;
import org.seasar.kijimuna.core.dicon.model.IInitMethodElement;
import org.seasar.kijimuna.core.parser.IElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.kijimuna.core.rtti.RttiLoader;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.DiconModelManager;

public class AutoRegisterElement implements IAutoRegisterElement {

	private final int autoRegisterType;
	private final AutoRegister autoRegister;
	private final IComponentElement autoRegisterComponent;
	
	public AutoRegisterElement(IComponentElement component) {
		if (component == null) {
			throw new IllegalArgumentException();
		}
		autoRegister = new AutoRegister(component);
		autoRegisterType = createAutoRegisterType(component);
		autoRegisterComponent = component;
	}
	
	public boolean isApplied(String packageName, String shortClassName) {
		return autoRegister.match(packageName, shortClassName);
	}
	
	public int getAutoRegisterType() {
		return autoRegisterType;
	}

	public String getComponentName() {
		return autoRegisterComponent.getComponentName();
	}

	public String getComponentClassName() {
		return autoRegisterComponent.getComponentClassName();
	}

	public String getAutoBindingMode() {
		return autoRegisterComponent.getAutoBindingMode();
	}

	public String getInstanceMode() {
		return autoRegisterComponent.getInstanceMode();
	}

	public List getArgList() {
		return autoRegisterComponent.getArgList();
	}

	public List getAspectList() {
		return autoRegisterComponent.getAspectList();
	}

	public List getDestroyMethodList() {
		return autoRegisterComponent.getDestroyMethodList();
	}

	public List getInitMethodList() {
		return autoRegisterComponent.getInitMethodList();
	}

	public List getPropertyList() {
		return autoRegisterComponent.getPropertyList();
	}

	public IComponentKey[] getTooManyComponentKeyArray(int tooMany) {
		return autoRegisterComponent.getTooManyComponentKeyArray(tooMany);
	}

	public void setLocking(boolean lock) {
		autoRegisterComponent.setLocking(lock);
	}

	public String getExpression() {
		return autoRegisterComponent.getExpression();
	}

	public boolean isOGNL() {
		return autoRegisterComponent.isOGNL();
	}

	public RttiLoader getRttiLoader() {
		return autoRegisterComponent.getRttiLoader();
	}

	public IContainerElement getContainerElement() {
		return autoRegisterComponent.getContainerElement();
	}

	public int getMarkerSeverity() {
		return autoRegisterComponent.getMarkerSeverity();
	}

	public void setMarkerServerity(int severity) {
		autoRegisterComponent.setMarkerServerity(severity);
	}

	public String getDisplayName() {
		return autoRegisterComponent.getDisplayName();
	}

	public void addChild(IElement child) {
		autoRegisterComponent.addChild(child);
	}

	public IElement getParent() {
		return autoRegisterComponent.getParent();
	}

	public void setRootElement(IElement root) {
		autoRegisterComponent.setRootElement(root);
	}

	public void setParent(IElement parent) {
		autoRegisterComponent.setParent(parent);
	}

	public void setStartLocation(int depth, int startLine, int startColumn) {
		autoRegisterComponent.setStartLocation(depth, startLine, startColumn);
	}

	public int getStartLine() {
		return autoRegisterComponent.getStartLine();
	}

	public void setEndLocation(int endLine, int endColumn) {
		autoRegisterComponent.setEndLocation(endLine, endColumn);
	}

	public int getEndLine() {
		return autoRegisterComponent.getEndLine();
	}

	public void setAttributes(Map properties) {
		autoRegisterComponent.setAttributes(properties);
	}

	public String getElementName() {
		return autoRegisterComponent.getElementName();
	}

	public String getBody() {
		return autoRegisterComponent.getBody();
	}

	public void setBody(String body) {
		autoRegisterComponent.setBody(body);
	}

	public String getAttribute(String name) {
		return autoRegisterComponent.getAttribute(name);
	}

	public List getChildren() {
		return autoRegisterComponent.getChildren();
	}

	public IStorage getStorage() {
		return autoRegisterComponent.getStorage();
	}

	public IProject getProject() {
		return autoRegisterComponent.getProject();
	}

	public Object getAdapter(Class adapter) {
		if (IAutoRegisterElement.class.equals(adapter)) {
			return this;
		}
		return autoRegisterComponent.getAdapter(adapter);
	}
	
	public int hashCode() {
		return autoRegisterComponent.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof AutoRegisterElement) {
			return autoRegisterComponent.equals(
					((AutoRegisterElement) obj).autoRegisterComponent);
		}
		return autoRegisterComponent.equals(obj);
	}
	
	public String toString() {
		return autoRegisterComponent.toString();
	}
	
	private int createAutoRegisterType(IComponentElement component) {
		return isComponentType(component) ? TYPE_COMPONENT : TYPE_COMPONENT_TARGET;
	}
	
	private boolean isComponentType(IComponentElement component) {
		return isType(CacheConstants.COMPONENT_AUTO_REGISTERS, component);
	}
	
	private boolean isType(String[] classNames, IComponentElement component) {
		DiconModelManager manager = DiconModelManager.getInstance(component.getProject());
		if (manager == null) {
			throw new IllegalStateException();
		}
		for (int i = 0; i < classNames.length; i++) {
			IRtti rtti1 = manager.getRtti(component.getComponentClassName());
			IRtti rtti2 = manager.getRtti(classNames[i]);
			if (rtti1 != null && rtti2 != null &&
					rtti1.getType().equals(rtti2.getType())) {
				return true;
			}
		}
		return false;
	}
	
	
	private static class AutoRegister {

		private IComponentElement autoRegisterComponent;
		private ClassPattern classPattern;
		private ClassPattern ignoreClassPattern;
		
		public AutoRegister(IComponentElement autoRegister) {
			if (autoRegister == null) {
				throw new IllegalArgumentException();
			}
			buildPatterns(autoRegister);
			this.autoRegisterComponent = autoRegister;
		}
		
		public boolean match(String packageName, String className) {
			return isIgnore(packageName, className) ?
					false : isApplied(packageName, className);
		}
		
	    protected boolean isIgnore(String packageName, String shortClassName) {
	    	return isAppliedPattern(ignoreClassPattern, packageName, shortClassName);
	    }
	    
	    protected boolean isApplied(String packageName, String shortClassName) {
	    	return isAppliedPattern(classPattern, packageName, shortClassName);
	    }
	    
	    protected boolean isAppliedPattern(ClassPattern cp, String packageName,
	    		String shortClassName) {
	    	if (cp == null) {
	    		return false;
	        }
	        if (cp.isAppliedPackageName(packageName) &&
	        		cp.isAppliedShortClassName(shortClassName)) {
	        	return true;
	        }
	    	return false;
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
				setClassPattern((IArgElement) args.get(0), (IArgElement) args.get(1));
			}
		}
		
		private void setClassPattern(IArgElement packageArg, IArgElement classArg) {
			setClassPattern(trim(packageArg.getBody()), trim(classArg.getBody()));
		}
		
		private void setClassPattern(String packageName, String className) {
			classPattern = new ClassPattern(packageName, className);
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
			ignoreClassPattern = new ClassPattern(packageName, className);
		}
		
		private String trim(String value) {
			return value.replaceAll("\"", "").trim();
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
		
		public boolean equals(Object obj) {
			if (!(obj instanceof AutoRegister)) {
				return false;
			}
			return toString().equals(obj.toString());
		}
		
		public String toString() {
			IContainerElement container = autoRegisterComponent.getContainerElement();
			return new StringBuffer()
				.append(container.getStorage().getFullPath()).append(":")
				.append(nullToEmpty(autoRegisterComponent.getComponentName())).append(":")
				.append(autoRegisterComponent.getComponentClassName())
				.toString();
		}
		
		private String nullToEmpty(String value) {
			return value == null ? "" : value;
		}
	}

}
