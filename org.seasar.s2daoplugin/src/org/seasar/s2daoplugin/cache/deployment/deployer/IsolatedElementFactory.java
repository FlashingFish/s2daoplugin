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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seasar.kijimuna.core.ConstCore;
import org.seasar.kijimuna.core.dicon.DiconElementFactory;
import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IPropertyElement;
import org.seasar.kijimuna.core.parser.IElement;
import org.seasar.s2daoplugin.S2DaoPlugin;
import org.seasar.s2daoplugin.cache.util.DiconUtil;

public class IsolatedElementFactory implements ConstCore {

	private static final DiconElementFactory factory = new DiconElementFactory();
	
	public static IComponentElement createComponentElement(IComponentElement autoRegister,
			String fullyQualifiedClassName) {
		return ComponentElementCreator.create(autoRegister, fullyQualifiedClassName);
	}
	
	public static IAspectElement createAspectElement(IComponentElement autoRegister) {
		return AspectElementCreator.create(autoRegister);
	}

    private static IElement createElement(String elementName,
    		IComponentElement autoRegister) {
    	IElement element = factory.createElement(autoRegister.getProject(),
    			autoRegister.getStorage(), elementName);
		element.setStartLocation(2, autoRegister.getStartLine(), 1);
		element.setEndLocation(autoRegister.getEndLine(), 1);
		element.setRootElement(autoRegister.getContainerElement());
		return element;
    }
    
    
    private static class ComponentElementCreator {
    	
    	public static IComponentElement create(IComponentElement autoRegister,
    			String fullyQualifiedClassName) {
    		IComponentElement component = (IComponentElement) createElement(
    				DICON_TAG_COMPONENT, autoRegister);
    		createAttributes(component, fullyQualifiedClassName);
    		setParent(component, autoRegister.getParent());
    		return component;
    	}
    	
    	private static void createAttributes(IComponentElement component, String fqcn) {
    		Map attribues = new HashMap();
    		attribues.put(DICON_ATTR_INSTANCE, "singleton");
    		attribues.put(DICON_ATTR_CLASS, fqcn);
    		attribues.put(DICON_ATTR_AUTOBINDING, "auto");
    		component.setAttributes(attribues);
    	}
    	
    	// IComponentElement#setParent‚·‚é‚Æparent#addChild‚à“®‚­‚½‚ß
    	private static void setParent(IElement child, IElement parent) {
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
    	
    	private static Field findParentField(Class clazz) {
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
    }
	
	private static class AspectElementCreator {

		public static IAspectElement create(IComponentElement autoRegister) {
			IAspectElement aspect = (IAspectElement) createElement(DICON_TAG_ASPECT,
					autoRegister);
			createAttributes(autoRegister, aspect);
			createInterceptors(autoRegister, aspect);
			return aspect;
		}
		
		private static void createAttributes(IComponentElement autoRegister,
				IAspectElement aspect) {
			String pointcut = getPropertyBody(autoRegister, "pointcut");
			Map attributes = new HashMap();
			if (pointcut != null) {
				attributes.put("pointcut", trimQuote(pointcut));
			}
			aspect.setAttributes(attributes);
		}
		
		private static void createInterceptors(IComponentElement autoRegister,
				IAspectElement aspect) {
			String interceptor = getPropertyBody(autoRegister, "interceptor");
			if (interceptor != null) {
				aspect.setBody(interceptor);
			}
			List children = getPropertyChildren(autoRegister, "interceptor");
			for (int i = 0; i < children.size(); i++) {
				aspect.addChild((IElement) children.get(i));
			}
		}
		
		private static String getPropertyBody(IComponentElement autoRegister,
				String propertyName) {
			IPropertyElement prop = DiconUtil.getProperty(autoRegister, propertyName);
			return prop != null ? prop.getBody() : null;
		}
		
		private static List getPropertyChildren(IComponentElement autoRegister,
				String propertyName) {
			IPropertyElement prop = DiconUtil.getProperty(autoRegister, propertyName);
			return prop != null ? prop.getChildren() : Collections.EMPTY_LIST;
		}
		
		private static String trimQuote(String value) {
			String newValue = value.trim();
			if (newValue.startsWith("\"") && newValue.endsWith("\"")) {
				int s = newValue.indexOf('"');
				int e = newValue.lastIndexOf('"');
				return newValue.substring(s + 1, e);
			}
			return newValue;
		}
	}

}
