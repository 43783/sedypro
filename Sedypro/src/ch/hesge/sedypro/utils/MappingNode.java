package ch.hesge.sedypro.utils;

import java.util.ArrayList;
import java.util.List;

public class MappingNode {

	private MappingRegion mappingRegion;
	private List<MappingNode> children;
	
	public MappingNode() {
		children = new ArrayList<>();
	}

	public MappingRegion getMappingRegion() {
		return mappingRegion;
	}

	public void setMappingRegion(MappingRegion mappingRegion) {
		this.mappingRegion = mappingRegion;
	}

	public List<MappingNode> getChildren() {
		return children;
	}
}
