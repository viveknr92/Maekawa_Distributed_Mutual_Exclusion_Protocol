
public class ExpProbTime{
	protected double var = 1.0;
	public ExpProbTime(double var){
		this.var = var;
	}
	
	public int RandomNum(){
		return (int)(-var * Math.log(Math.random()));
	}
	
	public static void main(String[] args) {
		for(double i = 20; i < 35; i++) {
			ExpProbTime obj = new ExpProbTime(50);
			System.out.println(obj.RandomNum());
		}
		
	}
	
	
}