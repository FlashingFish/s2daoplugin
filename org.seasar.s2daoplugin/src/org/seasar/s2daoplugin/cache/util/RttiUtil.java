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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.seasar.kijimuna.core.dicon.DiconNature;
import org.seasar.kijimuna.core.rtti.IRtti;
import org.seasar.kijimuna.core.rtti.RttiLoader;
import org.seasar.s2daoplugin.util.ArrayUtil;

public class RttiUtil {

	private static final IRtti[] EMPTY_RTTIES = new IRtti[0];
	
	public static IRtti[] getAllClasses(IRtti rtti) {
		if (rtti == null) {
			return EMPTY_RTTIES;
		}
		Set result = new HashSet();
		do {
			result.add(rtti);
			rtti = rtti.getSuperClass();
		} while (rtti != null);
		return (IRtti[]) result.toArray(new IRtti[result.size()]);
	}
	
	public static IRtti[] getAllTypes(IRtti rtti) {
		IRtti[] classes = RttiUtil.getAllClasses(rtti);
		IRtti[] types = EMPTY_RTTIES;
		for (int i = 0; i < classes.length; i++) {
			types = (IRtti[]) ArrayUtil.add(types, classes[i].getInterfaces());
		}
		return types;
	}
	
	public static boolean existsType(IRtti rtti) {
		return rtti != null && rtti.getType() != null;
	}
	
	public static RttiLoader getRttiLoader(IProject project) {
		DiconNature nature = DiconNature.getInstance(project);
		return nature != null ? nature.getRttiLoader() : null;
	}

}
