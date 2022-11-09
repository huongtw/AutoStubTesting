package auto_testcase_generation.testdata.object;

import java.util.*;

public class StatementInTestpath_Mark {
	private final Map<String, String> properties = new HashMap<>();

	public StatementInTestpath_Mark() {

	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public Property_Marker getPropertyByName(String name) {
		Property_Marker marker = null;
		String value = properties.get(name);
		if (value != null)
			marker = new Property_Marker(name, value);
		return marker;
	}

	@Override
	public String toString() {
		return properties.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StatementInTestpath_Mark that = (StatementInTestpath_Mark) o;
		return properties.equals(that.properties);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
