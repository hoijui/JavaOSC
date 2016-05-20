/*
 * Copyright (C) 2014, C. Ramakrishnan / Auracle.
 * All rights reserved.
 *
 * This code is licensed under the BSD 3-Clause license.
 * See file LICENSE (or LICENSE.html) for more information.
 */

package com.illposed.osc.messageselector;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCMessage;

/**
 * Checks whether a certain logical combination of two message selectors matches.
 */
public class CombinedMessageSelector implements MessageSelector {

	public enum LogicOperator {
		AND {
			@Override
			public boolean matches(final boolean matches1, final boolean matches2) {
				return (matches1 && matches2);
			}
		},
		OR {
			@Override
			public boolean matches(final boolean matches1, final boolean matches2) {
				return (matches1 || matches2);
			}
		},
		XOR {
			@Override
			public boolean matches(final boolean matches1, final boolean matches2) {
				return ((matches1 && !matches2) || (!matches1 && matches2));
			}
		};

		public abstract boolean matches(boolean matches1, boolean matches2);
	}

	private final MessageSelector selector1;
	private final MessageSelector selector2;
	private final LogicOperator logicOperator;

	public CombinedMessageSelector(
			final MessageSelector selector1,
			final MessageSelector selector2,
			final LogicOperator logicOperator)
	{
		this.selector1 = selector1;
		this.selector2 = selector2;
		this.logicOperator = logicOperator;
	}

	public CombinedMessageSelector(
			final MessageSelector selector1,
			final MessageSelector selector2)
	{
		this(selector1, selector2, LogicOperator.AND);
	}

	@Override
	public boolean isInfoRequired() {
		return (selector1.isInfoRequired() || selector2.isInfoRequired());
	}

	@Override
	public boolean matches(final OSCMessage message) {
		return logicOperator.matches(selector1.matches(message), selector2.matches(message));
	}
}
