package starcraft.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Window extends JFrame {
	private static final long serialVersionUID = 5721004154735024544L;
	
	private JTextField subgoal1, subgoal2, currentPlan1, currentPlan2;
	private JTextArea msgs1, msgs2;
	private JLabel l1, l2;

	public Window() {
		super( "Starcraft agent listener" );
		setSize(800, 200);
		
		l1 = new JLabel("none", JLabel.CENTER);
		l2 = new JLabel("none", JLabel.CENTER);
	     
		l1.setPreferredSize(new Dimension(getWidth()/2-10, 15));
		l2.setPreferredSize(new Dimension(getWidth()/2-10, 15));
		
		 // Construct the TextFields
	     subgoal1 = new JTextField(35);
	     subgoal2 = new JTextField(35);
	     
	     msgs1 = new JTextArea(6,35);
	     msgs2 = new JTextArea(6,35);
	     
	     currentPlan1 = new JTextField(35);
	     currentPlan2 = new JTextField(35);

	     this.setLayout(new FlowLayout());
	     
	     this.add(l1);
	     this.add(l2);
	     
	     this.add(subgoal1);
	     this.add(subgoal2);
	     
	     this.add(msgs1);
	     this.add(msgs2);
	     
	     this.add(currentPlan1);
	     this.add(currentPlan2);
	     
	     this.setVisible(true);
	}
	
	public void setSubgoal(String agName, int attackPr, int defendPr) {
		if (agName.equals("officer1")) {
			msgs1.setText("");
			subgoal1.setBackground((attackPr > defendPr) ? Color.red : Color.green);
			subgoal1.setText(String.valueOf((attackPr > defendPr) ? attackPr : defendPr));
		} else if (agName.equals("officer2")) {
			msgs2.setText("");
			subgoal2.setBackground((attackPr > defendPr) ? Color.red : Color.green);
			subgoal2.setText(String.valueOf((attackPr > defendPr) ? attackPr : defendPr));
		}
	}
	
	public void setWta(String agName, double wta) {
		if (agName.equals("officer1")) {
			l1.setText("Agent 1 (wta: " + (double)Math.round(wta*100)/100 + ")");
		} else if (agName.equals("officer2")) {
			l2.setText("Agent 2 (wta: " + (double)Math.round(wta*100)/100 + ")");
		}
	}
	
	public void selectedPlan(String agName, int planId, String jointPlan, String singlePlan) {
		if (agName.equals("officer1")) {
			msgs1.append("-> selected new plan\n#id: )" + planId + "\nJoint: " + jointPlan + "\nSingle: " + singlePlan + "\n");
		} else if (agName.equals("officer2")) {
			msgs2.append("-> selected new plan\n#id: )" + planId + "\nJoint: " + jointPlan + "\nSingle: " + singlePlan + "\n");
		}
	}
	
	public void receivedPlan(String agName) {
		if (agName.equals("officer1")) {
			msgs1.append("received coplan!\n");
		} else if (agName.equals("officer2")) {
			msgs2.append("received coplan!\n");
		}
	}
	
	public void newPlan(String agName, String plan) {
		if (agName.equals("officer1")) {
			msgs1.append("-> new plan!");
			currentPlan1.setText("curPlan: " + plan);
		} else if (agName.equals("officer2")) {
			msgs2.append("-> new plan!");
			currentPlan2.setText("curPlan: " + plan);
		}
	}
	
	public void continuePlan(String agName) {
		if (agName.equals("officer1")) {
			msgs1.append("-> continuing current plan");
		} else if (agName.equals("officer2")) {
			msgs2.append("-> continuing current plan");
		}
	}
}
