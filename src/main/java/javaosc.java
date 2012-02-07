/* $Id$
 * Created on 28.10.2003
 */

/**
 * @author cramakrishnan
 *
 * Copyright (C) 2003, C. Ramakrishnan / Auracle
 * All rights reserved.
 * 
 * See license.txt (or license.rtf) for license information.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.illposed.osc.ui.OscUI;

public class javaosc extends JFrame {

	private Font font = new Font("serif", Font.ITALIC + Font.BOLD, 36);
	private OscUI myUi;
	
	public void addOscUI() {
		myUi = new OscUI(this);
		setBounds(10, 10, 500, 350);
		setContentPane(myUi);
	}

	public javaosc() {
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


	public static void main(String args[]) {
		new javaosc();
	}

}
