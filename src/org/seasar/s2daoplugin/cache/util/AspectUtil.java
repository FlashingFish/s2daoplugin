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
package org.seasar.s2daoplugin.cache.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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

public final class AspectUtil implements CacheConstants {

	public static boolean containsInterceptorType(IAspectElement aspect, IType type) {
		return hasInterceptor(DiconUtil.getChildComponent(aspect), type);
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
		return getAllInterceptors(DiconUtil.getChildComponent(aspect));
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
		if (!isApplieableMethodModifier(method)) {
			return false;
		}
		IAspectInfo info = (IAspectInfo) aspect.getAdapter(IAspectInfo.class);
		if (info != null && matchPointcut(info.getPointcuts(), method)) {
			return true;
		}
		return false;
	}
	
	private static boolean isApplieableMethodModifier(IMethod method) {
		IType type = method.getDeclaringType();
		try {
			if (type != null && type.isInterface()) {
				return true;
			}
		} catch (JavaModelException ignore) {
		}
		if (MethodUtil.isFinal(method) || MethodUtil.isStatic(method) ||
				MethodUtil.isPrivate(method) || MethodUtil.isProtected(method) ||
				MethodUtil.isPackagePrivate(method)) {
			return false;
		}
		return true;
	}
	
	private static boolean matchPointcut(IPointcut[] pointcuts, IMethod method) {
		for (int i = 0; i < pointcuts.length; i++) {
			if (pointcuts[i].isAutoApply() &&
					isApplieableMethodOnInterface(method)) {
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
	
	private static boolean isApplieableMethodOnInterface(IMethod method) {
		return isDefineOnInterface(method.getDeclaringType(), method);
	}
	
	private static boolean isDefineOnInterface(IType type, IMethod method) {
		try {
			if (type == null || !type.isInterface()) {
				return false;
			}
		} catch (JavaModelException e) {
			return false;
		}
		IMethod[] methods = TypeUtil.getMethods(type);
		for (int i = 0; i < methods.length; i++) {
			if (method.isSimilar(methods[i])) {
				return true;
			}
		}
		IType[] interfaces = TypeUtil.findSuperInterfaces(type);
		for (int i = 0; i < interfaces.length; i++) {
			if (isDefineOnInterface(interfaces[i], method)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isInterceptorChain(IComponentElement component) {
		RttiLoader loader = component.getRttiLoader();
		IRtti interceptorChainRtti = loader.loadRtti(INTERCEPTOR_CHAIN);
		if (!RttiUtil.existsType(interceptorChainRtti)) {
			return false;
		}
		IRtti rtti = loader.loadRtti(component.getComponentClassName());
		if (rtti == null) {
			return false;
		}
		return interceptorChainRtti.getType().equals(rtti.getType());
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
					DiconUtil.getChildComponent((IArgElement) args.get(0));
				if (component != null) {
					result.add(component);
				}
			}
		}
		return DiconUtil.toComponentArray(result);
	}

}
