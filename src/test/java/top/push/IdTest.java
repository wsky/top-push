package top.push;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class IdTest {
	class ID implements Comparable<ID> {
		private String id;

		public ID(String id) {
			this.id = id;
		}

		@Override
		public int compareTo(ID o) {
			return this.id.compareTo(o.id);
		}
	}

	@Test
	public void compare_test() {
		assertEquals(0, new ID("a").compareTo(new ID("a")));
		assertEquals(1, new ID("b").compareTo(new ID("a")));
		assertEquals(-1, new ID("a").compareTo(new ID("b")));
	}

	@Test
	public void tree_map_test() {
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("a", 0);
		map.put("b", 1);
		assertEquals(0, map.get("a"));
		assertEquals(1, map.get("b"));
	}
}
