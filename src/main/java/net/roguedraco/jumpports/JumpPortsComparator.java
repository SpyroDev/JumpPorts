package net.roguedraco.jumpports;

import java.util.Comparator;

public class JumpPortsComparator implements Comparator<JumpPort> {
	@Override
	public int compare(JumpPort o1, JumpPort o2) {
		return o1.getName().compareTo(o2.getName());
	}
}