
public class ExpProbTime{
	protected double var = 1.0;
	public ExpProbTime(double var){
		this.var = var;
	}
	
	public int RandomNum(){
		return (int)(-var * Math.log(Math.random()));
	}
	
}