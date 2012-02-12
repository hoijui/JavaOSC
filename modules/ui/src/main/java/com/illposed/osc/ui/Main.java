/* $Id$
 * Created on 28.10.2003
 */

/**
 * @author cramakrishnan
 *
 * Copyright (C) 2003, C. Ramakrishnan / Auracle
 * All rights reserved.
 *
 * See file LICENSE (or LICENSE.html) for license information.
 */
package com.illposed.osc.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class Main extends JFrame {

	private OscUI myUi;

	public void addOscUI() {
		myUi = new OscUI(this);
		setBounds(10, 10, 500, 350);
		setContentPane(myUi);
	}

	public Main() {
		super("OSC");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myUi.doSendGlobalOff(1000, 1001, 1002);
				System.exit(0);
			}
		});

		addOscUI();
		setVisible(true);
	}


	public static void main(String[] args) {
		new Main();
	}
}
