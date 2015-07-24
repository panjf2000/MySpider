package bin.spider.bean;

public class FINISH {

	private Integer ID;
	private String visited;
		
	@Override
	public String toString(){
		return ID+"__"+visited;
	}

	public Integer getID() {
		return ID;
	}


	public void setID(Integer iD) {
		ID = iD;
	}

	public String getVisited() {
		return visited;
	}

	public void setVisited(String visited) {
		this.visited = visited;
	}	
	
}
