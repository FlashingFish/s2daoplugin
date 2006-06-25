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
package org.seasar.s2daoplugin.cache.cache.filter;

import java.util.List;

import org.seasar.kijimuna.core.dicon.model.IAspectElement;
import org.seasar.kijimuna.core.dicon.model.IComponentElement;
import org.seasar.s2daoplugin.cache.util.DiconUtil;

public class AspectFilter extends AbstractDecorationFilter {

	public AspectFilter(String interceptorClassName) {
		this(new String[] {interceptorClassName});
	}
	
	public AspectFilter(String[] interceptorClassNames) {
		this(new InterceptorFilter(new ClassNameFilter(interceptorClassNames)));
	}
	
	public AspectFilter(IComponentFilter filter) {
		super(filter);
	}
	
	public boolean isPassable(IComponentElement component) {
		List aspects = component.getAspectList();
		for (int i = 0; i < aspects.size(); i++) {
			IComponentElement interceptor =
				DiconUtil.getAvailableComponent((IAspectElement) aspects.get(i));
			if (getFilter().isPassable(interceptor)) {
				return true;
			}
		}
		return false;
	}

}
