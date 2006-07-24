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
package org.seasar.s2daoplugin;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.kijimuna.core.dicon.model.IDiconElement;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.kijimuna.core.rtti.RttiLoader;
import org.seasar.s2daoplugin.cache.CacheConstants;
import org.seasar.s2daoplugin.cache.cache.IComponentCache;
import org.seasar.s2daoplugin.cache.util.AspectUtil;
import org.seasar.s2daoplugin.cache.util.FlagsUtil;

public class S2DaoUtil implements S2DaoConstants, CacheConstants {

	public static IComponentCache getS2DaoComponentCache(IProject project) {
		S2DaoNature nature = S2DaoNature.getInstance(project);
		return nature != null ? nature.getComponentCache() : null;
	}
	
	public static boolean isS2DaoInterceptorAppliedMethod(IMethod method) {
		if (method == null || !isOnApplieableType(method)) {
			return false;
		}
		IComponentCache cache = getS2DaoComponentCache(method.getJavaProject().getProject());
		if (cache == null) {
			return false;
		}
		IComponentElement[] components = cache.getComponents(method.getDeclaringType());
		for (int i = 0; i < components.length; i++) {
			List aspects = components[i].getAspectList();
			for (int j = 0; j < aspects.size(); j++) {
				IAspectElement aspect = (IAspectElement) aspects.get(j);
				if (!AspectUtil.hasInterceptor(aspect, getS2DaoInterceptorTypes(aspect))) {
					continue;
				}
				if (AspectUtil.isApplied(aspect, method)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean isOnApplieableType(IMethod method) {
		IType type = method.getDeclaringType();
		return FlagsUtil.isInterface(type) || FlagsUtil.isAbstract(method);
	}
	
	private static IType[] getS2DaoInterceptorTypes(IDiconElement element) {
		RttiLoader loader = element.getRttiLoader();
		return new IType[] {
				getType(loader.loadRtti(S2DAO_INTERCEPTOR)),
				getType(loader.loadRtti(S2DAO_PAGER_INTERCEPTOR))};
	}

	private static IType getType(IRtti rtti) {
		return rtti != null ? rtti.getType() : null;
	}
}
