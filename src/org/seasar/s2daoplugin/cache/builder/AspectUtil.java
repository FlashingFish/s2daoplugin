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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.info.IAspectInfo;
import org.seasar.kijimuna.core.dicon.info.IPointcut;
import org.seasar.kijimuna.core.dicon.model.IArgElement;
import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IComponentHolderElement;
import org.seasar.kijimuna.core.dicon.model.IInitMethodElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.kijimuna.core.rtti.RttiLoader;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.DiconUtil;

public final class AspectUtil implements CacheConstants {

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
			IRtti rtti = component.getRttiLoader().loadRtti(component.getComponentClassName());
			return rtti != null ? type.equals(rtti.getType()) : false;
		}
	}
	
	public static IComponentElement[] getAllInterceptors(IComponentHolderElement aspect) {
		if (aspect == null) {
			return EMPTY_COMPONENTS;
		}
		return getAllInterceptors(DiconUtil.getAvailableComponent(aspect));
	}
	
	public static IComponentElement[] getAllInterceptors(IComponentElement interceptor) {
		if (interceptor == null) {
			return EMPTY_COMPONENTS;
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
		return DiconUtil.toComponentArray(result);
	}
	
	public static boolean isApplied(IAspectElement aspect, IMethod method) {
		if (aspect == null || method == null) {
			return false;
		}
		IAspectInfo info = (IAspectInfo) aspect.getAdapter(IAspectInfo.class);
		if (info != null && matchPointcut(info.getPointcuts(), method)) {
			return true;
		}
		return false;
	}
	
	private static boolean matchPointcut(IPointcut[] pointcuts, IMethod method) {
		for (int i = 0; i < pointcuts.length; i++) {
			if (pointcuts[i].isAutoApply()) {
				return true;
			}
			// Œp³Œ³‚à‹–‚·‚½‚ßIRttiMethodDescriptor‚Å”äŠr‚µ‚È‚¢
			try {
				Pattern p = Pattern.compile(pointcuts[i].getRegexp());
				if (p.matcher(method.getElementName()).matches()) {
					return true;
				}
			} catch (PatternSyntaxException ignore) {
			}
		}
		return false;
	}
	
	private static boolean isInterceptorChain(IComponentElement ognlComponent) {
		RttiLoader loader = ognlComponent.getRttiLoader();
		IRtti interceptorChainRtti = loader.loadRtti(CacheConstants.INTERCEPTOR_CHAIN);
		if (interceptorChainRtti == null || interceptorChainRtti.getType() == null) {
			return false;
		}
		IRtti componentRtti = loader.loadRtti(ognlComponent.getComponentClassName());
		if (componentRtti == null) {
			return false;
		}
		return interceptorChainRtti.getType().equals(componentRtti.getType());
	}
	
	private static IComponentElement[] getInterceptors(IComponentElement interceptorChain) {
		Set result = new HashSet();
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
		return DiconUtil.toComponentArray(result);
	}

}
