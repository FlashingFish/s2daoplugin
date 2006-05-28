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
package org.seasar.s2daoplugin.cache.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IArgElement;
import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IComponentHolderElement;
import org.seasar.kijimuna.core.dicon.model.IInitMethodElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.DiconUtil;

public final class AspectUtil {

	public static boolean containsInterceptorType(IAspectElement aspect, IType type) {
		IComponentElement component = DiconUtil.getAvailableComponent(aspect);
		return hasInterceptor(component, type);
	}
	
	public static boolean hasInterceptor(IComponentElement component, IType type) {
		if (component == null || type == null) {
			return false;
		}
		if (isInterceptorChain(component)) {
			IComponentElement[] interceptors = getInterceptors(component);
			for (int i = 0; i < interceptors.length; i++) {
				if (hasInterceptor(interceptors[i], type)) {
					return true;
				}
			}
			return false;
		} else {
			IRtti rtti = (IRtti) component.getAdapter(IRtti.class);
			return rtti != null ? type.equals(rtti.getType()) : false;
		}
	}
	
	public static IComponentElement[] getAllInterceptors(IComponentHolderElement aspect) {
		if (aspect == null) {
			return new IComponentElement[0];
		}
		return getAllInterceptors(DiconUtil.getAvailableComponent(aspect));
	}
	
	public static IComponentElement[] getAllInterceptors(IComponentElement interceptor) {
		if (interceptor == null) {
			return new IComponentElement[0];
		}
		Set result = new HashSet();
		if (isInterceptorChain(interceptor)) {
			IComponentElement[] interceptors = getInterceptors(interceptor);
			for (int i = 0; i < interceptors.length; i++) {
				result.addAll(Arrays.asList(getAllInterceptors(interceptors[i])));
			}
		} else {
			result.add(interceptor);
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}
	
	private static boolean isInterceptorChain(IComponentElement ognlComponent) {
		IRtti interceptorChainRtti =
			ognlComponent.getRttiLoader().loadRtti(CacheConstants.INTERCEPTOR_CHAIN);
		if (interceptorChainRtti == null) {
			return false;
		}
		IRtti componentRtti = (IRtti) ognlComponent.getAdapter(IRtti.class);
		return interceptorChainRtti.equals(componentRtti);
	}
	
	private static IComponentElement[] getInterceptors(IComponentElement interceptorChain) {
		List result = new ArrayList();
		List initMethods = interceptorChain.getInitMethodList();
		for (int i = 0; i < initMethods.size(); i++) {
			IInitMethodElement initMethod = (IInitMethodElement) initMethods.get(i);
			if (!"add".equals(initMethod.getMethodName())) {
				continue;
			}
			List args = initMethod.getArgList();
			for (int j = 0; j < args.size(); j++) {
				if (args.size() != 1) {
					continue;
				}
				IComponentElement component =
					DiconUtil.getAvailableComponent((IArgElement) args.get(0));
				if (component != null) {
					result.add(component);
				}
			}
		}
		return (IComponentElement[]) result.toArray(new IComponentElement[result.size()]);
	}

}
